package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.DashboardMetricsDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.SolicitudIntercambio;
import com.pp.economia_circular.repositories.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final ArticleRepository articleRepository;
    private final SolicitudIntercambioRepository solicitudRepository;
    private final VistaArticuloRepository vistaRepository;
    private final MensajeRepository mensajeRepository;

    public DashboardService(
            UsuarioRepository usuarioRepository,
            ArticleRepository articleRepository,
            SolicitudIntercambioRepository solicitudRepository,
            VistaArticuloRepository vistaRepository,
            MensajeRepository mensajeRepository) {

        this.usuarioRepository = usuarioRepository;
        this.articleRepository = articleRepository;
        this.solicitudRepository = solicitudRepository;
        this.vistaRepository = vistaRepository;
        this.mensajeRepository = mensajeRepository;
    }

    public DashboardMetricsDto obtenerMetricas() {

        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);

        DashboardMetricsDto dto = new DashboardMetricsDto();

        dto.setTotalUsuarios(usuarioRepository.count());
        dto.setUsuariosActivos(usuarioRepository.countByActivoTrue());
        dto.setUsuariosNuevosUltimos30Dias(usuarioRepository.countByCreadoEnAfter(hace30Dias));

        dto.setTotalArticulos(articleRepository.count());
        dto.setArticulosDisponibles(
                articleRepository.countByEstado(Articulo.EstadoArticulo.DISPONIBLE));
        dto.setArticulosIntercambiados(
                articleRepository.countByEstado(Articulo.EstadoArticulo.INTERCAMBIADO));
        dto.setArticulosUltimos30Dias(
                articleRepository.countByCreadoEnAfter(hace30Dias));

        dto.setTotalSolicitudes(solicitudRepository.count());
        dto.setIntercambiosCompletados(
                solicitudRepository.countByEstado(
                        SolicitudIntercambio.EstadoIntercambio.COMPLETADO));
        dto.setSolicitudesPendientes(
                solicitudRepository.countByEstado(
                        SolicitudIntercambio.EstadoIntercambio.PENDIENTE));

        dto.setTotalVistas(vistaRepository.count());
        dto.setVistasUltimos30Dias(
                vistaRepository.countByVistoEnAfter(hace30Dias));

        dto.setTotalMensajes(mensajeRepository.count());
        dto.setMensajesUltimos30Dias(
                mensajeRepository.countByCreadoEnAfter(hace30Dias));

        return dto;
    }
}
