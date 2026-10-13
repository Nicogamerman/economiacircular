package com.pp.economia_circular.controller;

import com.pp.economia_circular.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*")
public class MetricsController {

    @Autowired
    private MetricsService metricsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardSummary() {
        try {
            return ResponseEntity.ok(metricsService.getDashboardSummary());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/timeline/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> timelineUsers(@RequestParam(defaultValue = "12") int months) {
        try {
            return ResponseEntity.ok(metricsService.timelineUsuarios(months));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/timeline/articles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> timelineArticles(@RequestParam(defaultValue = "12") int months) {
        try {
            return ResponseEntity.ok(metricsService.timelineArticulos(months));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/timeline/exchanges")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> timelineExchanges(@RequestParam(defaultValue = "12") int months) {
        try {
            return ResponseEntity.ok(metricsService.timelineIntercambios(months));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/distribution/articles-by-category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> articlesByCategory() {
        try {
            return ResponseEntity.ok(metricsService.distribucionPorCategoria());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/distribution/articles-by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> articlesByStatus() {
        try {
            return ResponseEntity.ok(metricsService.distribucionPorEstado());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/geo/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> geoEvents() {
        try {
            return ResponseEntity.ok(metricsService.geoEventos());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/geo/recycling-centers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> geoRecyclingCenters() {
        try {
            return ResponseEntity.ok(metricsService.geoCentrosReciclaje());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
