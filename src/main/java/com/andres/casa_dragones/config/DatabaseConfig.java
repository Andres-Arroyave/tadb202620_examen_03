package com.andres.casa_dragones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Capa de Contexto: configuración explícita de acceso a la base de datos.
 * <p>
 * Aunque Spring Boot autoconfigura el DataSource y JdbcTemplate a partir de application.properties,
 * esta clase hace explícita la capa de Contexto requerida por el patrón arquitectónico del enunciado,
 * exponiendo el JdbcTemplate utilizado por los repositorios.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
