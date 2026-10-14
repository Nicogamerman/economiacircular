package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.OferenteValoracionUpdateDto;
import com.pp.economia_circular.service.OferenteValoracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/valoraciones")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminValoracionController {

    @Autowired
    private OferenteValoracionService valoracionService;

    @GetMapping
    public ResponseEntity<?> getValoraciones() {
        return ResponseEntity.ok(valoracionService.getAllReviewsForAdmin());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateValoracion(
            @PathVariable Long id,
            @Valid @RequestBody OferenteValoracionUpdateDto updateDto) {
        try {
            return ResponseEntity.ok(valoracionService.updateReviewAsAdmin(id, updateDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobarValoracion(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(valoracionService.approveReview(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
