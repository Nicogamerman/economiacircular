package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.OferenteValoracionCreateDto;
import com.pp.economia_circular.DTO.OferenteValoracionResponseDto;
import com.pp.economia_circular.DTO.OferenteValoracionesSummaryDto;
import com.pp.economia_circular.service.OferenteValoracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/oferentes")
@CrossOrigin(origins = "*")
public class OferenteValoracionController {

    @Autowired
    private OferenteValoracionService valoracionService;

    @GetMapping("/{oferenteId}/valoraciones")
    public ResponseEntity<?> getValoraciones(@PathVariable Long oferenteId) {
        try {
            OferenteValoracionesSummaryDto summary = valoracionService.getSummaryByOferenteId(oferenteId);
            return ResponseEntity.ok(summary);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{oferenteId}/valoraciones")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createValoracion(
            @PathVariable Long oferenteId,
            @Valid @RequestBody OferenteValoracionCreateDto createDto) {
        try {
            OferenteValoracionResponseDto created = valoracionService.createReview(oferenteId, createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
