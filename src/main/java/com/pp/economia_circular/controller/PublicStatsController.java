package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.DashboardMetricsDto;
import com.pp.economia_circular.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contadores agregados para la portada pública. Expone solo un subconjunto
 * de las métricas del dashboard de administración.
 */
@RestController
@RequestMapping("/api/stats")
public class PublicStatsController {

    private final DashboardService dashboardService;

    public PublicStatsController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticasPublicas() {
        DashboardMetricsDto metricas = dashboardService.obtenerMetricas();
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalUsuarios", metricas.getTotalUsuarios());
        stats.put("totalArticulos", metricas.getTotalArticulos());
        stats.put("articulosDisponibles", metricas.getArticulosDisponibles());
        stats.put("intercambiosCompletados", metricas.getIntercambiosCompletados());
        stats.put("totalVistas", metricas.getTotalVistas());
        return ResponseEntity.ok(stats);
    }
}
