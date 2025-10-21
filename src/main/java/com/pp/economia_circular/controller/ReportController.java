package com.pp.economia_circular.controller;


import com.pp.economia_circular.DTO.ReportDto;
import com.pp.economia_circular.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateUserReport() {
        try {
            ReportDto report = reportService.generateUserReport();
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/articles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateArticleReport() {
        try {
            ReportDto report = reportService.generateArticleReport();
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/top-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateTopUsersReport() {
        try {
            ReportDto report = reportService.generateTopUsersReport();
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/top-articles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateTopArticlesReport() {
        try {
            ReportDto report = reportService.generateTopArticlesReport();
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/communication")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateCommunicationReport() {
        try {
            ReportDto report = reportService.generateCommunicationReport();
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/environmental-impact")
    public ResponseEntity<?> generateEnvironmentalImpactReport() {
        try {
            ReportDto report = reportService.generateEnvironmentalImpactReport();
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
