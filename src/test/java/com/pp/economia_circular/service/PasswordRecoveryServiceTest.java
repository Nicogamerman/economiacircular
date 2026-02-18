package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.PasswordResetToken;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.PasswordResetTokenRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para PasswordRecoveryService")
class PasswordRecoveryServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordRecoveryService passwordRecoveryService;

    private Usuario usuarioActivo;
    private static final String EMAIL = "test@test.com";
    private static final String HASHED = "$2a$10$hashed";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordRecoveryService, "baseUrl", "http://localhost:8080");
        usuarioActivo = new Usuario();
        usuarioActivo.setId(1L);
        usuarioActivo.setEmail(EMAIL);
        usuarioActivo.setNombre("Test");
        usuarioActivo.setApellido("User");
        usuarioActivo.setContrasena("encoded");
        usuarioActivo.setAuthProvider("local");
        usuarioActivo.setActivo(true);
        usuarioActivo.setCreadoEn(LocalDateTime.now());
        usuarioActivo.setActualizadoEn(LocalDateTime.now());
    }

    @Test
    @DisplayName("solicitarRecuperacion con email existente crea token y envía email")
    void solicitarRecuperacionExitoso() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioActivo));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> {
            PasswordResetToken t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });
        doNothing().when(tokenRepository).deleteByUsuarioId(1L);

        passwordRecoveryService.solicitarRecuperacion(EMAIL);

        verify(tokenRepository).deleteByUsuarioId(1L);
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken saved = tokenCaptor.getValue();
        assertThat(saved.getUsuario()).isEqualTo(usuarioActivo);
        assertThat(saved.getToken()).isNotEmpty();
        assertThat(saved.isUsado()).isFalse();
        assertThat(saved.getExpiraEn()).isAfter(LocalDateTime.now());

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetWithToken(eq(EMAIL), linkCaptor.capture(), eq(60));
        assertThat(linkCaptor.getValue()).contains("token=");
        assertThat(linkCaptor.getValue()).startsWith("http://localhost:8080");
    }

    @Test
    @DisplayName("solicitarRecuperacion con email inexistente no hace nada")
    void solicitarRecuperacionEmailInexistente() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        passwordRecoveryService.solicitarRecuperacion(EMAIL);

        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailService, never()).sendPasswordResetWithToken(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("solicitarRecuperacion con usuario inactivo no hace nada")
    void solicitarRecuperacionUsuarioInactivo() {
        usuarioActivo.setActivo(false);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioActivo));

        passwordRecoveryService.solicitarRecuperacion(EMAIL);

        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailService, never()).sendPasswordResetWithToken(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("solicitarRecuperacion con usuario solo Google (sin contraseña) no hace nada")
    void solicitarRecuperacionUsuarioSoloGoogle() {
        usuarioActivo.setAuthProvider("google");
        usuarioActivo.setContrasena(null);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioActivo));

        passwordRecoveryService.solicitarRecuperacion(EMAIL);

        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailService, never()).sendPasswordResetWithToken(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("restablecerConToken con token válido actualiza contraseña y marca token usado")
    void restablecerConTokenExitoso() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .token("valid-token")
                .usuario(usuarioActivo)
                .expiraEn(LocalDateTime.now().plusHours(1))
                .usado(false)
                .creadoEn(LocalDateTime.now())
                .build();
        when(tokenRepository.findByTokenAndUsadoFalseAndExpiraEnAfter(eq("valid-token"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("nueva123")).thenReturn(HASHED);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = passwordRecoveryService.restablecerConToken("valid-token", "nueva123");

        assertThat(result).isTrue();
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        ArgumentCaptor<Usuario> userCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getContrasena()).isEqualTo(HASHED);
        assertThat(userCaptor.getValue().getAuthProvider()).isEqualTo("local");
    }

    @Test
    @DisplayName("restablecerConToken con token inexistente retorna false")
    void restablecerConTokenInvalido() {
        when(tokenRepository.findByTokenAndUsadoFalseAndExpiraEnAfter(eq("invalid"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        boolean result = passwordRecoveryService.restablecerConToken("invalid", "nueva123");

        assertThat(result).isFalse();
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("restablecerConToken con token expirado retorna false")
    void restablecerConTokenExpirado() {
        when(tokenRepository.findByTokenAndUsadoFalseAndExpiraEnAfter(eq("expired"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        boolean result = passwordRecoveryService.restablecerConToken("expired", "nueva123");

        assertThat(result).isFalse();
    }
}
