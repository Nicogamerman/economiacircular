package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.PasswordResetToken;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.PasswordResetTokenRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Value("${app.password-reset.token-ttl-minutes:60}")
    private long ttlMinutes;

    @Value("${app.password-reset.reset-url-base:http://localhost:8080/reset-password}")
    private String resetUrlBase;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    public void solicitarRecuperacion(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (!usuarioOpt.isPresent() || !usuarioOpt.get().isActivo()) {
            return;
        }

        Usuario usuario = usuarioOpt.get();

        tokenRepository.invalidarTokensVigentesDeUsuario(usuario.getId(), LocalDateTime.now());

        String rawToken = generarTokenAleatorio();
        String tokenHash = hash(rawToken);

        PasswordResetToken token = new PasswordResetToken();
        token.setTokenHash(tokenHash);
        token.setUsuario(usuario);
        token.setExpiraEn(LocalDateTime.now().plusMinutes(ttlMinutes));
        tokenRepository.save(token);

        String resetUrl = resetUrlBase + "?token=" + rawToken;
        emailService.enviarEmailRecuperacionPassword(usuario.getEmail(), usuario.getNombre(), resetUrl, ttlMinutes);
    }

    @Transactional(readOnly = true)
    public boolean validarToken(String rawToken) {
        if (rawToken == null || rawToken.isEmpty()) {
            return false;
        }
        return tokenRepository.findByTokenHash(hash(rawToken))
                .map(PasswordResetToken::estaVigente)
                .orElse(false);
    }

    public void confirmarCambioPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (!token.estaVigente()) {
            throw new RuntimeException("Token expirado o ya utilizado");
        }

        Usuario usuario = token.getUsuario();
        if (!usuario.isActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }

        if (PASSWORD_ENCODER.matches(newPassword, usuario.getContrasena())) {
            throw new RuntimeException("La nueva contraseña debe ser diferente a la actual");
        }

        usuario.setContrasena(PASSWORD_ENCODER.encode(newPassword));
        usuario.setActualizadoEn(LocalDateTime.now());
        usuarioRepository.save(usuario);

        token.setUsadoEn(LocalDateTime.now());
        tokenRepository.save(token);

        tokenRepository.invalidarTokensVigentesDeUsuario(usuario.getId(), LocalDateTime.now());

        emailService.enviarEmailConfirmacionCambioPassword(usuario.getEmail(), usuario.getNombre());
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
