package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.FavoritoResponseDto;
import com.pp.economia_circular.service.FavoritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favoritos")
@CrossOrigin(origins = "*")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

    @PostMapping("/{articuloId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> agregar(@PathVariable Long articuloId) {
        try {
            FavoritoResponseDto dto = favoritoService.agregarFavorito(articuloId);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{articuloId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> quitar(@PathVariable Long articuloId) {
        try {
            favoritoService.quitarFavorito(articuloId);
            return ResponseEntity.ok("Artículo eliminado de favoritos");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<FavoritoResponseDto> favoritos = favoritoService.listarFavoritos(pageable);
            return ResponseEntity.ok(favoritos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{articuloId}/estado")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> estado(@PathVariable Long articuloId) {
        try {
            boolean esFavorito = favoritoService.esFavorito(articuloId);
            return ResponseEntity.ok(Map.of("esFavorito", esFavorito, "articuloId", articuloId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{articuloId}/count")
    public ResponseEntity<?> contar(@PathVariable Long articuloId) {
        try {
            long total = favoritoService.contarFavoritos(articuloId);
            return ResponseEntity.ok(Map.of("articuloId", articuloId, "total", total));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
