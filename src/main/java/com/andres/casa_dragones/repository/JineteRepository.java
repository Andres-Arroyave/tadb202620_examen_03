package com.andres.casa_dragones.repository;

import com.andres.casa_dragones.model.Dragon;
import com.andres.casa_dragones.model.Jinete;

import java.util.List;
import java.util.Optional;

public interface JineteRepository {
    List<Jinete> listarTodos();
    Optional<Jinete> buscarPorId(String id);
    Optional<Jinete> buscarPorNombre(String nombre);
    List<Dragon> listarDragonesPorJineteId(String jineteId);
    String insertar(Jinete jinete);
    void actualizar(Jinete jinete);
    void eliminar(String id);
    boolean tieneRelaciones(String id);
}
