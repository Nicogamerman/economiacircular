package com.pp.economia_circular.controller;

import com.pp.economia_circular.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(notificacionService.listarMisNotificaciones());
    }

    @GetMapping("/no-leidas")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> contarNoLeidas() {
        Map<String, Long> response = new HashMap<>();
        response.put("count", notificacionService.contarNoLeidas());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/leer")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> marcarComoLeidas() {
        notificacionService.marcarComoLeidas();
        return ResponseEntity.ok().build();
    }
}
