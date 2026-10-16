package com.andres.casa_dragones.repository;

import com.andres.casa_dragones.model.Dragon;
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
 * Implementación del repositorio de Jinetes.
 * - Consultas (SELECT) mediante JdbcTemplate.
 * - Modificaciones (INSERT/UPDATE/DELETE) exclusivamente mediante procedimientos almacenados (SimpleJdbcCall).
 */
@Repository
public class JineteRepositoryImpl implements JineteRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall spInsertar;
    private final SimpleJdbcCall spActualizar;
    private final SimpleJdbcCall spEliminar;

    private final RowMapper<Jinete> jineteRowMapper = (rs, rowNum) -> new Jinete(
            rs.getString("id"),
            rs.getString("nombre"),
            rs.getString("casa")
    );

    private final RowMapper<Dragon> dragonRowMapper = (rs, rowNum) -> new Dragon(
            rs.getString("id"),
            rs.getString("nombre"),
            rs.getString("color"),
            rs.getString("tamano")
    );

    public JineteRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // sp_jinete_insertar
        this.spInsertar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_jinete_insertar")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_nombre", Types.VARCHAR),
                        new SqlParameter("p_casa", Types.VARCHAR),
                        new SqlOutParameter("p_id", Types.CHAR)
                );

        // sp_jinete_actualizar
        this.spActualizar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_jinete_actualizar")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_id", Types.CHAR),
                        new SqlParameter("p_nombre", Types.VARCHAR),
                        new SqlParameter("p_casa", Types.VARCHAR)
                );

        // sp_jinete_eliminar
        this.spEliminar = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_jinete_eliminar")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_id", Types.CHAR)
                );
    }

    @Override
    public List<Jinete> listarTodos() {
        String sql = "SELECT id, nombre, casa FROM jinete ORDER BY nombre";
        return jdbcTemplate.query(sql, jineteRowMapper);
    }

    @Override
    public Optional<Jinete> buscarPorId(String id) {
        String sql = "SELECT id, nombre, casa FROM jinete WHERE id = ?";
        try {
            Jinete jinete = jdbcTemplate.queryForObject(sql, jineteRowMapper, id);
            return Optional.ofNullable(jinete);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Jinete> buscarPorNombre(String nombre) {
        String sql = "SELECT id, nombre, casa FROM jinete WHERE nombre = ?";
        try {
            Jinete jinete = jdbcTemplate.queryForObject(sql, jineteRowMapper, nombre);
            return Optional.ofNullable(jinete);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Dragon> listarDragonesPorJineteId(String jineteId) {
        String sql = """
                SELECT d.id, d.nombre, d.color, d.tamano
                FROM jinete_dragon jd
                JOIN dragon d ON jd.dragon_id = d.id
                WHERE jd.jinete_id = ?
                ORDER BY d.nombre
                """;
        return jdbcTemplate.query(sql, dragonRowMapper, jineteId);
    }

    @Override
    public String insertar(Jinete jinete) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_nombre", jinete.nombre());
        inParams.put("p_casa", jinete.casa());

        Map<String, Object> out = spInsertar.execute(inParams);
        return (String) out.get("p_id");
    }

    @Override
    public void actualizar(Jinete jinete) {
        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_id", jinete.id());
        inParams.put("p_nombre", jinete.nombre());
        inParams.put("p_casa", jinete.casa());

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
        String sql = "SELECT COUNT(*) FROM jinete_dragon WHERE jinete_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
