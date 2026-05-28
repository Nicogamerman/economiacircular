package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.ReportDto;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Mensaje;
import com.pp.economia_circular.entity.SolicitudIntercambio;
import com.pp.economia_circular.entity.Event;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.repositories.VistaArticuloRepository;
import com.pp.economia_circular.repositories.SolicitudIntercambioRepository;
import com.pp.economia_circular.repositories.EventRepository;
import com.pp.economia_circular.repositories.OferenteValoracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;


@Service
public class ReportService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private VistaArticuloRepository vistaArticuloRepository;

    @Autowired
    private SolicitudIntercambioRepository solicitudIntercambioRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OferenteValoracionRepository oferenteValoracionRepository;

    @Autowired
    private JWTService jwtService;

    public ReportDto generateOverviewReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Panel ejecutivo de reportes");
        report.setGeneratedAt(LocalDateTime.now());

        Map<String, Object> data = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last30Days = now.minusDays(30);

        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Articulo> articulos = articleRepository.findAll();
        List<SolicitudIntercambio> solicitudes = solicitudIntercambioRepository.findAll();
        List<Mensaje> mensajes = mensajeRepository.findAll();
        List<Object[]> topViews = vistaArticuloRepository.findTopViewedArticles();

        long totalUsuarios = usuarios.size();
        long usuariosActivos = usuarios.stream().filter(Usuario::isActivo).count();
        long usuariosNuevos30 = usuarioRepository.countByCreadoEnAfter(last30Days);
        long totalArticulos = articulos.size();
        long articulosNuevos30 = articleRepository.countByCreadoEnAfter(last30Days);
        long totalVistas = vistaArticuloRepository.count();
        long vistas30 = vistaArticuloRepository.countByVistoEnAfter(last30Days);
        long mensajes30 = mensajeRepository.countByCreadoEnAfter(last30Days);
        long solicitudes30 = solicitudIntercambioRepository.countByCreadoEnAfter(last30Days);

        long disponibles = countArticlesByStatus(articulos, Articulo.EstadoArticulo.DISPONIBLE);
        long reservados = countArticlesByStatus(articulos, Articulo.EstadoArticulo.RESERVADO);
        long pausados = countArticlesByStatus(articulos, Articulo.EstadoArticulo.PAUSADO);
        long reutilizados = articulos.stream().filter(this::isReusedArticle).count();

        long solicitudesPendientes = solicitudIntercambioRepository.countByEstado(SolicitudIntercambio.EstadoIntercambio.PENDIENTE);
        long solicitudesCompletadas = solicitudIntercambioRepository.countByEstado(SolicitudIntercambio.EstadoIntercambio.COMPLETADO);
        long valoracionesPendientes = oferenteValoracionRepository.countByAprobadoFalse();
        long valoracionesAprobadas = oferenteValoracionRepository.countByAprobadoTrue();
        Double promedioRating = oferenteValoracionRepository.findAverageApprovedRating();

        Set<Long> publicadores = new HashSet<>();
        for (Articulo articulo : articulos) {
            if (articulo.getUsuario() != null) {
                publicadores.add(articulo.getUsuario().getId());
            }
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("usuariosTotales", totalUsuarios);
        resumen.put("usuariosActivos", usuariosActivos);
        resumen.put("usuariosNuevos30", usuariosNuevos30);
        resumen.put("articulosTotales", totalArticulos);
        resumen.put("articulosDisponibles", disponibles);
        resumen.put("articulosReutilizados", reutilizados);
        resumen.put("articulosNuevos30", articulosNuevos30);
        resumen.put("vistasTotales", totalVistas);
        resumen.put("vistas30", vistas30);
        resumen.put("mensajes30", mensajes30);
        resumen.put("solicitudes30", solicitudes30);
        resumen.put("valoracionesPendientes", valoracionesPendientes);
        resumen.put("promedioRating", round(promedioRating == null ? 0.0 : promedioRating));
        resumen.put("conversionIntercambios", percentage(solicitudesCompletadas, solicitudes.size()));
        resumen.put("tasaReutilizacion", percentage(reutilizados, totalArticulos));
        resumen.put("participacionPublicadores", percentage(publicadores.size(), usuariosActivos));
        resumen.put("promedioVistasPorArticulo", totalArticulos == 0 ? 0.0 : round(totalVistas * 1.0 / totalArticulos));
        data.put("resumen", resumen);

        Map<String, Object> moderacion = new LinkedHashMap<>();
        moderacion.put("valoracionesPendientes", valoracionesPendientes);
        moderacion.put("valoracionesAprobadas", valoracionesAprobadas);
        moderacion.put("totalValoraciones", valoracionesPendientes + valoracionesAprobadas);
        moderacion.put("ratingPromedioAprobado", round(promedioRating == null ? 0.0 : promedioRating));
        data.put("moderacion", moderacion);

        Map<String, Object> inventario = new LinkedHashMap<>();
        inventario.put("porEstado", buildArticlesByStatus(articulos));
        inventario.put("porCategoria", buildArticlesByCategory(articulos));
        inventario.put("disponibles", disponibles);
        inventario.put("reservados", reservados);
        inventario.put("pausados", pausados);
        data.put("inventario", inventario);

        Map<String, Object> intercambio = new LinkedHashMap<>();
        intercambio.put("totales", solicitudes.size());
        intercambio.put("pendientes", solicitudesPendientes);
        intercambio.put("completados", solicitudesCompletadas);
        intercambio.put("conversionCompletados", percentage(solicitudesCompletadas, solicitudes.size()));
        intercambio.put("porEstado", buildExchangeStatus(solicitudes));
        data.put("intercambio", intercambio);

        Map<String, Object> actividad = new LinkedHashMap<>();
        actividad.put("ultimos6Meses", buildActivityByMonth(usuarios, articulos, solicitudes, mensajes));
        actividad.put("embudo", buildFunnel(totalArticulos, totalVistas, solicitudes.size(), solicitudesCompletadas));
        data.put("actividad", actividad);

        Map<String, Object> ambiental = buildEnvironmentalSummary(articulos);
        data.put("ambiental", ambiental);

        data.put("topArticulos", buildTopViewedArticles(topViews));
        data.put("topUsuarios", buildTopUsers(usuarios, articulos));
        data.put("alertas", buildInsights(valoracionesPendientes, solicitudesPendientes, disponibles, totalVistas, totalArticulos));

        report.setData(data);
        return report;
    }

    public ReportDto generateEnvironmentalReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte Ambiental");
        report.setGeneratedAt(LocalDateTime.now());

        Map<String, Object> data = new LinkedHashMap<>();

        List<Articulo> articulos = articleRepository.findAll();
        int totalArticulos = articulos.size();

        List<Articulo> reutilizados = new ArrayList<>();
        for (Articulo a : articulos) {
            if (a.getEstado() == Articulo.EstadoArticulo.INTERCAMBIADO
                    || a.getEstado() == Articulo.EstadoArticulo.DONADO
                    || a.getEstado() == Articulo.EstadoArticulo.VENDIDO) {
                reutilizados.add(a);
            }
        }

        int cantidadReutilizados = reutilizados.size();

        double co2Total = 0.0;
        double aguaTotal = 0.0;
        double residuosTotal = 0.0;
        double energiaTotal = 0.0;

        Map<String, Map<String, Object>> impactoPorCategoria = new HashMap<>();
        Map<String, Map<String, Object>> impactoPorTipo = new HashMap<>();
        Map<String, Map<String, Object>> impactoPorMes = new HashMap<>();

        for (Articulo art : reutilizados) {
            double co2 = calcularCo2(art);
            double agua = calcularAgua(art);
            double residuos = calcularResiduos(art);
            double energia = calcularEnergia(art);

            co2Total += co2;
            aguaTotal += agua;
            residuosTotal += residuos;
            energiaTotal += energia;

            String categoria = art.getCategoria().name();
            if (!impactoPorCategoria.containsKey(categoria)) {
                Map<String, Object> inicial = new HashMap<>();
                inicial.put("co2Kg", 0.0);
                inicial.put("residuosKg", 0.0);
                inicial.put("reutilizados", 0);
                impactoPorCategoria.put(categoria, inicial);
            }
            Map<String, Object> impactoCat = impactoPorCategoria.get(categoria);
            impactoCat.put("co2Kg", ((Double) impactoCat.get("co2Kg")) + co2);
            impactoCat.put("residuosKg", ((Double) impactoCat.get("residuosKg")) + residuos);
            impactoCat.put("reutilizados", ((Integer) impactoCat.get("reutilizados")) + 1);

            String tipo = art.getEstado().name();
            if (!impactoPorTipo.containsKey(tipo)) {
                Map<String, Object> inicial = new HashMap<>();
                inicial.put("cantidad", 0);
                inicial.put("co2Kg", 0.0);
                impactoPorTipo.put(tipo, inicial);
            }
            Map<String, Object> impactoTipo = impactoPorTipo.get(tipo);
            impactoTipo.put("cantidad", ((Integer) impactoTipo.get("cantidad")) + 1);
            impactoTipo.put("co2Kg", ((Double) impactoTipo.get("co2Kg")) + co2);

            if (art.getActualizadoEn() != null) {
                String mes = art.getActualizadoEn().toLocalDate().toString().substring(0, 7);
                if (!impactoPorMes.containsKey(mes)) {
                    Map<String, Object> inicial = new HashMap<>();
                    inicial.put("reutilizados", 0);
                    inicial.put("co2Kg", 0.0);
                    impactoPorMes.put(mes, inicial);
                }
                Map<String, Object> impactoMes = impactoPorMes.get(mes);
                impactoMes.put("reutilizados", ((Integer) impactoMes.get("reutilizados")) + 1);
                impactoMes.put("co2Kg", ((Double) impactoMes.get("co2Kg")) + co2);
            }
        }

        Map<String, Object> equivalentes = new HashMap<>();
        equivalentes.put("autosSinUsarUnDia", (int) (co2Total / 120));
        equivalentes.put("duchasAhorradas", (int) (aguaTotal / 80));

        Map<String, Object> impactoUsuario = new HashMap<>();
        Usuario usuario = jwtService.getCurrentUser();
        if (usuario != null) {
            List<Articulo> delUsuario = new ArrayList<>();
            for (Articulo a : reutilizados) {
                if (a.getUsuario() != null && a.getUsuario().getId().equals(usuario.getId())) {
                    delUsuario.add(a);
                }
            }

            double co2Usuario = 0.0;
            for (Articulo a : delUsuario) {
                co2Usuario += calcularCo2(a);
            }

            impactoUsuario.put("email", usuario.getEmail());
            impactoUsuario.put("reutilizados", delUsuario.size());
            impactoUsuario.put("co2EvitadoKg", co2Usuario);
        }

        double porcentajeReutilizacion = totalArticulos == 0 ? 0.0 :
                (cantidadReutilizados * 100.0 / totalArticulos);

        data.put("articulosTotales", totalArticulos);
        data.put("articulosReutilizados", cantidadReutilizados);
        data.put("porcentajeReutilizacion", porcentajeReutilizacion);
        data.put("co2EvitadoKg", co2Total);
        data.put("aguaEvitadaL", aguaTotal);
        data.put("residuosEvitadosKg", residuosTotal);
        data.put("energiaEvitadaKwh", energiaTotal);
        data.put("impactoPorCategoria", impactoPorCategoria);
        data.put("impactoPorTipo", impactoPorTipo);
        data.put("impactoPorMes", impactoPorMes);
        data.put("equivalentes", equivalentes);
        if (!impactoUsuario.isEmpty()) {
            data.put("usuario", impactoUsuario);
        }

        report.setData(data);
        return report;
    }

    // =======================
