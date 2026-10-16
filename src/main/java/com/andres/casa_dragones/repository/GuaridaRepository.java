package com.andres.casa_dragones.repository;

import com.andres.casa_dragones.model.Dragon;
import com.andres.casa_dragones.model.Guarida;

import java.util.List;
import java.util.Optional;

public interface GuaridaRepository {
    List<Guarida> listarTodos();
    Optional<Guarida> buscarPorId(String id);
    Optional<Guarida> buscarPorNombre(String nombre);
    List<Dragon> listarDragonesPorGuaridaId(String guaridaId);
    String insertar(Guarida guarida);
    void actualizar(Guarida guarida);
    void eliminar(String id);
    boolean tieneRelaciones(String id);
}
