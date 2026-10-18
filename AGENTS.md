# Plan de Trabajo y Guía de Interacción con IA (AGENTS.md)

> **Asignatura:** Tópicos Avanzados de Bases de Datos (TADB 202620)  
> **Estudiante:** Andrés Arroyave Londoño  
> **Propósito:** Documentar la interacción, instrucciones de prompting y colaboración con el agente de IA para el desarrollo de la API "Casa del Dragón".

---

## 1. Pautas e Instrucciones para el Asistente de IA

1. **Idioma y comunicación:** Toda la interacción, comentarios de código y documentación técnica se realizan en español.
2. **Respeto a la arquitectura manual:** No usar `Spring Data JPA` ni abstracciones mágicas. El patrón repositorio debe implementarse con `JdbcTemplate` para consultas y `SimpleJdbcCall` para procedimientos almacenados.
3. **Credenciales y seguridad:** Las credenciales nunca se queman en el código ni en el repositorio; se consumen exclusivamente de las variables de entorno (`DB_USERNAME`, `DB_PASSWORD`).
4. **Verificación estricta:** Antes de dar una tarea por terminada, se deben ejecutar los comandos o llamadas reales en el entorno y registrar la evidencia en este documento.

---

## 2. Estado de Implementación y Fases de Trabajo

### Fases Ejecutadas Manualmente por el Estudiante (Fase 0 a Fase 4)

El estudiante realizó la conceptualización, verificación y el núcleo base del proyecto previamente:

#### Fase 0: Verificación del Entorno (Realizada por el estudiante)
- [x] Contenedor MySQL 8.x corriendo en local con la base de datos `casa_dragon`.
- [x] Verificación de la versión del motor MySQL 8.0+ para soporte de expresiones `DEFAULT (UUID())` y restricciones `CHECK`.
- [x] Verificación de creación del esquema `01_schema.sql` con sus 5 tablas y constraints (`ck_*`, `uk_*`, `fk_*`).
- [x] Configuración inicial del proyecto con Gradle Wrapper (`gradlew.bat`), Java 21 y dependencias en `build.gradle`.

#### Fase 1: Contexto y Conexión (Realizada por el estudiante)
- [x] Configuración de variables de entorno para la conexión en `src/main/resources/application.properties` vinculadas a `DB_USERNAME` y `DB_PASSWORD`.
- [x] Implementación de la clase explícita `config/DatabaseConfig` para exponer el `JdbcTemplate` y evidenciar la capa de Contexto requerida.
- [x] Comprobación del pool de conexiones HikariCP.

#### Fase 2: Datos de Prueba y Archivos CSV (Realizada por el estudiante)
- [x] Diseño y ejecución del script `sql/02_datos.sql` con identificadores UUID fijos en minúsculas.
- [x] Definición de los 5 dragones y sus relaciones compartidas cruzadas (Vhagar con múltiples jinetes, Daemon con múltiples dragones, Pozo Dragón con múltiples dragones, etc.).
- [x] Generación y sincronización de los 5 archivos planos en la carpeta `datos/` (`dragon.csv`, `jinete.csv`, `guarida.csv`, `jinete_dragon.csv`, `dragon_guarida.csv`).

#### Fase 3: Procedimientos Almacenados (Realizada por el estudiante)
- [x] Creación de los 9 procedimientos almacenados en `sql/03_procedimientos.sql` (`sp_*_insertar`, `sp_*_actualizar`, `sp_*_eliminar`).
- [x] Manejo de generación de UUID mediante `SET p_id = UUID();` en inserciones y propagación de errores de duplicados (código 1062) hacia Spring.
- [x] Ejecución del script mediante cliente MySQL Workbench / consola con sintaxis `DELIMITER //`.

#### Fase 4: Módulo Base de Dragones (Realizada por el estudiante)
- [x] Implementación del record inmutable `model/Dragon` (con `@JsonAlias("tamaño")`).
- [x] Implementación de `repository/DragonRepository` e implementación con `JdbcTemplate` y `SimpleJdbcCall`.
- [x] Implementación de `service/DragonService` con validaciones de UUID y reglas de negocio.
- [x] Implementación de `controller/DragonController` con los 7 endpoints REST.
- [x] Configuración del manejador global de excepciones `GlobalExceptionHandler` con respuestas de error uniformes (`400`, `404`, `409`, `500`).

---

### Fases Asistidas por IA y Finalización (Fase 5 a Fase 7)

A partir de la plantilla y arquitectura base establecida por el estudiante, se colaboró con la IA para extender y completar los siguientes entregables:

#### Fase 5: Implementación de Jinetes y Guaridas (Asistida por IA)
- [x] Replicación del patrón de Dragones en el módulo de **Jinetes** (Controller, Service, Repository) completando los 6 endpoints (CRUD y consulta cruzada `/api/jinetes/{id}/dragones`).
- [x] Replicación del patrón en el módulo de **Guaridas** (Controller, Service, Repository) completando los 6 endpoints (CRUD y consulta cruzada `/api/guaridas/{id}/dragones`).
- [x] Implementación de DTOs de creación (`DragonCrearDto`, `JineteCrearDto`, `GuaridaCrearDto`) para desacoplar el `id` en las peticiones `POST`.

#### Fase 6: Colección de Pruebas y Diagrama ER (Asistida por IA)
- [x] Creación de la colección completa de Postman en `docs/coleccion/Casa_Dragones.postman_collection.json` con los 19 endpoints y pruebas de casos de error.
- [x] Generación y renderizado del diagrama relacional en notación Pata de Gallo (Crow's foot) con fondo blanco en `docs/diagrama_relacional.jpg`.
- [x] Integración de Swagger UI / OpenAPI interactivo en `/swagger-ui/index.html`.

#### Fase 7: Documentación Técnica de Cierre (Asistida por IA)
- [x] Elaboración de `docs/SPRING.md` con el análisis crítico de la intervención del framework frente al desarrollo artesanal.
- [x] Refinamiento de `README.md` con guías de despliegue, Swagger, Postman, diagrama y ejemplos con cURL.
- [x] Mantenimiento de bitácora y registro de prompts e interacción en `AGENTS.md`.
