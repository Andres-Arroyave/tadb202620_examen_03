package com.andres.casa_dragones.service;

import com.andres.casa_dragones.exception.ConflictoException;
import com.andres.casa_dragones.exception.RecursoNoEncontradoException;
import com.andres.casa_dragones.exception.ValidacionException;
import com.andres.casa_dragones.model.Dragon;
import com.andres.casa_dragones.model.Guarida;
import com.andres.casa_dragones.repository.GuaridaRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Capa de Servicio: validación de reglas de negocio para Guaridas.
 */
@Service
public class GuaridaService {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final GuaridaRepository guaridaRepository;

    public GuaridaService(GuaridaRepository guaridaRepository) {
        this.guaridaRepository = guaridaRepository;
    }

    public List<Guarida> listarTodos() {
        return guaridaRepository.listarTodos();
    }

    public Guarida buscarPorId(String id) {
        String idValido = validarYNormalizarId(id);
        return guaridaRepository.buscarPorId(idValido)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una guarida con id " + idValido));
    }

    public List<Dragon> listarDragonesPorGuaridaId(String id) {
        String idValido = validarYNormalizarId(id);
        if (guaridaRepository.buscarPorId(idValido).isEmpty()) {
            throw new RecursoNoEncontradoException("No existe una guarida con id " + idValido);
        }
        return guaridaRepository.listarDragonesPorGuaridaId(idValido);
    }

    public Guarida crear(Guarida guarida) {
        if (guarida == null) {
            throw new ValidacionException("Los datos de la guarida son requeridos.");
        }

        String nombre = normalizarTexto(guarida.nombre(), "nombre", 100);
        String ubicacion = normalizarTexto(guarida.ubicacion(), "ubicacion", 150);

        Optional<Guarida> existente = guaridaRepository.buscarPorNombre(nombre);
        if (existente.isPresent()) {
            throw new ConflictoException("Ya existe una guarida con el nombre '" + nombre + "'");
        }

        Guarida guaridaNormalizada = new Guarida(null, nombre, ubicacion);
        try {
            String nuevoId = guaridaRepository.insertar(guaridaNormalizada);
            return new Guarida(nuevoId, nombre, ubicacion);
        } catch (DuplicateKeyException e) {
            throw new ConflictoException("Ya existe una guarida con el nombre '" + nombre + "'");
        }
    }

    public Guarida actualizar(Guarida guarida) {
        if (guarida == null) {
            throw new ValidacionException("Los datos de la guarida son requeridos.");
        }
        if (guarida.id() == null || guarida.id().isBlank()) {
            throw new ValidacionException("El campo 'id' es requerido en el cuerpo de la petición.");
        }

        String idValido = validarYNormalizarId(guarida.id());
        Guarida guaridaActual = guaridaRepository.buscarPorId(idValido)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una guarida con id " + idValido));

        String nombre = normalizarTexto(guarida.nombre(), "nombre", 100);
        String ubicacion = normalizarTexto(guarida.ubicacion(), "ubicacion", 150);

        if (!guaridaActual.nombre().equalsIgnoreCase(nombre)) {
            Optional<Guarida> existente = guaridaRepository.buscarPorNombre(nombre);
            if (existente.isPresent() && !existente.get().id().equalsIgnoreCase(idValido)) {
                throw new ConflictoException("Ya existe una guarida con el nombre '" + nombre + "'");
            }
        }

        Guarida guaridaActualizada = new Guarida(idValido, nombre, ubicacion);
        try {
            guaridaRepository.actualizar(guaridaActualizada);
            return guaridaActualizada;
        } catch (DuplicateKeyException e) {
            throw new ConflictoException("Ya existe una guarida con el nombre '" + nombre + "'");
        }
    }

    public void eliminar(String id) {
        String idValido = validarYNormalizarId(id);
        if (guaridaRepository.buscarPorId(idValido).isEmpty()) {
            throw new RecursoNoEncontradoException("No existe una guarida con id " + idValido);
        }

        if (guaridaRepository.tieneRelaciones(idValido)) {
            throw new ConflictoException("No se puede eliminar la guarida con id " + idValido + " porque tiene relaciones asociadas.");
        }

        guaridaRepository.eliminar(idValido);
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
