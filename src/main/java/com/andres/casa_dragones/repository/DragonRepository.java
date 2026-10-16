package com.andres.casa_dragones.repository;

import com.andres.casa_dragones.model.Dragon;
import com.andres.casa_dragones.model.Guarida;
import com.andres.casa_dragones.model.Jinete;

import java.util.List;
import java.util.Optional;

public interface DragonRepository {
    List<Dragon> listarTodos();
    Optional<Dragon> buscarPorId(String id);
    Optional<Dragon> buscarPorNombre(String nombre);
    List<Jinete> listarJinetesPorDragonId(String dragonId);
    List<Guarida> listarGuaridasPorDragonId(String dragonId);
    String insertar(Dragon dragon);
    void actualizar(Dragon dragon);
    void eliminar(String id);
    boolean tieneRelaciones(String id);
}
