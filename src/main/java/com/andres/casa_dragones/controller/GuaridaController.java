package com.andres.casa_dragones.controller;

import com.andres.casa_dragones.model.Dragon;
import com.andres.casa_dragones.model.Guarida;
import com.andres.casa_dragones.service.GuaridaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la entidad Guarida.
 * Rutas: /api/guaridas
 * Responsabilidad: peticiones y respuestas HTTP delegando a GuaridaService.
 */
@RestController
@RequestMapping("/api/guaridas")
public class GuaridaController {

    private final GuaridaService guaridaService;

    public GuaridaController(GuaridaService guaridaService) {
        this.guaridaService = guaridaService;
    }

    @GetMapping
    public ResponseEntity<List<Guarida>> listar() {
        return ResponseEntity.ok(guaridaService.listarTodos());
    }

    @GetMapping("/{guarida_id}")
    public ResponseEntity<Guarida> buscarPorId(@PathVariable("guarida_id") String guaridaId) {
        return ResponseEntity.ok(guaridaService.buscarPorId(guaridaId));
    }

    @GetMapping("/{guarida_id}/dragones")
    public ResponseEntity<List<Dragon>> listarDragones(@PathVariable("guarida_id") String guaridaId) {
        return ResponseEntity.ok(guaridaService.listarDragonesPorGuaridaId(guaridaId));
    }

    @PostMapping
    public ResponseEntity<Guarida> crear(@RequestBody com.andres.casa_dragones.dto.GuaridaCrearDto dto) {
        Guarida creada = guaridaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping
    public ResponseEntity<Guarida> actualizar(@RequestBody Guarida guarida) {
        Guarida actualizada = guaridaService.actualizar(guarida);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{guarida_id}")
    public ResponseEntity<Void> eliminar(@PathVariable("guarida_id") String guaridaId) {
        guaridaService.eliminar(guaridaId);
        return ResponseEntity.noContent().build();
    }
}
