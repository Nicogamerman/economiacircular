package com.pp.economia_circular.controller;

import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.service.JWTService;
import com.pp.economia_circular.service.ReportService;
import com.pp.economia_circular.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@RestController
@RequestMapping("/api/article-status")
@CrossOrigin(origins = "*")
public class ArticleStatusController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ReportService reportService;

    @Autowired
    private JWTService authService;

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> updateArticleStatus(
            @PathVariable Long id,
            @RequestBody(required = true) EstadoRequest estadoRequest) {

        try {
            Optional<Articulo> optionalArticulo = articleRepository.findById(id);
            if (!optionalArticulo.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Artículo no encontrado");
            }

            Articulo articulo = optionalArticulo.get();

            Usuario currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }
            boolean esAdmin = currentUser.getRol() != null && currentUser.getRol().contains("ADMIN");
            if (!esAdmin && !articulo.getUsuario().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tenés permiso para modificar este artículo");
            }

            String nuevoEstado = estadoRequest.getNuevoEstado().toUpperCase();

            // Validar estados permitidos
            try {
                articulo.setEstado(Articulo.EstadoArticulo.valueOf(nuevoEstado));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Estado inválido: " + nuevoEstado);
            }

            articleRepository.save(articulo);

            // Si el estado es INTERCAMBIADO, DONADO o VENDIDO → actualizar métricas ambientales
            if (nuevoEstado.equals("INTERCAMBIADO") ||
                    nuevoEstado.equals("DONADO") ||
                    nuevoEstado.equals("VENDIDO")) {

                reportService.recalcularImpactoAmbiental();
            }

            return ResponseEntity.ok().body(
                    new RespuestaCambioEstado("Estado actualizado correctamente", articulo)
            );

        } catch (Exception e) {
            e.printStackTrace(); // muestra el error exacto en consola
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar estado: " + e.toString());
        }

    }

    // DTOs internos

    public static class EstadoRequest {
        @JsonProperty("nuevoEstado")
        private String nuevoEstado;

        public String getNuevoEstado() { return nuevoEstado; }
        public void setNuevoEstado(String nuevoEstado) { this.nuevoEstado = nuevoEstado; }

        @Override
        public String toString() {
            return "EstadoRequest{nuevoEstado='" + nuevoEstado + "'}";
        }
    }


    public static class RespuestaCambioEstado {
        private String mensaje;
        private Long articuloId;
        private String nuevoEstado;

        public RespuestaCambioEstado(String mensaje, Articulo articulo) {
            this.mensaje = mensaje;
            this.articuloId = articulo.getId();
            this.nuevoEstado = articulo.getEstado().toString();
        }

        public String getMensaje() { return mensaje; }
        public Long getArticuloId() { return articuloId; }
        public String getNuevoEstado() { return nuevoEstado; }
    }

}
