package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.DevolucionDto;
import com.pp.economia_circular.service.DevolucionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;

@RestController
public class DevolucionController {

    @Autowired
    private DevolucionService devolucionService;

    @PostMapping("/api/intercambios/{solicitudId}/devolucion")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> solicitar(@PathVariable Long solicitudId,
                                       @Valid @RequestBody DevolucionDto.Crear body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(devolucionService.solicitar(solicitudId, body.getMotivo()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/devoluciones/mias")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> misDevoluciones() {
        try {
            return ResponseEntity.ok(devolucionService.misDevoluciones());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/devoluciones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> todas() {
        try {
            return ResponseEntity.ok(devolucionService.todas());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/devoluciones/plazo")
    public ResponseEntity<?> plazo() {
        return ResponseEntity.ok(Collections.singletonMap("plazoDias", devolucionService.getPlazoDias()));
    }

    @PutMapping("/api/devoluciones/{id}/responder")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> responder(@PathVariable Long id, @Valid @RequestBody DevolucionDto.Responder body) {
        try {
            return ResponseEntity.ok(devolucionService.responder(id, body.isAceptar(), body.getRespuesta()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/api/devoluciones/{id}/cancelar")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(devolucionService.cancelar(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
