package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.DashboardMetricsDto;
import com.pp.economia_circular.DTO.DashboardChartsDto;
import com.pp.economia_circular.DTO.DashboardMapDto;
import com.pp.economia_circular.DTO.MapPointDto;

import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.SolicitudIntercambio;
import com.pp.economia_circular.entity.RecyclingCenter;
import com.pp.economia_circular.entity.Event;

import com.pp.economia_circular.repositories.*;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final ArticleRepository articleRepository;
    private final SolicitudIntercambioRepository solicitudRepository;
    private final VistaArticuloRepository vistaRepository;
    private final MensajeRepository mensajeRepository;
    private final RecyclingCenterRepository recyclingCenterRepository;
    private final EventRepository eventRepository;

    public DashboardService(
            UsuarioRepository usuarioRepository,
            ArticleRepository articleRepository,
            SolicitudIntercambioRepository solicitudRepository,
            VistaArticuloRepository vistaRepository,
            MensajeRepository mensajeRepository,
            RecyclingCenterRepository recyclingCenterRepository,
            EventRepository eventRepository) {

        this.usuarioRepository = usuarioRepository;
        this.articleRepository = articleRepository;
        this.solicitudRepository = solicitudRepository;
        this.vistaRepository = vistaRepository;
        this.mensajeRepository = mensajeRepository;
        this.recyclingCenterRepository = recyclingCenterRepository;
        this.eventRepository = eventRepository;
    }

    // ==============================
    // MÉTRICAS BÁSICAS
    // ==============================
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

    // ==============================
    // DATOS PARA GRÁFICOS
    // ==============================
    public DashboardChartsDto obtenerDatosGraficos() {

        DashboardChartsDto dto = new DashboardChartsDto();

        // =========================
        // Usuarios por mes
        // =========================
        List<Object[]> usuariosRaw = usuarioRepository.countUsuariosAgrupadosPorMes();

        Map<String, Long> usuariosPorMes = new LinkedHashMap<>();

        for (Object[] fila : usuariosRaw) {
            String mes = formatearMes(fila[0], fila[1]);
            Long cantidad = (Long) fila[2];
            usuariosPorMes.put(mes, cantidad);
        }

        dto.setUsuariosPorMes(usuariosPorMes);

        // =========================
        // Intercambios completados por mes
        // =========================
        List<Object[]> intercambiosRaw =
                solicitudRepository.countIntercambiosPorMesPorEstado(
                        SolicitudIntercambio.EstadoIntercambio.COMPLETADO);

        Map<String, Long> intercambiosPorMes = new LinkedHashMap<>();

        for (Object[] fila : intercambiosRaw) {
            String mes = formatearMes(fila[0], fila[1]);
            Long cantidad = (Long) fila[2];
            intercambiosPorMes.put(mes, cantidad);
        }

        dto.setIntercambiosCompletadosPorMes(intercambiosPorMes);

        // =========================
        // Tasa de éxito
        // =========================
        long totalSolicitudes = solicitudRepository.count();
        long intercambiosCompletados =
                solicitudRepository.countByEstado(
                        SolicitudIntercambio.EstadoIntercambio.COMPLETADO);

        dto.setTotalSolicitudes(totalSolicitudes);
        dto.setIntercambiosCompletados(intercambiosCompletados);

        double tasa = 0.0;

        if (totalSolicitudes > 0) {
            tasa = ((double) intercambiosCompletados / totalSolicitudes) * 100;
        }

        dto.setTasaExitoIntercambios(tasa);
        // =========================
        // Artículos por categoría
        // =========================
        List<Object[]> categoriasRaw =
                articleRepository.countArticulosPorCategoria();

        Map<String, Long> articulosPorCategoria = new LinkedHashMap<>();

        for (Object[] fila : categoriasRaw) {
            String categoria = fila[0].toString();
            Long cantidad = (Long) fila[1];
            articulosPorCategoria.put(categoria, cantidad);
        }

        dto.setArticulosPorCategoria(articulosPorCategoria);


        return dto;
    }

    private String formatearMes(Object anioValue, Object mesValue) {
        int anio = ((Number) anioValue).intValue();
        int mes = ((Number) mesValue).intValue();
        return String.format("%04d-%02d", anio, mes);
    }


    // ==============================
    // DATOS PARA MAPA
    // ==============================
    public DashboardMapDto obtenerDatosMapa() {

        DashboardMapDto dto = new DashboardMapDto();

        List<RecyclingCenter> centrosRaw = recyclingCenterRepository.findAll();
        List<MapPointDto> centros = centrosRaw.stream()
                .map(rc -> new MapPointDto(
                        rc.getName(),
                        rc.getLatitude(),
                        rc.getLongitude()))
                .collect(Collectors.toList());

        List<Event> eventosRaw = eventRepository.findAll();
        List<MapPointDto> eventos = eventosRaw.stream()
                .map(e -> new MapPointDto(
                        e.getEventName(),
                        e.getLatitude(),
                        e.getLongitude()))
                .collect(Collectors.toList());

        dto.setRecyclingCenters(centros);
        dto.setEvents(eventos);

        return dto;
    }
}
