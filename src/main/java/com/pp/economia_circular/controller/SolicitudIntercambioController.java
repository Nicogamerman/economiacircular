package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.CambiarEstadoSolicitudDto;
import com.pp.economia_circular.DTO.SolicitudCreateDto;
import com.pp.economia_circular.DTO.SolicitudResponseDto;
import com.pp.economia_circular.service.SolicitudIntercambioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/intercambios")
@CrossOrigin(origins = "*")
public class SolicitudIntercambioController {

    @Autowired
    private SolicitudIntercambioService solicitudService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody SolicitudCreateDto dto) {
        try {
            SolicitudResponseDto result = solicitudService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mis-solicitudes")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> misSolicitudes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SolicitudResponseDto> result = solicitudService.listarMisSolicitudes(pageable);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/recibidas")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> recibidas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SolicitudResponseDto> result = solicitudService.listarSolicitudesRecibidas(pageable);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> historial(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SolicitudResponseDto> result = solicitudService.listarHistorial(pageable);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        try {
            SolicitudResponseDto result = solicitudService.obtener(id);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoSolicitudDto dto) {
        try {
            SolicitudResponseDto result = solicitudService.cambiarEstado(id, dto);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
