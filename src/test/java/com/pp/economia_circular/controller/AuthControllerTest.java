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
import com.pp.economia_circular.service.GoogleAuthService;
import com.pp.economia_circular.service.JWTService;
import com.pp.economia_circular.service.PasswordRecoveryService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

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
    private PasswordRecoveryService passwordRecoveryService;

    @MockBean
    private GoogleAuthService googleAuthService;

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
        usuarioActivo.setAuthProvider("local");

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
        usuarioInactivo.setAuthProvider("local");
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
    @DisplayName("Reset password con email inexistente retorna error")
    void testResetPasswordEmailInexistente() throws Exception {
        // Arrange - Cambiar contraseña requiere oldPassword; si usuario no existe, 400
        String emailInexistente = "noexiste@test.com";
        String requestBody = String.format("{\"email\":\"%s\",\"newPassword\":\"NewPass123!\",\"oldPassword\":\"old\"}", emailInexistente);

        when(usuarioRepository.findByEmail(emailInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuario no encontrado"));

        verify(usuarioRepository, times(1)).findByEmail(emailInexistente);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reset password con usuario inactivo debe fallar")
    void testResetPasswordUsuarioInactivo() throws Exception {
        // Arrange
        String requestBody = String.format("{\"email\":\"%s\",\"newPassword\":\"NewPass123!\",\"oldPassword\":\"%s\"}", usuarioInactivo.getEmail(), TEST_PASSWORD);

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
    @DisplayName("Reset password sin contraseña actual debe fallar")
    void testResetPasswordSinOldPassword() throws Exception {
        // Arrange
        String requestBody = String.format("{\"email\":\"%s\",\"newPassword\":\"NewPass123!\"}", TEST_EMAIL);

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La contraseña actual es requerida para cambiar la contraseña."));

        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reset password sin nueva contraseña debe fallar")
    void testResetPasswordSinNuevaContrasena() throws Exception {
        // Arrange - solo email y oldPassword; newPassword falta, el controlador valida antes de buscar usuario
        String requestBody = String.format("{\"email\":\"%s\",\"oldPassword\":\"%s\"}", TEST_EMAIL, TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nueva contraseña es requerida"));

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

    // ==================== TESTS PARA FORGOT PASSWORD ====================

    @Test
    @DisplayName("Forgot password con email válido devuelve mensaje genérico")
    void testForgotPasswordExitoso() throws Exception {
        String requestBody = String.format("{\"email\":\"%s\"}", TEST_EMAIL);
        doNothing().when(passwordRecoveryService).solicitarRecuperacion(TEST_EMAIL);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Si el correo está registrado")));

        verify(passwordRecoveryService, times(1)).solicitarRecuperacion(TEST_EMAIL);
    }

    @Test
    @DisplayName("Forgot password sin email debe fallar")
    void testForgotPasswordSinEmail() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email es requerido"));

        verify(passwordRecoveryService, never()).solicitarRecuperacion(anyString());
    }

    @Test
    @DisplayName("Forgot password con email vacío debe fallar")
    void testForgotPasswordEmailVacio() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email es requerido"));
    }

    // ==================== TESTS PARA RESET PASSWORD WITH TOKEN ====================

    @Test
    @DisplayName("Reset password con token exitoso")
    void testResetPasswordWithTokenExitoso() throws Exception {
        String token = "abc123token";
        String newPassword = "NuevaPass123!";
        String requestBody = String.format("{\"token\":\"%s\",\"newPassword\":\"%s\"}", token, newPassword);

        when(passwordRecoveryService.restablecerConToken(token, newPassword)).thenReturn(true);

        mockMvc.perform(post("/api/auth/reset-password-with-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contraseña actualizada")));

        verify(passwordRecoveryService, times(1)).restablecerConToken(token, newPassword);
    }

    @Test
    @DisplayName("Reset password con token inválido o expirado")
    void testResetPasswordWithTokenInvalido() throws Exception {
        String requestBody = "{\"token\":\"tokenExpirado\",\"newPassword\":\"NuevaPass123!\"}";

        when(passwordRecoveryService.restablecerConToken("tokenExpirado", "NuevaPass123!")).thenReturn(false);

        mockMvc.perform(post("/api/auth/reset-password-with-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Token inválido o expirado")));
    }

    @Test
    @DisplayName("Reset password with token sin token debe fallar")
    void testResetPasswordWithTokenSinToken() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password-with-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"NuevaPass123!\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Token es requerido"));

        verify(passwordRecoveryService, never()).restablecerConToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Reset password with token con contraseña corta debe fallar")
    void testResetPasswordWithTokenContrasenaCorta() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password-with-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc\",\"newPassword\":\"12345\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("al menos 6 caracteres")));
    }

    // ==================== TESTS PARA LOGIN CON GOOGLE ====================

    @Test
    @DisplayName("Login con Google sin configurar retorna 503")
    void testLoginGoogleNoConfigurado() throws Exception {
        when(googleAuthService.isConfigured()).thenReturn(false);

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"fake-google-token\"}"))
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("no está configurado")));

        verify(googleAuthService, times(1)).isConfigured();
        verify(googleAuthService, never()).verifyToken(anyString());
    }

    @Test
    @DisplayName("Login con Google sin idToken debe fallar")
    void testLoginGoogleSinIdToken() throws Exception {
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("idToken es requerido"));

        verify(googleAuthService, never()).verifyToken(anyString());
    }

    @Test
    @DisplayName("Login con Google exitoso retorna JWT")
    void testLoginGoogleExitoso() throws Exception {
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        doReturn("google-id-123").when(payload).getSubject();
        doReturn("googleuser@test.com").when(payload).getEmail();
        // name/given_name/family_name solo se usan al crear usuario nuevo; este test tiene usuario existente

        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNombre("Google");
        usuario.setApellido("User");
        usuario.setEmail("googleuser@test.com");
        usuario.setGoogleId("google-id-123");
        usuario.setAuthProvider("google");
        usuario.setRol("USER");
        usuario.setActivo(true);
        usuario.setCreadoEn(LocalDateTime.now());
        usuario.setActualizadoEn(LocalDateTime.now());

        when(googleAuthService.isConfigured()).thenReturn(true);
        when(googleAuthService.verifyToken(anyString())).thenReturn(payload);
        when(usuarioRepository.findByGoogleId("google-id-123")).thenReturn(Optional.of(usuario));
        when(jwtService.generarToken("googleuser@test.com", "USER")).thenReturn(TEST_TOKEN);

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"valid-google-token\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(TEST_TOKEN)));

        verify(googleAuthService).verifyToken("valid-google-token");
        verify(jwtService).generarToken("googleuser@test.com", "USER");
    }

    @Test
    @DisplayName("Login con Google token inválido retorna 401")
    void testLoginGoogleTokenInvalido() throws Exception {
        when(googleAuthService.isConfigured()).thenReturn(true);
        when(googleAuthService.verifyToken("invalid-token")).thenReturn(null);

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"invalid-token\"}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Token de Google inválido")));

        verify(usuarioRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Login con usuario solo Google (sin contraseña) debe indicar usar Google")
    void testLoginUsuarioSoloGoogle() throws Exception {
        Usuario usuarioGoogle = new Usuario();
        usuarioGoogle.setId(10L);
        usuarioGoogle.setNombre("Google");
        usuarioGoogle.setApellido("User");
        usuarioGoogle.setEmail("google@test.com");
        usuarioGoogle.setContrasena(null);
        usuarioGoogle.setGoogleId("google-sub-123");
        usuarioGoogle.setAuthProvider("google");
        usuarioGoogle.setRol("USER");
        usuarioGoogle.setActivo(true);
        usuarioGoogle.setCreadoEn(LocalDateTime.now());
        usuarioGoogle.setActualizadoEn(LocalDateTime.now());

        AuthRequest request = AuthRequest.builder()
                .email("google@test.com")
                .contrasena("cualquier")
                .build();

        when(usuarioRepository.findByEmail("google@test.com")).thenReturn(Optional.of(usuarioGoogle));

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Iniciar con Google")));

        verify(jwtService, never()).generarToken(anyString(), anyString());
    }

    // ==================== TESTS PARA LINK GOOGLE ====================

    @Test
    @DisplayName("Link Google sin autenticación retorna 401")
    void testLinkGoogleSinAuth() throws Exception {
        mockMvc.perform(post("/api/auth/link-google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"token\"}"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        verify(googleAuthService, never()).verifyToken(anyString());
    }

    @Test
    @DisplayName("Link Google exitoso con usuario autenticado")
    @WithMockUser(username = "link@test.com", roles = "USER")
    void testLinkGoogleExitoso() throws Exception {
        Usuario usuarioActual = new Usuario();
        usuarioActual.setId(7L);
        usuarioActual.setEmail("link@test.com");
        usuarioActual.setNombre("Link");
        usuarioActual.setApellido("User");
        usuarioActual.setAuthProvider("local");
        usuarioActual.setCreadoEn(LocalDateTime.now());
        usuarioActual.setActualizadoEn(LocalDateTime.now());

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        doReturn("google-sub-link").when(payload).getSubject();
        doReturn("link@test.com").when(payload).getEmail();

        when(jwtService.getCurrentUser()).thenReturn(usuarioActual);
        when(googleAuthService.isConfigured()).thenReturn(true);
        when(googleAuthService.verifyToken("link-token")).thenReturn(payload);
        when(usuarioRepository.findByGoogleId("google-sub-link")).thenReturn(Optional.empty());
        when(usuarioRepository.save(ArgumentMatchers.any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/auth/link-google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"link-token\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("vinculada con Google")));

        verify(usuarioRepository, times(1)).save(ArgumentMatchers.any(Usuario.class));
    }
}

