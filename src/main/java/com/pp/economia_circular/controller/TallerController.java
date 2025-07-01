package com.pp.economia_circular.controller;

import com.pp.economia_circular.entity.Taller;
import com.pp.economia_circular.repositories.TallerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/talleres")
public class TallerController {

    @Autowired
    private TallerRepository tallerRepository;

    // GET - listar todos
    @GetMapping
    public List<Taller> obtenerTalleres() {
        return tallerRepository.findAll();
    }

    // GET - uno por ID
    @GetMapping("/{id}")
    public ResponseEntity<Taller> obtenerTaller(@PathVariable Long id) {
        return tallerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST - crear nuevo
    @PostMapping
    public Taller crearTaller(@RequestBody Taller taller) {
        return tallerRepository.save(taller);
    }

    // PUT - actualizar existente
    @PutMapping("/{id}")
    public ResponseEntity<Taller> actualizarTaller(@PathVariable Long id, @RequestBody Taller tallerActualizado) {
        return tallerRepository.findById(id)
                .map(taller -> {
                    taller.setNombre(tallerActualizado.getNombre());
                    taller.setDireccion(tallerActualizado.getDireccion());
                    taller.setEmail(tallerActualizado.getEmail());
                    taller.setTelefono(tallerActualizado.getTelefono());
                    taller.setTipoServicio(tallerActualizado.getTipoServicio());
                    return ResponseEntity.ok(tallerRepository.save(taller));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE - eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTaller(@PathVariable Long id) {
        return tallerRepository.findById(id)
                .map(taller -> {
                    tallerRepository.delete(taller);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
