# Plan de Trabajo y Bitácora de Desarrollo: Examen 03 TADB (API "Casa del Dragón")

> **Asignatura:** Tópicos Avanzados de Bases de Datos (TADB 202620)  
> **Estudiante:** Andrés Arroyave Londoño    
> **Bonificación elegida:** Lenguaje distinto de C# (**Java 21 + Spring Boot 4.x**) y gestor de BD distinto de PostgreSQL (**MySQL 8+**).

---

## 1. Contexto Técnico y Arquitectura

### 1.1. Modelo
API REST basada en el **patrón repositorio** sobre tres entidades principales (`dragon`, `jinete`, `guarida`) y dos tablas intermedias para relaciones muchos a muchos (`jinete_dragon`, `dragon_guarida`).

| Capa | Responsabilidad según el enunciado | Implementación en este proyecto |
|---|---|---|
| Controlador | Peticiones y respuestas HTTP | `@RestController` |
| Servicio | Validaciones de reglas de negocio | `@Service` |
| Repositorio | Acciones CRUD y persistencia | `@Repository` con `JdbcTemplate` y `SimpleJdbcCall` |
| Modelo | Entidades y DTOs | Records inmutables Java (`Dragon`, `Jinete`, `Guarida`) |
| Contexto | Conexión y pool a la BD | Clase explícita `config/DatabaseConfig` + `application.properties` |

### 1.2. Reglas de Rúbrica
- **Consultas (GET):** Sentencias `SELECT` directas en el repositorio a través de `JdbcTemplate`.
- **Crear, actualizar y eliminar (POST, PUT, DELETE):** Implementadas **exclusivamente mediante procedimientos almacenados** (`SimpleJdbcCall`). Sin `INSERT/UPDATE/DELETE` directos desde Java.
- **Trazabilidad de capas:** Cada petición recorre el flujo completo: `Controller` → `Service` → `Repository`.
- **Validaciones en capa de servicio:** Se validan sintaxis UUID, campos en blanco y restricciones de unicidad antes de persistir, mapeando los fallos a códigos **400, 404, 409 y 500**.
- **Sin Spring Data JPA ni JpaRepository:** Todo el SQL y llamadas a procedimientos son explícitas en el código fuente.

---

## 2. Decisiones de Diseño

1. **Integridad referencial `RESTRICT`:**  
   Las tablas intermedias no aplican `CASCADE`. Como la API no dispone de endpoints para re-asociar relaciones muchos a muchos, un registro con relaciones activas no puede eliminarse vía API y retorna `409 Conflict`. Solo se permite eliminar entidades sin relaciones.
2. **Contrato JSON:**  
   Se usa `id`, `nombre`, `color`, `tamano` (soportando también el alias `"tamaño"` con `@JsonAlias`).
3. **DTOs de Creación:**  
   Se diseñaron DTOs (`DragonCrearDto`, etc.) para los `POST`, omitiendo el campo `id` para que la generación del UUID recaiga 100% en el procedimiento almacenado.
4. **Collation y sensibilidad:**  
   En MySQL 8 con collation `utf8mb4_0900_ai_ci`, la unicidad de nombres previene colisiones tipo "Syrax" vs "syrax", devolviendo `409 Conflict`.

---

## 3. Bitácora de Fases y Progreso

### Fase 0: Verificación del Entorno
- [x] Contenedor MySQL 8.x corriendo en local con base de datos `casa_dragon`.
- [x] Verificación de creación de las 5 tablas y sus restricciones (`ck_*`, `uk_*`, `fk_*`).
- [x] Verificación de versión de motor: MySQL 8.0.x (soporte para expresiones por defecto `UUID()` y evaluación real de restricciones `CHECK`).
- [x] Configuración de compilación con Gradle Wrapper (`build.gradle`, Java 21, dependencias `spring-boot-starter-web`, `spring-boot-starter-jdbc`, `mysql-connector-j`, `spring-boot-starter-validation`).
- [x] Archivo `.gitignore` configurado para excluir artefactos de compilación (`build/`, `.gradle/`, `.idea/`).

**Evidencia de verificación:**
- Tablas creadas: `dragon`, `jinete`, `guarida`, `jinete_dragon`, `dragon_guarida`.
- Restricciones de no vacío activas: `char_length(trim(nombre)) > 0`, etc.

---

### Fase 1: Contexto y Conexión (Capa "Contexto")
- [x] Configuración de `application.properties` con soporte para variables de entorno y valores por defecto para desarrollo (`${DB_URL:...}`, `${DB_USER:...}`, `${DB_PASSWORD:...}`).
- [x] Clase explícita `config/DatabaseConfig` exponiendo el `DataSource` y `JdbcTemplate` para evidenciar la capa de contexto requerida por el docente.
- [x] Arranque limpio verificado con pool de conexiones HikariCP.

**Evidencia de arranque:**
```text
HikariPool-1 - Starting...
HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@...
HikariPool-1 - Start completed.
Tomcat started on port 8080 (http) with context path '/'
```

---

