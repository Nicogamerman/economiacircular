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

import java.util.Map;
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
            
            System.out.println("=== DEBUG LOGIN ===");
            System.out.println("Email: [" + request.getEmail() + "]");
            System.out.println("Pass recibida: [" + request.getContrasena() + "]");
            
            Optional<Usuario> usuarioOpt = usuarioRepo.findByEmail(request.getEmail());
 
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                
                System.out.println("Usuario encontrado: " + usuario.getEmail());
                System.out.println("Activo: " + usuario.isActivo());
                System.out.println("Pass en BD: [" + usuario.getContrasena() + "]");
                System.out.println("¿Es BCrypt?: " + usuario.getContrasena().startsWith("$2"));
                
                // Verificar si el usuario está activo
                if (!usuario.isActivo()) {
                    System.out.println("Usuario INACTIVO");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Usuario inactivo");
                }
                
                // Verificar contraseña (tanto texto plano como encriptada)
                boolean passwordMatches = false;
                boolean plainMatch = usuario.getContrasena().equals(request.getContrasena());
                boolean bcryptMatch = passwordEncoder.matches(request.getContrasena(), usuario.getContrasena());
                
                System.out.println("Plain match: " + plainMatch);
                System.out.println("BCrypt match: " + bcryptMatch);
                
                if (plainMatch) {
                    // Contraseña en texto plano (para usuarios existentes)
                    passwordMatches = true;
                } else if (bcryptMatch) {
                    // Contraseña encriptada
                    passwordMatches = true;
                }
                
                System.out.println("Match final: " + passwordMatches);
                System.out.println("==================");
                
                if (passwordMatches) {
                    // Generar token con el rol del usuario
                    String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol());
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

    /**
     * Endpoint para resetear la contraseña de un usuario
     * POST /api/auth/reset-password
     * Body: { "email": "usuario@ejemplo.com", "newPassword": "NuevaPassword123!" }
     * 
     * NOTA: En producción esto debería:
     * 1. Enviar un email con token de verificación
     * 2. Validar el token antes de permitir el cambio
     * 3. Tener rate limiting para evitar abuso
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String newPassword = request.get("newPassword");

            String oldPassword = request.get("oldPassword");


            // Validaciones
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email es requerido");
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nueva contraseña es requerida");
            }

            // Validar formato de contraseña (mínimo 6 caracteres)
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body("La contraseña debe tener al menos 6 caracteres");
            }

            // Buscar usuario
            Optional<Usuario> usuarioOpt = usuarioRepo.findByEmail(email);
            if (!usuarioOpt.isPresent()) {
                // Por seguridad, no revelar si el usuario existe o no
                return ResponseEntity.ok("Si el email existe, la contraseña ha sido actualizada");
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar que el usuario esté activo
            if (!usuario.isActivo()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Usuario inactivo");
            }
            if (passwordEncoder.matches(newPassword, usuarioOpt.get().getContrasena())){
                return ResponseEntity.badRequest().body("La contraseña debe ser diferente a la anterior.");
            }

            if (!passwordEncoder.matches(oldPassword, usuarioOpt.get().getContrasena())){
               return ResponseEntity.badRequest().body("La contraseña actual no coincide.");
            }
            // Encriptar la nueva contraseña
            String hashedPassword = passwordEncoder.encode(newPassword);

            usuario.setContrasena(hashedPassword);
            usuario.setActualizadoEn(java.time.LocalDateTime.now());
            usuarioRepo.save(usuario);

            System.out.println("=== PASSWORD RESET ===");
            System.out.println("Email: " + email);
            System.out.println("Nueva contraseña establecida exitosamente");
            System.out.println("=====================");

            // En producción, aquí enviarías un email de confirmación
            return ResponseEntity.ok("Contraseña actualizada exitosamente");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al resetear contraseña: " + e.getMessage());
        }
    }

    /**
     * Endpoint para verificar si un email existe (útil para validación en frontend)
     * POST /api/auth/check-email
     * Body: { "email": "usuario@ejemplo.com" }
     */
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email es requerido");
            }

            Optional<Usuario> usuarioOpt = usuarioRepo.findByEmail(email);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("exists", usuarioOpt.isPresent());
            response.put("active", usuarioOpt.map(Usuario::isActivo).orElse(false));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar email: " + e.getMessage());
        }
    }

    /**
     * Endpoint de debug para decodificar un token JWT
     * POST /api/auth/decode-token
     * Body: { "token": "eyJhbGci..." }
     * 
     * Este endpoint es útil para desarrollo/debug
     */
    @PostMapping("/decode-token")
    public ResponseEntity<?> decodeToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");

            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Token es requerido");
            }

            // Verificar si el token es válido primero
            if (!jwtService.validarToken(token)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Token inválido o expirado");
            }

            // Extraer claims del token
            String email = jwtService.extraerEmail(token);
            String rol = jwtService.extraerRol(token);
            java.util.Map<String, Object> claims = jwtService.extraerClaims(token);

            // Crear respuesta con la información decodificada
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("email", email);
            response.put("rol", rol);
            response.put("claims", claims);
            response.put("valid", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("email", null);
            errorResponse.put("rol", null);
            errorResponse.put("claims", null);
            errorResponse.put("valid", false);
            errorResponse.put("error", "Token inválido: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
