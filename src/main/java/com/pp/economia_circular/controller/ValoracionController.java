package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.ResumenValoracionDto;
import com.pp.economia_circular.DTO.ValoracionCreateDto;
import com.pp.economia_circular.DTO.ValoracionResponseDto;
import com.pp.economia_circular.service.ValoracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/valoraciones")
@CrossOrigin(origins = "*")
public class ValoracionController {

    @Autowired
    private ValoracionService valoracionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody ValoracionCreateDto dto) {
        try {
            ValoracionResponseDto creada = valoracionService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ValoracionCreateDto dto) {
        try {
            return ResponseEntity.ok(valoracionService.actualizar(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            valoracionService.eliminar(id);
            return ResponseEntity.ok("Valoración eliminada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long usuarioId) {
        try {
            List<ValoracionResponseDto> valoraciones = valoracionService.listarPorUsuarioValorado(usuarioId);
            return ResponseEntity.ok(valoraciones);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/usuario/{usuarioId}/resumen")
    public ResponseEntity<?> obtenerResumen(@PathVariable Long usuarioId) {
        try {
            ResumenValoracionDto resumen = valoracionService.obtenerResumen(usuarioId);
            return ResponseEntity.ok(resumen);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/articulo/{articuloId}")
    public ResponseEntity<?> listarPorArticulo(@PathVariable Long articuloId) {
        try {
            return ResponseEntity.ok(valoracionService.listarPorArticulo(articuloId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mis-valoraciones")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> misValoraciones() {
        try {
            return ResponseEntity.ok(valoracionService.listarMisValoracionesDadas());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
