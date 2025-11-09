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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public ReportDto generateUserReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte de Usuarios");
        report.setGeneratedAt(LocalDateTime.now());

        Map<String, Object> data = new HashMap<>();

        long totalUsers = usuarioRepository.count();
        data.put("totalUsers", totalUsers);

        long activeUsers = usuarioRepository.findAll().stream()
                .filter(Usuario::isActivo)
                .count();
        data.put("activeUsers", activeUsers);

        report.setData(data);
        return report;
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
    public ReportDto generateEnvironmentalReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte Ambiental");
        report.setGeneratedAt(LocalDateTime.now());

        Map<String, Object> data = new HashMap<>();

        long totalArticulos = articleRepository.count();
        List<Articulo> articulosReutilizados = articleRepository.findByEstado(Articulo.EstadoArticulo.INTERCAMBIADO);

        double porcentajeReutilizacion = totalArticulos > 0
                ? (articulosReutilizados.size() * 100.0 / totalArticulos)
                : 0.0;

        // Factores ambientales por categoría [CO2 kg, Energía kWh, Agua L, Peso promedio kg]
        Map<String, double[]> factores = new HashMap<>();
        factores.put("ELECTRONICA", new double[]{55.0, 200.0, 35.0, 0.4});
        factores.put("ROPA", new double[]{1.0, 5.0, 50.0, 0.8});
        factores.put("MUEBLE", new double[]{25.0, 100.0, 300.0, 25.0});
        factores.put("GENERIC", new double[]{3.0, 20.0, 100.0, 1.5});

        double co2Total = 0.0;
        double energiaTotal = 0.0;
        double aguaTotal = 0.0;
        double residuosEvitados = 0.0;

        Map<String, Map<String, Object>> impactoPorCategoria = new HashMap<>();

        for (Articulo articulo : articulosReutilizados) {
            String categoria = articulo.getCategoria() != null
                    ? articulo.getCategoria().name()
                    : "GENERIC";

            double[] f = factores.getOrDefault(categoria, factores.get("GENERIC"));

            co2Total += f[0];
            energiaTotal += f[1];
            aguaTotal += f[2];
            residuosEvitados += f[3];

            impactoPorCategoria.putIfAbsent(categoria, new HashMap<>());
            Map<String, Object> impacto = impactoPorCategoria.get(categoria);

            impacto.put("reutilizados", (long) impacto.getOrDefault("reutilizados", 0L) + 1);
            impacto.put("co2Kg", (double) impacto.getOrDefault("co2Kg", 0.0) + f[0]);
            impacto.put("residuosKg", (double) impacto.getOrDefault("residuosKg", 0.0) + f[3]);
            impactoPorCategoria.put(categoria, impacto);
        }

        data.put("articulosTotales", totalArticulos);
        data.put("articulosReutilizados", articulosReutilizados.size());
        data.put("porcentajeReutilizacion", porcentajeReutilizacion);
        data.put("co2EvitadoKg", co2Total);
        data.put("energiaEvitadaKwh", energiaTotal);
        data.put("aguaEvitadaL", aguaTotal);
        data.put("residuosEvitadosKg", residuosEvitados);
        data.put("impactoPorCategoria", impactoPorCategoria);

        report.setData(data);
        return report;
    }
    public void recalcularImpactoAmbiental() {
        // Este método simplemente volverá a generar el reporte ambiental
        // y actualizará los totales internos si los estás guardando en memoria o base
    }




}

