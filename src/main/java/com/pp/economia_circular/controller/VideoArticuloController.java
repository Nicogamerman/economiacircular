package com.pp.economia_circular.controller;

import com.pp.economia_circular.entity.VideoArticulo;
import com.pp.economia_circular.service.VideoArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/articles/{articuloId}/video")
public class VideoArticuloController {

    @Autowired
    private VideoArticuloService videoService;

    /** Metadatos del video; 204 si el artículo no tiene. */
    @GetMapping
    public ResponseEntity<?> metadatos(@PathVariable Long articuloId) {
        return videoService.metadatos(articuloId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Contenido del video. Al devolver un Resource, Spring atiende los pedidos con
     * cabecera Range (necesario para adelantar/retroceder en el reproductor).
     */
    @GetMapping("/file")
    public ResponseEntity<Resource> archivo(@PathVariable Long articuloId) {
        try {
            VideoArticulo video = videoService.obtenerConContenido(articuloId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(video.getContentType()))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(new ByteArrayResource(video.getContenido()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> subir(@PathVariable Long articuloId,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "conAudio", defaultValue = "true") boolean conAudio) {
        try {
            return ResponseEntity.ok(videoService.subir(articuloId, file, conAudio));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> cambiarAudio(@PathVariable Long articuloId, @RequestBody Map<String, Boolean> body) {
        try {
            return ResponseEntity.ok(videoService.cambiarAudio(articuloId, Boolean.TRUE.equals(body.get("conAudio"))));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Long articuloId) {
        try {
            videoService.eliminar(articuloId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
