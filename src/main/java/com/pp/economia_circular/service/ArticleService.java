package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.ArticleCreateDto;
import com.pp.economia_circular.DTO.ArticleResponseDto;
import com.pp.economia_circular.DTO.ArticleSearchDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.EtiquetaArticulo;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.EtiquetaArticuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private JWTService authService;

    @Autowired(required = false)
    private EtiquetaArticuloRepository etiquetaRepository;

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
        article.setSubcategoria(normalizar(createDto.getSubcategoria()));
        article.setMarca(normalizar(createDto.getMarca()));
        article.setModelo(normalizar(createDto.getModelo()));
        article.setUsuario(currentUser);

        Articulo savedArticle = articleRepository.save(article);
        reemplazarEtiquetas(savedArticle, createDto.getEtiquetas());
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
        boolean usaCamposAvanzados = !esVacio(searchDto.getQ())
                || !esVacio(searchDto.getSubcategoria())
                || !esVacio(searchDto.getMarca())
                || !esVacio(searchDto.getTag());

        if (!usaCamposAvanzados) {
            return articleRepository.searchArticles(
                    searchDto.getTitle(),
                    searchDto.getCategory(),
                    searchDto.getCondition(),
                    pageable
            ).map(this::convertToResponseDto);
        }

        String q = !esVacio(searchDto.getQ()) ? searchDto.getQ() : searchDto.getTitle();
        return articleRepository.searchArticlesAvanzada(
                esVacio(q) ? null : q,
                searchDto.getCategory(),
                searchDto.getCondition(),
                esVacio(searchDto.getSubcategoria()) ? null : searchDto.getSubcategoria(),
                esVacio(searchDto.getMarca()) ? null : searchDto.getMarca(),
                esVacio(searchDto.getTag()) ? null : searchDto.getTag(),
                pageable
        ).map(this::convertToResponseDto);
    }

    public List<String> listarSubcategorias(Articulo.CategoriaArticulo category) {
        return articleRepository.findDistinctSubcategorias(category);
    }

    public List<String> listarMarcas(Articulo.CategoriaArticulo category) {
        return articleRepository.findDistinctMarcas(category);
    }

    public List<Map<String, Object>> listarTopEtiquetas(int limit) {
        if (etiquetaRepository == null) return Collections.emptyList();
        int max = Math.max(1, Math.min(limit, 100));
        List<Object[]> raw = etiquetaRepository.findTopEtiquetas();
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (int i = 0; i < Math.min(raw.size(), max); i++) {
            Object[] fila = raw.get(i);
            Map<String, Object> entry = new HashMap<>();
            entry.put("etiqueta", fila[0]);
            entry.put("cantidad", fila[1]);
            resultado.add(entry);
        }
        return resultado;
    }

    @Autowired
    private ReportService reportService;
    // ✅ CORRECTO: actualización con validación de propietario
    public ArticleResponseDto updateArticle(Long id, ArticleCreateDto updateDto) {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Articulo articulo = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        if (!articulo.getUsuario().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tienes permisos para editar este artículo");
        }

        articulo.setTitulo(updateDto.getTitle());
        articulo.setDescripcion(updateDto.getDescription());
        articulo.setCategoria(updateDto.getCategory());
        articulo.setCondicion(updateDto.getCondition());
        articulo.setEstado(updateDto.getEstado());
        articulo.setSubcategoria(normalizar(updateDto.getSubcategoria()));
        articulo.setMarca(normalizar(updateDto.getMarca()));
        articulo.setModelo(normalizar(updateDto.getModelo()));
        articulo.setActualizadoEn(LocalDateTime.now());

        Articulo updated = articleRepository.save(articulo);

        if (updateDto.getEtiquetas() != null) {
            reemplazarEtiquetas(updated, updateDto.getEtiquetas());
        }

        Articulo.EstadoArticulo estado = articulo.getEstado();
        if (estado == Articulo.EstadoArticulo.INTERCAMBIADO
                || estado == Articulo.EstadoArticulo.DONADO
                || estado == Articulo.EstadoArticulo.VENDIDO) {
            reportService.recalcularImpactoAmbiental();
        }

        return convertToResponseDto(updated);
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
        dto.setSubcategoria(article.getSubcategoria());
        dto.setMarca(article.getMarca());
        dto.setModelo(article.getModelo());
        dto.setCondition(article.getCondicion());
        dto.setStatus(article.getEstado());
        if (article.getEtiquetas() != null) {
            dto.setEtiquetas(article.getEtiquetas().stream()
                    .map(EtiquetaArticulo::getEtiqueta)
                    .collect(Collectors.toList()));
        }
        if (article.getUsuario() != null) {
            dto.setUserId(article.getUsuario().getId());
            dto.setUsername(article.getUsuario().getEmail());
        }
        dto.setCreatedAt(article.getCreadoEn());
        dto.setUpdatedAt(article.getActualizadoEn());
        return dto;
    }

    private void reemplazarEtiquetas(Articulo articulo, List<String> nuevasEtiquetas) {
        if (etiquetaRepository == null) return;

        List<EtiquetaArticulo> actuales = etiquetaRepository.findByArticulo_Id(articulo.getId());
        if (!actuales.isEmpty()) {
            etiquetaRepository.deleteAll(actuales);
        }

        if (nuevasEtiquetas == null || nuevasEtiquetas.isEmpty()) return;

        Set<String> sanitizadas = new LinkedHashSet<>();
        for (String raw : nuevasEtiquetas) {
            if (raw == null) continue;
            String norm = raw.trim().toLowerCase();
            if (norm.isEmpty() || norm.length() > 60) continue;
            sanitizadas.add(norm);
        }

        List<EtiquetaArticulo> nuevas = new ArrayList<>();
        for (String etiqueta : sanitizadas) {
            nuevas.add(new EtiquetaArticulo(etiqueta, articulo));
        }
        etiquetaRepository.saveAll(nuevas);
    }

    private String normalizar(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean esVacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
