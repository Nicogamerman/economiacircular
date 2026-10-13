package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.FavoritoResponseDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Favorito;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.FavoritoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritoServiceTest {

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private JWTService authService;

    @InjectMocks
    private FavoritoService favoritoService;

    private Usuario usuario;
    private Articulo articulo;
    private Favorito favorito;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("user@test.com");
        usuario.setNombre("Juan");
        usuario.setApellido("Perez");

        articulo = new Articulo();
        articulo.setId(10L);
        articulo.setTitulo("Bicicleta");
        articulo.setEstado(Articulo.EstadoArticulo.DISPONIBLE);
        articulo.setUsuario(new Usuario());
        articulo.getUsuario().setId(2L);

        favorito = new Favorito(usuario, articulo);
        favorito.setId(1L);
    }

    @Test
    void agregar_Success() {
        when(authService.getCurrentUser()).thenReturn(usuario);
        when(articleRepository.findById(10L)).thenReturn(Optional.of(articulo));
        when(favoritoRepository.existsByUsuario_IdAndArticulo_Id(1L, 10L)).thenReturn(false);
        when(favoritoRepository.save(any(Favorito.class))).thenReturn(favorito);

        FavoritoResponseDto result = favoritoService.agregarFavorito(10L);

        assertNotNull(result);
        verify(favoritoRepository).save(any(Favorito.class));
    }

    @Test
    void agregar_YaExiste_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(usuario);
        when(articleRepository.findById(10L)).thenReturn(Optional.of(articulo));
        when(favoritoRepository.existsByUsuario_IdAndArticulo_Id(1L, 10L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> favoritoService.agregarFavorito(10L));
        assertEquals("El artículo ya está en tus favoritos", ex.getMessage());
        verify(favoritoRepository, never()).save(any());
    }

    @Test
    void agregar_ArticuloCancelado_ThrowsException() {
        articulo.setEstado(Articulo.EstadoArticulo.CANCELADO);
        when(authService.getCurrentUser()).thenReturn(usuario);
        when(articleRepository.findById(10L)).thenReturn(Optional.of(articulo));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> favoritoService.agregarFavorito(10L));
        assertEquals("El artículo no está disponible", ex.getMessage());
    }

    @Test
    void agregar_NoAutenticado_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> favoritoService.agregarFavorito(10L));
        assertEquals("Usuario no autenticado", ex.getMessage());
    }

    @Test
    void quitar_Success() {
        when(authService.getCurrentUser()).thenReturn(usuario);
        when(favoritoRepository.existsByUsuario_IdAndArticulo_Id(1L, 10L)).thenReturn(true);
        doNothing().when(favoritoRepository).deleteByUsuario_IdAndArticulo_Id(1L, 10L);

        favoritoService.quitarFavorito(10L);

        verify(favoritoRepository).deleteByUsuario_IdAndArticulo_Id(1L, 10L);
    }

    @Test
    void quitar_NoExiste_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(usuario);
        when(favoritoRepository.existsByUsuario_IdAndArticulo_Id(1L, 10L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> favoritoService.quitarFavorito(10L));
        assertEquals("El artículo no está en tus favoritos", ex.getMessage());
    }

    @Test
    void esFavorito_True() {
        when(authService.getCurrentUser()).thenReturn(usuario);
        when(favoritoRepository.existsByUsuario_IdAndArticulo_Id(1L, 10L)).thenReturn(true);

        assertTrue(favoritoService.esFavorito(10L));
    }

    @Test
    void esFavorito_SinAutenticacion_ReturnsFalse() {
        when(authService.getCurrentUser()).thenReturn(null);

        assertFalse(favoritoService.esFavorito(10L));
    }

    @Test
    void contarFavoritos_Success() {
        when(favoritoRepository.countByArticulo_Id(10L)).thenReturn(5L);

        assertEquals(5L, favoritoService.contarFavoritos(10L));
    }

    @Test
    void listarFavoritos_Success() {
        when(authService.getCurrentUser()).thenReturn(usuario);
        Page<Favorito> page = new PageImpl<>(Arrays.asList(favorito));
        when(favoritoRepository.findByUsuario_Id(eq(1L), any())).thenReturn(page);

        Page<FavoritoResponseDto> result = favoritoService.listarFavoritos(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}
