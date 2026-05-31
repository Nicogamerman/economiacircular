package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.CambiarEstadoSolicitudDto;
import com.pp.economia_circular.DTO.SolicitudCreateDto;
import com.pp.economia_circular.DTO.SolicitudResponseDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Notificacion;
import com.pp.economia_circular.entity.SolicitudIntercambio;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.SolicitudIntercambioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SolicitudIntercambioService {

    @Autowired
    private SolicitudIntercambioRepository solicitudRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JWTService authService;

    @Autowired(required = false)
    private NotificacionService notificacionService;

    public SolicitudResponseDto crear(SolicitudCreateDto dto) {
        Usuario solicitante = authService.getCurrentUser();
        if (solicitante == null) throw new RuntimeException("Usuario no autenticado");

        Articulo solicitado = articleRepository.findById(dto.getArticuloSolicitadoId())
                .orElseThrow(() -> new RuntimeException("Artículo solicitado no encontrado"));

        Articulo ofrecido = articleRepository.findById(dto.getArticuloOfrecidoId())
                .orElseThrow(() -> new RuntimeException("Artículo ofrecido no encontrado"));

        if (solicitado.getUsuario().getId().equals(solicitante.getId())) {
            throw new RuntimeException("No puedes solicitar tu propio artículo");
        }

        if (!ofrecido.getUsuario().getId().equals(solicitante.getId())) {
            throw new RuntimeException("El artículo ofrecido no te pertenece");
        }

        if (solicitado.getEstado() != Articulo.EstadoArticulo.DISPONIBLE) {
            throw new RuntimeException("El artículo solicitado no está disponible");
        }

        if (ofrecido.getEstado() != Articulo.EstadoArticulo.DISPONIBLE) {
            throw new RuntimeException("El artículo ofrecido no está disponible");
        }

        if (solicitudRepository.existeSolicitudPendiente(solicitante.getId(), solicitado.getId(), ofrecido.getId())) {
            throw new RuntimeException("Ya existe una solicitud pendiente para este intercambio");
        }

        SolicitudIntercambio solicitud = new SolicitudIntercambio(solicitado, ofrecido, solicitante);
        SolicitudIntercambio saved = solicitudRepository.save(solicitud);

        if (notificacionService != null) {
            notificacionService.crear(
                    solicitado.getUsuario(),
                    solicitante,
                    Notificacion.TipoNotificacion.SOLICITUD_INTERCAMBIO,
                    "Nueva solicitud de intercambio",
                    solicitante.getNombre() + " quiere intercambiar por tu artículo: " + solicitado.getTitulo(),
                    Notificacion.ReferenciaTipo.SOLICITUD,
                    saved.getId()
            );
        }

        return SolicitudResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<SolicitudResponseDto> listarMisSolicitudes(Pageable pageable) {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");
        return solicitudRepository.findBySolicitante_Id(usuario.getId(), pageable)
                .map(SolicitudResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<SolicitudResponseDto> listarSolicitudesRecibidas(Pageable pageable) {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");
        return solicitudRepository.findByPropietarioArticuloSolicitado(usuario.getId(), pageable)
                .map(SolicitudResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<SolicitudResponseDto> listarHistorial(Pageable pageable) {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");
        return solicitudRepository.findHistorialByUsuarioId(usuario.getId(), pageable)
                .map(SolicitudResponseDto::from);
    }

    @Transactional(readOnly = true)
    public SolicitudResponseDto obtener(Long id) {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");

        SolicitudIntercambio solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        verificarAcceso(solicitud, usuario);
        return SolicitudResponseDto.from(solicitud);
    }

    public SolicitudResponseDto cambiarEstado(Long id, CambiarEstadoSolicitudDto dto) {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");

        SolicitudIntercambio solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        SolicitudIntercambio.EstadoIntercambio nuevoEstado = dto.getNuevoEstado();
        boolean esSolicitante = solicitud.getSolicitante().getId().equals(usuario.getId());
        boolean esPropietario = solicitud.getArticuloSolicitado().getUsuario().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRol() != null && usuario.getRol().contains("ADMIN");

        validarTransicionEstado(solicitud.getEstado(), nuevoEstado, esSolicitante, esPropietario, esAdmin);

        solicitud.setEstado(nuevoEstado);
        SolicitudIntercambio updated = solicitudRepository.save(solicitud);

        if (nuevoEstado == SolicitudIntercambio.EstadoIntercambio.ACEPTADO) {
            solicitudRepository.rechazarPendientesPorArticulo(
                    solicitud.getArticuloSolicitado().getId(), solicitud.getId());
        }

        if (nuevoEstado == SolicitudIntercambio.EstadoIntercambio.COMPLETADO) {
            Articulo solicitado = solicitud.getArticuloSolicitado();
            Articulo ofrecido = solicitud.getArticuloOfrecido();
            solicitado.setEstado(Articulo.EstadoArticulo.INTERCAMBIADO);
            ofrecido.setEstado(Articulo.EstadoArticulo.INTERCAMBIADO);
            articleRepository.save(solicitado);
            articleRepository.save(ofrecido);
        }

        if (notificacionService != null) {
            Usuario destinatario = esSolicitante
                    ? solicitud.getArticuloSolicitado().getUsuario()
                    : solicitud.getSolicitante();
            String msg = "Tu solicitud de intercambio para \"" + solicitud.getArticuloSolicitado().getTitulo()
                    + "\" ha sido " + nuevoEstado.name().toLowerCase();
            notificacionService.crear(
                    destinatario,
                    usuario,
                    Notificacion.TipoNotificacion.SOLICITUD_INTERCAMBIO,
                    "Intercambio actualizado",
                    msg,
                    Notificacion.ReferenciaTipo.SOLICITUD,
                    updated.getId()
            );
        }

        return SolicitudResponseDto.from(updated);
    }

    private void validarTransicionEstado(
            SolicitudIntercambio.EstadoIntercambio actual,
            SolicitudIntercambio.EstadoIntercambio nuevo,
            boolean esSolicitante, boolean esPropietario, boolean esAdmin) {

        if (actual == SolicitudIntercambio.EstadoIntercambio.CANCELADO
                || actual == SolicitudIntercambio.EstadoIntercambio.RECHAZADO
                || actual == SolicitudIntercambio.EstadoIntercambio.COMPLETADO) {
            throw new RuntimeException("La solicitud ya está en estado final: " + actual);
        }

        switch (nuevo) {
            case CANCELADO:
                if (!esSolicitante && !esAdmin)
                    throw new RuntimeException("Solo el solicitante puede cancelar la solicitud");
                break;
            case ACEPTADO:
            case RECHAZADO:
                if (!esPropietario && !esAdmin)
                    throw new RuntimeException("Solo el propietario del artículo puede aceptar o rechazar");
                if (actual != SolicitudIntercambio.EstadoIntercambio.PENDIENTE)
                    throw new RuntimeException("Solo se puede aceptar/rechazar una solicitud pendiente");
                break;
            case COMPLETADO:
                if (!esPropietario && !esAdmin)
                    throw new RuntimeException("Solo el propietario puede marcar como completado");
                if (actual != SolicitudIntercambio.EstadoIntercambio.ACEPTADO)
                    throw new RuntimeException("Solo se puede completar una solicitud aceptada");
                break;
            default:
                throw new RuntimeException("Transición de estado no permitida");
        }
    }

    private void verificarAcceso(SolicitudIntercambio solicitud, Usuario usuario) {
        boolean esSolicitante = solicitud.getSolicitante().getId().equals(usuario.getId());
        boolean esPropietario = solicitud.getArticuloSolicitado().getUsuario().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRol() != null && usuario.getRol().contains("ADMIN");
        if (!esSolicitante && !esPropietario && !esAdmin) {
            throw new RuntimeException("No tienes acceso a esta solicitud");
        }
    }
}
