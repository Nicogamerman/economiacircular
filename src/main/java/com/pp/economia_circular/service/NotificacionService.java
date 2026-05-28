package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.NotificacionDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Notificacion;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final JWTService jwtService;

    public NotificacionService(NotificacionRepository notificacionRepository, JWTService jwtService) {
        this.notificacionRepository = notificacionRepository;
        this.jwtService = jwtService;
    }

    public Notificacion crear(Usuario usuario, Articulo articulo, String mensaje, String tipo) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setArticulo(articulo);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo);
        return notificacionRepository.save(notificacion);
    }

    @Transactional(readOnly = true)
    public List<NotificacionDto> listarMisNotificaciones() {
        Usuario currentUser = requireCurrentUser();
        return notificacionRepository.findByUsuario_IdOrderByCreadoEnDesc(currentUser.getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas() {
        Usuario currentUser = requireCurrentUser();
        return notificacionRepository.countByUsuario_IdAndLeidaFalse(currentUser.getId());
    }

    public void marcarComoLeidas() {
        Usuario currentUser = requireCurrentUser();
        List<Notificacion> notificaciones = notificacionRepository.findByUsuario_IdOrderByCreadoEnDesc(currentUser.getId());
        for (Notificacion notificacion : notificaciones) {
            notificacion.setLeida(true);
        }
        notificacionRepository.saveAll(notificaciones);
    }

    private Usuario requireCurrentUser() {
        Usuario currentUser = jwtService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return currentUser;
    }

    private NotificacionDto toDto(Notificacion notificacion) {
        NotificacionDto dto = new NotificacionDto();
        dto.setId(notificacion.getId());
        dto.setMensaje(notificacion.getMensaje());
        dto.setTipo(notificacion.getTipo());
        dto.setLeida(notificacion.isLeida());
        dto.setCreadoEn(notificacion.getCreadoEn());
        if (notificacion.getArticulo() != null) {
            dto.setArticuloId(notificacion.getArticulo().getId());
            dto.setArticuloTitulo(notificacion.getArticulo().getTitulo());
        }
        return dto;
    }
}
