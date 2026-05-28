package com.pp.economia_circular.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pp.economia_circular.DTO.PoliticaCreateDto;
import com.pp.economia_circular.DTO.PoliticaResponseDto;
import com.pp.economia_circular.entity.Politica;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.JWTService;
import com.pp.economia_circular.service.PoliticaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

@WebMvcTest(controllers = PoliticaController.class)
@org.springframework.context.annotation.Import(com.pp.economia_circular.config.TestSecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class PoliticaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private PoliticaService politicaService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private PoliticaResponseDto responseDto;
    private PoliticaCreateDto createDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        responseDto = new PoliticaResponseDto();
        responseDto.setId(1L);
        responseDto.setTipo(Politica.TipoPolitica.TERMINOS_USO);
        responseDto.setTitulo("Términos de uso");
        responseDto.setContenido("Contenido...");
        responseDto.setActivo(true);
        responseDto.setOrden(1);
        responseDto.setCreadoEn(LocalDateTime.now());
        responseDto.setActualizadoEn(LocalDateTime.now());

        createDto = new PoliticaCreateDto();
        createDto.setTipo(Politica.TipoPolitica.TERMINOS_USO);
        createDto.setTitulo("Términos de uso");
        createDto.setContenido("Contenido de términos de uso");
        createDto.setActivo(true);
        createDto.setOrden(1);
    }

    @Test
    void listar_Publico_Success() throws Exception {
        when(politicaService.listarActivas(null)).thenReturn(Arrays.asList(responseDto));

        mockMvc.perform(get("/api/politicas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Términos de uso"))
                .andExpect(jsonPath("$[0].tipo").value("TERMINOS_USO"));
    }

    @Test
    void listar_ConFiltroTipo_Success() throws Exception {
        when(politicaService.listarActivas(Politica.TipoPolitica.TERMINOS_USO))
                .thenReturn(Arrays.asList(responseDto));

        mockMvc.perform(get("/api/politicas").param("tipo", "TERMINOS_USO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("TERMINOS_USO"));
    }

    @Test
    void obtenerPorId_Publico_Success() throws Exception {
        when(politicaService.obtener(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/politicas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void obtenerPorId_NoEncontrado_BadRequest() throws Exception {
        when(politicaService.obtener(999L)).thenThrow(new RuntimeException("Política no encontrada"));

        mockMvc.perform(get("/api/politicas/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Política no encontrada"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_Admin_Success() throws Exception {
        when(politicaService.crear(any(PoliticaCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/politicas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(politicaService).crear(any(PoliticaCreateDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void crear_Usuario_Forbidden() throws Exception {
        mockMvc.perform(post("/api/politicas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());

        verify(politicaService, never()).crear(any());
    }

    @Test
    void crear_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/politicas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_Admin_Success() throws Exception {
        when(politicaService.actualizar(eq(1L), any())).thenReturn(responseDto);

        mockMvc.perform(put("/api/politicas/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminar_Admin_Success() throws Exception {
        doNothing().when(politicaService).eliminar(1L);

        mockMvc.perform(delete("/api/politicas/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Política eliminada"));

        verify(politicaService).eliminar(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggle_Admin_Success() throws Exception {
        responseDto.setActivo(false);
        when(politicaService.toggleActivo(1L)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/politicas/1/toggle").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void eliminar_Usuario_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/politicas/1").with(csrf()))
                .andExpect(status().isForbidden());
        verify(politicaService, never()).eliminar(any());
    }
}
