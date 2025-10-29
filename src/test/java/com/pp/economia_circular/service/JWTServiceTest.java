package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para JWTService - Generación y Validación de Tokens")
class JWTServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private JWTService jwtService;

    private Usuario usuarioTest;
    private static final String TEST_EMAIL = "test@test.com";
    private static final String SECRET_KEY = "clave-secreta-super-segura-para-economia-circular-2024"; // Debe coincidir con el que usa JWTService

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setId(1L);
        usuarioTest.setNombre("Test");
        usuarioTest.setApellido("User");
        usuarioTest.setEmail(TEST_EMAIL);
        usuarioTest.setContrasena("password");
        usuarioTest.setRol("USER");
        usuarioTest.setActivo(true);
        usuarioTest.setCreadoEn(LocalDateTime.now());
        usuarioTest.setActualizadoEn(LocalDateTime.now());
    }

    @Test
    @DisplayName("Generar token JWT con email válido")
    void testGenerarTokenExitoso() {
        // Act
        String token = jwtService.generarToken(TEST_EMAIL);

        // Assert
        assertNotNull(token, "El token no debería ser null");
        assertFalse(token.isEmpty(), "El token no debería estar vacío");
        assertTrue(token.startsWith("eyJ"), "El token JWT debería empezar con 'eyJ'");
        
        // Verificar que el token tiene 3 partes separadas por puntos (header.payload.signature)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "Un JWT válido debe tener 3 partes");
    }

    @Test
    @DisplayName("Token generado contiene el email correcto en el subject")
    void testTokenContieneEmailCorrecto() {
        // Act
        String token = jwtService.generarToken(TEST_EMAIL);

        // Assert - Extraer el subject del token
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        assertEquals(TEST_EMAIL, claims.getSubject(), "El subject del token debe ser el email");
    }

    @Test
    @DisplayName("Token generado tiene fecha de expiración válida")
    void testTokenTieneExpiracionValida() {
        // Act
        String token = jwtService.generarToken(TEST_EMAIL);

        // Assert
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(claims.getExpiration(), "El token debe tener fecha de expiración");
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis(),
                "La fecha de expiración debe estar en el futuro");
    }

    @Test
    @DisplayName("Token generado tiene fecha de emisión válida")
    void testTokenTieneIssuedAtValido() {
        // Act
        String token = jwtService.generarToken(TEST_EMAIL);

        // Assert
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(claims.getIssuedAt(), "El token debe tener fecha de emisión");
        assertTrue(claims.getIssuedAt().getTime() <= System.currentTimeMillis(),
                "La fecha de emisión debe estar en el pasado o presente");
    }

    @Test
    @DisplayName("Obtener email desde token válido")
    void testObtenerEmailDesdeTokenValido() {
        // Arrange
        String token = jwtService.generarToken(TEST_EMAIL);

        // Act
        String emailExtraido = jwtService.extraerEmail(token);

        // Assert
        assertEquals(TEST_EMAIL, emailExtraido, "El email extraído debe coincidir con el original");
    }

    @Test
    @DisplayName("Validar token válido retorna true")
    void testValidarTokenValido() {
        // Arrange
        String token = jwtService.generarToken(TEST_EMAIL);

        // Act
        boolean esValido = jwtService.validarToken(token);

        // Assert
        assertTrue(esValido, "El token debería ser válido");
    }

    @Test
    @DisplayName("Validar token con formato incorrecto retorna false")
    void testValidarTokenFormatoIncorrecto() {
        // Arrange
        String tokenInvalido = "token.invalido.aqui";

        // Act
        boolean esValido = jwtService.validarToken(tokenInvalido);

        // Assert
        assertFalse(esValido, "El token no debería ser válido");
    }

    @Test
    @DisplayName("Validar token malformado retorna false")
    void testValidarTokenMalformado() {
        // Arrange
        String tokenMalformado = "este.no.es.un.token.valido";

        // Act
        boolean esValido = jwtService.validarToken(tokenMalformado);

        // Assert
        assertFalse(esValido, "El token malformado no debería ser válido");
    }

    @Test
    @DisplayName("Validar token vacío retorna false")
    void testValidarTokenVacio() {
        // Act
        boolean esValido = jwtService.validarToken("");

        // Assert
        assertFalse(esValido, "El token vacío no debería ser válido");
    }

    @Test
    @DisplayName("Validar token null retorna false")
    void testValidarTokenNull() {
        // Act
        boolean esValido = jwtService.validarToken(null);

        // Assert
        assertFalse(esValido, "El token null no debería ser válido");
    }

    @Test
    @DisplayName("Generar múltiples tokens para el mismo email genera tokens diferentes")
    void testGenerarTokensDiferentes() throws InterruptedException {
        // Act
        String token1 = jwtService.generarToken(TEST_EMAIL);
        Thread.sleep(1000); // Esperar 1 segundo para que cambie el timestamp
        String token2 = jwtService.generarToken(TEST_EMAIL);

        // Assert
        assertNotEquals(token1, token2, "Tokens generados en momentos diferentes deben ser distintos");
        
        // Pero ambos deben contener el mismo email
        assertEquals(jwtService.extraerEmail(token1),
                    jwtService.extraerEmail(token2),
                    "Ambos tokens deben contener el mismo email");
    }

    @Test
    @DisplayName("Token generado puede ser parseado sin errores")
    void testTokenPuedeSerParseado() {
        // Act
        String token = jwtService.generarToken(TEST_EMAIL);

        // Assert
        assertDoesNotThrow(() -> {
            Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token);
        }, "Parsear el token no debería lanzar excepciones");
    }

    @Test
    @DisplayName("Generar token con email válido no lanza excepciones")
    void testGenerarTokenNoLanzaExcepciones() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            String token = jwtService.generarToken(TEST_EMAIL);
            assertNotNull(token);
        }, "Generar token no debería lanzar excepciones");
    }

    @Test
    @DisplayName("Token contiene claims necesarios")
    void testTokenContieneClaimsNecesarios() {
        // Act
        String token = jwtService.generarToken(TEST_EMAIL);

        // Assert
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        assertAll("Verificar claims del token",
            () -> assertNotNull(claims.getSubject(), "Subject debe existir"),
            () -> assertNotNull(claims.getIssuedAt(), "IssuedAt debe existir"),
            () -> assertNotNull(claims.getExpiration(), "Expiration debe existir"),
            () -> assertEquals(TEST_EMAIL, claims.getSubject(), "Subject debe ser el email")
        );
    }

    @Test
    @DisplayName("Generar token con diferentes emails genera tokens únicos")
    void testGenerarTokensUnicosParaDiferentesEmails() {
        // Arrange
        String email1 = "user1@test.com";
        String email2 = "user2@test.com";

        // Act
        String token1 = jwtService.generarToken(email1);
        String token2 = jwtService.generarToken(email2);

        // Assert
        assertNotEquals(token1, token2, "Tokens para diferentes emails deben ser distintos");
        assertEquals(email1, jwtService.extraerEmail(token1));
        assertEquals(email2, jwtService.extraerEmail(token2));
    }

    @Test
    @DisplayName("Token generado es una cadena no vacía")
    void testTokenNoVacio() {
        // Act
        String token = jwtService.generarToken(TEST_EMAIL);

        // Assert
        assertThat(token)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(20);
    }

    @Test
    @DisplayName("Verificar estructura del token JWT (header.payload.signature)")
    void testEstructuraTokenJWT() {
        // Act
        String token = jwtService.generarToken(TEST_EMAIL);

        // Assert
        String[] parts = token.split("\\.");
        assertThat(parts)
                .hasSize(3)
                .allMatch(part -> !part.isEmpty(), "Todas las partes del token deben tener contenido");
    }
}

