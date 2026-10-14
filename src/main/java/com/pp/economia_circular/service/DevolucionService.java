package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.DevolucionDto;
import com.pp.economia_circular.entity.*;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.DevolucionRepository;
import com.pp.economia_circular.repositories.SolicitudIntercambioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Devolución de un intercambio completado:
 * - cualquiera de los dos participantes puede pedirla dentro del plazo;
 * - la otra parte (o un admin) la acepta o la rechaza;
 * - si se acepta, el intercambio pasa a DEVUELTO y los artículos vuelven a estar disponibles.
 */
@Service
@Transactional
public class DevolucionService {

    @Value("${app.devoluciones.plazo-dias:7}")
    private int plazoDias = 7;

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private SolicitudIntercambioRepository solicitudRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JWTService authService;

    @Autowired(required = false)
    private NotificacionService notificacionService;

    public DevolucionDto solicitar(Long solicitudId, String motivo) {
        Usuario usuario = requerirUsuario();
        SolicitudIntercambio solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Intercambio no encontrado"));

        if (!esParticipante(solicitud, usuario)) {
            throw new RuntimeException("Solo los participantes del intercambio pueden pedir una devolución");
        }
        if (solicitud.getEstado() != SolicitudIntercambio.EstadoIntercambio.COMPLETADO) {
            throw new RuntimeException("Solo se puede pedir la devolución de un intercambio completado");
        }
        LocalDateTime completado = solicitud.getActualizadoEn() != null ? solicitud.getActualizadoEn() : solicitud.getCreadoEn();
        if (completado != null && completado.plusDays(plazoDias).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Pasó el plazo de " + plazoDias + " días para pedir la devolución");
        }
        if (devolucionRepository.existsBySolicitud_IdAndEstado(solicitudId, Devolucion.EstadoDevolucion.PENDIENTE)) {
            throw new RuntimeException("Ya hay un pedido de devolución pendiente para este intercambio");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new RuntimeException("Contanos el motivo de la devolución");
        }

        Devolucion devolucion = new Devolucion();
        devolucion.setSolicitud(solicitud);
        devolucion.setSolicitante(usuario);
        devolucion.setMotivo(motivo.trim());
        Devolucion guardada = devolucionRepository.save(devolucion);

        notificar(otraParte(solicitud, usuario), usuario,
                "Pedido de devolución",
                "Pidieron devolver el intercambio de \"" + tituloSolicitado(solicitud) + "\": " + guardada.getMotivo(),
                guardada.getId());
        return DevolucionDto.from(guardada);
    }

    public DevolucionDto responder(Long devolucionId, boolean aceptar, String respuesta) {
        Usuario usuario = requerirUsuario();
        Devolucion devolucion = buscarPendiente(devolucionId);
        SolicitudIntercambio solicitud = devolucion.getSolicitud();

        boolean esSolicitante = devolucion.getSolicitante().getId().equals(usuario.getId());
        if (esSolicitante || (!esParticipante(solicitud, usuario) && !esAdmin(usuario))) {
            throw new RuntimeException("Solo la otra parte del intercambio puede responder la devolución");
        }

        devolucion.setEstado(aceptar ? Devolucion.EstadoDevolucion.ACEPTADA : Devolucion.EstadoDevolucion.RECHAZADA);
        devolucion.setRespuesta(respuesta != null && !respuesta.trim().isEmpty() ? respuesta.trim() : null);
        devolucion.setResueltoEn(LocalDateTime.now());

        if (aceptar) {
            solicitud.setEstado(SolicitudIntercambio.EstadoIntercambio.DEVUELTO);
            solicitudRepository.save(solicitud);
            reactivar(solicitud.getArticuloSolicitado());
            reactivar(solicitud.getArticuloOfrecido());
        }
        Devolucion guardada = devolucionRepository.save(devolucion);

        notificar(devolucion.getSolicitante(), usuario,
                aceptar ? "Devolución aceptada" : "Devolución rechazada",
                "Tu pedido de devolución de \"" + tituloSolicitado(solicitud) + "\" fue "
                        + (aceptar ? "aceptado. Los artículos vuelven a estar disponibles." : "rechazado.")
                        + (guardada.getRespuesta() != null ? " " + guardada.getRespuesta() : ""),
                guardada.getId());
        return DevolucionDto.from(guardada);
    }

    public DevolucionDto cancelar(Long devolucionId) {
        Usuario usuario = requerirUsuario();
        Devolucion devolucion = buscarPendiente(devolucionId);
        if (!devolucion.getSolicitante().getId().equals(usuario.getId())) {
            throw new RuntimeException("Solo quien pidió la devolución puede cancelarla");
        }
        devolucion.setEstado(Devolucion.EstadoDevolucion.CANCELADA);
        devolucion.setResueltoEn(LocalDateTime.now());
        return DevolucionDto.from(devolucionRepository.save(devolucion));
    }

    @Transactional(readOnly = true)
    public List<DevolucionDto> misDevoluciones() {
        Usuario usuario = requerirUsuario();
        return devolucionRepository.findByParticipante(usuario.getId()).stream()
                .map(DevolucionDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DevolucionDto> todas() {
        Usuario usuario = requerirUsuario();
        if (!esAdmin(usuario)) throw new RuntimeException("Solo administradores");
        return devolucionRepository.findAllByOrderByCreadoEnDesc().stream()
                .map(DevolucionDto::from)
                .collect(Collectors.toList());
    }

    public int getPlazoDias() {
        return plazoDias;
    }

    private Devolucion buscarPendiente(Long devolucionId) {
        Devolucion devolucion = devolucionRepository.findById(devolucionId)
                .orElseThrow(() -> new RuntimeException("Devolución no encontrada"));
        if (devolucion.getEstado() != Devolucion.EstadoDevolucion.PENDIENTE) {
            throw new RuntimeException("La devolución ya fue resuelta");
        }
        return devolucion;
    }

    private void reactivar(Articulo articulo) {
        if (articulo == null) return;
        articulo.setEstado(Articulo.EstadoArticulo.DISPONIBLE);
        articleRepository.save(articulo);
    }

    private boolean esParticipante(SolicitudIntercambio s, Usuario u) {
        return s.getSolicitante().getId().equals(u.getId())
                || s.getArticuloSolicitado().getUsuario().getId().equals(u.getId());
    }

    private Usuario otraParte(SolicitudIntercambio s, Usuario u) {
        return s.getSolicitante().getId().equals(u.getId())
                ? s.getArticuloSolicitado().getUsuario()
                : s.getSolicitante();
    }

    private boolean esAdmin(Usuario u) {
        return u.getRol() != null && u.getRol().contains("ADMIN");
    }

    private String tituloSolicitado(SolicitudIntercambio s) {
        return s.getArticuloSolicitado() != null ? s.getArticuloSolicitado().getTitulo() : "tu artículo";
    }

    private void notificar(Usuario destinatario, Usuario emisor, String titulo, String mensaje, Long devolucionId) {
        if (notificacionService == null) return;
        notificacionService.crear(destinatario, emisor, Notificacion.TipoNotificacion.DEVOLUCION,
                titulo, mensaje, Notificacion.ReferenciaTipo.DEVOLUCION, devolucionId);
    }

    private Usuario requerirUsuario() {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");
        return usuario;
    }
}
