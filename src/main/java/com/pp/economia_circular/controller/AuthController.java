package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.AuthRequest;
import com.pp.economia_circular.DTO.AuthResponse;
import com.pp.economia_circular.DTO.ConfirmResetPasswordRequest;
import com.pp.economia_circular.DTO.ForgotPasswordRequest;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.EmailVerificationService;
import com.pp.economia_circular.service.GoogleAuthService;
import com.pp.economia_circular.service.JWTService;
import com.pp.economia_circular.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired(required = false)
    private EmailVerificationService emailVerificationService;

    @Value("${app.email-verification.required:false}")
    private boolean emailVerificationRequired;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
                // Usuario solo Google (sin contraseña local) debe usar login con Google
                if (usuario.getContrasena() == null || usuario.getContrasena().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Esta cuenta usa inicio de sesión con Google. Usa el botón \"Iniciar con Google\".");
                }

                // Acepta contraseñas en texto plano (usuarios viejos) o BCrypt
                boolean passwordMatches =
                        usuario.getContrasena().equals(request.getContrasena()) ||
                        passwordEncoder.matches(request.getContrasena(), usuario.getContrasena());
                
                if (passwordMatches) {
                    if (emailVerificationRequired && !usuario.isEmailVerificado()) {
                        Map<String, Object> body = new HashMap<>();
                        body.put("message", "Email no verificado. Revisá tu casilla o solicitá un nuevo enlace.");
                        body.put("emailVerificado", false);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
                    }
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
     * Restablecer contraseña usando el token recibido por email.
     * POST /api/auth/reset-password-with-token
     * Body: { "token": "...", "newPassword": "NuevaPassword123!" }
     * O token como query param: ?token=...
     */
    @PostMapping("/reset-password-with-token")
    public ResponseEntity<?> resetPasswordWithToken(
            @RequestParam(required = false) String token,
            @RequestBody(required = false) Map<String, String> body) {
        String tokenValue = token;
        String newPassword = null;
        if (body != null) {
            if (tokenValue == null) tokenValue = body.get("token");
            newPassword = body.get("newPassword");
        }
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Token es requerido");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nueva contraseña es requerida");
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 6 caracteres");
        }
        // Alias de /confirm-reset-password (se mantiene por compatibilidad)
        try {
            passwordResetService.confirmarCambioPassword(tokenValue.trim(), newPassword);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Token inválido o expirado. Solicita de nuevo el enlace de recuperación.");
        }
        return ResponseEntity.ok("Contraseña actualizada. Ya puedes iniciar sesión.");
    }

    /**
     * Iniciar sesión con Google.
     * El frontend obtiene el ID token con Google Sign-In y lo envía aquí.
     * POST /api/auth/google
     * Body: { "idToken": "..." }
     */
    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> request) {
        String idToken = request != null ? request.get("idToken") : null;
        if (idToken == null || idToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("idToken es requerido");
        }
        if (!googleAuthService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Login con Google no está configurado. Configure app.google.client-id.");
        }
        GoogleIdToken.Payload payload = googleAuthService.verifyToken(idToken);
        if (payload == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de Google inválido o expirado.");
        }
        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String name = payload.get("name") != null ? (String) payload.get("name") : "";
        String givenName = payload.get("given_name") != null ? (String) payload.get("given_name") : "";
        String familyName = payload.get("family_name") != null ? (String) payload.get("family_name") : "";

        Usuario usuario = usuarioRepo.findByGoogleId(googleId).orElse(null);
        if (usuario == null) {
            usuario = usuarioRepo.findByEmail(email).orElse(null);
            if (usuario != null) {
                // Vincular cuenta existente (mismo email)
                usuario.setGoogleId(googleId);
                usuario.setActualizadoEn(LocalDateTime.now());
                usuarioRepo.save(usuario);
            }
        }
        if (usuario == null) {
            // Nuevo usuario: crear cuenta con Google
            String nombre = !givenName.isEmpty() ? givenName : (name.isEmpty() ? "Usuario" : name);
            String apellido = !familyName.isEmpty() ? familyName : "";
            usuario = Usuario.builder()
                    .nombre(nombre)
                    .apellido(apellido)
                    .email(email)
                    .contrasena(null)
                    .rol("USER")
                    .googleId(googleId)
                    .authProvider("google")
                    .activo(true)
                    .creadoEn(LocalDateTime.now())
                    .actualizadoEn(LocalDateTime.now())
                    .build();
            usuarioRepo.save(usuario);
        }
        if (!usuario.isActivo()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario inactivo");
        }
        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol());
        return ResponseEntity.ok(AuthResponse.builder().token(token).build());
    }

    /**
     * Vincular la cuenta actual (autenticada por JWT) con Google.
     * Útil para poder iniciar sesión después con "Iniciar con Google".
     * POST /api/auth/link-google
     * Headers: Authorization: Bearer <jwt>
     * Body: { "idToken": "..." }
     */
    @PostMapping("/link-google")
    public ResponseEntity<?> linkGoogle(@RequestBody Map<String, String> request) {
        String idToken = request != null ? request.get("idToken") : null;
        if (idToken == null || idToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("idToken es requerido");
        }
        if (!googleAuthService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Vinculación con Google no está configurada.");
        }
        Usuario current = jwtService.getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión para vincular Google.");
        }
        GoogleIdToken.Payload payload = googleAuthService.verifyToken(idToken);
        if (payload == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de Google inválido o expirado.");
        }
        String googleId = payload.getSubject();
        String email = payload.getEmail();
        if (!email.equalsIgnoreCase(current.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("El correo de Google (" + email + ") no coincide con tu cuenta (" + current.getEmail() + "). Usa la misma cuenta de Google.");
        }
        Optional<Usuario> existingByGoogle = usuarioRepo.findByGoogleId(googleId);
        if (existingByGoogle.isPresent() && !existingByGoogle.get().getId().equals(current.getId())) {
            return ResponseEntity.badRequest().body("Esta cuenta de Google ya está vinculada a otro usuario.");
        }
        current.setGoogleId(googleId);
        current.setActualizadoEn(LocalDateTime.now());
        usuarioRepo.save(current);
        return ResponseEntity.ok("Cuenta vinculada con Google correctamente. Ya puedes iniciar sesión con Google.");
    }

    /**
     * Cambiar contraseña estando autenticado (requiere contraseña actual).
     * POST /api/auth/reset-password
     * Body: { "email": "...", "oldPassword": "...", "newPassword": "..." }
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

            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La contraseña actual es requerida para cambiar la contraseña.");
            }

            // Buscar usuario
            Optional<Usuario> usuarioOpt = usuarioRepo.findByEmail(email);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar que el usuario esté activo
            if (!usuario.isActivo()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Usuario inactivo");
            }
            if (usuario.getContrasena() == null || usuario.getContrasena().isEmpty()) {
                return ResponseEntity.badRequest().body("Esta cuenta usa solo Google. Vincula una contraseña desde configuración o usa \"Olvidé mi contraseña\" si ya la vinculaste.");
            }
            if (passwordEncoder.matches(newPassword, usuario.getContrasena())) {
                return ResponseEntity.badRequest().body("La contraseña debe ser diferente a la anterior.");
            }
            if (!passwordEncoder.matches(oldPassword, usuario.getContrasena())) {
                return ResponseEntity.badRequest().body("La contraseña actual no coincide.");
            }
            // Encriptar la nueva contraseña
            String hashedPassword = passwordEncoder.encode(newPassword);

            usuario.setContrasena(hashedPassword);
            usuario.setActualizadoEn(java.time.LocalDateTime.now());
            usuarioRepo.save(usuario);

            return ResponseEntity.ok("Contraseña actualizada exitosamente");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al resetear contraseña: " + e.getMessage());
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            if (emailVerificationService == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Verificación de email no disponible");
            }
            emailVerificationService.verificar(token);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email verificado correctamente");
            response.put("verified", true);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            if (emailVerificationService != null) {
                emailVerificationService.reenviar(request.getEmail());
            }
        } catch (Exception ignored) {
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Si el email está registrado y aún no fue verificado, recibirás un nuevo enlace.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.solicitarRecuperacion(request.getEmail());
        } catch (Exception ignored) {
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Si el email está registrado, recibirás un enlace para restablecer tu contraseña.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        Map<String, Object> response = new HashMap<>();
        response.put("valid", passwordResetService.validarToken(token));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm-reset-password")
    public ResponseEntity<?> confirmResetPassword(@Valid @RequestBody ConfirmResetPasswordRequest request) {
        try {
            passwordResetService.confirmarCambioPassword(request.getToken(), request.getNewPassword());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Contraseña actualizada exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
