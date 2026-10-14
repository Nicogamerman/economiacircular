package com.pp.economia_circular.controller;

import com.pp.economia_circular.service.VerificacionIdentidadService;
import com.pp.economia_circular.service.ia.IaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/verificacion/identidad")
public class VerificacionIdentidadController {

    @Autowired
    private VerificacionIdentidadService verificacionService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> verificar(@RequestParam("documento") MultipartFile documento,
                                       @RequestParam("selfie") MultipartFile selfie) {
        try {
            return ResponseEntity.ok(verificacionService.verificar(documento, selfie));
        } catch (IaException e) {
            return IaController.error(e);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Último intento del usuario; 204 si nunca intentó. */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> miVerificacion() {
        return verificacionService.miUltimaVerificacion()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> pendientes() {
        return ResponseEntity.ok(verificacionService.pendientesDeRevision());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resolver(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            boolean aprobar = Boolean.TRUE.equals(body.get("aprobar"));
            Object nota = body.get("nota");
            return ResponseEntity.ok(verificacionService.resolver(id, aprobar, nota != null ? nota.toString() : null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
