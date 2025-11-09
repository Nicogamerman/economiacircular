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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    @Autowired
    private JWTService jwtService;

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

