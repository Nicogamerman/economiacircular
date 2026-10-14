package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.DevolucionDto;
import com.pp.economia_circular.entity.Devolucion;
import com.pp.economia_circular.entity.Seguimiento;
import com.pp.economia_circular.entity.VideoArticulo;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.DevolucionService;
import com.pp.economia_circular.service.JWTService;
import com.pp.economia_circular.service.SeguimientoService;
import com.pp.economia_circular.service.VideoArticuloService;
import com.pp.economia_circular.service.ia.IaException;
import com.pp.economia_circular.service.ia.IaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Controladores de video, IA, seguimientos y devoluciones (capa web). */
@WebMvcTest(controllers = {VideoArticuloController.class, IaController.class,
        SeguimientoController.class, DevolucionController.class})
@org.springframework.context.annotation.Import(com.pp.economia_circular.config.TestSecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class NuevasFuncionesControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private VideoArticuloService videoService;
    @MockBean private IaService iaService;
    @MockBean private SeguimientoService seguimientoService;
    @MockBean private DevolucionService devolucionService;
    @MockBean private JWTService jwtService;
    @MockBean private UsuarioRepository usuarioRepository;

    private VideoArticulo video(byte[] contenido) {
        VideoArticulo v = new VideoArticulo();
        v.setContentType("video/mp4");
        v.setContenido(contenido);
        return v;
    }

    @Test
    void videoCompletoSeDevuelveConSuTipo() throws Exception {
        when(videoService.obtenerConContenido(10L)).thenReturn(video(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));

        mockMvc.perform(get("/api/articles/10/video/file"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("video/mp4"))
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"));
    }

    @Test
    void videoAtiendePedidosParcialesConRange() throws Exception {
        when(videoService.obtenerConContenido(10L)).thenReturn(video(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));

        mockMvc.perform(get("/api/articles/10/video/file").header(HttpHeaders.RANGE, "bytes=2-5"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 2-5/10"))
                .andExpect(content().bytes(new byte[]{2, 3, 4, 5}));
    }

    @Test
    void articuloSinVideoDevuelve204() throws Exception {
        when(videoService.metadatos(10L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/articles/10/video")).andExpect(status().isNoContent());
    }

    @Test
    void estadoDeIaEsPublico() throws Exception {
        when(iaService.disponible()).thenReturn(false);

        mockMvc.perform(get("/api/ia/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void iaNoConfiguradaRespondeServicioNoDisponible() throws Exception {
        when(iaService.sugerirArticulo(any())).thenThrow(new IaException("La IA no está configurada", true));

        mockMvc.perform(multipart("/api/ia/sugerir-articulo")
                        .file(new MockMultipartFile("imagen", "a.jpg", "image/jpeg", new byte[]{1}))
                        .with(csrf()))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @WithMockUser(roles = "USER")
    void chatDevuelveLaRespuestaDelAsistente() throws Exception {
        when(iaService.chat(anyList())).thenReturn("¡Hola! Te ayudo.");

        mockMvc.perform(post("/api/ia/chat").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mensajes\":[{\"rol\":\"user\",\"contenido\":\"hola\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta").value("¡Hola! Te ayudo."));
    }

    @Test
    @WithMockUser(roles = "USER")
    void seguirOferenteUsaElTipoDeLaUrl() throws Exception {
        Map<String, Object> estado = new LinkedHashMap<>();
        estado.put("siguiendo", true);
        estado.put("seguidores", 4L);
        when(seguimientoService.seguir(Seguimiento.TipoSeguimiento.OFERENTE, 2L)).thenReturn(estado);

        mockMvc.perform(post("/api/seguimientos/oferentes/2").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.siguiendo").value(true))
                .andExpect(jsonPath("$.seguidores").value(4));
    }

    @Test
    @WithMockUser(roles = "USER")
    void tipoDeSeguimientoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/seguimientos/talleres/2").with(csrf()))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(seguimientoService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void pedirDevolucionSinMotivoEsRechazadoPorValidacion() throws Exception {
        mockMvc.perform(post("/api/intercambios/100/devolucion").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(devolucionService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void pedirDevolucionDevuelve201() throws Exception {
        DevolucionDto dto = new DevolucionDto();
        dto.setId(500L);
        dto.setEstado(Devolucion.EstadoDevolucion.PENDIENTE);
        when(devolucionService.solicitar(100L, "No funciona")).thenReturn(dto);

        mockMvc.perform(post("/api/intercambios/100/devolucion").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"No funciona\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void plazoDeDevolucionEsPublico() throws Exception {
        when(devolucionService.getPlazoDias()).thenReturn(7);

        mockMvc.perform(get("/api/devoluciones/plazo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plazoDias").value(7));
    }
}
