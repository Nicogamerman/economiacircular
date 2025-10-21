package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.ReportDto;
import com.pp.economia_circular.entity.Article;
import com.pp.economia_circular.entity.Message;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    
    @Autowired
    private Usuario userRepository;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    public ReportDto generateUserReport() {
        ReportDto report = new ReportDto();
        report.setTitle("Reporte de Usuarios");
        report.setGeneratedAt(LocalDateTime.now());
        
        Map<String, Object> data = new HashMap<>();
        
        // Total de usuarios
        long totalUsers = userRepository.count();
        data.put("totalUsers", totalUsers);
        
        // Usuarios activos
        long activeUsers = userRepository.findByStatus(User.UserStatus.ACTIVE).size();
        data.put("activeUsers", activeUsers);
        
        // Usuarios verificados
        long verifiedUsers = userRepository.findVerifiedUsers().size();
        data.put("verifiedUsers", verifiedUsers);
        
        // Usuarios por rol
        Map<String, Long> usersByRole = new HashMap<>();
        usersByRole.put("USER", (long) userRepository.findByRole(User.Role.USER).size());
        usersByRole.put("ADMIN", (long) userRepository.findByRole(User.Role.ADMIN).size());
        usersByRole.put("MODERATOR", (long) userRepository.findByRole(User.Role.MODERATOR).size());
        data.put("usersByRole", usersByRole);
        
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
        long availableArticles = articleRepository.findByStatus(Article.ArticleStatus.AVAILABLE).size();
        data.put("availableArticles", availableArticles);
        
        // Artículos intercambiados
        long exchangedArticles = articleRepository.findByStatus(Article.ArticleStatus.EXCHANGED).size();
        data.put("exchangedArticles", exchangedArticles);
        
        // Artículos por categoría
        Map<String, Long> articlesByCategory = new HashMap<>();
        for (Article.ArticleCategory category : Article.ArticleCategory.values()) {
            articlesByCategory.put(category.name(), 
                (long) articleRepository.findByCategoryAndStatus(category, Article.ArticleStatus.AVAILABLE).size());
        }
        data.put("articlesByCategory", articlesByCategory);
        
        // Artículos por condición
        Map<String, Long> articlesByCondition = new HashMap<>();
        for (Article.ArticleCondition condition : Article.ArticleCondition.values()) {
            articlesByCondition.put(condition.name(), 
                (long) articleRepository.findByStatus(Article.ArticleStatus.AVAILABLE).stream()
                    .filter(a -> a.getCondition() == condition)
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
        List<Usuario> users = userRepository.findAll();
        Map<String, Long> usersByArticleCount = new HashMap<>();
        for (User user : users) {
            long articleCount = articleRepository.countAvailableArticlesByUser(user.getId());
            if (articleCount > 0) {
                usersByArticleCount.put(user.getUsername(), articleCount);
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
        
        // Artículos más consultados (simulado con artículos más recientes)
        List<Article> recentArticles = articleRepository.findAvailableArticles();
        Map<String, Object> popularArticles = new HashMap<>();
        
        for (int i = 0; i < Math.min(10, recentArticles.size()); i++) {
            Article article = recentArticles.get(i);
            Map<String, Object> articleInfo = new HashMap<>();
            articleInfo.put("title", article.getTitle());
            articleInfo.put("category", article.getCategory());
            articleInfo.put("user", article.getUser().getUsername());
            articleInfo.put("createdAt", article.getCreatedAt());
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
        long totalMessages = messageRepository.count();
        data.put("totalMessages", totalMessages);
        
        // Mensajes no leídos
        long unreadMessages = messageRepository.findByStatus(Message.MessageStatus.READ).size();
        data.put("unreadMessages", unreadMessages);
        
        // Mensajes leídos
        long readMessages = messageRepository.findByStatus(Message.MessageStatus.UNREAD).size();
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
        long exchangedArticles = articleRepository.findByStatus(Article.ArticleStatus.EXCHANGED).size();
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
