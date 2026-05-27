package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.NotificacionResponseDto;
import com.pp.economia_circular.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> listar(@RequestParam(value = "soloNoLeidas", defaultValue = "false") boolean soloNoLeidas) {
        try {
            List<NotificacionResponseDto> lista = notificacionService.listarMisNotificaciones(soloNoLeidas);
            return ResponseEntity.ok(lista);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/no-leidas/count")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> contarNoLeidas() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("count", notificacionService.contarNoLeidas());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/leer")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/leer-todas")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> marcarTodasComoLeidas() {
        try {
            int actualizadas = notificacionService.marcarTodasComoLeidas();
            Map<String, Object> response = new HashMap<>();
            response.put("actualizadas", actualizadas);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            notificacionService.eliminar(id);
            return ResponseEntity.ok("Notificación eliminada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
