package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.ArticleCreateDto;
import com.pp.economia_circular.DTO.ArticleResponseDto;
import com.pp.economia_circular.DTO.ArticleSearchDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

        Articulo article = new Articulo();
        article.setTitulo(createDto.getTitle());
        article.setDescripcion(createDto.getDescription());
        article.setCategoria(createDto.getCategory());
        article.setCondicion(createDto.getCondition());
        article.setUsuario(currentUser);

        Articulo savedArticle = articleRepository.save(article);
        return convertToResponseDto(savedArticle);
    }

    public ArticleResponseDto getArticleById(Long id) {
        Articulo article = articleRepository.findById(id)
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
        return articleRepository.findByUsuario_Id(userId).stream()
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

    public List<ArticleResponseDto> getArticlesByCategory(Articulo.CategoriaArticulo category) {
        return articleRepository.findByCategoriaAndEstado(category, Articulo.EstadoArticulo.DISPONIBLE).stream()
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

    // ✅ CORRECTO: actualización con validación de propietario
    public ArticleResponseDto updateArticle(Long id, ArticleCreateDto updateDto) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Articulo article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        // Verificar que el usuario sea el propietario
        if (!article.getUsuario().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permisos para editar este artículo");
        }

        article.setTitulo(updateDto.getTitle());
        article.setDescripcion(updateDto.getDescription());
        article.setCategoria(updateDto.getCategory());
        article.setCondicion(updateDto.getCondition());
        article.setEstado(updateDto.getEstado());
        article.setActualizadoEn(LocalDateTime.now());

        Articulo updatedArticle = articleRepository.save(article);
        return convertToResponseDto(updatedArticle);
    }

    public void deleteArticle(Long id) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Articulo article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        if (!article.getUsuario().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permisos para eliminar este artículo");
        }

        article.setEstado(Articulo.EstadoArticulo.CANCELADO); // eliminación lógica
        articleRepository.save(article);
    }

    public List<ArticleResponseDto> getMostViewedArticles(Pageable pageable) {
        return articleRepository.findMostViewedArticles(pageable).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    private ArticleResponseDto convertToResponseDto(Articulo article) {
        ArticleResponseDto dto = new ArticleResponseDto();
        dto.setId(article.getId());
        dto.setTitle(article.getTitulo());
        dto.setDescription(article.getDescripcion());
        dto.setCategory(article.getCategoria());
        dto.setCondition(article.getCondicion());
        dto.setStatus(article.getEstado());
        dto.setUserId(article.getUsuario().getId());
        dto.setUsername(article.getUsuario().getEmail());
        dto.setCreatedAt(article.getCreadoEn());
        dto.setUpdatedAt(article.getActualizadoEn());
        return dto;
    }
}
