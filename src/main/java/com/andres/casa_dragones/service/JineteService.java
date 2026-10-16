package com.andres.casa_dragones.service;

import com.andres.casa_dragones.exception.ConflictoException;
import com.andres.casa_dragones.exception.RecursoNoEncontradoException;
import com.andres.casa_dragones.exception.ValidacionException;
import com.andres.casa_dragones.model.Dragon;
import com.andres.casa_dragones.model.Jinete;
import com.andres.casa_dragones.repository.JineteRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Capa de Servicio: validación de reglas de negocio para Jinetes.
 */
@Service
public class JineteService {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final JineteRepository jineteRepository;

    public JineteService(JineteRepository jineteRepository) {
        this.jineteRepository = jineteRepository;
    }

    public List<Jinete> listarTodos() {
        return jineteRepository.listarTodos();
    }

    public Jinete buscarPorId(String id) {
        String idValido = validarYNormalizarId(id);
        return jineteRepository.buscarPorId(idValido)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un jinete con id " + idValido));
    }

    public List<Dragon> listarDragonesPorJineteId(String id) {
        String idValido = validarYNormalizarId(id);
        if (jineteRepository.buscarPorId(idValido).isEmpty()) {
            throw new RecursoNoEncontradoException("No existe un jinete con id " + idValido);
        }
        return jineteRepository.listarDragonesPorJineteId(idValido);
    }

    public Jinete crear(Jinete jinete) {
        if (jinete == null) {
            throw new ValidacionException("Los datos del jinete son requeridos.");
        }

        String nombre = normalizarTexto(jinete.nombre(), "nombre", 100);
        String casa = normalizarTexto(jinete.casa(), "casa", 100);

        Optional<Jinete> existente = jineteRepository.buscarPorNombre(nombre);
        if (existente.isPresent()) {
            throw new ConflictoException("Ya existe un jinete con el nombre '" + nombre + "'");
        }

        Jinete jineteNormalizado = new Jinete(null, nombre, casa);
        try {
            String nuevoId = jineteRepository.insertar(jineteNormalizado);
            return new Jinete(nuevoId, nombre, casa);
        } catch (DuplicateKeyException e) {
            throw new ConflictoException("Ya existe un jinete con el nombre '" + nombre + "'");
        }
    }

    public Jinete actualizar(Jinete jinete) {
        if (jinete == null) {
            throw new ValidacionException("Los datos del jinete son requeridos.");
        }
        if (jinete.id() == null || jinete.id().isBlank()) {
            throw new ValidacionException("El campo 'id' es requerido en el cuerpo de la petición.");
        }

        String idValido = validarYNormalizarId(jinete.id());
        Jinete jineteActual = jineteRepository.buscarPorId(idValido)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un jinete con id " + idValido));

        String nombre = normalizarTexto(jinete.nombre(), "nombre", 100);
        String casa = normalizarTexto(jinete.casa(), "casa", 100);

        if (!jineteActual.nombre().equalsIgnoreCase(nombre)) {
            Optional<Jinete> existente = jineteRepository.buscarPorNombre(nombre);
            if (existente.isPresent() && !existente.get().id().equalsIgnoreCase(idValido)) {
                throw new ConflictoException("Ya existe un jinete con el nombre '" + nombre + "'");
            }
        }

        Jinete jineteActualizado = new Jinete(idValido, nombre, casa);
        try {
            jineteRepository.actualizar(jineteActualizado);
            return jineteActualizado;
        } catch (DuplicateKeyException e) {
            throw new ConflictoException("Ya existe un jinete con el nombre '" + nombre + "'");
        }
    }

    public void eliminar(String id) {
        String idValido = validarYNormalizarId(id);
        if (jineteRepository.buscarPorId(idValido).isEmpty()) {
            throw new RecursoNoEncontradoException("No existe un jinete con id " + idValido);
        }

        if (jineteRepository.tieneRelaciones(idValido)) {
            throw new ConflictoException("No se puede eliminar el jinete con id " + idValido + " porque tiene relaciones asociadas.");
        }

        jineteRepository.eliminar(idValido);
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
