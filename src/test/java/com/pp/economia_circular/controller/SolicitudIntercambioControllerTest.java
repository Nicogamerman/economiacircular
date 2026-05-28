package com.pp.economia_circular.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pp.economia_circular.DTO.CambiarEstadoSolicitudDto;
import com.pp.economia_circular.DTO.SolicitudCreateDto;
import com.pp.economia_circular.DTO.SolicitudResponseDto;
import com.pp.economia_circular.entity.SolicitudIntercambio;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.JWTService;
import com.pp.economia_circular.service.SolicitudIntercambioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

@WebMvcTest(controllers = SolicitudIntercambioController.class)
@org.springframework.context.annotation.Import(com.pp.economia_circular.config.TestSecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class SolicitudIntercambioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private SolicitudIntercambioService solicitudService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private SolicitudResponseDto responseDto;
    private SolicitudCreateDto createDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        responseDto = new SolicitudResponseDto();
        responseDto.setId(1L);
        responseDto.setEstado(SolicitudIntercambio.EstadoIntercambio.PENDIENTE);
        responseDto.setArticuloSolicitadoId(10L);
        responseDto.setArticuloSolicitadoTitulo("Bicicleta");
        responseDto.setArticuloOfrecidoId(20L);
        responseDto.setArticuloOfrecidoTitulo("Raqueta");
        responseDto.setSolicitanteId(1L);
        responseDto.setSolicitanteEmail("user@test.com");
        responseDto.setCreadoEn(LocalDateTime.now());
        responseDto.setActualizadoEn(LocalDateTime.now());

        createDto = new SolicitudCreateDto();
        createDto.setArticuloSolicitadoId(10L);
        createDto.setArticuloOfrecidoId(20L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void crear_Success() throws Exception {
        when(solicitudService.crear(any(SolicitudCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/intercambios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        verify(solicitudService).crear(any(SolicitudCreateDto.class));
    }

    @Test
    void crear_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/intercambios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());

        verify(solicitudService, never()).crear(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void crear_ErrorNegocio_BadRequest() throws Exception {
        when(solicitudService.crear(any()))
                .thenThrow(new RuntimeException("No puedes solicitar tu propio artículo"));

        mockMvc.perform(post("/api/intercambios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No puedes solicitar tu propio artículo"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void misSolicitudes_Success() throws Exception {
        Page<SolicitudResponseDto> page = new PageImpl<>(Arrays.asList(responseDto));
        when(solicitudService.listarMisSolicitudes(any())).thenReturn(page);

        mockMvc.perform(get("/api/intercambios/mis-solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void misSolicitudes_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/intercambios/mis-solicitudes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void recibidas_Success() throws Exception {
        Page<SolicitudResponseDto> page = new PageImpl<>(Arrays.asList(responseDto));
        when(solicitudService.listarSolicitudesRecibidas(any())).thenReturn(page);

        mockMvc.perform(get("/api/intercambios/recibidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void historial_Success() throws Exception {
        Page<SolicitudResponseDto> page = new PageImpl<>(Arrays.asList(responseDto));
        when(solicitudService.listarHistorial(any())).thenReturn(page);

        mockMvc.perform(get("/api/intercambios/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].articuloSolicitadoTitulo").value("Bicicleta"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void obtener_Success() throws Exception {
        when(solicitudService.obtener(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/intercambios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "USER")
    void cambiarEstado_Success() throws Exception {
        responseDto.setEstado(SolicitudIntercambio.EstadoIntercambio.ACEPTADO);
        CambiarEstadoSolicitudDto dto = new CambiarEstadoSolicitudDto();
        dto.setNuevoEstado(SolicitudIntercambio.EstadoIntercambio.ACEPTADO);

        when(solicitudService.cambiarEstado(eq(1L), any())).thenReturn(responseDto);

        mockMvc.perform(put("/api/intercambios/1/estado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACEPTADO"));
    }

    @Test
    void cambiarEstado_Unauthorized() throws Exception {
        CambiarEstadoSolicitudDto dto = new CambiarEstadoSolicitudDto();
        dto.setNuevoEstado(SolicitudIntercambio.EstadoIntercambio.ACEPTADO);

        mockMvc.perform(put("/api/intercambios/1/estado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
