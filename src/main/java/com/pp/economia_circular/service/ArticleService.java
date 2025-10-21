package com.pp.economia_circular.service;


import com.pp.economia_circular.DTO.ArticleCreateDto;
import com.pp.economia_circular.DTO.ArticleResponseDto;
import com.pp.economia_circular.DTO.ArticleSearchDto;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.Article;
import com.pp.economia_circular.repositories.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArticleService {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private JWTService authService;
    
    public ArticleResponseDto createArticle(ArticleCreateDto createDto) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        
        Article article = new Article();
        article.setTitle(createDto.getTitle());
        article.setDescription(createDto.getDescription());
        article.setCategory(createDto.getCategory());
        article.setCondition(createDto.getCondition());
        article.setUser(currentUser);
        
        Article savedArticle = articleRepository.save(article);
        return convertToResponseDto(savedArticle);
    }
    
    public ArticleResponseDto getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
        return convertToResponseDto(article);
    }
    
    public List<ArticleResponseDto> getAllArticles() {
        return articleRepository.findAvailableArticles().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public Page<ArticleResponseDto> getAllArticles(Pageable pageable) {
        return articleRepository.findAvailableArticles(pageable)
                .map(this::convertToResponseDto);
    }
    
    public List<ArticleResponseDto> getArticlesByUser(Long userId) {
        return articleRepository.findByUser_Id(userId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public List<ArticleResponseDto> getMyArticles() {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return getArticlesByUser(currentUser.getId());
    }
    
    public List<ArticleResponseDto> getArticlesByCategory(Article.ArticleCategory category) {
        return articleRepository.findByCategoryAndStatus(category, Article.ArticleStatus.AVAILABLE).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public Page<ArticleResponseDto> searchArticles(ArticleSearchDto searchDto, Pageable pageable) {
        return articleRepository.searchArticles(
                searchDto.getTitle(),
                searchDto.getCategory(),
                searchDto.getCondition(),
                pageable
        ).map(this::convertToResponseDto);
    }
    
    public ArticleResponseDto updateArticle(Long id, ArticleCreateDto updateDto) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
        
        // Verificar que el usuario sea el propietario del artículo
        if (!article.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permisos para editar este artículo");
        }
        
        article.setTitle(updateDto.getTitle());
        article.setDescription(updateDto.getDescription());
        article.setCategory(updateDto.getCategory());
        article.setCondition(updateDto.getCondition());
        
        Article updatedArticle = articleRepository.save(article);
        return convertToResponseDto(updatedArticle);
    }
    
    public void deleteArticle(Long id) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
        
        // Verificar que el usuario sea el propietario del artículo
        if (!article.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permisos para eliminar este artículo");
        }
        
        article.setStatus(Article.ArticleStatus.DELETED);
        articleRepository.save(article);
    }
    
    public List<ArticleResponseDto> getMostViewedArticles(Pageable pageable) {
        return articleRepository.findMostViewedArticles(pageable).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    private ArticleResponseDto convertToResponseDto(Article article) {
        ArticleResponseDto dto = new ArticleResponseDto();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setDescription(article.getDescription());
        dto.setCategory(article.getCategory());
        dto.setCondition(article.getCondition());
        dto.setStatus(article.getStatus());
        dto.setUserId(article.getUser().getId());
        dto.setUsername(article.getUser().getUsername());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        return dto;
    }
}
