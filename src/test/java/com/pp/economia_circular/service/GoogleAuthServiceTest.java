package com.pp.economia_circular.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para GoogleAuthService")
class GoogleAuthServiceTest {

    @InjectMocks
    private GoogleAuthService googleAuthService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleAuthService, "clientId", "test-client-id.apps.googleusercontent.com");
        googleAuthService.init();
    }

    @Test
    @DisplayName("isConfigured retorna true cuando clientId está configurado")
    void isConfiguredCuandoClientIdPresente() {
        assertThat(googleAuthService.isConfigured()).isTrue();
    }

    @Test
    @DisplayName("isConfigured retorna false cuando clientId está vacío")
    void isConfiguredCuandoClientIdVacio() {
        ReflectionTestUtils.setField(googleAuthService, "clientId", "");
        googleAuthService.init();
        assertThat(googleAuthService.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("verifyToken con token null retorna null")
    void verifyTokenNull() {
        GoogleIdToken.Payload result = googleAuthService.verifyToken(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("verifyToken con token vacío retorna null")
    void verifyTokenVacio() {
        GoogleIdToken.Payload result = googleAuthService.verifyToken("   ");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("verifyToken con token inválido (no firmado por Google) retorna null")
    void verifyTokenInvalido() {
        // Un token que no es un JWT válido de Google
        GoogleIdToken.Payload result = googleAuthService.verifyToken("token-invalido-cualquiera");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("verifyToken con token malformado o inválido retorna null sin lanzar")
    void verifyTokenMalformadoRetornaNull() {
        // Tokens que no son JWT válidos de Google deben devolver null (no lanzar)
        assertThat(googleAuthService.verifyToken("a.b.c")).isNull();
    }
}
