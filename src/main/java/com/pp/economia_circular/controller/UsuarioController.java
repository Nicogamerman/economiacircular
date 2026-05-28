package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.PerfilUpdateDto;
import com.pp.economia_circular.DTO.UsuarioPerfilDto;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.UsuarioRepository;
import com.pp.economia_circular.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // --- Perfil público ---

    @GetMapping("/perfil/{id}")
    public ResponseEntity<?> perfilPublico(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(usuarioService.obtenerPerfilPublico(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
 
    // GET: listar todos los usuarios
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll(); // <- esto ya trae todos si el repository no filtra por "activo"
    }

    // GET: usuario por id
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST: crear nuevo usuario.
    @PostMapping
    public void crearUsuario(@RequestBody Usuario usuario) {
         usuarioRepository.save(usuario);
    }

    // PUT: modificar usuario
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario datosActualizados) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setNombre(datosActualizados.getNombre());
                    usuario.setApellido(datosActualizados.getApellido());
                    usuario.setEmail(datosActualizados.getEmail());
                    usuario.setContrasena(datosActualizados.getContrasena());
                    usuario.setRol(datosActualizados.getRol());
                    usuario.setDomicilio(datosActualizados.getDomicilio());
                    usuario.setFoto(datosActualizados.getFoto());
                    usuario.setActivo(datosActualizados.isActivo());
                    usuario.setActualizadoEn(java.time.LocalDateTime.now());
                    return ResponseEntity.ok(usuarioRepository.save(usuario));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE: eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {

        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuarioRepository.delete(usuario);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
