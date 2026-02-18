package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.PasswordResetToken;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.PasswordResetTokenRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordRecoveryService {

    private static final int TOKEN_VALID_MINUTES = 60;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Crea un token de recuperación, lo guarda y envía el email con el enlace.
     * Por seguridad siempre devuelve el mismo mensaje aunque el email no exista.
     */
    @Transactional
    public void solicitarRecuperacion(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.trim());
        if (!usuarioOpt.isPresent() || !usuarioOpt.get().isActivo()) {
            return;
        }
        Usuario usuario = usuarioOpt.get();
        // Usuarios que solo tienen Google no tienen contraseña para resetear
        if (usuario.getAuthProvider() != null && "google".equalsIgnoreCase(usuario.getAuthProvider())
                && (usuario.getContrasena() == null || usuario.getContrasena().isEmpty())) {
            return;
        }
        // Invalidar tokens previos no usados
        tokenRepository.deleteByUsuarioId(usuario.getId());
        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiraEn = LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES);
        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenValue)
                .usuario(usuario)
                .expiraEn(expiraEn)
                .usado(false)
                .creadoEn(LocalDateTime.now())
                .build();
        tokenRepository.save(token);
        String resetLink = baseUrl + "/api/auth/reset-password-with-token?token=" + tokenValue;
        emailService.sendPasswordResetWithToken(email, resetLink, TOKEN_VALID_MINUTES);
    }

    /**
     * Valida el token y actualiza la contraseña. Marca el token como usado.
     */
    @Transactional
    public boolean restablecerConToken(String tokenValue, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository
                .findByTokenAndUsadoFalseAndExpiraEnAfter(tokenValue, LocalDateTime.now());
        if (!tokenOpt.isPresent()) {
            return false;
        }
        PasswordResetToken token = tokenOpt.get();
        token.setUsado(true);
        tokenRepository.save(token);
        Usuario usuario = token.getUsuario();
        usuario.setContrasena(passwordEncoder.encode(newPassword));
        usuario.setActualizadoEn(LocalDateTime.now());
        usuario.setAuthProvider("local"); // por si era solo Google, ahora tiene contraseña
        usuarioRepository.save(usuario);
        return true;
    }
}
