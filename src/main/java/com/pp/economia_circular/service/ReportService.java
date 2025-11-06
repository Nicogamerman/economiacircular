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

        // Traer todos los artículos
        List<Articulo> allArticles = articleRepository.findAll();

        // Ordenar por fecha, evitando NULLs
        allArticles.sort((a, b) -> {
            LocalDateTime fechaA = (a.getCreadoEn() != null) ? a.getCreadoEn() : LocalDateTime.MIN;
            LocalDateTime fechaB = (b.getCreadoEn() != null) ? b.getCreadoEn() : LocalDateTime.MIN;
            return fechaB.compareTo(fechaA);
        });

        Map<String, Object> popularArticles = new HashMap<>();

        for (int i = 0; i < Math.min(10, allArticles.size()); i++) {
            Articulo article = allArticles.get(i);
            Map<String, Object> articleInfo = new HashMap<>();

            articleInfo.put("title", article.getTitulo());
            articleInfo.put("category",
                    article.getCategoria() != null ? article.getCategoria().toString() : "SIN_CATEGORIA");
            articleInfo.put("condition",
                    article.getCondicion() != null ? article.getCondicion().toString() : "SIN_CONDICION");

            // Evita NPE si el usuario es null
            if (article.getUsuario() != null) {
                articleInfo.put("user", article.getUsuario().getEmail());
                articleInfo.put("userActivo", article.getUsuario().isActivo());
            } else {
                articleInfo.put("user", "DESCONOCIDO");
                articleInfo.put("userActivo", false);
            }

            articleInfo.put("createdAt", article.getCreadoEn());

            popularArticles.put("article_" + (i + 1), articleInfo);
        }

        data.put("popularArticles", popularArticles);
        report.setData(data);
        return report;
    }

    public ReportDto generateCommunicationReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte de Comunicación");
        report.setGeneratedAt(LocalDateTime.now());
        
        Map<String, Object> data = new HashMap<>();
        
        // Total de mensajes
        long totalMessages = mensajeRepository.count();
        data.put("totalMessages", totalMessages);
        
        // Mensajes no leídos
        long unreadMessages = mensajeRepository.findByEstado(Mensaje.EstadoMensaje.NO_LEIDO).size();
        data.put("unreadMessages", unreadMessages);
        
        // Mensajes leídos
        long readMessages = mensajeRepository.findByEstado(Mensaje.EstadoMensaje.LEIDO).size();
        data.put("readMessages", readMessages);
        
        report.setData(data);
        return report;
    }
    
    public ReportDto generateEnvironmentalImpactReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte de Impacto Ambiental");
        report.setGeneratedAt(LocalDateTime.now());
        
        Map<String, Object> data = new HashMap<>();
        
        // Artículos intercambiados (evitando compras nuevas)
        long exchangedArticles = articleRepository.findByEstado(Articulo.EstadoArticulo.INTERCAMBIADO).size();
        data.put("exchangedArticles", exchangedArticles);
        
        // Estimación de impacto ambiental
        Map<String, Object> environmentalImpact = new HashMap<>();
        environmentalImpact.put("co2SavedKg", exchangedArticles * 5.2); // Estimación: 5.2kg CO2 por artículo
        environmentalImpact.put("wasteReducedKg", exchangedArticles * 2.1); // Estimación: 2.1kg residuos evitados
        environmentalImpact.put("resourcesSaved", exchangedArticles * 1.5); // Estimación: 1.5 recursos naturales
        
        data.put("environmentalImpact", environmentalImpact);
        report.setData(data);
        return report;
    }
}
