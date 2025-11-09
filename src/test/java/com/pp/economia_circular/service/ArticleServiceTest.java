package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.ArticleCreateDto;
import com.pp.economia_circular.DTO.ArticleResponseDto;
import com.pp.economia_circular.DTO.ArticleSearchDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private JWTService authService;

    @InjectMocks
    private ArticleService articleService;

    private Usuario testUser;
    private Articulo testArticulo;
    private ArticleCreateDto createDto;

    @BeforeEach
    void setUp() {
        testUser = new Usuario();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setNombre("Test");
        testUser.setApellido("User");
        testUser.setRol("USER");

        testArticulo = new Articulo();
        testArticulo.setId(1L);
        testArticulo.setTitulo("Test Article");
        testArticulo.setDescripcion("Test Description");
        testArticulo.setCategoria(Articulo.CategoriaArticulo.ELECTRONICOS);
        testArticulo.setCondicion(Articulo.CondicionArticulo.USADO);
        testArticulo.setEstado(Articulo.EstadoArticulo.DISPONIBLE);
        testArticulo.setUsuario(testUser);
        testArticulo.setCreadoEn(LocalDateTime.now());
        testArticulo.setActualizadoEn(LocalDateTime.now());

        createDto = new ArticleCreateDto();
        createDto.setTitle("Test Article");
        createDto.setDescription("Test Description");
        createDto.setCategory(Articulo.CategoriaArticulo.ELECTRONICOS);
        createDto.setCondition(Articulo.CondicionArticulo.USADO);
    }

    @Test
    void createArticle_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(articleRepository.save(any(Articulo.class))).thenReturn(testArticulo);

        ArticleResponseDto result = articleService.createArticle(createDto);

        assertNotNull(result);
        assertEquals("Test Article", result.getTitle());
        verify(articleRepository, times(1)).save(any(Articulo.class));
    }

    @Test
    void createArticle_UserNotAuthenticated_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> articleService.createArticle(createDto));
        assertEquals("Usuario no autenticado", exception.getMessage());
        verify(articleRepository, never()).save(any());
    }

    @Test
    void getArticleById_Success() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticulo));

        ArticleResponseDto result = articleService.getArticleById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getArticleById_NotFound_ThrowsException() {
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> articleService.getArticleById(999L));
        assertEquals("Artículo no encontrado", exception.getMessage());
    }

    @Test
    void getAllArticles_Success() {
        when(articleRepository.findAvailableArticles()).thenReturn(Arrays.asList(testArticulo));

        List<ArticleResponseDto> result = articleService.getAllArticles();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllArticles_WithPagination_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Articulo> articlePage = new PageImpl<>(Arrays.asList(testArticulo));
        when(articleRepository.findAvailableArticles(pageable)).thenReturn(articlePage);

        Page<ArticleResponseDto> result = articleService.getAllArticles(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getArticlesByUser_Success() {
        when(articleRepository.findByUsuario_Id(1L)).thenReturn(Arrays.asList(testArticulo));

        List<ArticleResponseDto> result = articleService.getArticlesByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getMyArticles_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(articleRepository.findByUsuario_Id(1L)).thenReturn(Arrays.asList(testArticulo));

        List<ArticleResponseDto> result = articleService.getMyArticles();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getMyArticles_NotAuthenticated_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> articleService.getMyArticles());
        assertEquals("Usuario no autenticado", exception.getMessage());
    }

    @Test
    void getArticlesByCategory_Success() {
        when(articleRepository.findByCategoriaAndEstado(
                Articulo.CategoriaArticulo.ELECTRONICOS,
                Articulo.EstadoArticulo.DISPONIBLE))
                .thenReturn(Arrays.asList(testArticulo));

        List<ArticleResponseDto> result =
                articleService.getArticlesByCategory(Articulo.CategoriaArticulo.ELECTRONICOS);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void searchArticles_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        ArticleSearchDto searchDto = new ArticleSearchDto(
                "Test",
                Articulo.CategoriaArticulo.ELECTRONICOS,
                Articulo.CondicionArticulo.USADO);

        Page<Articulo> articlePage = new PageImpl<>(Arrays.asList(testArticulo));

        when(articleRepository.searchArticles(
                "Test",
                Articulo.CategoriaArticulo.ELECTRONICOS,
                Articulo.CondicionArticulo.USADO,
                pageable)).thenReturn(articlePage);

        Page<ArticleResponseDto> result = articleService.searchArticles(searchDto, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateArticle_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticulo));
        when(articleRepository.save(any(Articulo.class))).thenReturn(testArticulo);

        ArticleCreateDto updateDto = new ArticleCreateDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");
        updateDto.setCategory(Articulo.CategoriaArticulo.LIBROS);
        updateDto.setCondition(Articulo.CondicionArticulo.REACONDICIONADO);

        ArticleResponseDto result = articleService.updateArticle(1L, updateDto);

        assertNotNull(result);
        verify(articleRepository, times(1)).save(any(Articulo.class));
    }

    @Test
    void updateArticle_NotOwner_ThrowsException() {
        Usuario otherUser = new Usuario();
        otherUser.setId(2L);

        when(authService.getCurrentUser()).thenReturn(otherUser);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticulo));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> articleService.updateArticle(1L, createDto));
        assertEquals("No tienes permisos para editar este artículo", exception.getMessage());
    }

    @Test
    void deleteArticle_Success() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticulo));
        when(articleRepository.save(any(Articulo.class))).thenReturn(testArticulo);

        articleService.deleteArticle(1L);

        verify(articleRepository, times(1)).save(any(Articulo.class));
        assertEquals(Articulo.EstadoArticulo.CANCELADO, testArticulo.getEstado());
    }

    @Test
    void deleteArticle_NotOwner_ThrowsException() {
        Usuario otherUser = new Usuario();
        otherUser.setId(2L);

        when(authService.getCurrentUser()).thenReturn(otherUser);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticulo));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> articleService.deleteArticle(1L));
        assertEquals("No tienes permisos para eliminar este artículo", exception.getMessage());
    }

    @Test
    void getMostViewedArticles_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(articleRepository.findMostViewedArticles(pageable))
                .thenReturn(Arrays.asList(testArticulo));

        List<ArticleResponseDto> result = articleService.getMostViewedArticles(pageable);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
