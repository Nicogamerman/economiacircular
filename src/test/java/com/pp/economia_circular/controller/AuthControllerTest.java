package com.pp.economia_circular.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pp.economia_circular.DTO.AuthRequest;
import com.pp.economia_circular.DTO.AuthResponse;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.ArticleService;
import com.pp.economia_circular.service.EventService;
import com.pp.economia_circular.service.JWTService;
import com.pp.economia_circular.service.RecyclingCenterService;
import com.pp.economia_circular.service.ReportService;
import com.pp.economia_circular.service.ServicioMensaje;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@org.springframework.context.annotation.Import(com.pp.economia_circular.config.TestSecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class AuthControllerTest {

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

    private Usuario usuarioActivo;
    private Usuario usuarioInactivo;
    private BCryptPasswordEncoder passwordEncoder;
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "Test123!";
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        
        // Usuario activo con contraseña encriptada
        usuarioActivo = new Usuario();
        usuarioActivo.setId(1L);
        usuarioActivo.setNombre("Test");
        usuarioActivo.setApellido("User");
        usuarioActivo.setEmail(TEST_EMAIL);
        usuarioActivo.setContrasena(passwordEncoder.encode(TEST_PASSWORD));
        usuarioActivo.setRol("USER");
        usuarioActivo.setActivo(true);
        usuarioActivo.setCreadoEn(LocalDateTime.now());
        usuarioActivo.setActualizadoEn(LocalDateTime.now());

        // Usuario inactivo
        usuarioInactivo = new Usuario();
        usuarioInactivo.setId(2L);
        usuarioInactivo.setNombre("Inactive");
        usuarioInactivo.setApellido("User");
        usuarioInactivo.setEmail("inactive@test.com");
        usuarioInactivo.setContrasena(passwordEncoder.encode(TEST_PASSWORD));
        usuarioInactivo.setRol("USER");
        usuarioInactivo.setActivo(false);
        usuarioInactivo.setCreadoEn(LocalDateTime.now());
        usuarioInactivo.setActualizadoEn(LocalDateTime.now());
    }

    @Test
    @DisplayName("Login exitoso con credenciales válidas")
    void testLoginExitoso() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .email(TEST_EMAIL)
                .contrasena(TEST_PASSWORD)
                .build();

        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(usuarioActivo));
        when(jwtService.generarToken(TEST_EMAIL)).thenReturn(TEST_TOKEN);

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is(TEST_TOKEN)))
                .andExpect(jsonPath("$.token", notNullValue()));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(TEST_EMAIL);
        verify(jwtService, times(1)).generarToken(TEST_EMAIL);
    }

    @Test
    @DisplayName("Login fallido con email inexistente")
    void testLoginEmailInexistente() throws Exception {
        // Arrange
        String emailInexistente = "noexiste@test.com";
        AuthRequest request = AuthRequest.builder()
                .email(emailInexistente)
                .contrasena(TEST_PASSWORD)
                .build();

        when(usuarioRepository.findByEmail(emailInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Credenciales inválidas")));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(emailInexistente);
        verify(jwtService, never()).generarToken(anyString());
    }

    @Test
    @DisplayName("Login fallido con contraseña incorrecta")
    void testLoginContrasenaIncorrecta() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .email(TEST_EMAIL)
                .contrasena("ContrasenaIncorrecta123!")
                .build();

        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(usuarioActivo));

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Credenciales inválidas")));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(TEST_EMAIL);
        verify(jwtService, never()).generarToken(anyString());
    }

    @Test
    @DisplayName("Login fallido con usuario inactivo")
    void testLoginUsuarioInactivo() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .email("inactive@test.com")
                .contrasena(TEST_PASSWORD)
                .build();

        when(usuarioRepository.findByEmail("inactive@test.com")).thenReturn(Optional.of(usuarioInactivo));

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Usuario inactivo")));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail("inactive@test.com");
        verify(jwtService, never()).generarToken(anyString());
    }

    @Test
    @DisplayName("Login exitoso con contraseña en texto plano (legacy)")
    void testLoginContrasenaTextoPlano() throws Exception {
        // Arrange - Usuario con contraseña en texto plano
        Usuario usuarioLegacy = new Usuario();
        usuarioLegacy.setId(3L);
        usuarioLegacy.setNombre("Legacy");
        usuarioLegacy.setApellido("User");
        usuarioLegacy.setEmail("legacy@test.com");
        usuarioLegacy.setContrasena(TEST_PASSWORD); // Sin encriptar
        usuarioLegacy.setRol("USER");
        usuarioLegacy.setActivo(true);
        usuarioLegacy.setCreadoEn(LocalDateTime.now());
        usuarioLegacy.setActualizadoEn(LocalDateTime.now());

        AuthRequest request = AuthRequest.builder()
                .email("legacy@test.com")
                .contrasena(TEST_PASSWORD)
                .build();

        when(usuarioRepository.findByEmail("legacy@test.com")).thenReturn(Optional.of(usuarioLegacy));
        when(jwtService.generarToken("legacy@test.com")).thenReturn(TEST_TOKEN);

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(TEST_TOKEN)));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail("legacy@test.com");
        verify(jwtService, times(1)).generarToken("legacy@test.com");
    }

    @Test
    @DisplayName("Login con request body vacío")
    void testLoginRequestVacio() throws Exception {
        // Act & Assert - Con un objeto vacío {}, email y contraseña son null
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email es requerido")));

        // Verify - el controlador valida antes de llamar al repositorio
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generarToken(anyString());
    }

    @Test
    @DisplayName("Login con email null")
    void testLoginEmailNull() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .email(null)
                .contrasena(TEST_PASSWORD)
                .build();

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Login con contraseña null")
    void testLoginContrasenaNull() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .email(TEST_EMAIL)
                .contrasena(null)
                .build();

        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(usuarioActivo));

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Login con formato JSON inválido")
    void testLoginFormatoJsonInvalido() throws Exception {
        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // Verify
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generarToken(anyString());
    }

    @Test
    @DisplayName("Login con Content-Type incorrecto")
    void testLoginContentTypeIncorrecto() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .email(TEST_EMAIL)
                .contrasena(TEST_PASSWORD)
                .build();

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Login permite CORS desde cualquier origen")
    void testLoginPermiteCORS() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .email(TEST_EMAIL)
                .contrasena(TEST_PASSWORD)
                .build();

        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(usuarioActivo));
        when(jwtService.generarToken(TEST_EMAIL)).thenReturn(TEST_TOKEN);

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Origin", "http://localhost:3000")
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Login con email en mayúsculas funciona correctamente")
    void testLoginEmailMayusculas() throws Exception {
        // Arrange
        String emailMayusculas = "TEST@TEST.COM";
        
        // Crear un usuario con el email en mayúsculas
        Usuario usuarioMayusculas = new Usuario();
        usuarioMayusculas.setId(1L);
        usuarioMayusculas.setNombre("Test");
        usuarioMayusculas.setApellido("User");
        usuarioMayusculas.setEmail(emailMayusculas);
        usuarioMayusculas.setContrasena(passwordEncoder.encode(TEST_PASSWORD));
        usuarioMayusculas.setRol("USER");
        usuarioMayusculas.setActivo(true);
        usuarioMayusculas.setCreadoEn(LocalDateTime.now());
        usuarioMayusculas.setActualizadoEn(LocalDateTime.now());
        
        AuthRequest request = AuthRequest.builder()
                .email(emailMayusculas)
                .contrasena(TEST_PASSWORD)
                .build();

        when(usuarioRepository.findByEmail(emailMayusculas)).thenReturn(Optional.of(usuarioMayusculas));
        when(jwtService.generarToken(emailMayusculas)).thenReturn(TEST_TOKEN);

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(TEST_TOKEN)));
    }

    @Test
    @DisplayName("Login retorna el formato correcto de respuesta")
    void testLoginFormatoRespuesta() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .email(TEST_EMAIL)
                .contrasena(TEST_PASSWORD)
                .build();

        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(usuarioActivo));
        when(jwtService.generarToken(TEST_EMAIL)).thenReturn(TEST_TOKEN);

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").value(TEST_TOKEN))
                .andExpect(jsonPath("$.token").value(org.hamcrest.Matchers.startsWith("eyJ")));
    }

    @Test
    @DisplayName("Login maneja excepción del repositorio correctamente")
    void testLoginExcepcionRepositorio() throws Exception {
        // Arrange
        AuthRequest request = AuthRequest.builder()
                .email(TEST_EMAIL)
                .contrasena(TEST_PASSWORD)
                .build();

        when(usuarioRepository.findByEmail(TEST_EMAIL))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error interno del servidor")));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(TEST_EMAIL);
        verify(jwtService, never()).generarToken(anyString());
    }
}

