package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.NotificacionResponseDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Notificacion;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private JWTService authService;

    public Notificacion crear(Usuario destinatario,
                              Usuario emisor,
                              Notificacion.TipoNotificacion tipo,
                              String titulo,
                              String mensaje,
                              Notificacion.ReferenciaTipo referenciaTipo,
                              Long referenciaId) {
        if (destinatario == null) {
            return null;
        }
        if (emisor != null && emisor.getId().equals(destinatario.getId())) {
            return null;
        }
        Notificacion n = new Notificacion();
        n.setDestinatario(destinatario);
        n.setEmisor(emisor);
        n.setTipo(tipo);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setReferenciaTipo(referenciaTipo);
        n.setReferenciaId(referenciaId);
        return notificacionRepository.save(n);
    }

    /**
     * Notificación sobre un artículo (pendiente de aprobación, aprobado, etc.).
     * Se guarda con referencia ARTICULO para que el front pueda navegar al artículo.
     */
    public Notificacion crear(Usuario destinatario, Articulo articulo, String mensaje, String tipo) {
        Notificacion.TipoNotificacion tipoEnum;
        try {
            tipoEnum = Notificacion.TipoNotificacion.valueOf(tipo);
        } catch (IllegalArgumentException | NullPointerException e) {
            tipoEnum = Notificacion.TipoNotificacion.SISTEMA;
        }
        return crear(destinatario, null, tipoEnum, null, mensaje,
                articulo != null ? Notificacion.ReferenciaTipo.ARTICULO : null,
                articulo != null ? articulo.getId() : null);
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponseDto> listarMisNotificaciones(boolean soloNoLeidas) {
        Usuario actual = requerirUsuario();
        List<Notificacion> notificaciones = soloNoLeidas
                ? notificacionRepository.findByDestinatario_IdAndLeidaFalseOrderByCreadoEnDesc(actual.getId())
                : notificacionRepository.findByDestinatario_IdOrderByCreadoEnDesc(actual.getId());
        return notificaciones.stream()
                .map(NotificacionResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas() {
        Usuario actual = requerirUsuario();
        return notificacionRepository.countByDestinatario_IdAndLeidaFalse(actual.getId());
    }

    public NotificacionResponseDto marcarComoLeida(Long id) {
        Usuario actual = requerirUsuario();
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        if (!n.getDestinatario().getId().equals(actual.getId())) {
            throw new RuntimeException("No tenés permiso sobre esta notificación");
        }
        if (!n.isLeida()) {
            n.setLeida(true);
            n.setLeidoEn(LocalDateTime.now());
            notificacionRepository.save(n);
        }
        return new NotificacionResponseDto(n);
    }

    public int marcarTodasComoLeidas() {
        Usuario actual = requerirUsuario();
        return notificacionRepository.marcarTodasComoLeidas(actual.getId(), LocalDateTime.now());
    }

    public void eliminar(Long id) {
        Usuario actual = requerirUsuario();
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        if (!n.getDestinatario().getId().equals(actual.getId())) {
            throw new RuntimeException("No tenés permiso sobre esta notificación");
        }
        notificacionRepository.delete(n);
    }

    private Usuario requerirUsuario() {
        Usuario actual = authService.getCurrentUser();
        if (actual == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return actual;
    }
}
