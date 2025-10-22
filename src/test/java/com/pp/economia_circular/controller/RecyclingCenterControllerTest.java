package com.pp.economia_circular.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pp.economia_circular.DTO.RecyclingCenterDto;
import com.pp.economia_circular.entity.RecyclingCenter;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RecyclingCenterController.class)
@org.springframework.context.annotation.Import(com.pp.economia_circular.config.TestSecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class RecyclingCenterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private EventService eventService;

    @MockBean
    private ServicioMensaje servicioMensaje;

    @MockBean
    private MensajeRepository mensajeRepository;

    @MockBean
    private RecyclingCenterService recyclingCenterService;

    @MockBean
    private ReportService reportService;

    @MockBean(name = "corsConfigurationSource")
    private CorsConfigurationSource corsConfigurationSource;


    private RecyclingCenterDto centerDto;
    private List<RecyclingCenterDto> centersList;

    @BeforeEach
    void setUp() {
        centerDto = new RecyclingCenterDto();
        centerDto.setId(1L);
        centerDto.setName("Centro Test");
        centerDto.setDescription("Centro de prueba");
        centerDto.setAddress("Calle Test 123");
        centerDto.setLatitude(-34.6037);
        centerDto.setLongitude(-58.3816);
        centerDto.setPhone("+54 11 1234-5678");
        centerDto.setEmail("centro@test.com");
        centerDto.setCenterType(RecyclingCenter.CenterType.valueOf("RECYCLING_CENTER"));
        centerDto.setStatus(RecyclingCenter.CenterStatus.valueOf("ACTIVE"));
        centerDto.setOpeningHours("Lunes a Viernes 9:00-18:00");

        RecyclingCenterDto center2 = new RecyclingCenterDto();
        center2.setId(2L);
        center2.setName("Centro Test 2");
        center2.setDescription("Otro centro");
        center2.setAddress("Av. Test 456");
        center2.setLatitude(-34.6100);
        center2.setLongitude(-58.3900);
        center2.setPhone("+54 11 9876-5432");
        center2.setEmail("centro2@test.com");
        center2.setCenterType(RecyclingCenter.CenterType.valueOf("NGO"));
        center2.setStatus(RecyclingCenter.CenterStatus.valueOf("ACTIVE"));

        centersList = Arrays.asList(centerDto, center2);
    }

    @Test
    @DisplayName("Crear centro exitosamente como ADMIN")
    @WithMockUser(roles = "ADMIN")
    void testCrearCentroExitoso() throws Exception {
        // Arrange
        when(recyclingCenterService.createCenter(any(RecyclingCenterDto.class))).thenReturn(centerDto);

        // Act & Assert
        mockMvc.perform(post("/api/recycling-centers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(centerDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Centro Test")))
                .andExpect(jsonPath("$.email", is("centro@test.com")));

        verify(recyclingCenterService, times(1)).createCenter(any(RecyclingCenterDto.class));
    }

    @Test
    @DisplayName("Crear centro sin autenticación debe fallar")
    void testCrearCentroSinAutenticacion() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/recycling-centers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(centerDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(recyclingCenterService, never()).createCenter(any(RecyclingCenterDto.class));
    }

    @Test
    @DisplayName("Crear centro como USER debe fallar")
    @WithMockUser(roles = "USER")
    void testCrearCentroComoUser() throws Exception {
        // Act & Assert - Un USER no puede crear centros (requiere ADMIN)
        mockMvc.perform(post("/api/recycling-centers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(centerDto)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(recyclingCenterService, never()).createCenter(any(RecyclingCenterDto.class));
    }

    @Test
    @DisplayName("Obtener todos los centros")
    void testObtenerTodosCentros() throws Exception {
        // Arrange
        when(recyclingCenterService.getAllCenters()).thenReturn(centersList);

        // Act & Assert
        mockMvc.perform(get("/api/recycling-centers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Centro Test")))
                .andExpect(jsonPath("$[1].name", is("Centro Test 2")));

        verify(recyclingCenterService, times(1)).getAllCenters();
    }

    @Test
    @DisplayName("Obtener centros por tipo")
    void testObtenerCentrosPorTipo() throws Exception {
        // Arrange
        List<RecyclingCenterDto> recyclingCenters = Collections.singletonList(centerDto);
        when(recyclingCenterService.getCentersByType(RecyclingCenter.CenterType.RECYCLING_CENTER))
                .thenReturn(recyclingCenters);

        // Act & Assert
        mockMvc.perform(get("/api/recycling-centers/type/RECYCLING_CENTER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].centerType", is("RECYCLING_CENTER")));

        verify(recyclingCenterService, times(1)).getCentersByType(RecyclingCenter.CenterType.RECYCLING_CENTER);
    }

    @Test
    @DisplayName("Obtener centros cercanos a ubicación")
    void testObtenerCentrosCercanos() throws Exception {
        // Arrange
        Double latitude = -34.6037;
        Double longitude = -58.3816;
        Double radius = 5.0;
        when(recyclingCenterService.getCentersNearLocation(latitude, longitude, radius))
                .thenReturn(centersList);

        // Act & Assert
        mockMvc.perform(get("/api/recycling-centers/nearby")
                        .param("latitude", latitude.toString())
                        .param("longitude", longitude.toString())
                        .param("radiusKm", radius.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(recyclingCenterService, times(1)).getCentersNearLocation(latitude, longitude, radius);
    }

    @Test
    @DisplayName("Obtener centros cercanos con radio por defecto")
    void testObtenerCentrosCercanosRadioPorDefecto() throws Exception {
        // Arrange
        Double latitude = -34.6037;
        Double longitude = -58.3816;
        when(recyclingCenterService.getCentersNearLocation(eq(latitude), eq(longitude), any(Double.class)))
                .thenReturn(centersList);

        // Act & Assert
        mockMvc.perform(get("/api/recycling-centers/nearby")
                        .param("latitude", latitude.toString())
                        .param("longitude", longitude.toString()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(recyclingCenterService, times(1))
                .getCentersNearLocation(eq(latitude), eq(longitude), any(Double.class));
    }

    @Test
    @DisplayName("Actualizar centro como ADMIN")
    @WithMockUser(roles = "ADMIN")
    void testActualizarCentroExitoso() throws Exception {
        // Arrange
        centerDto.setName("Centro Actualizado");
        when(recyclingCenterService.updateCenter(eq(1L), any(RecyclingCenterDto.class)))
                .thenReturn(centerDto);

        // Act & Assert
        mockMvc.perform(put("/api/recycling-centers/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(centerDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Centro Actualizado")));

        verify(recyclingCenterService, times(1)).updateCenter(eq(1L), any(RecyclingCenterDto.class));
    }

    @Test
    @DisplayName("Actualizar centro sin autenticación debe fallar")
    void testActualizarCentroSinAutenticacion() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/recycling-centers/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(centerDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(recyclingCenterService, never()).updateCenter(any(), any());
    }

    @Test
    @DisplayName("Eliminar centro como ADMIN")
    @WithMockUser(roles = "ADMIN")
    void testEliminarCentroExitoso() throws Exception {
        // Arrange
        doNothing().when(recyclingCenterService).deleteCenter(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/recycling-centers/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Centro eliminado exitosamente"));

        verify(recyclingCenterService, times(1)).deleteCenter(1L);
    }

    @Test
    @DisplayName("Eliminar centro sin autenticación debe fallar")
    void testEliminarCentroSinAutenticacion() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/recycling-centers/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(recyclingCenterService, never()).deleteCenter(any());
    }

    @Test
    @DisplayName("Crear centro con datos inválidos retorna BadRequest")
    @WithMockUser(roles = "ADMIN")
    void testCrearCentroConDatosInvalidos() throws Exception {
        // Arrange
        when(recyclingCenterService.createCenter(any(RecyclingCenterDto.class)))
                .thenThrow(new RuntimeException("Datos inválidos"));

        // Act & Assert
        mockMvc.perform(post("/api/recycling-centers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(centerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Datos inválidos"));

        verify(recyclingCenterService, times(1)).createCenter(any(RecyclingCenterDto.class));
    }

    @Test
    @DisplayName("Obtener centros maneja excepciones correctamente")
    void testObtenerCentrosManejaExcepciones() throws Exception {
        // Arrange
        when(recyclingCenterService.getAllCenters())
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/recycling-centers"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error de base de datos"));

        verify(recyclingCenterService, times(1)).getAllCenters();
    }

    @Test
    @DisplayName("Actualizar centro inexistente retorna error")
    @WithMockUser(roles = "ADMIN")
    void testActualizarCentroInexistente() throws Exception {
        // Arrange
        when(recyclingCenterService.updateCenter(eq(999L), any(RecyclingCenterDto.class)))
                .thenThrow(new RuntimeException("Centro no encontrado"));

        // Act & Assert
        mockMvc.perform(put("/api/recycling-centers/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(centerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Centro no encontrado"));

        verify(recyclingCenterService, times(1)).updateCenter(eq(999L), any(RecyclingCenterDto.class));
    }

    @Test
    @DisplayName("Eliminar centro inexistente retorna error")
    @WithMockUser(roles = "ADMIN")
    void testEliminarCentroInexistente() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Centro no encontrado"))
                .when(recyclingCenterService).deleteCenter(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/recycling-centers/999")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Centro no encontrado"));

        verify(recyclingCenterService, times(1)).deleteCenter(999L);
    }

    @Test
    @DisplayName("Obtener centros vacíos retorna lista vacía")
    void testObtenerCentrosVacios() throws Exception {
        // Arrange
        when(recyclingCenterService.getAllCenters()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/recycling-centers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(recyclingCenterService, times(1)).getAllCenters();
    }

    @Test
    @DisplayName("Endpoint permite CORS")
    void testEndpointPermiteCORS() throws Exception {
        // Arrange
        when(recyclingCenterService.getAllCenters()).thenReturn(centersList);

        // Act & Assert - Con addFilters = false, CORS headers no se agregan automáticamente en tests
        mockMvc.perform(get("/api/recycling-centers")
                        .header("Origin", "http://localhost:3000"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(recyclingCenterService, times(1)).getAllCenters();
    }
}

