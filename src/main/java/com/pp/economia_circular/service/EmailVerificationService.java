package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.EmailVerificationToken;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.EmailVerificationTokenRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class EmailVerificationService {

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.email-verification.token-ttl-hours:48}")
    private long ttlHoras;

    @Value("${app.email-verification.verify-url-base:http://localhost:8080/api/auth/verify-email}")
    private String verifyUrlBase;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    public void generarYEnviar(Usuario usuario) {
        if (usuario == null || usuario.isEmailVerificado()) {
            return;
        }

        tokenRepository.invalidarTokensVigentesDeUsuario(usuario.getId(), LocalDateTime.now());

        String rawToken = generarTokenAleatorio();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUsuario(usuario);
        token.setTokenHash(hash(rawToken));
        token.setExpiraEn(LocalDateTime.now().plusHours(ttlHoras));
        tokenRepository.save(token);

        String verifyUrl = verifyUrlBase + "?token=" + rawToken;
        emailService.enviarEmailVerificacion(usuario.getEmail(), usuario.getNombre(), verifyUrl, ttlHoras);
    }

    public void reenviar(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (!usuarioOpt.isPresent()) {
            return;
        }
        Usuario usuario = usuarioOpt.get();
        if (usuario.isEmailVerificado()) {
            return;
        }
        generarYEnviar(usuario);
    }

    public Usuario verificar(String rawToken) {
        if (rawToken == null || rawToken.isEmpty()) {
            throw new RuntimeException("Token inválido");
        }

        EmailVerificationToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (!token.estaVigente()) {
            throw new RuntimeException("Token expirado o ya utilizado");
        }

        Usuario usuario = token.getUsuario();
        usuario.setEmailVerificado(true);
        usuario.setEmailVerificadoEn(LocalDateTime.now());
        usuario.setActualizadoEn(LocalDateTime.now());
        usuarioRepository.save(usuario);

        token.setUsadoEn(LocalDateTime.now());
        tokenRepository.save(token);

        return usuario;
    }

    private String generarTokenAleatorio() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] out = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
