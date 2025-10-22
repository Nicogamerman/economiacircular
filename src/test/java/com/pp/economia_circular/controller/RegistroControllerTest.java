package com.pp.economia_circular.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pp.economia_circular.DTO.UsuarioRequest;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MensajeRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RegistroController.class)
@org.springframework.context.annotation.Import(com.pp.economia_circular.config.TestSecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
class RegistroControllerTest {

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


    private UsuarioRequest usuarioRequest;
    private Usuario usuarioGuardado;
    private static final String REGISTRO_ENDPOINT = "/api/registrar/registrar";
    private static final String FOTO_ENDPOINT = "/api/registrar/{id}/foto";

    @BeforeEach
    void setUp() {
        // Request válido
        usuarioRequest = new UsuarioRequest();
        usuarioRequest.setNombre("Test");
        usuarioRequest.setApellido("User");
        usuarioRequest.setEmail("test@test.com");
        usuarioRequest.setContrasena("Test123!");
        usuarioRequest.setRol("USER");
        usuarioRequest.setDomicilio("Calle Test 123");
        usuarioRequest.setFotoBase64(null);

        // Usuario guardado simulado
        usuarioGuardado = new Usuario();
        usuarioGuardado.setId(1L);
        usuarioGuardado.setNombre("Test");
        usuarioGuardado.setApellido("User");
        usuarioGuardado.setEmail("test@test.com");
        usuarioGuardado.setContrasena(new BCryptPasswordEncoder().encode("Test123!"));
        usuarioGuardado.setRol("USER");
        usuarioGuardado.setDomicilio("Calle Test 123");
        usuarioGuardado.setActivo(true);
        usuarioGuardado.setCreadoEn(LocalDateTime.now());
        usuarioGuardado.setActualizadoEn(LocalDateTime.now());
    }

    @Test
    @DisplayName("Registro exitoso de usuario nuevo")
    void testRegistroExitoso() throws Exception {
        // Arrange
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(usuarioRequest.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro fallido con email ya existente")
    void testRegistroEmailExistente() throws Exception {
        // Arrange
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail())).thenReturn(Optional.of(usuarioGuardado));

        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ya existe un usuario registrado"));

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(usuarioRequest.getEmail());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro con foto en base64")
    void testRegistroConFoto() throws Exception {
        // Arrange
        String fotoBase64 = Base64.getEncoder().encodeToString("imagen de prueba".getBytes());
        usuarioRequest.setFotoBase64(fotoBase64);

        when(usuarioRepository.findByEmail(usuarioRequest.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify
        verify(usuarioRepository, times(1)).findByEmail(usuarioRequest.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro con todos los campos obligatorios")
    void testRegistroCamposCompletos() throws Exception {
        // Arrange
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro encripta la contraseña")
    void testRegistroEncriptaContrasena() throws Exception {
        // Arrange
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            // Verificar que la contraseña está encriptada
            assert !usuario.getContrasena().equals("Test123!");
            assert usuario.getContrasena().startsWith("$2a$");
            return usuarioGuardado;
        });

        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Obtener foto de usuario existente")
    void testObtenerFotoExitoso() throws Exception {
        // Arrange
        byte[] fotoBytes = "imagen de prueba".getBytes();
        usuarioGuardado.setFoto(fotoBytes);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioGuardado));

        // Act & Assert
        mockMvc.perform(get(FOTO_ENDPOINT, 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("foto_1.jpg")))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(fotoBytes));

        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener foto de usuario sin foto retorna 404")
    void testObtenerFotoSinFoto() throws Exception {
        // Arrange
        usuarioGuardado.setFoto(null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioGuardado));

        // Act & Assert
        mockMvc.perform(get(FOTO_ENDPOINT, 1L))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener foto de usuario inexistente lanza excepción")
    void testObtenerFotoUsuarioInexistente() throws Exception {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get(FOTO_ENDPOINT, 999L))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(usuarioRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Registro sin nombre debe fallar")
    void testRegistroSinNombre() throws Exception {
        // Arrange
        usuarioRequest.setNombre(null);

        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nombre es requerido"));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro sin email debe fallar")
    void testRegistroSinEmail() throws Exception {
        // Arrange
        usuarioRequest.setEmail(null);

        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email es requerido"));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro sin contraseña debe fallar")
    void testRegistroSinContrasena() throws Exception {
        // Arrange
        usuarioRequest.setContrasena(null);

        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Contraseña es requerida"));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro establece usuario como activo por defecto")
    void testRegistroUsuarioActivoPorDefecto() throws Exception {
        // Arrange
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            assert usuario.isActivo();
            return usuarioGuardado;
        });

        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro con formato JSON inválido")
    void testRegistroJsonInvalido() throws Exception {
        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro permite CORS desde cualquier origen")
    void testRegistroPermiteCORS() throws Exception {
        // Arrange
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Origin", "http://localhost:3000")
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Registro establece fechas de creación y actualización")
    void testRegistroFechas() throws Exception {
        // Arrange
        when(usuarioRepository.findByEmail(usuarioRequest.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            assert usuario.getCreadoEn() != null;
            assert usuario.getActualizadoEn() != null;
            return usuarioGuardado;
        });

        // Act & Assert
        mockMvc.perform(post(REGISTRO_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }
}

