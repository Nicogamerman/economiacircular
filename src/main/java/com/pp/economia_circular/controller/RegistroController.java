package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.UsuarioRequest;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;

@RestController
@RequestMapping("/api/registrar")
public class RegistroController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody UsuarioRequest request) {
        byte[] fotoBytes = null;
        if (request.getFotoBase64() != null && !request.getFotoBase64().isEmpty()) {
            fotoBytes = Base64.getDecoder().decode(request.getFotoBase64());
        }
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()){
            return ResponseEntity.badRequest().body("Ya existe un usuario registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .rol(request.getRol())
                .domicilio(request.getDomicilio())
                .foto(fotoBytes)
                .activo(true)
                .creadoEn(LocalDateTime.now())
                .actualizadoEn(LocalDateTime.now())
                .build();
        usuarioRepository.save(usuario);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> obtenerFoto(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFoto() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"foto_" + usuario.getId() + ".jpg\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(usuario.getFoto());
    }
}