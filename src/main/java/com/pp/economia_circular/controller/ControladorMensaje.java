package com.pp.economia_circular.controller;


import com.pp.economia_circular.DTO.CrearMensajeDto;
import com.pp.economia_circular.DTO.RespuestaMensajeDto;
import com.pp.economia_circular.service.ServicioMensaje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin(origins = "*")
public class ControladorMensaje {
    
    @Autowired
    private ServicioMensaje servicioMensaje;
    
    @PostMapping
    public ResponseEntity<?> enviarMensaje(@Valid @RequestBody CrearMensajeDto crearDto, @RequestParam Long remitenteId) {
        try {
            RespuestaMensajeDto mensaje = servicioMensaje.enviarMensaje(crearDto, remitenteId);
            return ResponseEntity.status(HttpStatus.CREATED).body(mensaje);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerMisMensajes(@PathVariable Long usuarioId) {
        try {
            List<RespuestaMensajeDto> mensajes = servicioMensaje.obtenerMisMensajes(usuarioId);
            return ResponseEntity.ok(mensajes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/conversacion/{usuarioId}/{otroUsuarioId}")
    public ResponseEntity<?> obtenerConversacionConUsuario(@PathVariable Long usuarioId, @PathVariable Long otroUsuarioId) {
        try {
            List<RespuestaMensajeDto> mensajes = servicioMensaje.obtenerConversacionConUsuario(usuarioId, otroUsuarioId);
            return ResponseEntity.ok(mensajes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/articulo/{articuloId}")
    public ResponseEntity<?> obtenerMensajesPorArticulo(@PathVariable Long articuloId) {
        try {
            List<RespuestaMensajeDto> mensajes = servicioMensaje.obtenerMensajesPorArticulo(articuloId);
            return ResponseEntity.ok(mensajes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/no-leidos/{usuarioId}")
    public ResponseEntity<?> obtenerMensajesNoLeidos(@PathVariable Long usuarioId) {
        try {
            List<RespuestaMensajeDto> mensajes = servicioMensaje.obtenerMensajesNoLeidos(usuarioId);
            return ResponseEntity.ok(mensajes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/no-leidos-cantidad/{usuarioId}")
    public ResponseEntity<?> contarMensajesNoLeidos(@PathVariable Long usuarioId) {
        try {
            Long cantidad = servicioMensaje.contarMensajesNoLeidos(usuarioId);
            return ResponseEntity.ok(cantidad);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{mensajeId}/leer")
    public ResponseEntity<?> marcarComoLeido(@PathVariable Long mensajeId, @RequestParam Long usuarioId) {
        try {
            RespuestaMensajeDto mensaje = servicioMensaje.marcarComoLeido(mensajeId, usuarioId);
            return ResponseEntity.ok(mensaje);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{mensajeId}")
    public ResponseEntity<?> eliminarMensaje(@PathVariable Long mensajeId, @RequestParam Long usuarioId) {
        try {
            servicioMensaje.eliminarMensaje(mensajeId, usuarioId);
            return ResponseEntity.ok("Mensaje eliminado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
