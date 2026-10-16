package com.andres.casa_dragones.controller;

import com.andres.casa_dragones.model.Dragon;
import com.andres.casa_dragones.model.Jinete;
import com.andres.casa_dragones.service.JineteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la entidad Jinete.
 * Rutas: /api/jinetes
 * Responsabilidad: peticiones y respuestas HTTP delegando al JineteService.
 */
@RestController
@RequestMapping("/api/jinetes")
public class JineteController {

    private final JineteService jineteService;

    public JineteController(JineteService jineteService) {
        this.jineteService = jineteService;
    }

    @GetMapping
    public ResponseEntity<List<Jinete>> listar() {
        return ResponseEntity.ok(jineteService.listarTodos());
    }

    @GetMapping("/{jinete_id}")
    public ResponseEntity<Jinete> buscarPorId(@PathVariable("jinete_id") String jineteId) {
        return ResponseEntity.ok(jineteService.buscarPorId(jineteId));
    }

    @GetMapping("/{jinete_id}/dragones")
    public ResponseEntity<List<Dragon>> listarDragones(@PathVariable("jinete_id") String jineteId) {
        return ResponseEntity.ok(jineteService.listarDragonesPorJineteId(jineteId));
    }

    @PostMapping
    public ResponseEntity<Jinete> crear(@RequestBody com.andres.casa_dragones.dto.JineteCrearDto dto) {
        Jinete creado = jineteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping
    public ResponseEntity<Jinete> actualizar(@RequestBody Jinete jinete) {
        Jinete actualizado = jineteService.actualizar(jinete);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{jinete_id}")
    public ResponseEntity<Void> eliminar(@PathVariable("jinete_id") String jineteId) {
        jineteService.eliminar(jineteId);
        return ResponseEntity.noContent().build();
    }
}
