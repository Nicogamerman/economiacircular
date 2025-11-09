package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.MensajeDto;
import com.pp.economia_circular.entity.Mensaje;
import com.pp.economia_circular.service.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin(origins = "*")
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;

    // 📩 Enviar mensaje
    @PostMapping("/enviar")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> enviarMensaje(@RequestBody Map<String, Object> payload) {
        try {
            Long destinatarioId = Long.valueOf(payload.get("destinatarioId").toString());
            Long articuloId = Long.valueOf(payload.get("articuloId").toString());
            String contenido = payload.get("contenido").toString();

            Mensaje mensaje = mensajeService.enviarMensaje(destinatarioId, articuloId, contenido);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MensajeDto(mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al enviar mensaje: " + e.getMessage());
        }
    }

    // 💬 Obtener conversación entre dos usuarios
    @GetMapping("/conversacion")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> obtenerConversacion(
            @RequestParam Long usuarioId1,
            @RequestParam Long usuarioId2) {
        try {
            List<Mensaje> mensajes = mensajeService.obtenerConversacion(usuarioId1, usuarioId2);
            List<MensajeDto> mensajesDto = mensajes.stream()
                    .map(MensajeDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(mensajesDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener conversación: " + e.getMessage());
        }
    }

    // 🧵 Obtener mensajes de un artículo
    @GetMapping("/articulo/{articuloId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> obtenerMensajesPorArticulo(@PathVariable Long articuloId) {
        try {
            List<Mensaje> mensajes = mensajeService.obtenerMensajesPorArticulo(articuloId);
            List<MensajeDto> mensajesDto = mensajes.stream()
                    .map(MensajeDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(mensajesDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener mensajes del artículo: " + e.getMessage());
        }
    }

    // ✅ Marcar mensaje como leído
    @PutMapping("/leer/{mensajeId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> marcarComoLeido(@PathVariable Long mensajeId) {
        try {
            Mensaje mensaje = mensajeService.marcarComoLeido(mensajeId);
            return ResponseEntity.ok(new MensajeDto(mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al marcar mensaje como leído: " + e.getMessage());
        }
    }

    // 🔢 Contar mensajes no leídos del usuario autenticado
    @GetMapping("/no-leidos/count")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> contarMensajesNoLeidos() {
        try {
            Long cantidad = mensajeService.contarMensajesNoLeidos();
            return ResponseEntity.ok(cantidad);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al contar mensajes no leídos: " + e.getMessage());
        }
    }

    // 📥 Obtener todos los mensajes no leídos
    @GetMapping("/no-leidos")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> obtenerMensajesNoLeidos() {
        try {
            List<Mensaje> mensajes = mensajeService.obtenerMensajesNoLeidos();
            List<MensajeDto> mensajesDto = mensajes.stream()
                    .map(MensajeDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(mensajesDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener mensajes no leídos: " + e.getMessage());
        }
    }
}
