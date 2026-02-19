package com.pp.economia_circular.controller;
import com.pp.economia_circular.DTO.DashboardChartsDto;
import com.pp.economia_circular.DTO.DashboardMetricsDto;
import com.pp.economia_circular.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pp.economia_circular.DTO.DashboardMapDto;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardMetricsDto> obtenerMetricas() {
        return ResponseEntity.ok(dashboardService.obtenerMetricas());
    }

    @GetMapping("/charts")
    public ResponseEntity<DashboardChartsDto> obtenerCharts() {
        return ResponseEntity.ok(dashboardService.obtenerDatosGraficos());
    }

    @GetMapping("/map")
    public ResponseEntity<DashboardMapDto> obtenerMapa() {
        return ResponseEntity.ok(dashboardService.obtenerDatosMapa());
    }


}
