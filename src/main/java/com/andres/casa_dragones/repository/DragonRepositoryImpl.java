package com.andres.casa_dragones.repository;

import com.andres.casa_dragones.model.Dragon;
import com.andres.casa_dragones.model.Guarida;
import com.andres.casa_dragones.model.Jinete;
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
 * Implementación del repositorio de Dragones.
 * - Consultas (SELECT) mediante JdbcTemplate.
 * - Modificaciones (INSERT/UPDATE/DELETE) exclusivamente mediante procedimientos almacenados (SimpleJdbcCall).
 */
@Repository
public class DragonRepositoryImpl implements DragonRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall spInsertar;
    private final SimpleJdbcCall spActualizar;
    private final SimpleJdbcCall spEliminar;

    private final RowMapper<Dragon> dragonRowMapper = (rs, rowNum) -> new Dragon(
            rs.getString("id"),
            rs.getString("nombre"),
            rs.getString("color"),
            rs.getString("tamano")
    );

    private final RowMapper<Jinete> jineteRowMapper = (rs, rowNum) -> new Jinete(
            rs.getString("id"),
            rs.getString("nombre"),
            rs.getString("casa")
    );

    private final RowMapper<Guarida> guaridaRowMapper = (rs, rowNum) -> new Guarida(
            rs.getString("id"),
            rs.getString("nombre"),
            rs.getString("ubicacion")
    );

    public DragonRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // sp_dragon_insertar
        this.spInsertar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_dragon_insertar")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_nombre", Types.VARCHAR),
                        new SqlParameter("p_color", Types.VARCHAR),
                        new SqlParameter("p_tamano", Types.VARCHAR),
                        new SqlOutParameter("p_id", Types.CHAR)
                );

        // sp_dragon_actualizar
        this.spActualizar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_dragon_actualizar")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_id", Types.CHAR),
                        new SqlParameter("p_nombre", Types.VARCHAR),
                        new SqlParameter("p_color", Types.VARCHAR),
                        new SqlParameter("p_tamano", Types.VARCHAR)
                );

        // sp_dragon_eliminar
        this.spEliminar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_dragon_eliminar")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_id", Types.CHAR)
                );
    }

    @Override
    public List<Dragon> listarTodos() {
        String sql = "SELECT id, nombre, color, tamano FROM dragon ORDER BY nombre";
        return jdbcTemplate.query(sql, dragonRowMapper);
    }

    @Override
    public Optional<Dragon> buscarPorId(String id) {
        String sql = "SELECT id, nombre, color, tamano FROM dragon WHERE id = ?";
        try {
            Dragon dragon = jdbcTemplate.queryForObject(sql, dragonRowMapper, id);
            return Optional.ofNullable(dragon);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Dragon> buscarPorNombre(String nombre) {
        String sql = "SELECT id, nombre, color, tamano FROM dragon WHERE nombre = ?";
        try {
            Dragon dragon = jdbcTemplate.queryForObject(sql, dragonRowMapper, nombre);
            return Optional.ofNullable(dragon);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Jinete> listarJinetesPorDragonId(String dragonId) {
        String sql = """
                SELECT j.id, j.nombre, j.casa
                FROM jinete_dragon jd
                JOIN jinete j ON jd.jinete_id = j.id
                WHERE jd.dragon_id = ?
                ORDER BY j.nombre
                """;
        return jdbcTemplate.query(sql, jineteRowMapper, dragonId);
    }

    @Override
    public List<Guarida> listarGuaridasPorDragonId(String dragonId) {
        String sql = """
                SELECT g.id, g.nombre, g.ubicacion
                FROM dragon_guarida dg
                JOIN guarida g ON dg.guarida_id = g.id
                WHERE dg.dragon_id = ?
                ORDER BY g.nombre
                """;
        return jdbcTemplate.query(sql, guaridaRowMapper, dragonId);
    }

    @Override
    public String insertar(Dragon dragon) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_nombre", dragon.nombre());
        inParams.put("p_color", dragon.color());
        inParams.put("p_tamano", dragon.tamano());

        Map<String, Object> out = spInsertar.execute(inParams);
        return (String) out.get("p_id");
    }

    @Override
    public void actualizar(Dragon dragon) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id", dragon.id());
        inParams.put("p_nombre", dragon.nombre());
        inParams.put("p_color", dragon.color());
        inParams.put("p_tamano", dragon.tamano());

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
        String sqlJinetes = "SELECT COUNT(*) FROM jinete_dragon WHERE dragon_id = ?";
        Integer countJinetes = jdbcTemplate.queryForObject(sqlJinetes, Integer.class, id);
        if (countJinetes != null && countJinetes > 0) {
            return true;
        }

        String sqlGuaridas = "SELECT COUNT(*) FROM dragon_guarida WHERE dragon_id = ?";
        Integer countGuaridas = jdbcTemplate.queryForObject(sqlGuaridas, Integer.class, id);
        return countGuaridas != null && countGuaridas > 0;
    }
}
