package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.ArticleCreateDto;
import com.pp.economia_circular.DTO.ArticleResponseDto;
import com.pp.economia_circular.DTO.ArticleSearchDto;
import com.pp.economia_circular.DTO.ArticleUserDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "*")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @GetMapping
    public ResponseEntity<?> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ArticleResponseDto> articles = articleService.getAllArticles(pageable);
            return ResponseEntity.ok(articles);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/articulos-usuario")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getArticulosPorUsuario(@RequestParam String email) {
        try {
            List<Articulo> articulos = articleRepository.findByUsuarioEmail(email)
                    .stream()
                    .filter(a -> a.getEstado() != Articulo.EstadoArticulo.CANCELADO)
                    .collect(Collectors.toList());

            if (articulos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("No se encontraron artículos activos para el usuario con email: " + email);
            }

            List<ArticleUserDto> articulosDto = articulos.stream()
                    .map(a -> new ArticleUserDto(
                            a.getId(),
                            a.getTitulo(),
                            a.getCategoria(),
                            a.getEstado(),
                            a.getCondicion(),
                            a.getDescripcion(),
                            a.getUsuario() != null ? a.getUsuario().getEmail() : "Desconocido"
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(articulosDto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los artículos del usuario: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchArticles(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Articulo.CategoriaArticulo category,
            @RequestParam(required = false) Articulo.CondicionArticulo condition,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            ArticleSearchDto searchDto = new ArticleSearchDto(title, category, condition);
            Page<ArticleResponseDto> articles = articleService.searchArticles(searchDto, pageable);
            return ResponseEntity.ok(articles);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getArticlesByCategory(@PathVariable Articulo.CategoriaArticulo category) {
        try {
            List<ArticleResponseDto> articles = articleService.getArticlesByCategory(category);
            return ResponseEntity.ok(articles);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/most-viewed")
    public ResponseEntity<?> getMostViewedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<ArticleResponseDto> articles = articleService.getMostViewedArticles(pageable);
            return ResponseEntity.ok(articles);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-articles")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyArticles() {
        try {
            List<ArticleResponseDto> articles = articleService.getMyArticles();
            return ResponseEntity.ok(articles);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getArticlesByUser(@PathVariable Long userId) {
        try {
            List<ArticleResponseDto> articles = articleService.getArticlesByUser(userId);
            return ResponseEntity.ok(articles);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticleById(@PathVariable Long id) {
        try {
            ArticleResponseDto article = articleService.getArticleById(id);
            return ResponseEntity.ok(article);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // ✅ MÉTODO POST: Crear artículo
    @PostMapping("")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createArticle(@Valid @RequestBody ArticleCreateDto createDto) {
        try {
            ArticleResponseDto article = articleService.createArticle(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(article);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ MÉTODO PUT: Actualizar artículo existente
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleCreateDto updateDto) {
        try {
            ArticleResponseDto article = articleService.updateArticle(id, updateDto);
            return ResponseEntity.ok(article);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
        try {
            articleService.deleteArticle(id);
            return ResponseEntity.ok("Artículo eliminado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
