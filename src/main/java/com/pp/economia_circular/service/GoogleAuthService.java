package com.pp.economia_circular.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Verifica el ID token de Google (obtenido en el frontend con Google Sign-In)
 * y devuelve el payload con sub (googleId), email, name, etc.
 */
@Service
public class GoogleAuthService {

    @Value("${app.google.client-id:}")
    private String clientId;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    public void init() {
        if (clientId != null && !clientId.trim().isEmpty()) {
            verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId.trim()))
                    .build();
        } else {
            verifier = null;
        }
    }

    /**
     * Verifica el ID token y devuelve el payload (sub, email, name, picture) o null si no es válido.
     */
    public GoogleIdToken.Payload verifyToken(String idToken) {
        if (verifier == null || idToken == null || idToken.trim().isEmpty()) {
            return null;
        }
        try {
            GoogleIdToken token = verifier.verify(idToken.trim());
            return token != null ? token.getPayload() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isConfigured() {
        return verifier != null;
    }
}
