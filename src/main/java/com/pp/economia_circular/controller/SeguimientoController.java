package com.pp.economia_circular.controller;

import com.pp.economia_circular.entity.Event;
import com.pp.economia_circular.entity.Seguimiento;
import com.pp.economia_circular.service.SeguimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seguimientos")
public class SeguimientoController {

    @Autowired
    private SeguimientoService seguimientoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> misSeguimientos() {
        try {
            return ResponseEntity.ok(seguimientoService.misSeguimientos());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/feed")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> feed(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "12") int size) {
        try {
            return ResponseEntity.ok(seguimientoService.feed(PageRequest.of(page, size)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Público: cantidad de seguidores y, si hay sesión, si el usuario actual sigue. */
    @GetMapping("/{tipo}/{id}")
    public ResponseEntity<?> estado(@PathVariable String tipo, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(seguimientoService.estado(parsearTipo(tipo), id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{tipo}/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> seguir(@PathVariable String tipo, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(seguimientoService.seguir(parsearTipo(tipo), id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{tipo}/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> dejarDeSeguir(@PathVariable String tipo, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(seguimientoService.dejarDeSeguir(parsearTipo(tipo), id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/intereses")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> misIntereses() {
        try {
            return ResponseEntity.ok(seguimientoService.misIntereses());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/intereses")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> actualizarIntereses(@RequestBody List<Event.EventType> tipos) {
        try {
            return ResponseEntity.ok(seguimientoService.actualizarIntereses(tipos));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Acepta "oferentes"/"eventos" en la URL. */
    static Seguimiento.TipoSeguimiento parsearTipo(String tipo) {
        if ("oferentes".equalsIgnoreCase(tipo) || "oferente".equalsIgnoreCase(tipo)) {
            return Seguimiento.TipoSeguimiento.OFERENTE;
        }
        if ("eventos".equalsIgnoreCase(tipo) || "evento".equalsIgnoreCase(tipo)) {
            return Seguimiento.TipoSeguimiento.EVENTO;
        }
        throw new RuntimeException("Tipo de seguimiento inválido: " + tipo);
    }
}
