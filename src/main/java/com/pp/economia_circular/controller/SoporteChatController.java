package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.MensajeSoporteEnviarDto;
import com.pp.economia_circular.DTO.MensajeSoporteResponseDto;
import com.pp.economia_circular.DTO.SoporteChatCreateDto;
import com.pp.economia_circular.DTO.SoporteChatResponseDto;
import com.pp.economia_circular.entity.SoporteChat;
import com.pp.economia_circular.service.SoporteChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/soporte")
@CrossOrigin(origins = "*")
public class SoporteChatController {

    @Autowired
    private SoporteChatService soporteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> abrirChat(@Valid @RequestBody SoporteChatCreateDto dto) {
        try {
            SoporteChatResponseDto result = soporteService.abrirChat(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{chatId}/mensajes")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> enviarMensaje(
            @PathVariable Long chatId,
            @Valid @RequestBody MensajeSoporteEnviarDto dto) {
        try {
            MensajeSoporteResponseDto result = soporteService.enviarMensaje(chatId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mis-chats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> misChats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SoporteChatResponseDto> result = soporteService.listarMisChats(pageable);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarTodos(
            @RequestParam(required = false) SoporteChat.EstadoChat estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SoporteChatResponseDto> result = soporteService.listarTodos(estado, pageable);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{chatId}/mensajes")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> mensajes(@PathVariable Long chatId) {
        try {
            List<MensajeSoporteResponseDto> result = soporteService.listarMensajes(chatId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{chatId}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long chatId,
            @RequestBody Map<String, String> body) {
        try {
            SoporteChat.EstadoChat nuevoEstado = SoporteChat.EstadoChat.valueOf(body.get("estado"));
            SoporteChatResponseDto result = soporteService.cambiarEstado(chatId, nuevoEstado);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido. Valores posibles: ABIERTO, EN_PROGRESO, RESUELTO, CERRADO");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{chatId}/mensajes/leer")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> marcarLeidos(@PathVariable Long chatId) {
        try {
            int actualizados = soporteService.marcarMensajesLeidos(chatId);
            return ResponseEntity.ok(Map.of("mensajesMarcados", actualizados));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
