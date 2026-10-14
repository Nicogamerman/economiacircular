package com.pp.economia_circular.service.ia;

import com.anthropic.models.messages.Base64ImageSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas que no llaman a la API: configuración y conversión de tipos. */
class ClaudeIaClientTest {

    @Test
    void sinApiKeyNoEstaDisponibleYNoIntentaLlamar() {
        ClaudeIaClient client = new ClaudeIaClient("", "claude-opus-5-5");

        assertFalse(client.disponible());
        IaException ex = assertThrows(IaException.class,
                () -> client.responderChat("system", Collections.emptyList()));
        assertTrue(ex.isNoDisponible());
    }

    @Test
    void conApiKeyEstaDisponible() {
        assertTrue(new ClaudeIaClient("sk-ant-test", "claude-opus-5-5").disponible());
    }

    @Test
    void convierteLosTiposDeImagenSoportados() {
        assertEquals(Base64ImageSource.MediaType.IMAGE_JPEG, ClaudeIaClient.mediaTypeImagen("image/jpeg"));
        assertEquals(Base64ImageSource.MediaType.IMAGE_PNG, ClaudeIaClient.mediaTypeImagen("image/png"));
        assertEquals(Base64ImageSource.MediaType.IMAGE_WEBP, ClaudeIaClient.mediaTypeImagen("image/webp"));
        assertEquals(Base64ImageSource.MediaType.IMAGE_GIF, ClaudeIaClient.mediaTypeImagen("image/gif"));
        assertThrows(IaException.class, () -> ClaudeIaClient.mediaTypeImagen("image/bmp"));
    }
}
