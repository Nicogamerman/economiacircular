package com.pp.economia_circular.service;

import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService {

    private final String SECRET_KEY = "clave-secreta-super-segura-para-economia-circular-2024";
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    public String generarToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 horas
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * Genera un token JWT con el rol del usuario
     * @param email Email del usuario
     * @param rol Rol del usuario (ADMIN, USER, etc.)
     * @return Token JWT
     */
    public String generarToken(String email, String rol) {
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .claim("rol", rol)
                .claim("authorities", "ROLE_" + rol)  // Formato que Spring Security usa
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 horas
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String extraerEmail(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Extrae el rol del token JWT
     * @param token Token JWT
     * @return Rol del usuario
     */
    public String extraerRol(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("rol", String.class);
    }

    /**
     * Extrae todas las claims del token
     * @param token Token JWT
     * @return Claims del token
     */
    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public Usuario getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                return usuarioRepository.findByEmail(email).orElse(null);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
