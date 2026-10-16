package com.andres.casa_dragones.controller;

import com.andres.casa_dragones.model.Dragon;
import com.andres.casa_dragones.model.Guarida;
import com.andres.casa_dragones.model.Jinete;
import com.andres.casa_dragones.service.DragonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la entidad Dragón.
 * Rutas: /api/dragones
 * Responsabilidad: mapeo de peticiones y respuestas HTTP delegando al DragonService.
 */
@RestController
@RequestMapping("/api/dragones")
public class DragonController {

    private final DragonService dragonService;

    public DragonController(DragonService dragonService) {
        this.dragonService = dragonService;
    }

    @GetMapping
    public ResponseEntity<List<Dragon>> listar() {
        return ResponseEntity.ok(dragonService.listarTodos());
    }

    @GetMapping("/{dragon_id}")
    public ResponseEntity<Dragon> buscarPorId(@PathVariable("dragon_id") String dragonId) {
        return ResponseEntity.ok(dragonService.buscarPorId(dragonId));
    }

    @GetMapping("/{dragon_id}/jinetes")
    public ResponseEntity<List<Jinete>> listarJinetes(@PathVariable("dragon_id") String dragonId) {
        return ResponseEntity.ok(dragonService.listarJinetesPorDragonId(dragonId));
    }

    @GetMapping("/{dragon_id}/guaridas")
    public ResponseEntity<List<Guarida>> listarGuaridas(@PathVariable("dragon_id") String dragonId) {
        return ResponseEntity.ok(dragonService.listarGuaridasPorDragonId(dragonId));
    }

    @PostMapping
    public ResponseEntity<Dragon> crear(@RequestBody Dragon dragon) {
        Dragon creado = dragonService.crear(dragon);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping
    public ResponseEntity<Dragon> actualizar(@RequestBody Dragon dragon) {
        Dragon actualizado = dragonService.actualizar(dragon);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{dragon_id}")
    public ResponseEntity<Void> eliminar(@PathVariable("dragon_id") String dragonId) {
        dragonService.eliminar(dragonId);
        return ResponseEntity.noContent().build();
    }
}
