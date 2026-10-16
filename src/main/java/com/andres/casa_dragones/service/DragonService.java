package com.andres.casa_dragones.service;

import com.andres.casa_dragones.exception.ConflictoException;
import com.andres.casa_dragones.exception.RecursoNoEncontradoException;
import com.andres.casa_dragones.exception.ValidacionException;
import com.andres.casa_dragones.model.Dragon;
import com.andres.casa_dragones.model.Guarida;
import com.andres.casa_dragones.model.Jinete;
import com.andres.casa_dragones.repository.DragonRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Capa de Servicio: validación de reglas de negocio para Dragones.
 */
@Service
public class DragonService {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final DragonRepository dragonRepository;

    public DragonService(DragonRepository dragonRepository) {
        this.dragonRepository = dragonRepository;
    }

    public List<Dragon> listarTodos() {
        return dragonRepository.listarTodos();
    }

    public Dragon buscarPorId(String id) {
        String idValido = validarYNormalizarId(id);
        return dragonRepository.buscarPorId(idValido)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un dragón con id " + idValido));
    }

    public List<Jinete> listarJinetesPorDragonId(String id) {
        String idValido = validarYNormalizarId(id);
        // Validar primero si el dragón existe (devuelve 404 si no existe)
        if (dragonRepository.buscarPorId(idValido).isEmpty()) {
            throw new RecursoNoEncontradoException("No existe un dragón con id " + idValido);
        }
        return dragonRepository.listarJinetesPorDragonId(idValido);
    }

    public List<Guarida> listarGuaridasPorDragonId(String id) {
        String idValido = validarYNormalizarId(id);
        // Validar primero si el dragón existe (devuelve 404 si no existe)
        if (dragonRepository.buscarPorId(idValido).isEmpty()) {
            throw new RecursoNoEncontradoException("No existe un dragón con id " + idValido);
        }
        return dragonRepository.listarGuaridasPorDragonId(idValido);
    }

    public Dragon crear(com.andres.casa_dragones.dto.DragonCrearDto dto) {
        if (dto == null) {
            throw new ValidacionException("Los datos del dragón son requeridos.");
        }

        String nombre = normalizarTexto(dto.nombre(), "nombre", 100);
        String color = normalizarTexto(dto.color(), "color", 50);
        String tamano = normalizarTexto(dto.tamano(), "tamano", 50);

        // Validar unicidad de nombre antes de insertar
        Optional<Dragon> existente = dragonRepository.buscarPorNombre(nombre);
        if (existente.isPresent()) {
            throw new ConflictoException("Ya existe un dragón con el nombre '" + nombre + "'");
        }

        Dragon dragonNormalizado = new Dragon(null, nombre, color, tamano);
        try {
            String nuevoId = dragonRepository.insertar(dragonNormalizado);
            return new Dragon(nuevoId, nombre, color, tamano);
        } catch (DuplicateKeyException e) {
            throw new ConflictoException("Ya existe un dragón con el nombre '" + nombre + "'");
        }
    }

    public Dragon actualizar(Dragon dragon) {
        if (dragon == null) {
            throw new ValidacionException("Los datos del dragón son requeridos.");
        }
        if (dragon.id() == null || dragon.id().isBlank()) {
            throw new ValidacionException("El campo 'id' es requerido en el cuerpo de la petición.");
        }

        String idValido = validarYNormalizarId(dragon.id());
        // Comprobar existencia previa (404 si no existe)
        Dragon dragonActual = dragonRepository.buscarPorId(idValido)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un dragón con id " + idValido));

        String nombre = normalizarTexto(dragon.nombre(), "nombre", 100);
        String color = normalizarTexto(dragon.color(), "color", 50);
        String tamano = normalizarTexto(dragon.tamano(), "tamano", 50);

        // Si cambió de nombre, comprobar que el nuevo nombre no esté ocupado por otro dragón
        if (!dragonActual.nombre().equalsIgnoreCase(nombre)) {
            Optional<Dragon> existente = dragonRepository.buscarPorNombre(nombre);
            if (existente.isPresent() && !existente.get().id().equalsIgnoreCase(idValido)) {
                throw new ConflictoException("Ya existe un dragón con el nombre '" + nombre + "'");
            }
        }

        Dragon dragonActualizado = new Dragon(idValido, nombre, color, tamano);
        try {
            dragonRepository.actualizar(dragonActualizado);
            return dragonActualizado;
        } catch (DuplicateKeyException e) {
            throw new ConflictoException("Ya existe un dragón con el nombre '" + nombre + "'");
        }
    }

    public void eliminar(String id) {
        String idValido = validarYNormalizarId(id);
        // Comprobar existencia previa (404 si no existe)
        if (dragonRepository.buscarPorId(idValido).isEmpty()) {
            throw new RecursoNoEncontradoException("No existe un dragón con id " + idValido);
        }

        // Regla RESTRICT / integridad referencial
        if (dragonRepository.tieneRelaciones(idValido)) {
            throw new ConflictoException("No se puede eliminar el dragón con id " + idValido + " porque tiene relaciones asociadas.");
        }

        dragonRepository.eliminar(idValido);
    }

    private String validarYNormalizarId(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidacionException("El ID no puede ser nulo ni vacío.");
        }
        String normalizado = id.trim().toLowerCase();
        if (!UUID_PATTERN.matcher(normalizado).matches()) {
            throw new ValidacionException("El formato del UUID es inválido: '" + id + "'");
        }
        return normalizado;
    }

    private String normalizarTexto(String valor, String campo, int maxLongitud) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException("El campo '" + campo + "' es obligatorio y no puede estar vacío.");
        }
        String limpio = valor.trim();
        if (limpio.length() > maxLongitud) {
            throw new ValidacionException("El campo '" + campo + "' no puede superar los " + maxLongitud + " caracteres.");
        }
        return limpio;
    }
}
