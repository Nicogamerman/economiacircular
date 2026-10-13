package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.PerfilUpdateDto;
import com.pp.economia_circular.DTO.UsuarioPerfilDto;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // --- Perfil público ---

    @GetMapping("/perfil/{id}")
    public ResponseEntity<?> perfilPublico(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(usuarioService.obtenerPerfilPublico(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- Perfil propio ---

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> miPerfil() {
        try {
            return ResponseEntity.ok(usuarioService.obtenerMiPerfil());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> actualizarMiPerfil(@Valid @RequestBody PerfilUpdateDto dto) {
        try {
            return ResponseEntity.ok(usuarioService.actualizarMiPerfil(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Foto de perfil ---

    @PostMapping("/me/foto")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> subirFoto(@RequestParam("foto") MultipartFile archivo) {
        try {
            usuarioService.subirFoto(archivo);
            return ResponseEntity.ok("Foto actualizada correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/me/foto")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> eliminarFoto() {
        try {
            usuarioService.eliminarFoto();
            return ResponseEntity.ok("Foto eliminada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<?> obtenerFoto(@PathVariable Long id) {
        try {
            byte[] foto = usuarioService.obtenerFoto(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(foto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // --- CRUD admin ---

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        if (usuario.getContrasena() != null && !usuario.getContrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioRepository.save(usuario));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario datosActualizados) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNombre(datosActualizados.getNombre());
                    usuario.setApellido(datosActualizados.getApellido());
                    usuario.setEmail(datosActualizados.getEmail());
                    if (datosActualizados.getContrasena() != null && !datosActualizados.getContrasena().isBlank()) {
                        usuario.setContrasena(passwordEncoder.encode(datosActualizados.getContrasena()));
                    }
                    usuario.setRol(datosActualizados.getRol());
                    usuario.setDomicilio(datosActualizados.getDomicilio());
                    usuario.setActivo(datosActualizados.isActivo());
                    usuario.setActualizadoEn(java.time.LocalDateTime.now());
                    return ResponseEntity.ok(usuarioRepository.save(usuario));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuarioRepository.delete(usuario);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
