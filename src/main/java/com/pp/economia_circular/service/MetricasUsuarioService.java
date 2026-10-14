package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Seguimiento;
import com.pp.economia_circular.entity.SolicitudIntercambio;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Métricas personales: actividad como oferente (artículos, vistas, favoritos, seguidores,
 * valoraciones) y como participante de intercambios, más el impacto ambiental generado.
 */
@Service
@Transactional(readOnly = true)
public class MetricasUsuarioService {

    static final int MESES_ACTIVIDAD = 6;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private SolicitudIntercambioRepository solicitudRepository;

    @Autowired
    private VistaArticuloRepository vistaRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private SeguimientoRepository seguimientoRepository;

    @Autowired(required = false)
    private ValoracionRepository valoracionRepository;

    @Autowired
    private JWTService authService;

    public Map<String, Object> misMetricas() {
        Usuario usuario = authService.getCurrentUser();
        if (usuario == null) throw new RuntimeException("Usuario no autenticado");
        return calcular(usuario.getId());
    }

    Map<String, Object> calcular(Long usuarioId) {
        List<Articulo> articulos = articleRepository.findByUsuario_Id(usuarioId);
        List<SolicitudIntercambio> intercambios = solicitudRepository.findAllByParticipante(usuarioId);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("oferente", metricasOferente(usuarioId, articulos));
        resultado.put("intercambios", metricasIntercambios(usuarioId, intercambios));
        resultado.put("impacto", impacto(usuarioId, intercambios));
        resultado.put("actividadMensual", actividadMensual(usuarioId, articulos, intercambios));
        return resultado;
    }

    private Map<String, Object> metricasOferente(Long usuarioId, List<Articulo> articulos) {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (Articulo a : articulos) {
            String estado = a.getEstado() != null ? a.getEstado().name() : "SIN_ESTADO";
            porEstado.merge(estado, 1L, Long::sum);
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("articulosPublicados", (long) articulos.size());
        m.put("articulosPorEstado", porEstado);
        m.put("vistasTotales", vistaRepository.countByPropietario(usuarioId));
        m.put("favoritosRecibidos", favoritoRepository.countByPropietario(usuarioId));
        m.put("seguidores", seguimientoRepository.countByTipoAndObjetivoId(Seguimiento.TipoSeguimiento.OFERENTE, usuarioId));
        if (valoracionRepository != null) {
            Double promedio = valoracionRepository.calcularPromedioDeUsuario(usuarioId);
            m.put("valoracionPromedio", promedio != null ? redondear(promedio) : null);
            m.put("cantidadValoraciones", valoracionRepository.contarValoracionesDeUsuario(usuarioId));
        }
        return m;
    }

    private Map<String, Object> metricasIntercambios(Long usuarioId, List<SolicitudIntercambio> intercambios) {
        long enviados = 0, recibidos = 0, recibidosRespondidos = 0, recibidosAceptados = 0;
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (SolicitudIntercambio.EstadoIntercambio e : SolicitudIntercambio.EstadoIntercambio.values()) {
            porEstado.put(e.name(), 0L);
        }

        for (SolicitudIntercambio s : intercambios) {
            porEstado.merge(s.getEstado().name(), 1L, Long::sum);
            if (esSolicitante(s, usuarioId)) {
                enviados++;
            } else {
                recibidos++;
                if (s.getEstado() != SolicitudIntercambio.EstadoIntercambio.PENDIENTE
                        && s.getEstado() != SolicitudIntercambio.EstadoIntercambio.CANCELADO) {
                    recibidosRespondidos++;
                    if (s.getEstado() != SolicitudIntercambio.EstadoIntercambio.RECHAZADO) {
                        recibidosAceptados++;
                    }
                }
            }
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", (long) intercambios.size());
        m.put("enviados", enviados);
        m.put("recibidos", recibidos);
        m.put("completados", porEstado.get("COMPLETADO"));
        m.put("porEstado", porEstado);
        m.put("tasaAceptacion", recibidosRespondidos == 0 ? null
                : redondear(100.0 * recibidosAceptados / recibidosRespondidos));
        return m;
    }

    /** CO2 y residuos evitados por los artículos propios que se reutilizaron en intercambios completados. */
    private Map<String, Object> impacto(Long usuarioId, List<SolicitudIntercambio> intercambios) {
        double co2 = 0, residuos = 0;
        long reutilizados = 0;
        for (SolicitudIntercambio s : intercambios) {
            if (s.getEstado() != SolicitudIntercambio.EstadoIntercambio.COMPLETADO) continue;
            Articulo propio = esSolicitante(s, usuarioId) ? s.getArticuloOfrecido() : s.getArticuloSolicitado();
            if (propio == null) continue;
            co2 += ReportService.calcularCo2(propio);
            residuos += ReportService.calcularResiduos(propio);
            reutilizados++;
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("articulosReutilizados", reutilizados);
        m.put("co2EvitadoKg", redondear(co2));
        m.put("residuosEvitadosKg", redondear(residuos));
        return m;
    }

    /** Artículos publicados e intercambios completados por mes (últimos meses, del más viejo al actual). */
    private List<Map<String, Object>> actividadMensual(Long usuarioId, List<Articulo> articulos,
                                                       List<SolicitudIntercambio> intercambios) {
        YearMonth actual = YearMonth.now();
        Map<YearMonth, long[]> meses = new LinkedHashMap<>();
        for (int i = MESES_ACTIVIDAD - 1; i >= 0; i--) {
            meses.put(actual.minusMonths(i), new long[2]);
        }
        for (Articulo a : articulos) {
            long[] fila = meses.get(mes(a.getCreadoEn()));
            if (fila != null) fila[0]++;
        }
        for (SolicitudIntercambio s : intercambios) {
            if (s.getEstado() != SolicitudIntercambio.EstadoIntercambio.COMPLETADO) continue;
            long[] fila = meses.get(mes(s.getActualizadoEn() != null ? s.getActualizadoEn() : s.getCreadoEn()));
            if (fila != null) fila[1]++;
        }

        List<Map<String, Object>> lista = new ArrayList<>();
        for (Map.Entry<YearMonth, long[]> e : meses.entrySet()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("mes", e.getKey().toString());
            fila.put("articulosPublicados", e.getValue()[0]);
            fila.put("intercambiosCompletados", e.getValue()[1]);
            lista.add(fila);
        }
        return lista;
    }

    private boolean esSolicitante(SolicitudIntercambio s, Long usuarioId) {
        return s.getSolicitante() != null && usuarioId.equals(s.getSolicitante().getId());
    }

    private YearMonth mes(LocalDateTime fecha) {
        return fecha != null ? YearMonth.from(fecha) : null;
    }

    private double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