// Métodos auxiliares
// =======================
    private long countArticlesByStatus(List<Articulo> articulos, Articulo.EstadoArticulo estado) {
        return articulos.stream().filter(articulo -> articulo.getEstado() == estado).count();
    }

    private boolean isReusedArticle(Articulo articulo) {
        return articulo.getEstado() == Articulo.EstadoArticulo.INTERCAMBIADO
                || articulo.getEstado() == Articulo.EstadoArticulo.DONADO
                || articulo.getEstado() == Articulo.EstadoArticulo.VENDIDO;
    }

    private double percentage(long value, long total) {
        if (total == 0) {
            return 0.0;
        }
        return round(value * 100.0 / total);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private Map<String, Long> buildArticlesByStatus(List<Articulo> articulos) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Articulo.EstadoArticulo estado : Articulo.EstadoArticulo.values()) {
            result.put(estado.name(), countArticlesByStatus(articulos, estado));
        }
        return result;
    }

    private List<Map<String, Object>> buildArticlesByCategory(List<Articulo> articulos) {
        Map<String, Map<String, Object>> categories = new LinkedHashMap<>();
        for (Articulo.CategoriaArticulo categoria : Articulo.CategoriaArticulo.values()) {
            Map<String, Object> initial = new LinkedHashMap<>();
            initial.put("categoria", categoria.name());
            initial.put("total", 0L);
            initial.put("disponibles", 0L);
            initial.put("reutilizados", 0L);
            initial.put("vistasEstimadas", 0L);
            categories.put(categoria.name(), initial);
        }

        for (Articulo articulo : articulos) {
            if (articulo.getCategoria() == null) {
                continue;
            }
            Map<String, Object> item = categories.get(articulo.getCategoria().name());
            item.put("total", ((Long) item.get("total")) + 1L);
            if (articulo.getEstado() == Articulo.EstadoArticulo.DISPONIBLE) {
                item.put("disponibles", ((Long) item.get("disponibles")) + 1L);
            }
            if (isReusedArticle(articulo)) {
                item.put("reutilizados", ((Long) item.get("reutilizados")) + 1L);
            }
            if (articulo.getVistas() != null) {
                item.put("vistasEstimadas", ((Long) item.get("vistasEstimadas")) + articulo.getVistas().size());
            }
        }

        return new ArrayList<>(categories.values());
    }

    private Map<String, Long> buildExchangeStatus(List<SolicitudIntercambio> solicitudes) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (SolicitudIntercambio.EstadoIntercambio estado : SolicitudIntercambio.EstadoIntercambio.values()) {
            result.put(estado.name(), 0L);
        }
        for (SolicitudIntercambio solicitud : solicitudes) {
            result.put(solicitud.getEstado().name(), result.get(solicitud.getEstado().name()) + 1L);
        }
        return result;
    }

    private List<Map<String, Object>> buildActivityByMonth(
            List<Usuario> usuarios,
            List<Articulo> articulos,
            List<SolicitudIntercambio> solicitudes,
            List<Mensaje> mensajes) {
        Map<String, Map<String, Object>> months = new LinkedHashMap<>();
        YearMonth current = YearMonth.now().minusMonths(5);
        for (int i = 0; i < 6; i++) {
            String key = current.plusMonths(i).toString();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mes", key);
            item.put("usuarios", 0L);
            item.put("articulos", 0L);
            item.put("solicitudes", 0L);
            item.put("mensajes", 0L);
            months.put(key, item);
        }

        for (Usuario usuario : usuarios) {
            incrementMonth(months, usuario.getCreadoEn(), "usuarios");
        }
        for (Articulo articulo : articulos) {
            incrementMonth(months, articulo.getCreadoEn(), "articulos");
        }
        for (SolicitudIntercambio solicitud : solicitudes) {
            incrementMonth(months, solicitud.getCreadoEn(), "solicitudes");
        }
        for (Mensaje mensaje : mensajes) {
            incrementMonth(months, mensaje.getCreadoEn(), "mensajes");
        }

        return new ArrayList<>(months.values());
    }

    private void incrementMonth(Map<String, Map<String, Object>> months, LocalDateTime date, String field) {
        if (date == null) {
            return;
        }
        String key = YearMonth.from(date).toString();
        if (!months.containsKey(key)) {
            return;
        }
        Map<String, Object> item = months.get(key);
        item.put(field, ((Long) item.get(field)) + 1L);
    }

    private List<Map<String, Object>> buildFunnel(
            long totalArticulos,
            long totalVistas,
            long totalSolicitudes,
            long solicitudesCompletadas) {
        List<Map<String, Object>> funnel = new ArrayList<>();
        funnel.add(funnelItem("Articulos publicados", totalArticulos));
        funnel.add(funnelItem("Vistas registradas", totalVistas));
        funnel.add(funnelItem("Solicitudes iniciadas", totalSolicitudes));
        funnel.add(funnelItem("Intercambios completados", solicitudesCompletadas));
        return funnel;
    }

    private Map<String, Object> funnelItem(String name, long value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    private Map<String, Object> buildEnvironmentalSummary(List<Articulo> articulos) {
        Map<String, Object> result = new LinkedHashMap<>();
        double co2Total = 0.0;
        double aguaTotal = 0.0;
        double residuosTotal = 0.0;
        double energiaTotal = 0.0;
        long reutilizados = 0L;

        for (Articulo articulo : articulos) {
            if (!isReusedArticle(articulo)) {
                continue;
            }
            reutilizados++;
            co2Total += calcularCo2(articulo);
            aguaTotal += calcularAgua(articulo);
            residuosTotal += calcularResiduos(articulo);
            energiaTotal += calcularEnergia(articulo);
        }

        result.put("articulosReutilizados", reutilizados);
        result.put("co2EvitadoKg", round(co2Total));
        result.put("aguaEvitadaL", round(aguaTotal));
        result.put("residuosEvitadosKg", round(residuosTotal));
        result.put("energiaEvitadaKwh", round(energiaTotal));
        return result;
    }

    private List<Map<String, Object>> buildTopViewedArticles(List<Object[]> topViews) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < Math.min(8, topViews.size()); i++) {
            Object[] row = topViews.get(i);
            Long articleId = ((Number) row[0]).longValue();
            Long views = ((Number) row[1]).longValue();
            articleRepository.findById(articleId).ifPresent(article -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", article.getId());
                item.put("titulo", article.getTitulo());
                item.put("categoria", article.getCategoria() == null ? "SIN_CATEGORIA" : article.getCategoria().name());
                item.put("estado", article.getEstado() == null ? "SIN_ESTADO" : article.getEstado().name());
                item.put("duenio", article.getUsuario() == null ? "Sin usuario" : article.getUsuario().getEmail());
                item.put("vistas", views);
                item.put("creadoEn", article.getCreadoEn());
                result.add(item);
            });
        }
        return result;
    }

    private List<Map<String, Object>> buildTopUsers(List<Usuario> usuarios, List<Articulo> articulos) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            long publicados = 0L;
            long disponibles = 0L;
            long reutilizados = 0L;
            for (Articulo articulo : articulos) {
                if (articulo.getUsuario() == null || !usuario.getId().equals(articulo.getUsuario().getId())) {
                    continue;
                }
                publicados++;
                if (articulo.getEstado() == Articulo.EstadoArticulo.DISPONIBLE) {
                    disponibles++;
                }
                if (isReusedArticle(articulo)) {
                    reutilizados++;
                }
            }
            if (publicados == 0) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", usuario.getId());
            item.put("nombre", ((usuario.getNombre() == null ? "" : usuario.getNombre()) + " " +
                    (usuario.getApellido() == null ? "" : usuario.getApellido())).trim());
            item.put("email", usuario.getEmail());
            item.put("publicados", publicados);
            item.put("disponibles", disponibles);
            item.put("reutilizados", reutilizados);
            item.put("activo", usuario.isActivo());
            result.add(item);
        }

        result.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                Long leftScore = ((Long) left.get("publicados")) + ((Long) left.get("reutilizados"));
                Long rightScore = ((Long) right.get("publicados")) + ((Long) right.get("reutilizados"));
                return rightScore.compareTo(leftScore);
            }
        });

        if (result.size() > 8) {
            return new ArrayList<>(result.subList(0, 8));
        }
        return result;
    }

    private List<Map<String, Object>> buildInsights(
            long valoracionesPendientes,
            long solicitudesPendientes,
            long disponibles,
            long totalVistas,
            long totalArticulos) {
        List<Map<String, Object>> insights = new ArrayList<>();
        if (valoracionesPendientes > 0) {
            insights.add(insight("Moderacion", valoracionesPendientes + " comentarios esperan aprobacion.", "warning"));
        }
        if (solicitudesPendientes > 0) {
            insights.add(insight("Intercambios", solicitudesPendientes + " solicitudes siguen pendientes.", "info"));
        }
        if (disponibles == 0) {
            insights.add(insight("Inventario", "No hay articulos disponibles para mostrar en la web.", "error"));
        }
        if (totalArticulos > 0 && totalVistas == 0) {
            insights.add(insight("Traccion", "Hay articulos publicados, pero todavia no registran vistas.", "warning"));
        }
        if (insights.isEmpty()) {
            insights.add(insight("Estado general", "La plataforma no presenta alertas operativas relevantes.", "success"));
        }
        return insights;
    }

    private Map<String, Object> insight(String title, String message, String severity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("titulo", title);
        item.put("mensaje", message);
        item.put("severidad", severity);
        return item;
    }

    private double calcularCo2(Articulo art) {
        switch (art.getCategoria()) {
            case ELECTRONICOS: return 3.0;
            case MUEBLES: return 1.5;
            case ROPA: return 1.0;
            default: return 0.5;
        }
    }

    private double calcularAgua(Articulo art) {
        switch (art.getCategoria()) {
            case ROPA: return 200.0;
            case MUEBLES: return 80.0;
            default: return 40.0;
        }
    }

    private double calcularResiduos(Articulo art) {
        switch (art.getCategoria()) {
            case ELECTRONICOS: return 1.5;
            case MUEBLES: return 1.0;
            case ROPA: return 0.8;
            default: return 0.5;
        }
    }

    private double calcularEnergia(Articulo art) {
        switch (art.getCategoria()) {
            case ELECTRONICOS: return 20.0;
            case MUEBLES: return 10.0;
            default: return 5.0;
        }
    }


    public ReportDto generateArticleReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte de Artículos");
        report.setGeneratedAt(LocalDateTime.now());

        Map<String, Object> data = new HashMap<>();

        long totalArticles = articleRepository.count();
        data.put("totalArticles", totalArticles);

        long availableArticles = articleRepository.findByEstado(Articulo.EstadoArticulo.DISPONIBLE).size();
        data.put("availableArticles", availableArticles);

        long exchangedArticles = articleRepository.findByEstado(Articulo.EstadoArticulo.INTERCAMBIADO).size();
        data.put("exchangedArticles", exchangedArticles);

        Map<String, Long> articlesByCategory = new HashMap<>();
        for (Articulo.CategoriaArticulo category : Articulo.CategoriaArticulo.values()) {
            articlesByCategory.put(category.name(),
                    (long) articleRepository.findByCategoriaAndEstado(category, Articulo.EstadoArticulo.DISPONIBLE).size());
        }
        data.put("articlesByCategory", articlesByCategory);

        Map<String, Long> articlesByCondition = new HashMap<>();
        for (Articulo.CondicionArticulo condition : Articulo.CondicionArticulo.values()) {
            articlesByCondition.put(condition.name(),
                    (long) articleRepository.findByEstado(Articulo.EstadoArticulo.DISPONIBLE).stream()
                            .filter(a -> a.getCondicion() == condition)
                            .count());
        }
        data.put("articlesByCondition", articlesByCondition);

        List<Object[]> topViewed = vistaArticuloRepository.findTopViewedArticles();
        Map<String, Object> topViewedArticles = new HashMap<>();

        for (int i = 0; i < Math.min(5, topViewed.size()); i++) {
            Object[] result = topViewed.get(i);
            Long articleId = (Long) result[0];
            Long views = (Long) result[1];

            final Long vistas = views;
            final int index = i;

            articleRepository.findById(articleId).ifPresent(article -> {
                Map<String, Object> info = new HashMap<>();
                info.put("titulo", article.getTitulo());
                info.put("categoria", article.getCategoria());
                info.put("condicion", article.getCondicion());
                info.put("vistas", vistas);
                topViewedArticles.put("articulo_" + (index + 1), info);
            });
        }

        data.put("topViewedArticles", topViewedArticles);

        report.setData(data);
        return report;
    }

    public ReportDto generateTopUsersReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte de Mejores Usuarios");
        report.setGeneratedAt(LocalDateTime.now());

        Map<String, Object> data = new HashMap<>();

        List<Usuario> users = usuarioRepository.findAll();
        Map<String, Long> usersByArticleCount = new HashMap<>();
        for (Usuario user : users) {
            long articleCount = articleRepository.countAvailableArticlesByUser(user.getId());
            if (articleCount > 0) {
                usersByArticleCount.put(user.getEmail(), articleCount);
            }
        }
        data.put("usersByArticleCount", usersByArticleCount);

        report.setData(data);
        return report;
    }

    public ReportDto generateTopArticlesReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte de Artículos Más Populares");
        report.setGeneratedAt(LocalDateTime.now());

        Map<String, Object> data = new HashMap<>();

        List<Articulo> allArticles = articleRepository.findAll();

        if (allArticles.isEmpty()) {
            return null;
        }

        allArticles.sort((a, b) -> b.getCreadoEn().compareTo(a.getCreadoEn()));

        Map<String, Object> popularArticles = new HashMap<>();

        for (int i = 0; i < Math.min(10, allArticles.size()); i++) {
            Articulo article = allArticles.get(i);
            Map<String, Object> articleInfo = new HashMap<>();

            if (article.getUsuario() != null) {
                articleInfo.put("title", article.getTitulo());
                articleInfo.put("category", article.getCategoria());
                articleInfo.put("user", article.getUsuario().getEmail());
                articleInfo.put("userActivo", article.getUsuario().isActivo());
            } else {
                articleInfo.put("user", "Desconocido");
                articleInfo.put("userActivo", false);
            }

            articleInfo.put("createdAt", article.getCreadoEn());
            articleInfo.put("condition", article.getCondicion());
            popularArticles.put("article_" + (i + 1), articleInfo);
        }

        data.put("popularArticles", popularArticles);
        report.setData(data);
        return report;
    }

    public ReportDto generateSocialReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte Social");
        report.setGeneratedAt(LocalDateTime.now());

        Map<String, Object> data = new HashMap<>();

        // Usuarios
        long totalUsuarios = usuarioRepository.count();
        long usuariosActivos = usuarioRepository.findAll().stream().filter(Usuario::isActivo).count();
        data.put("usuariosTotales", totalUsuarios);
        data.put("usuariosActivos", usuariosActivos);

        // Intercambios
        long totalIntercambios = solicitudIntercambioRepository.count();
        long intercambiosPendientes = solicitudIntercambioRepository.countByEstado(
                SolicitudIntercambio.EstadoIntercambio.PENDIENTE);
        long intercambiosCompletados = solicitudIntercambioRepository.countByEstado(
                SolicitudIntercambio.EstadoIntercambio.COMPLETADO);
        long intercambiosCancelados = solicitudIntercambioRepository.countByEstado(
                SolicitudIntercambio.EstadoIntercambio.CANCELADO);

        data.put("intercambiosTotales", totalIntercambios);
        data.put("pendientes", intercambiosPendientes);
        data.put("completados", intercambiosCompletados);
        data.put("cancelados", intercambiosCancelados);

        // Eventos
        long totalEventos = eventRepository.count();
        long eventosActivos = eventRepository.countByStatus(Event.EventStatus.ACTIVE);
        long eventosFinalizados = eventRepository.countByStatus(Event.EventStatus.COMPLETED);

        data.put("eventosTotales", totalEventos);
        data.put("eventosActivos", eventosActivos);
        data.put("eventosFinalizados", eventosFinalizados);

        report.setData(data);
        return report;
    }

    public void recalcularImpactoAmbiental() {
        // Este método simplemente volverá a generar el reporte ambiental
        // y actualizará los totales internos si los estás guardando en memoria o base
    }

    public ReportDto generateUserReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte de Usuarios");
        report.setGeneratedAt(LocalDateTime.now());

        Map<String, Object> data = new HashMap<>();

        long totalUsers = usuarioRepository.count();
        data.put("totalUsuarios", totalUsers);

        long activeUsers = usuarioRepository.findAll().stream()
                .filter(Usuario::isActivo)
                .count();
        data.put("usuariosActivos", activeUsers);

        // Porcentaje de usuarios activos
        double porcentajeActivos = totalUsers > 0
                ? (activeUsers * 100.0 / totalUsers)
                : 0.0;
        data.put("porcentajeActivos", porcentajeActivos);

        // Últimos 5 usuarios registrados
        List<Usuario> ultimos = usuarioRepository.findAll();
        if (ultimos.size() > 5) {
            ultimos = ultimos.subList(ultimos.size() - 5, ultimos.size());
        }

        Map<String, Object> ultimosUsuarios = new HashMap<>();
        for (Usuario u : ultimos) {
            Map<String, Object> info = new HashMap<>();
            info.put("email", u.getEmail());
            info.put("activo", u.isActivo());
            info.put("rol", u.getRol());
            ultimosUsuarios.put("usuario_" + u.getId(), info);
        }

        data.put("ultimosUsuarios", ultimosUsuarios);
        report.setData(data);

        return report;
    }




}