### Fase 2: Datos de Prueba y Archivos CSV
- [x] Creación del script `sql/02_datos.sql` con identificadores UUID textuales fijos en minúsculas.
- [x] Siembra de 5 dragones con relaciones compartidas cruzadas:
  - Dragón con 2 o más jinetes: *Vhagar* montada por Laena Velaryon y Aemond Targaryen.
  - Jinete con 2 o más dragones: *Daemon Targaryen* asociado a Caraxes y Vhagar.
  - Guarida con 2 o más dragones: *Pozo Dragón* albergando a Syrax, Meleys y Vermax.
  - Dragón con 2 o más guaridas: *Caraxes* registrado en Pozo Dragón y Monte Dragón.
- [x] Generación de los 5 archivos CSV sincronizados con los UUIDs exactos de la base de datos en `datos/`:
  - `dragon.csv`, `jinete.csv`, `guarida.csv`, `jinete_dragon.csv`, `dragon_guarida.csv`.
- [x] Verificación de carga en base de datos:
  - `SELECT COUNT(*) FROM dragon;` $\rightarrow$ 5 filas.
  - `SELECT COUNT(*) FROM jinete;` $\rightarrow$ 5 filas.
  - `SELECT COUNT(*) FROM guarida;` $\rightarrow$ 4 filas.
  - `SELECT COUNT(*) FROM jinete_dragon;` $\rightarrow$ 6 filas.
  - `SELECT COUNT(*) FROM dragon_guarida;` $\rightarrow$ 6 filas.

---

### Fase 3: Procedimientos Almacenados (`sql/03_procedimientos.sql`)
- [x] Implementación de los 9 procedimientos almacenados para las operaciones de mutación:
  - `sp_dragon_insertar`, `sp_dragon_actualizar`, `sp_dragon_eliminar`.
  - `sp_jinete_insertar`, `sp_jinete_actualizar`, `sp_jinete_eliminar`.
  - `sp_guarida_insertar`, `sp_guarida_actualizar`, `sp_guarida_eliminar`.
- [x] Generación interna del UUID mediante `SET p_id = UUID();` en los procedimientos de inserción retornando el parámetro `OUT p_id`.
- [x] Manejo de errores por unicidad delegados al motor (código de error 1062) para traducción limpia en Spring a `DuplicateKeyException` $\rightarrow$ `409 Conflict`.
- [x] Ejecución con cliente MySQL mediante sintaxis `DELIMITER //`.

**Evidencia de ejecución manual:**
```sql
CALL sp_dragon_insertar('Balerion', 'Negro', 'Colosal', @nuevo_id);
SELECT @nuevo_id; -- Retorna UUID generado
CALL sp_dragon_insertar('Balerion', 'Negro', 'Colosal', @otro_id); -- Falla con Error 1062 Duplicate entry
```

---

### Fase 4: Implementación Completa de Dragones (Módulo Base)
- [x] Modelo `Dragon` (record Java con soporte para `@JsonAlias("tamaño")`).
- [x] `DragonCrearDto` para desacoplar las altas del identificador primario.
- [x] Interfaz `DragonRepository` y su implementación `DragonRepositoryImpl`:
  - Consultas con `JdbcTemplate` y `RowMapper`.
  - Procedimientos configurados con `SimpleJdbcCall` preinstanciados en el constructor.
- [x] `DragonService`: validaciones de formato UUID, reglas de negocio y control de excepciones.
- [x] `DragonController`: endpoints HTTP según especificación REST.
- [x] Capa de excepciones centralizada: `GlobalExceptionHandler` con respuestas uniformes (`ErrorRespuesta`).
- [x] Verificación de los 7 endpoints de Dragones con `curl`.

---

### Fase 5: Implementación de Jinetes y Guaridas
- [x] Replicación del patrón en el módulo de **Jinetes** (6 endpoints: CRUD y consulta cruzada de dragones).
- [x] Replicación del patrón en el módulo de **Guaridas** (6 endpoints: CRUD y consulta cruzada de dragones).
- [x] Consultas de relaciones muchos a muchos mediante `INNER JOIN` con las tablas intermedias `jinete_dragon` y `dragon_guarida`.
- [x] Validaciones de existencia previa (si se consulta un jinete inexistente devuelve `404 Not Found` en lugar de una lista vacía).

---

### Fase 6: Pruebas, Colecciones y Diagrama ER
- [x] Colección completa de Postman en `docs/coleccion/Casa_Dragones.postman_collection.json` cubriendo los 19 endpoints y pruebas de error (`400`, `404`, `409`).
- [x] Diagrama relacional en notación Pata de Gallo exportado en formato JPG con fondo blanco: `docs/diagrama_relacional.jpg`.
- [x] Swagger UI / OpenAPI 3 configurado con `springdoc-openapi` en `/swagger-ui/index.html`.

---

### Fase 7: Documentación y Entrega
- [x] `README.md` detallado con requisitos, pasos de despliegue, arquitectura, tabla de endpoints, ejemplos de `curl` y decisiones de diseño.
- [x] `docs/SPRING.md` con el análisis solicitado por el docente: qué resuelve Spring, qué se haría sin él y qué fue implementado a mano.
- [x] Repositorio privado en GitHub con el colaborador docente invitado (`jdrodas`).
