package com.pp.economia_circular.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pp.economia_circular.DTO.ArticleResponseDto;
import com.pp.economia_circular.DTO.FavoritoResponseDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.FavoritoService;
import com.pp.economia_circular.service.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FavoritoController.class)
@org.springframework.context.annotation.Import(com.pp.economia_circular.config.TestSecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class FavoritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private FavoritoService favoritoService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private FavoritoResponseDto responseDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ArticleResponseDto artDto = ArticleResponseDto.builder()
                .id(10L).title("Bicicleta")
                .category(Articulo.CategoriaArticulo.DEPORTES)
                .status(Articulo.EstadoArticulo.DISPONIBLE)
                .build();
        responseDto = new FavoritoResponseDto(1L, LocalDateTime.now(), artDto);
    }

    @Test
    @WithMockUser(roles = "USER")
    void agregar_Success() throws Exception {
        when(favoritoService.agregarFavorito(10L)).thenReturn(responseDto);

        mockMvc.perform(post("/api/favoritos/10").with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.favoritoId").value(1L));

        verify(favoritoService).agregarFavorito(10L);
    }

    @Test
    void agregar_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/favoritos/10").with(csrf()))
                .andExpect(status().isUnauthorized());
        verify(favoritoService, never()).agregarFavorito(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void agregar_YaExiste_BadRequest() throws Exception {
        when(favoritoService.agregarFavorito(10L))
                .thenThrow(new RuntimeException("El artículo ya está en tus favoritos"));

        mockMvc.perform(post("/api/favoritos/10").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El artículo ya está en tus favoritos"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void quitar_Success() throws Exception {
        doNothing().when(favoritoService).quitarFavorito(10L);

        mockMvc.perform(delete("/api/favoritos/10").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Artículo eliminado de favoritos"));

        verify(favoritoService).quitarFavorito(10L);
    }

    @Test
    void quitar_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/favoritos/10").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void listar_Success() throws Exception {
        Page<FavoritoResponseDto> page = new PageImpl<>(Arrays.asList(responseDto));
        when(favoritoService.listarFavoritos(any())).thenReturn(page);

        mockMvc.perform(get("/api/favoritos").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].favoritoId").value(1L));
    }

    @Test
    void listar_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/favoritos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void estado_EsFavorito() throws Exception {
        when(favoritoService.esFavorito(10L)).thenReturn(true);

        mockMvc.perform(get("/api/favoritos/10/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.esFavorito").value(true))
                .andExpect(jsonPath("$.articuloId").value(10));
    }

    @Test
    void count_Publico() throws Exception {
        when(favoritoService.contarFavoritos(10L)).thenReturn(7L);

        mockMvc.perform(get("/api/favoritos/10/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(7));
    }
}
