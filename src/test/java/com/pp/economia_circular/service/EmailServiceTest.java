package com.pp.economia_circular.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para EmailService - Envío de Emails")
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    private ByteArrayOutputStream outputStreamCaptor;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        // Capturar System.out para verificar los logs
        outputStreamCaptor = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    @DisplayName("Enviar email de verificación")
    void testEnviarEmailVerificacion() {
        // Arrange
        String email = "test@test.com";
        Long userId = 1L;

        // Act
        emailService.sendVerificationEmail(email, userId);

        // Assert
        String output = outputStreamCaptor.toString();
        assertThat(output).contains("Email de verificación enviado a: " + email);
        assertThat(output).contains("para usuario: " + userId);
    }

    @Test
    @DisplayName("Enviar email de recuperación de contraseña")
    void testEnviarEmailRecuperacion() {
        // Arrange
        String email = "test@test.com";
        Long userId = 1L;

        // Act
        emailService.sendPasswordResetEmail(email, userId);

        // Assert
        String output = outputStreamCaptor.toString();
        assertThat(output).contains("Email de recuperación enviado a: " + email);
        assertThat(output).contains("para usuario: " + userId);
    }

    @Test
    @DisplayName("Enviar email de notificación")
    void testEnviarEmailNotificacion() {
        // Arrange
        String email = "test@test.com";
        String subject = "Asunto de prueba";
        String content = "Contenido del email";

        // Act
        emailService.sendNotificationEmail(email, subject, content);

        // Assert
        String output = outputStreamCaptor.toString();
        assertThat(output).contains("Email de notificación enviado a: " + email);
        assertThat(output).contains("Asunto: " + subject);
    }

    @Test
    @DisplayName("Enviar email de verificación con email válido no lanza excepciones")
    void testEnviarEmailVerificacionNoLanzaExcepciones() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail("test@test.com", 1L);
        });
    }

    @Test
    @DisplayName("Enviar email de recuperación con email válido no lanza excepciones")
    void testEnviarEmailRecuperacionNoLanzaExcepciones() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            emailService.sendPasswordResetEmail("test@test.com", 1L);
        });
    }

    @Test
    @DisplayName("Enviar email de notificación con datos válidos no lanza excepciones")
    void testEnviarEmailNotificacionNoLanzaExcepciones() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            emailService.sendNotificationEmail("test@test.com", "Subject", "Content");
        });
    }

    @Test
    @DisplayName("Enviar múltiples emails de verificación")
    void testEnviarMultiplesEmailsVerificacion() {
        // Arrange
        String email1 = "user1@test.com";
        String email2 = "user2@test.com";
        Long userId1 = 1L;
        Long userId2 = 2L;

        // Act
        emailService.sendVerificationEmail(email1, userId1);
        emailService.sendVerificationEmail(email2, userId2);

        // Assert
        String output = outputStreamCaptor.toString();
        assertThat(output).contains(email1);
        assertThat(output).contains(email2);
        assertThat(output).contains(userId1.toString());
        assertThat(output).contains(userId2.toString());
    }

    @Test
    @DisplayName("Enviar email con caracteres especiales en el asunto")
    void testEnviarEmailConCaracteresEspeciales() {
        // Arrange
        String email = "test@test.com";
        String subject = "Asunto con ñ, tildes áéíóú y símbolos !@#$%";
        String content = "Contenido de prueba";

        // Act & Assert
        assertDoesNotThrow(() -> {
            emailService.sendNotificationEmail(email, subject, content);
        });

        String output = outputStreamCaptor.toString();
        assertThat(output).contains(email);
    }

    @Test
    @DisplayName("Enviar email con contenido largo")
    void testEnviarEmailContenidoLargo() {
        // Arrange
        String email = "test@test.com";
        String subject = "Test Subject";
        // Crear contenido de 1000 caracteres (compatible con Java 8)
        StringBuilder sb = new StringBuilder(1000);
        for (int i = 0; i < 1000; i++) {
            sb.append("A");
        }
        String content = sb.toString();

        // Act & Assert
        assertDoesNotThrow(() -> {
            emailService.sendNotificationEmail(email, subject, content);
        });
    }

    @Test
    @DisplayName("Enviar email de verificación con diferentes IDs de usuario")
    void testEnviarEmailConDiferentesUserId() {
        // Act
        emailService.sendVerificationEmail("test@test.com", 1L);
        emailService.sendVerificationEmail("test@test.com", 100L);
        emailService.sendVerificationEmail("test@test.com", 999L);

        // Assert
        String output = outputStreamCaptor.toString();
        assertThat(output).contains("usuario: 1");
        assertThat(output).contains("usuario: 100");
        assertThat(output).contains("usuario: 999");
    }

    @Test
    @DisplayName("Servicio de email está desactivado temporalmente")
    void testServicioEmailDesactivado() {
        // Este test verifica que el servicio está desactivado
        // y solo hace log en lugar de enviar emails reales

        // Act
        emailService.sendVerificationEmail("test@test.com", 1L);

        // Assert - Verifica que solo se hizo log
        String output = outputStreamCaptor.toString();
        assertThat(output).contains("Email de verificación enviado a:");
        // No debería contener errores de conexión SMTP o similares
        assertThat(output).doesNotContain("SMTP");
        assertThat(output).doesNotContain("Connection refused");
    }

    @Test
    @DisplayName("Enviar email de verificación con email vacío")
    void testEnviarEmailVerificacionEmailVacio() {
        // Act & Assert - Debería funcionar sin lanzar excepciones
        // porque el servicio está desactivado y solo hace log
        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail("", 1L);
        });
    }

    @Test
    @DisplayName("Enviar email de verificación con userId null")
    void testEnviarEmailVerificacionUserIdNull() {
        // Act & Assert - Debería funcionar sin lanzar excepciones
        // porque el servicio está desactivado y solo hace log
        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail("test@test.com", null);
        });
    }

    @Test
    @DisplayName("Enviar email de notificación con subject vacío")
    void testEnviarEmailNotificacionSubjectVacio() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            emailService.sendNotificationEmail("test@test.com", "", "Content");
        });
    }

    @Test
    @DisplayName("Enviar email de notificación con content vacío")
    void testEnviarEmailNotificacionContentVacio() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            emailService.sendNotificationEmail("test@test.com", "Subject", "");
        });
    }

    @Test
    @DisplayName("Métodos de email son públicos y accesibles")
    void testMetodosPublicos() throws NoSuchMethodException {
        // Verify que los métodos existen y son públicos
        assertNotNull(EmailService.class.getMethod("sendVerificationEmail", String.class, Long.class));
        assertNotNull(EmailService.class.getMethod("sendPasswordResetEmail", String.class, Long.class));
        assertNotNull(EmailService.class.getMethod("sendNotificationEmail", String.class, String.class, String.class));
    }

    @AfterEach
    void tearDown() {
        // Restaurar System.out original
        System.setOut(originalOut);
    }
}

