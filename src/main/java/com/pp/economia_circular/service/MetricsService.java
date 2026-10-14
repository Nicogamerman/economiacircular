package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.DashboardSummaryDto;
import com.pp.economia_circular.DTO.DistributionEntryDto;
import com.pp.economia_circular.DTO.GeoPointDto;
import com.pp.economia_circular.DTO.TimelinePointDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Event;
import com.pp.economia_circular.entity.RecyclingCenter;
import com.pp.economia_circular.entity.SolicitudIntercambio;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.EventRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.RecyclingCenterRepository;
import com.pp.economia_circular.repositories.SolicitudIntercambioRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.repositories.ValoracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
public class MetricsService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private SolicitudIntercambioRepository solicitudIntercambioRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RecyclingCenterRepository recyclingCenterRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired(required = false) private ValoracionRepository valoracionRepository;

    public DashboardSummaryDto getDashboardSummary() {
        DashboardSummaryDto dto = new DashboardSummaryDto();

        List<Usuario> usuarios = usuarioRepository.findAll();
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        YearMonth mesActual = YearMonth.now();
        YearMonth mesAnterior = mesActual.minusMonths(1);

        long usuariosActivos = usuarios.stream().filter(Usuario::isActivo).count();
        long usuariosNuevos30 = usuarios.stream()
                .filter(u -> u.getCreadoEn() != null && u.getCreadoEn().isAfter(hace30Dias))
                .count();
        long usuariosMesActual = contarPorMes(usuarios, Usuario::getCreadoEn, mesActual);
        long usuariosMesAnterior = contarPorMes(usuarios, Usuario::getCreadoEn, mesAnterior);

        dto.setUsuariosTotales(usuarios.size());
        dto.setUsuariosActivos(usuariosActivos);
        dto.setUsuariosNuevosUltimos30Dias(usuariosNuevos30);
        dto.setCrecimientoUsuariosPctMesActualVsAnterior(crecimientoPorcentual(usuariosMesAnterior, usuariosMesActual));

        List<Articulo> articulos = articleRepository.findAll();
        long disponibles = articulos.stream()
                .filter(a -> a.getEstado() == Articulo.EstadoArticulo.DISPONIBLE)
                .count();
        long articulos30 = articulos.stream()
                .filter(a -> a.getCreadoEn() != null && a.getCreadoEn().isAfter(hace30Dias))
                .count();
        long articulosMesActual = contarPorMes(articulos, Articulo::getCreadoEn, mesActual);
        long articulosMesAnterior = contarPorMes(articulos, Articulo::getCreadoEn, mesAnterior);

        dto.setArticulosTotales(articulos.size());
        dto.setArticulosDisponibles(disponibles);
        dto.setArticulosPublicadosUltimos30Dias(articulos30);
        dto.setCrecimientoArticulosPctMesActualVsAnterior(crecimientoPorcentual(articulosMesAnterior, articulosMesActual));

        List<SolicitudIntercambio> solicitudes = solicitudIntercambioRepository.findAll();
        long intercambios30 = solicitudes.stream()
                .filter(s -> s.getCreadoEn() != null && s.getCreadoEn().isAfter(hace30Dias))
                .count();
        dto.setIntercambiosTotales(solicitudes.size());
        dto.setIntercambiosPendientes(solicitudIntercambioRepository.countByEstado(SolicitudIntercambio.EstadoIntercambio.PENDIENTE));
        dto.setIntercambiosCompletados(solicitudIntercambioRepository.countByEstado(SolicitudIntercambio.EstadoIntercambio.COMPLETADO));
        dto.setIntercambiosUltimos30Dias(intercambios30);

        dto.setEventosTotales(eventRepository.count());
        dto.setEventosActivos(eventRepository.countByStatus(Event.EventStatus.ACTIVE));

        List<RecyclingCenter> centros = recyclingCenterRepository.findAll();
        dto.setCentrosReciclajeTotales(centros.size());
        dto.setCentrosReciclajeActivos(centros.stream()
                .filter(c -> c.getStatus() == RecyclingCenter.CenterStatus.ACTIVE)
                .count());

        dto.setMensajesTotales(mensajeRepository.count());

        if (valoracionRepository != null) {
            dto.setValoracionesTotales(valoracionRepository.count());
            Double promedioGlobal = articulos.isEmpty() ? null : calcularPromedioGlobalValoraciones();
            dto.setPromedioValoraciones(promedioGlobal);
        } else {
            dto.setValoracionesTotales(0);
        }

        return dto;
    }

    public List<TimelinePointDto> timelineUsuarios(int meses) {
        return construirTimeline(meses, usuarioRepository.findAll(), Usuario::getCreadoEn);
    }

    public List<TimelinePointDto> timelineArticulos(int meses) {
        return construirTimeline(meses, articleRepository.findAll(), Articulo::getCreadoEn);
    }

    public List<TimelinePointDto> timelineIntercambios(int meses) {
        return construirTimeline(meses, solicitudIntercambioRepository.findAll(), SolicitudIntercambio::getCreadoEn);
    }

    public List<DistributionEntryDto> distribucionPorCategoria() {
        List<Articulo> articulos = articleRepository.findAll();
        long total = articulos.size();
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (Articulo.CategoriaArticulo cat : Articulo.CategoriaArticulo.values()) {
            conteo.put(cat.name(), 0L);
        }
        for (Articulo a : articulos) {
            if (a.getCategoria() != null) {
                conteo.merge(a.getCategoria().name(), 1L, Long::sum);
            }
        }
        return convertirDistribucion(conteo, total);
    }

    public List<DistributionEntryDto> distribucionPorEstado() {
        List<Articulo> articulos = articleRepository.findAll();
        long total = articulos.size();
        Map<String, Long> conteo = new LinkedHashMap<>();
        for (Articulo.EstadoArticulo est : Articulo.EstadoArticulo.values()) {
            conteo.put(est.name(), 0L);
        }
        for (Articulo a : articulos) {
            if (a.getEstado() != null) {
                conteo.merge(a.getEstado().name(), 1L, Long::sum);
            }
        }
        return convertirDistribucion(conteo, total);
    }

    public List<GeoPointDto> geoEventos() {
        List<GeoPointDto> puntos = new ArrayList<>();
        for (Event e : eventRepository.findAll()) {
            if (e.getLatitude() == null || e.getLongitude() == null) continue;
            puntos.add(new GeoPointDto(
                    e.getId(),
                    e.getEventName(),
                    e.getDescription(),
                    e.getLatitude(),
                    e.getLongitude(),
                    "EVENTO",
                    e.getEventType() != null ? e.getEventType().name() : null,
                    e.getStatus() != null ? e.getStatus().name() : null
            ));
        }
        return puntos;
    }

    public List<GeoPointDto> geoCentrosReciclaje() {
        List<GeoPointDto> puntos = new ArrayList<>();
        for (RecyclingCenter c : recyclingCenterRepository.findAll()) {
            if (c.getLatitude() == null || c.getLongitude() == null) continue;
            puntos.add(new GeoPointDto(
                    c.getId(),
                    c.getName(),
                    c.getDescription(),
                    c.getLatitude(),
                    c.getLongitude(),
                    "CENTRO_RECICLAJE",
                    c.getCenterType() != null ? c.getCenterType().name() : null,
                    c.getStatus() != null ? c.getStatus().name() : null
            ));
        }
        return puntos;
    }

    private <T> List<TimelinePointDto> construirTimeline(int meses,
                                                         List<T> items,
                                                         Function<T, LocalDateTime> fechaGetter) {
        int ventana = Math.max(1, Math.min(meses, 60));
        YearMonth fin = YearMonth.now();
        YearMonth inicio = fin.minusMonths(ventana - 1L);

        Map<String, Long> conteoPorMes = new LinkedHashMap<>();
        for (YearMonth ym = inicio; !ym.isAfter(fin); ym = ym.plusMonths(1)) {
            conteoPorMes.put(ym.toString(), 0L);
        }

        for (T item : items) {
            LocalDateTime fecha = fechaGetter.apply(item);
            if (fecha == null) continue;
            YearMonth ym = YearMonth.from(fecha);
            if (ym.isBefore(inicio) || ym.isAfter(fin)) continue;
            conteoPorMes.merge(ym.toString(), 1L, Long::sum);
        }

        List<TimelinePointDto> puntos = new ArrayList<>(conteoPorMes.size());
        for (Map.Entry<String, Long> e : conteoPorMes.entrySet()) {
            puntos.add(new TimelinePointDto(e.getKey(), e.getValue()));
        }
        return puntos;
    }

    private List<DistributionEntryDto> convertirDistribucion(Map<String, Long> conteo, long total) {
        List<DistributionEntryDto> resultado = new ArrayList<>(conteo.size());
        for (Map.Entry<String, Long> e : conteo.entrySet()) {
            double pct = total == 0 ? 0.0 : (e.getValue() * 100.0 / total);
            resultado.add(new DistributionEntryDto(
                    e.getKey(),
                    e.getValue(),
                    Math.round(pct * 10.0) / 10.0
            ));
        }
        return resultado;
    }

    private <T> long contarPorMes(List<T> items, Function<T, LocalDateTime> fechaGetter, YearMonth ym) {
        return items.stream()
                .map(fechaGetter)
                .filter(f -> f != null && YearMonth.from(f).equals(ym))
                .count();
    }

    private double crecimientoPorcentual(long anterior, long actual) {
        if (anterior == 0) {
            return actual == 0 ? 0.0 : 100.0;
        }
        double pct = (actual - anterior) * 100.0 / anterior;
        return Math.round(pct * 10.0) / 10.0;
    }

    private Double calcularPromedioGlobalValoraciones() {
        if (valoracionRepository == null) return null;
        long total = valoracionRepository.count();
        if (total == 0) return null;
        Double suma = valoracionRepository.findAll().stream()
                .mapToInt(v -> v.getPuntaje() != null ? v.getPuntaje() : 0)
                .average()
                .orElse(0.0);
        return Math.round(suma * 10.0) / 10.0;
    }
}
