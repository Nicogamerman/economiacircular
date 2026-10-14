package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.ImagenArticuloResponseDto;
import com.pp.economia_circular.entity.ImagenArticulo;
import com.pp.economia_circular.service.ImagenArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/articles/{articuloId}/images")
@CrossOrigin(origins = "*")
public class ImagenArticuloController {

    @Autowired
    private ImagenArticuloService imagenService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> subir(@PathVariable Long articuloId,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "descripcion", required = false) String descripcion) {
        try {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            ImagenArticuloResponseDto dto = imagenService.subir(articuloId, file, descripcion, baseUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listar(@PathVariable Long articuloId) {
        try {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            List<ImagenArticuloResponseDto> imagenes = imagenService.listar(articuloId, baseUrl);
            return ResponseEntity.ok(imagenes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{imagenId}/file")
    public ResponseEntity<?> descargar(@PathVariable Long articuloId, @PathVariable Long imagenId) {
        try {
            ImagenArticulo imagen = imagenService.obtenerConContenido(articuloId, imagenId);
            MediaType mediaType = imagen.getContentType() != null
                    ? MediaType.parseMediaType(imagen.getContentType())
                    : MediaType.APPLICATION_OCTET_STREAM;
            String filename = imagen.getNombreArchivo() != null
                    ? imagen.getNombreArchivo()
                    : "imagen_" + imagen.getId();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(mediaType)
                    .body(imagen.getContenido());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{imagenId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Long articuloId, @PathVariable Long imagenId) {
        try {
            imagenService.eliminar(articuloId, imagenId);
            return ResponseEntity.ok("Imagen eliminada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
