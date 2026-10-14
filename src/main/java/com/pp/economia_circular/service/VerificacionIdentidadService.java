package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.VerificacionIdentidadDto;
import com.pp.economia_circular.DTO.ia.AnalisisIdentidadDto;
import com.pp.economia_circular.entity.Notificacion;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.VerificacionIdentidad;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.repositories.VerificacionIdentidadRepository;
import com.pp.economia_circular.service.ia.IaClient;
import com.pp.economia_circular.service.ia.IaException;
import com.pp.economia_circular.service.ia.IaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Verificación de identidad para prevenir fraudes: el usuario sube la foto de su documento y una
 * selfie sosteniéndolo; la IA evalúa la consistencia y los casos dudosos pasan a revisión manual.
 */
@Service
@Transactional
public class VerificacionIdentidadService {

    static final int MAX_INTENTOS_POR_DIA = 3;
    static final int CONFIANZA_MINIMA_APROBACION = 70;

    @Autowired
    private VerificacionIdentidadRepository verificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IaClient iaClient;

    @Autowired
    private JWTService authService;

    @Autowired(required = false)
    private NotificacionService notificacionService;

    public VerificacionIdentidadDto verificar(MultipartFile documento, MultipartFile selfie) {
        Usuario usuario = requerirUsuario();
        if (usuario.isIdentidadVerificada()) {
            throw new RuntimeException("Tu identidad ya está verificada");
        }
        IaService.validarImagen(documento);
        IaService.validarImagen(selfie);
        if (verificacionRepository.countByUsuario_IdAndCreadoEnAfter(usuario.getId(), LocalDateTime.now().minusDays(1))
                >= MAX_INTENTOS_POR_DIA) {
            throw new RuntimeException("Superaste los intentos de hoy. Probá de nuevo mañana");
        }
        if (!iaClient.disponible()) {
            throw new IaException("La verificación automática no está disponible en este momento", true);
        }

        String nombre = ((usuario.getNombre() != null ? usuario.getNombre() : "") + " "
                + (usuario.getApellido() != null ? usuario.getApellido() : "")).trim();
        AnalisisIdentidadDto analisis;
        try {
            analisis = iaClient.analizarIdentidad(documento.getBytes(), documento.getContentType(),
                    selfie.getBytes(), selfie.getContentType(), nombre);
        } catch (IOException e) {
            throw new RuntimeException("No se pudieron leer las imágenes");
        }

        VerificacionIdentidad v = new VerificacionIdentidad();
        v.setUsuario(usuario);
        v.setEstado(decidir(analisis));
        v.setTipoDocumento(analisis.tipoDocumento);
        v.setConfianza(analisis.confianza);
        v.setMotivo(analisis.motivo);
        if (v.getEstado() != VerificacionIdentidad.EstadoVerificacion.REVISION_MANUAL) {
            v.setResueltoEn(LocalDateTime.now());
        }
        VerificacionIdentidad guardada = verificacionRepository.save(v);

        if (guardada.getEstado() == VerificacionIdentidad.EstadoVerificacion.APROBADA) {
            usuario.setIdentidadVerificada(true);
            usuarioRepository.save(usuario);
        }
        return VerificacionIdentidadDto.from(guardada);
    }

    /**
     * La IA propone un veredicto, pero solo se aprueba automáticamente si todos los
     * chequeos dan bien y la confianza es suficiente. Lo demás no aprobado va a revisión.
     */
    static VerificacionIdentidad.EstadoVerificacion decidir(AnalisisIdentidadDto a) {
        boolean todoOk = Boolean.TRUE.equals(a.documentoLegible)
                && Boolean.TRUE.equals(a.nombreCoincide)
                && Boolean.TRUE.equals(a.selfieConDocumento)
                && !Boolean.TRUE.equals(a.indiciosDeFraude);
        int confianza = a.confianza != null ? a.confianza : 0;

        if ("APROBADO".equalsIgnoreCase(a.veredicto) && todoOk && confianza >= CONFIANZA_MINIMA_APROBACION) {
            return VerificacionIdentidad.EstadoVerificacion.APROBADA;
        }
        if ("RECHAZADO".equalsIgnoreCase(a.veredicto) && Boolean.TRUE.equals(a.indiciosDeFraude)) {
            return VerificacionIdentidad.EstadoVerificacion.RECHAZADA;
        }
        if ("RECHAZADO".equalsIgnoreCase(a.veredicto) && !Boolean.TRUE.equals(a.documentoLegible)) {
            // Foto ilegible: se rechaza para que el usuario vuelva a intentar con una mejor imagen
            return VerificacionIdentidad.EstadoVerificacion.RECHAZADA;
        }
        return VerificacionIdentidad.EstadoVerificacion.REVISION_MANUAL;
    }

    @Transactional(readOnly = true)
    public Optional<VerificacionIdentidadDto> miUltimaVerificacion() {
        Usuario usuario = requerirUsuario();
        return verificacionRepository.findFirstByUsuario_IdOrderByCreadoEnDesc(usuario.getId())
                .map(VerificacionIdentidadDto::from);
    }

    @Transactional(readOnly = true)
    public List<VerificacionIdentidadDto> pendientesDeRevision() {
        return verificacionRepository.findByEstado(VerificacionIdentidad.EstadoVerificacion.REVISION_MANUAL).stream()
                .map(VerificacionIdentidadDto::from)
                .collect(Collectors.toList());
    }

    /** Un admin resuelve una verificación que quedó en revisión manual. */
    public VerificacionIdentidadDto resolver(Long id, boolean aprobar, String nota) {
        Usuario admin = requerirUsuario();
        if (admin.getRol() == null || !admin.getRol().contains("ADMIN")) {
            throw new RuntimeException("Solo administradores");
        }
        VerificacionIdentidad v = verificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Verificación no encontrada"));
        if (v.getEstado() != VerificacionIdentidad.EstadoVerificacion.REVISION_MANUAL) {
            throw new RuntimeException("La verificación ya fue resuelta");
        }
        v.setEstado(aprobar ? VerificacionIdentidad.EstadoVerificacion.APROBADA
                : VerificacionIdentidad.EstadoVerificacion.RECHAZADA);
        v.setNotaRevision(nota);
        v.setRevisadoPor(admin);
        v.setResueltoEn(LocalDateTime.now());
        VerificacionIdentidad guardada = verificacionRepository.save(v);

        Usuario usuario = v.getUsuario();
        if (aprobar) {
            usuario.setIdentidadVerificada(true);
            usuarioRepository.save(usuario);
        }
        if (notificacionService != null) {
            notificacionService.crear(usuario, admin, Notificacion.TipoNotificacion.VERIFICACION_IDENTIDAD,
                    aprobar ? "Identidad verificada" : "Verificación rechazada",
                    aprobar ? "Revisamos tu documentación y tu identidad quedó verificada."
                            : "No pudimos verificar tu identidad." + (nota != null ? " " + nota : ""),
                    Notificacion.ReferenciaTipo.USUARIO, usuario.getId());
        }
        return VerificacionIdentidadDto.from(guardada);
    }

    private Usuario requerirUsuario() {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");
        return usuario;
    }
}
