package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.PoliticaCreateDto;
import com.pp.economia_circular.DTO.PoliticaResponseDto;
import com.pp.economia_circular.entity.Politica;
import com.pp.economia_circular.service.PoliticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/politicas")
@CrossOrigin(origins = "*")
public class PoliticaController {

    @Autowired
    private PoliticaService politicaService;

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) Politica.TipoPolitica tipo) {
        try {
            List<PoliticaResponseDto> result = politicaService.listarActivas(tipo);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarTodas() {
        try {
            return ResponseEntity.ok(politicaService.listarTodas());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(politicaService.obtener(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody PoliticaCreateDto dto) {
        try {
            PoliticaResponseDto result = politicaService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody PoliticaCreateDto dto) {
        try {
            return ResponseEntity.ok(politicaService.actualizar(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            politicaService.eliminar(id);
            return ResponseEntity.ok("Política eliminada");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleActivo(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(politicaService.toggleActivo(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
