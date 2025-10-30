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
        when(jwtService.generarToken(TEST_EMAIL, usuarioActivo.getRol())).thenReturn(TEST_TOKEN);

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
        verify(jwtService, times(1)).generarToken(TEST_EMAIL, usuarioActivo.getRol());
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
        when(jwtService.generarToken("legacy@test.com", usuarioLegacy.getRol())).thenReturn(TEST_TOKEN);

        // Act & Assert
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(TEST_TOKEN)));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail("legacy@test.com");
        verify(jwtService, times(1)).generarToken("legacy@test.com", usuarioLegacy.getRol());
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
        when(jwtService.generarToken(TEST_EMAIL, usuarioActivo.getRol())).thenReturn(TEST_TOKEN);

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
        when(jwtService.generarToken(emailMayusculas, usuarioMayusculas.getRol())).thenReturn(TEST_TOKEN);

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
        when(jwtService.generarToken(TEST_EMAIL, usuarioActivo.getRol())).thenReturn(TEST_TOKEN);

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

    // ==================== TESTS PARA RESET PASSWORD ====================

    @Test
    @DisplayName("Reset password exitoso")
    void testResetPasswordExitoso() throws Exception {
        // Arrange
        String newPassword = "NewPassword123!";
        String oldPassword = "passwordVieja";
        String requestBody = String.format("{\"email\":\"%s\",\"newPassword\":\"%s\",\"oldPassword\":\"%s\"}", TEST_EMAIL, newPassword, oldPassword);

        // Usuario activo con contraseña encriptada
        usuarioActivo = new Usuario();
        usuarioActivo.setId(1L);
        usuarioActivo.setNombre("Test");
        usuarioActivo.setApellido("User");
        usuarioActivo.setEmail(TEST_EMAIL);
        usuarioActivo.setContrasena(passwordEncoder.encode(oldPassword));
        usuarioActivo.setRol("USER");
        usuarioActivo.setActivo(true);
        usuarioActivo.setCreadoEn(LocalDateTime.now());
        usuarioActivo.setActualizadoEn(LocalDateTime.now());

        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(usuarioActivo));
        when(usuarioRepository.save(any())).thenReturn(usuarioActivo);

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Contraseña actualizada exitosamente"));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(TEST_EMAIL);
        verify(usuarioRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Reset password con email inexistente retorna mensaje genérico por seguridad")
    void testResetPasswordEmailInexistente() throws Exception {
        // Arrange
        String emailInexistente = "noexiste@test.com";
        String requestBody = String.format("{\"email\":\"%s\",\"newPassword\":\"NewPass123!\"}", emailInexistente);

        when(usuarioRepository.findByEmail(emailInexistente)).thenReturn(Optional.empty());

        // Act & Assert - Por seguridad, no revelar si el email existe
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Si el email existe, la contraseña ha sido actualizada"));

        // Verify - No debe guardar nada
        verify(usuarioRepository, times(1)).findByEmail(emailInexistente);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reset password con usuario inactivo debe fallar")
    void testResetPasswordUsuarioInactivo() throws Exception {
        // Arrange
        String requestBody = String.format("{\"email\":\"%s\",\"newPassword\":\"NewPass123!\"}", usuarioInactivo.getEmail());

        when(usuarioRepository.findByEmail(usuarioInactivo.getEmail())).thenReturn(Optional.of(usuarioInactivo));

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Usuario inactivo"));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(usuarioInactivo.getEmail());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reset password sin email debe fallar")
    void testResetPasswordSinEmail() throws Exception {
        // Arrange
        String requestBody = "{\"newPassword\":\"NewPass123!\"}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email es requerido"));

        // Verify
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reset password sin nueva contraseña debe fallar")
    void testResetPasswordSinNuevaContrasena() throws Exception {
        // Arrange
        String requestBody = String.format("{\"email\":\"%s\"}", TEST_EMAIL);

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nueva contraseña es requerida"));

        // Verify
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reset password con contraseña muy corta debe fallar")
    void testResetPasswordContrasenaMuyCorta() throws Exception {
        // Arrange
        String requestBody = String.format("{\"email\":\"%s\",\"newPassword\":\"12345\"}", TEST_EMAIL);

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La contraseña debe tener al menos 6 caracteres"));

        // Verify
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reset password con email vacío debe fallar")
    void testResetPasswordEmailVacio() throws Exception {
        // Arrange
        String requestBody = "{\"email\":\"\",\"newPassword\":\"NewPass123!\"}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email es requerido"));

        // Verify
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reset password encripta correctamente la nueva contraseña")
    void testResetPasswordEncriptaCorrectamente() throws Exception {
        // Arrange
        String newPassword = "NewPassword123!";
        String oldPassword = "passwordVieja";
        String requestBody = String.format("{\"email\":\"%s\",\"newPassword\":\"%s\",\"oldPassword\":\"%s\"}", TEST_EMAIL, newPassword, oldPassword);

        // Usuario activo con contraseña encriptada
        usuarioActivo = new Usuario();
        usuarioActivo.setId(1L);
        usuarioActivo.setNombre("Test");
        usuarioActivo.setApellido("User");
        usuarioActivo.setEmail(TEST_EMAIL);
        usuarioActivo.setContrasena(passwordEncoder.encode(oldPassword));
        usuarioActivo.setRol("USER");
        usuarioActivo.setActivo(true);
        usuarioActivo.setCreadoEn(LocalDateTime.now());
        usuarioActivo.setActualizadoEn(LocalDateTime.now());
        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(usuarioActivo));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            // Verificar que la contraseña fue encriptada (empieza con $2a$)
            assert usuario.getContrasena().startsWith("$2a$");
            // Verificar que NO es la contraseña en texto plano
            assert !usuario.getContrasena().equals(newPassword);
            return usuario;
        });

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify
        verify(usuarioRepository, times(1)).save(any());
    }

    // ==================== TESTS PARA CHECK EMAIL ====================

    @Test
    @DisplayName("Check email con usuario existente y activo")
    void testCheckEmailExistenteActivo() throws Exception {
        // Arrange
        String requestBody = String.format("{\"email\":\"%s\"}", TEST_EMAIL);

        when(usuarioRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(usuarioActivo));

        // Act & Assert
        mockMvc.perform(post("/api/auth/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.active").value(true));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Check email con usuario existente pero inactivo")
    void testCheckEmailExistenteInactivo() throws Exception {
        // Arrange
        String requestBody = String.format("{\"email\":\"%s\"}", usuarioInactivo.getEmail());

        when(usuarioRepository.findByEmail(usuarioInactivo.getEmail())).thenReturn(Optional.of(usuarioInactivo));

        // Act & Assert
        mockMvc.perform(post("/api/auth/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true))
                .andExpect(jsonPath("$.active").value(false));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(usuarioInactivo.getEmail());
    }

    @Test
    @DisplayName("Check email con email inexistente")
    void testCheckEmailInexistente() throws Exception {
        // Arrange
        String emailInexistente = "noexiste@test.com";
        String requestBody = String.format("{\"email\":\"%s\"}", emailInexistente);

        when(usuarioRepository.findByEmail(emailInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/auth/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false))
                .andExpect(jsonPath("$.active").value(false));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(emailInexistente);
    }

    @Test
    @DisplayName("Check email sin email debe fallar")
    void testCheckEmailSinEmail() throws Exception {
        // Arrange
        String requestBody = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email es requerido"));

        // Verify
        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Check email con email vacío debe fallar")
    void testCheckEmailVacio() throws Exception {
        // Arrange
        String requestBody = "{\"email\":\"\"}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email es requerido"));

        // Verify
        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Check email con formato JSON inválido")
    void testCheckEmailJsonInvalido() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/check-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // Verify
        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    // ==================== TESTS PARA DECODE TOKEN ====================

    @Test
    @DisplayName("Decodificar token válido retorna información correcta")
    void testDecodeTokenValido() throws Exception {
        // Arrange
        String token = "valid.test.token";
        String requestBody = String.format("{\"token\":\"%s\"}", token);

        when(jwtService.validarToken(token)).thenReturn(true);
        when(jwtService.extraerEmail(token)).thenReturn(TEST_EMAIL);
        when(jwtService.extraerRol(token)).thenReturn("ADMIN");
        when(jwtService.extraerClaims(token)).thenReturn(mock(io.jsonwebtoken.Claims.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/decode-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @DisplayName("Decodificar token con rol USER retorna rol correcto")
    void testDecodeTokenConRolUser() throws Exception {
        // Arrange
        String token = "valid.test.token";
        String requestBody = String.format("{\"token\":\"%s\"}", token);

        when(jwtService.validarToken(token)).thenReturn(true);
        when(jwtService.extraerEmail(token)).thenReturn(TEST_EMAIL);
        when(jwtService.extraerRol(token)).thenReturn("USER");

        io.jsonwebtoken.Claims claimsMock = mock(io.jsonwebtoken.Claims.class);
        when(jwtService.extraerClaims(token)).thenReturn(claimsMock);

        // Act & Assert
        mockMvc.perform(post("/api/auth/decode-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @DisplayName("Decodificar token inválido retorna error")
    void testDecodeTokenInvalido() throws Exception {
        // Arrange
        String tokenInvalido = "token.invalido.aqui";
        String requestBody = String.format("{\"token\":\"%s\"}", tokenInvalido);

        when(jwtService.validarToken(tokenInvalido)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/decode-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Token inválido o expirado"));
    }

    @Test
    @DisplayName("Decodificar token sin token debe fallar")
    void testDecodeTokenSinToken() throws Exception {
        // Arrange
        String requestBody = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/decode-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Token es requerido"));
    }

    @Test
    @DisplayName("Decodificar token con token vacío debe fallar")
    void testDecodeTokenVacio() throws Exception {
        // Arrange
        String requestBody = "{\"token\":\"\"}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/decode-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Token es requerido"));
    }

    @Test
    @DisplayName("Decodificar token contiene todas las claims")
    void testDecodeTokenContieneClaims() throws Exception {
        // Arrange
        String token = "valid.test.token";
        String requestBody = String.format("{\"token\":\"%s\"}", token);

        when(jwtService.validarToken(token)).thenReturn(true);
        when(jwtService.extraerEmail(token)).thenReturn(TEST_EMAIL);
        when(jwtService.extraerRol(token)).thenReturn("ADMIN");
        when(jwtService.extraerClaims(token)).thenReturn(mock(io.jsonwebtoken.Claims.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/decode-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.rol").exists())
                .andExpect(jsonPath("$.claims").exists())
                .andExpect(jsonPath("$.valid").exists());
    }

    @Test
    @DisplayName("Decodificar token retorna claims completas")
    void testDecodeTokenRetornaClaimsCompletas() throws Exception {
        // Arrange
        String token = "valid.test.token";
        String requestBody = String.format("{\"token\":\"%s\"}", token);

        io.jsonwebtoken.Claims claimsMock = mock(io.jsonwebtoken.Claims.class);
        when(jwtService.validarToken(token)).thenReturn(true);
        when(jwtService.extraerEmail(token)).thenReturn(TEST_EMAIL);
        when(jwtService.extraerRol(token)).thenReturn("ADMIN");
        when(jwtService.extraerClaims(token)).thenReturn(claimsMock);

        // Act & Assert
        mockMvc.perform(post("/api/auth/decode-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claims").exists());
    }
}

