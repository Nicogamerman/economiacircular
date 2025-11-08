package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.ReportDto;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Mensaje;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pp.economia_circular.repositories.VistaArticuloRepository;


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

    public ReportDto generateUserReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte de Usuarios");
        report.setGeneratedAt(LocalDateTime.now());

        Map<String, Object> data = new HashMap<>();

        // Total de usuarios
        long totalUsers = usuarioRepository.count();
        data.put("totalUsers", totalUsers);

        // Usuarios activos
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

        // Total de artículos
        long totalArticles = articleRepository.count();
        data.put("totalArticles", totalArticles);

        // Artículos disponibles
        long availableArticles = articleRepository.findByEstado(Articulo.EstadoArticulo.DISPONIBLE).size();
        data.put("availableArticles", availableArticles);

        // Artículos intercambiados
        long exchangedArticles = articleRepository.findByEstado(Articulo.EstadoArticulo.INTERCAMBIADO).size();
        data.put("exchangedArticles", exchangedArticles);

        // Artículos por categoría
        Map<String, Long> articlesByCategory = new HashMap<>();
        for (Articulo.CategoriaArticulo category : Articulo.CategoriaArticulo.values()) {
            articlesByCategory.put(category.name(),
                    (long) articleRepository.findByCategoriaAndEstado(category, Articulo.EstadoArticulo.DISPONIBLE).size());
        }
        data.put("articlesByCategory", articlesByCategory);

        // Artículos por condición
        Map<String, Long> articlesByCondition = new HashMap<>();
        for (Articulo.CondicionArticulo condition : Articulo.CondicionArticulo.values()) {
            articlesByCondition.put(condition.name(),
                    (long) articleRepository.findByEstado(Articulo.EstadoArticulo.DISPONIBLE).stream()
                            .filter(a -> a.getCondicion() == condition)
                            .count());
        }
        data.put("articlesByCondition", articlesByCondition);
        // Top 5 artículos más vistos
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

        // Usuarios con más artículos
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

        // Traer todos los artículos, incluso los de usuarios inactivos
        List<Articulo> allArticles = articleRepository.findAll();

        // Verificar si no hay artículos disponibles
        if (allArticles.isEmpty()) {
            return null;  // Retornamos null si no hay artículos
        }

        // Ordenarlos por fecha descendente (simulando popularidad)
        allArticles.sort((a, b) -> b.getCreadoEn().compareTo(a.getCreadoEn()));

        Map<String, Object> popularArticles = new HashMap<>();

        for (int i = 0; i < Math.min(10, allArticles.size()); i++) {
            Articulo article = allArticles.get(i);
            Map<String, Object> articleInfo = new HashMap<>();

            // Comprobamos si el artículo tiene un usuario asignado
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

        // Asignamos los artículos populares a los datos del reporte
        data.put("popularArticles", popularArticles);
        report.setData(data);

        return report;
    }

    @Autowired
    private VistaArticuloRepository  vistaArticuloRepository;


}