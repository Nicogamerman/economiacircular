package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.AuthRequest;
import com.pp.economia_circular.DTO.AuthResponse;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private JWTService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // Validar que email y contraseña no sean null
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Email es requerido");
            }
            
            if (request.getContrasena() == null || request.getContrasena().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Contraseña es requerida");
            }
            
            Optional<Usuario> usuarioOpt = usuarioRepo.findByEmail(request.getEmail());

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                
                // Verificar si el usuario está activo
                if (!usuario.isActivo()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Usuario inactivo");
                }
                
                // Verificar contraseña (tanto texto plano como encriptada)
                boolean passwordMatches = false;
                if (usuario.getContrasena().equals(request.getContrasena())) {
                    // Contraseña en texto plano (para usuarios existentes)
                    passwordMatches = true;
                } else if (passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
                    // Contraseña encriptada
                    passwordMatches = true;
                }
                
                if (passwordMatches) {
                    String token = jwtService.generarToken(usuario.getEmail());
                    AuthResponse response = AuthResponse.builder()
                            .token(token)
                            .build();
                    return ResponseEntity.ok(response);
                }
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales inválidas");
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }
}
