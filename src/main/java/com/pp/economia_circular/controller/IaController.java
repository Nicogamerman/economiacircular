package com.pp.economia_circular.controller;

import com.pp.economia_circular.DTO.ia.MensajeChatDto;
import com.pp.economia_circular.service.ia.IaException;
import com.pp.economia_circular.service.ia.IaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ia")
public class IaController {

    @Autowired
    private IaService iaService;

    /** Público: el front lo usa para mostrar u ocultar las funciones de IA. */
    @GetMapping("/estado")
    public ResponseEntity<?> estado() {
        return ResponseEntity.ok(Collections.singletonMap("disponible", iaService.disponible()));
    }

    @PostMapping(value = "/sugerir-articulo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> sugerirArticulo(@RequestParam("imagen") MultipartFile imagen) {
        try {
            return ResponseEntity.ok(iaService.sugerirArticulo(imagen));
        } catch (IaException e) {
            return error(e);
        }
    }

    /** Cuerpo: { "mensajes": [ { "rol": "user"|"assistant", "contenido": "..." } ] } */
    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> chat(@RequestBody Map<String, List<MensajeChatDto>> body) {
        try {
            String respuesta = iaService.chat(body.get("mensajes"));
            return ResponseEntity.ok(Collections.singletonMap("respuesta", respuesta));
        } catch (IaException e) {
            return error(e);
        }
    }

    static ResponseEntity<?> error(IaException e) {
        return ResponseEntity.status(e.isNoDisponible() ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
