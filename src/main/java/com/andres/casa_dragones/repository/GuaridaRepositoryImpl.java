package com.andres.casa_dragones.repository;

import com.andres.casa_dragones.model.Dragon;
import com.andres.casa_dragones.model.Guarida;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del repositorio de Guaridas.
 * - Consultas (SELECT) mediante JdbcTemplate.
 * - Modificaciones (INSERT/UPDATE/DELETE) exclusivamente mediante procedimientos almacenados (SimpleJdbcCall).
 */
@Repository
public class GuaridaRepositoryImpl implements GuaridaRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall spInsertar;
    private final SimpleJdbcCall spActualizar;
    private final SimpleJdbcCall spEliminar;

    private final RowMapper<Guarida> guaridaRowMapper = (rs, rowNum) -> new Guarida(
            rs.getString("id"),
            rs.getString("nombre"),
            rs.getString("ubicacion")
    );

    private final RowMapper<Dragon> dragonRowMapper = (rs, rowNum) -> new Dragon(
            rs.getString("id"),
            rs.getString("nombre"),
            rs.getString("color"),
            rs.getString("tamano")
    );

    public GuaridaRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // sp_guarida_insertar
        this.spInsertar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_guarida_insertar")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_nombre", Types.VARCHAR),
                        new SqlParameter("p_ubicacion", Types.VARCHAR),
                        new SqlOutParameter("p_id", Types.CHAR)
                );

        // sp_guarida_actualizar
        this.spActualizar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_guarida_actualizar")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_id", Types.CHAR),
                        new SqlParameter("p_nombre", Types.VARCHAR),
                        new SqlParameter("p_ubicacion", Types.VARCHAR)
                );

        // sp_guarida_eliminar
        this.spEliminar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_guarida_eliminar")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_id", Types.CHAR)
                );
    }

    @Override
    public List<Guarida> listarTodos() {
        String sql = "SELECT id, nombre, ubicacion FROM guarida ORDER BY nombre";
        return jdbcTemplate.query(sql, guaridaRowMapper);
    }

    @Override
    public Optional<Guarida> buscarPorId(String id) {
        String sql = "SELECT id, nombre, ubicacion FROM guarida WHERE id = ?";
        try {
            Guarida guarida = jdbcTemplate.queryForObject(sql, guaridaRowMapper, id);
            return Optional.ofNullable(guarida);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Guarida> buscarPorNombre(String nombre) {
        String sql = "SELECT id, nombre, ubicacion FROM guarida WHERE nombre = ?";
        try {
            Guarida guarida = jdbcTemplate.queryForObject(sql, guaridaRowMapper, nombre);
            return Optional.ofNullable(guarida);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Dragon> listarDragonesPorGuaridaId(String guaridaId) {
        String sql = """
                SELECT d.id, d.nombre, d.color, d.tamano
                FROM dragon_guarida dg
                JOIN dragon d ON dg.dragon_id = d.id
                WHERE dg.guarida_id = ?
                ORDER BY d.nombre
                """;
        return jdbcTemplate.query(sql, dragonRowMapper, guaridaId);
    }

    @Override
    public String insertar(Guarida guarida) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_nombre", guarida.nombre());
        inParams.put("p_ubicacion", guarida.ubicacion());

        Map<String, Object> out = spInsertar.execute(inParams);
        return (String) out.get("p_id");
    }

    @Override
    public void actualizar(Guarida guarida) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id", guarida.id());
        inParams.put("p_nombre", guarida.nombre());
        inParams.put("p_ubicacion", guarida.ubicacion());

        spActualizar.execute(inParams);
    }

    @Override
    public void eliminar(String id) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id", id);

        spEliminar.execute(inParams);
    }

    @Override
    public boolean tieneRelaciones(String id) {
        String sql = "SELECT COUNT(*) FROM dragon_guarida WHERE guarida_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
