# Plan de trabajo para el agente: Examen 3 TADB (API "Casa del Dragón")

> Entrega: **miércoles 21 de octubre de 2026, 6:00 pm (GMT-5)**. Meta interna: entregar el **martes 20**.
> Entrega tardía = 0.00, sin supletorios.

## 0. Reglas de trabajo (leer primero)

1. Responde y comenta en **español**. Nombres de clases, rutas y campos en español, consistentes con el enunciado (`Dragon`, `Jinete`, `Guarida`).
2. **No declares nada como verificado si no lo ejecutaste.** Cada fase termina con evidencia real (salida de consola, respuesta de `curl`), pegada en el resumen de la fase. Si no pudiste ejecutarlo, dilo.
3. **Cuestiona las decisiones** si encuentras un error o una contradicción con el enunciado; no lo ocultes ni lo "arregles" en silencio.
4. **No cambies el esquema SQL (`01_schema.sql`) sin aprobación del estudiante.** Propón el cambio y espera respuesta.
5. **No inventes credenciales.** La configuración de la BD sale de variables de entorno que se te proporcionarán Usuario : casa_dragon_andres , Contraseña: Dragon_Andres*
6. **Commits pequeños y frecuentes**, un tema por commit, mensaje claro en español (ej. `feat(dragones): agrega GET /api/dragones`).  **no hagas un commit gigante final**. No hagas `push --force`.
7. Antes de empezar, **inspecciona el proyecto real** (`pom.xml` o `build.gradle`, versión de Java, `groupId`/paquete base). Los nombres de paquete de este documento son una propuesta; usa los que ya existan.

## 1. Contexto técnico

### Enunciado (resumen)
API REST con **patrón repositorio** sobre tres entidades (Dragones, Jinetes, Guaridas) y dos relaciones muchos a muchos (jinete↔dragón, dragón↔guarida). Todos los ID son **UUID**.

| Capa | Responsabilidad según el enunciado | Implementación en este proyecto |
|---|---|---|
| Controlador | Peticiones y respuestas HTTP | `@RestController` |
| Servicio | Validaciones de reglas de negocio | `@Service` |
| Repositorio | Acciones CRUD | `@Repository` con `JdbcTemplate` y `SimpleJdbcCall` |
| Modelo | Entidades | `record` o POJO |
| Contexto | Conexión a la BD | Clase `config/DatabaseConfig` + `application.properties` |

### Reglas que puntúan (rúbrica)
- **Consultas** (GET): `SELECT` en el repositorio.
- **Crear, actualizar, eliminar**: **solo mediante procedimientos almacenados**. Nada de `INSERT/UPDATE/DELETE` directos desde Java.
- Cada petición debe mostrar el "stack" completo: método en Controller → Service → Repository.
- **Validaciones en la capa de servicio**, no solo con anotaciones en el controlador. Los fallos se mapean a **400, 404, 409, 500**.
- **No usar `JpaRepository` ni Spring Data JPA.** Dejaría el repositorio vacío y no evidenciaría el SQL ni los procedimientos.

### Decisión tomada
Java + Spring Boot + **MySQL 8+** (cumple la bonificación: lenguaje distinto de C# y BD distinta de PostgreSQL). Proyecto ya creado en IntelliJ con: Spring Web, JDBC API, MySQL Driver, Validation. BD ya creada en un contenedor local.

### Esquema actual (`sql/01_schema.sql`, versión del estudiante)
- Tablas: `dragon(id, nombre, color, tamano)`, `jinete(id, nombre, casa)`, `guarida(id, nombre, ubicacion)`, `jinete_dragon(id, jinete_id, dragon_id)`, `dragon_guarida(id, dragon_id, guarida_id)`.
- `id` = `CHAR(36)` con `DEFAULT (UUID())`. UK sobre `nombre` en las tres entidades y sobre el par de FK en las intermedias.
- **FK sin acción referencial explícita = RESTRICT** (en MySQL equivale a `NO ACTION`). **No hay CASCADE.** Borrar un registro con relaciones falla con el error 1451 de MySQL.
- CHECK de texto no vacío en las columnas de texto. **No hay CHECK de formato UUID**: el formato lo valida el servicio.
- El script **no ejecuta `CREATE DATABASE` ni `USE`**: la BD ya existe en el contenedor. El nombre real de la BD debe confirmarlo el estudiante (se asume `casa_dragon`).
- Collation por defecto de MySQL 8 Revisa si "Syrax" y "syrax" chocan en la UK, lo que produce un 409.
- Sin tildes ni eñ en nombres de columnas: `tamano`, `ubicacion`.


### Limitaciones conocidas (documentar en el README para la sustentación)
- No se puede forzar "uno o muchos": un dragón puede no tener jinete ni guarida.
- Las tablas intermedias no guardan fechas ni orden.
- **La API no tiene endpoints de asociación**: las relaciones solo se crean por script o CSV.
- **Consecuencia de lo anterior más RESTRICT:** un registro con relaciones **no se puede eliminar por la API** (409). Solo se pueden eliminar registros recién creados con `POST` y sin relaciones.

## 2. Decisiones abiertas (preguntar al estudiante, no asumir)

| # | Tema | Opciones | Postura por defecto mientras no haya respuesta |
|---|---|---|---|
| D1 | `ON DELETE CASCADE` vs `RESTRICT` en las intermedias | CASCADE: se puede borrar cualquier entidad; se pierden relaciones que la API no puede recrear. RESTRICT: protege datos, pero se necesita un 409 por "tiene relaciones" y el dato sembrado queda no borrable | **Mantener CASCADE**; consultar al docente |
| D2 | Contrato JSON (nombres de campos) | `id`, `nombre`, `color`, `tamano` (con alias `tamaño`) | Usar este contrato y documentarlo |
| D3 | Maven o Gradle | Lo que ya tenga el proyecto | No cambiar |
| D4 | ¿Trabajo en pareja o individual? | Afecta el reparto de commits | Preguntar |

## 3. Estructura objetivo

```
<paquete.base>/
├── controller/    DragonController, JineteController, GuaridaController
├── service/       DragonService, JineteService, GuaridaService
├── repository/    DragonRepository (interfaz) + DragonRepositoryImpl (@Repository)
│                  (ídem Jinete y Guarida)
├── model/         Dragon, Jinete, Guarida
├── config/        DatabaseConfig  (capa "Contexto")
└── exception/     RecursoNoEncontradoException (404)
                   ConflictoException (409)
                   ValidacionException (400)
                   GlobalExceptionHandler (@RestControllerAdvice)
sql/               01_schema.sql, 02_datos.sql, 03_procedimientos.sql
datos/             dragon.csv, jinete.csv, guarida.csv, jinete_dragon.csv, dragon_guarida.csv
docs/              diagrama_relacional.jpg, SPRING.md, coleccion/ (Postman o Bruno)
README.md
```

## 4. Fases

### Fase 0: Verificación del entorno (hoy, antes de escribir código)
- [ ] Confirmar que el contenedor MySQL corre y que el esquema existe: `SHOW TABLES;`, `SHOW CREATE TABLE dragon;`.
- [ ] Verificar que **las 5 tablas y sus constraints** se crearon sin error (`information_schema.TABLE_CONSTRAINTS` y `CHECK_CONSTRAINTS`). Si el script tuvo errores (por ejemplo CHECK sobre columnas referenciadas), **reportarlos al estudiante**.
- [ ] Confirmar versión: `SELECT VERSION();` (debe ser 8.0.16 o superior).
- [ ] Revisar `pom.xml`/`build.gradle`: Java, Spring Boot, dependencias presentes.
- [ ] Verificar que `.gitignore` excluye `target/`/`build/`, `.idea/` y archivos con credenciales.

**Evidencia:** salida de las consultas anteriores.

### Fase 1: Contexto y arranque (capa "Contexto")
- [ ] `application.properties` con URL, usuario y contraseña **tomados de variables de entorno con valor por defecto para desarrollo**, por ejemplo `${DB_URL:jdbc:mysql://localhost:3306/casa_dragon?serverTimezone=America/Bogota}`. Ajustar puerto y credenciales al contenedor real (preguntar al estudiante).
- [ ] Clase `config/DatabaseConfig` explícita (aunque Spring autoconfigure): deja la capa Contexto visible. Puede exponer el `DataSource` o un `JdbcTemplate` con comentario de qué ahorra Spring.
- [ ] Arranque limpio: la app inicia y conecta (log de HikariCP).

**Evidencia:** log de arranque sin errores.

### Fase 2: Datos de prueba y CSV (en paralelo con la Fase 3)
- [ ] `sql/02_datos.sql` con **UUID fijos explícitos, en minúsculas** (el `DEFAULT (UUID())` solo sirve para registros creados por la API).
- [ ] 5 dragones, con jinetes y guaridas **compartidos**. Requisitos mínimos de estructura (no tiene que ser fiel a la serie):
  - al menos **un dragón con 2 o más jinetes** (ej. Vhagar: Laena y Aemond);
  - al menos **un jinete con 2 o más dragones**;
  - al menos **una guarida con 2 o más dragones**;
  - al menos **un dragón con 2 o más guaridas**.
  - Propuesta base: Syrax, Caraxes, Vhagar, Meleys, Vermax.
- [ ] Generar `02_datos.sql` y los **5 CSV desde una misma fuente** (un script o una hoja), para que nunca diverjan. Los CSV llevan encabezado y los UUID reales, incluidos los de las intermedias.
- [ ] Cargar los datos y verificar con `SELECT COUNT(*)` por tabla y con un `JOIN` de ejemplo.

**Evidencia:** conteos por tabla y un JOIN que devuelva filas cruzadas.

### Fase 3: Procedimientos almacenados (`sql/03_procedimientos.sql`)
Nueve procedimientos. Firmas propuestas (ajustar tipos al esquema):

```
sp_dragon_insertar   (IN p_nombre, IN p_color, IN p_tamano, OUT p_id CHAR(36))
sp_dragon_actualizar (IN p_id, IN p_nombre, IN p_color, IN p_tamano)
sp_dragon_eliminar   (IN p_id)
sp_jinete_insertar   (IN p_nombre, IN p_casa, OUT p_id CHAR(36))
sp_jinete_actualizar (IN p_id, IN p_nombre, IN p_casa)
sp_jinete_eliminar   (IN p_id)
sp_guarida_insertar  (IN p_nombre, IN p_ubicacion, OUT p_id CHAR(36))
sp_guarida_actualizar(IN p_id, IN p_nombre, IN p_ubicacion)
sp_guarida_eliminar  (IN p_id)
```

Criterios:
- En los `insertar`: `SET p_id = UUID(); INSERT ... VALUES (p_id, ...)`. MySQL no tiene un `LAST_INSERT_ID()` para UUID.
- **No capturar ni ocultar errores** de duplicados: dejar que el error 1062 se propague para que Spring lo traduzca a `DuplicateKeyException`.
- **No depender de `ROW_COUNT()` para saber si existía el registro.** Un `UPDATE` con valores idénticos devuelve 0 filas afectadas en MySQL. La existencia se comprueba en el servicio con un `SELECT` previo.
- Prefijo y nomenclatura consistentes (`sp_`).
- El archivo usa `DELIMITER`, que **no funciona por JDBC**. Se ejecuta con el cliente `mysql` o Workbench, por ejemplo: `docker exec -i <contenedor> mysql -u<usuario> -p casa_dragon < sql/03_procedimientos.sql`. Documentarlo en el README.

**Evidencia:** `CALL` manual de cada procedimiento (caso feliz y duplicado) con su resultado.

### Fase 4: Plantilla completa de Dragones (la entidad modelo)
Implementar **Dragones completo**, de punta a punta, antes de tocar las otras dos. Aquí se definen los patrones que se replican.

Orden recomendado:
1. `model/Dragon` (con `@JsonAlias("tamaño")` en `tamano`).
2. `repository/DragonRepository` + `DragonRepositoryImpl`:
   - `JdbcTemplate` con `RowMapper` para los `SELECT`.
   - `SimpleJdbcCall` para los procedimientos. **Crear cada `SimpleJdbcCall` una sola vez** (constructor o campo), no en cada llamada.
   - Si la detección automática de parámetros falla con MySQL, usar `.withoutProcedureColumnMetaDataAccess()` y `declareParameters(...)` explícitos.
3. `service/DragonService`: validaciones y reglas (ver §5).
4. `controller/DragonController`: solo HTTP, sin lógica de negocio.
5. `exception/*` y `GlobalExceptionHandler`.
6. Probar los 7 endpoints con `curl` (o la colección) y registrar resultados.

**Evidencia:** respuesta real de cada uno de los 7 endpoints de Dragones, incluido un caso de error por tipo (400, 404, 409).

### Fase 5: Jinetes y Guaridas
- [ ] Replicar la plantilla de Dragones en **Jinetes** (6 endpoints) y **Guaridas** (6 endpoints).
- [ ] Las consultas cruzadas (`/dragones/{id}/jinetes`, `/dragones/{id}/guaridas`, `/jinetes/{id}/dragones`, `/guaridas/{id}/dragones`) usan `JOIN` sobre las tablas intermedias.
- [ ] Hacer un commit por endpoint o por pequeño grupo de endpoints.

### Fase 6: Pruebas y colección
- [ ] Colección Postman o Bruno (Bruno es texto plano y versiona mejor en git) en `docs/coleccion/`, con los **19 endpoints** y casos de error.
- [ ] Recorrer los 19 endpoints contra los datos de la Fase 2 y guardar la evidencia (tabla endpoint → código → resultado).
- [ ] Probar el borrado de un dragón con relaciones y confirmar que se comporta según la decisión D1.

### Fase 7: Documentación y entrega
- [ ] `docs/diagrama_relacional.jpg` (MySQL Workbench: *Database → Reverse Engineer*; exportar como imagen y convertir a JPG).
- [ ] `docs/SPRING.md`: **lo que el docente pidió documentar**: qué resuelve Spring por nosotros y dónde queda en el código (ver §6).
- [ ] `README.md` (ver §7).
- [ ] Revisar historial de commits y balance entre integrantes.
- [ ] Repositorio privado `tadb202620_examen_03`, colaborador `jdrodas` invitado (jdrodas@hotmail.com).
- [ ] Correo a juand.rodasm@upb.edu.co con la URL del repo y los integrantes. **No** compartir por OneDrive ni Teams.

## 5. Contrato de la API y reglas de servicio

### Códigos HTTP esperados

| Situación | Código |
|---|---|
| GET exitoso | 200 |
| POST exitoso | 201 (cuerpo: objeto creado con su UUID) |
| PUT exitoso | 200 (cuerpo: objeto actualizado) |
| DELETE exitoso | 204 |
| Datos inválidos: nombre vacío o en blanco, campo requerido ausente, UUID mal formado, PUT sin `id`, JSON ilegible | **400** |
| El recurso no existe (incluye consultas cruzadas con un `id` inexistente) | **404** |
| Nombre duplicado (UK) | **409** |
| Error inesperado | **500**, sin exponer trazas ni SQL al cliente |

### Reglas de servicio (comunes)
- **El `id` de ruta llega como `String`** y el servicio lo valida (formato UUID) para devolver 400 con el mensaje propio, en lugar de depender de la conversión automática de Spring.
- Antes de actualizar, eliminar o consultar relaciones: comprobar que el recurso existe (si no, 404). En `GET /api/dragones/{id}/jinetes` un dragón inexistente da **404**, no `[]`.
- Normalizar el texto (`trim`) antes de validar y persistir.
- Validar longitud máxima según el esquema (100/50/150) para dar un 400 claro en vez de un error de BD.
- Capturar `DuplicateKeyException` (carrera entre la verificación y el insert) y convertirla en 409.
- El PUT lleva el `id` en el cuerpo; rutas: `PUT /api/dragones`, `/api/jinetes`, `/api/guaridas`.

### Formato de error (uniforme)
```json
{ "timestamp": "...", "status": 404, "error": "No encontrado",
  "mensaje": "No existe un dragón con id ...", "ruta": "/api/dragones/..." }
```
El `GlobalExceptionHandler` también debe cubrir `HttpMessageNotReadableException` (JSON malformado → 400) y un `Exception` genérico (→ 500, con log interno).

### Los 19 endpoints

| # | Método y ruta | Capa Repositorio |
|---|---|---|
| 1 | GET `/api/dragones` | SELECT |
| 2 | GET `/api/dragones/{dragon_id}` | SELECT |
| 3 | GET `/api/dragones/{dragon_id}/jinetes` | SELECT + JOIN |
| 4 | GET `/api/dragones/{dragon_id}/guaridas` | SELECT + JOIN |
| 5 | POST `/api/dragones` | `sp_dragon_insertar` |
| 6 | PUT `/api/dragones` | `sp_dragon_actualizar` |
| 7 | DELETE `/api/dragones/{dragon_id}` | `sp_dragon_eliminar` |
| 8 | GET `/api/jinetes` | SELECT |
| 9 | GET `/api/jinetes/{jinete_id}` | SELECT |
| 10 | GET `/api/jinetes/{jinete_id}/dragones` | SELECT + JOIN |
| 11 | POST `/api/jinetes` | `sp_jinete_insertar` |
| 12 | PUT `/api/jinetes` | `sp_jinete_actualizar` |
| 13 | DELETE `/api/jinetes/{jinete_id}` | `sp_jinete_eliminar` |
| 14 | GET `/api/guaridas` | SELECT |
| 15 | GET `/api/guaridas/{guarida_id}` | SELECT |
| 16 | GET `/api/guaridas/{guarida_id}/dragones` | SELECT + JOIN |
| 17 | POST `/api/guaridas` | `sp_guarida_insertar` |
| 18 | PUT `/api/guaridas` | `sp_guarida_actualizar` |
| 19 | DELETE `/api/guaridas/{guarida_id}` | `sp_guarida_eliminar` |

Nota: el enunciado escribe "DEL" (el verbo real es `DELETE`) y `{ecosistema_id}` en guaridas (es un typo; usar `{guarida_id}`).

### Definición de "terminado" por endpoint
1. Tiene método en Controller, Service y Repository (los tres visibles).
2. Las consultas usan `SELECT`; crear/actualizar/eliminar llaman al procedimiento.
3. Probado con `curl`/colección: caso feliz y al menos un caso de error.
4. Commit propio.

## 6. Contenido de `docs/SPRING.md` (pedido por el docente)

Tabla con **qué resuelve Spring**, **qué haríamos sin él** y **dónde queda en nuestro código** (con ruta de archivo real). Mínimo:

| Qué hace Spring | Sin framework | Dónde queda |
|---|---|---|
| Inyección de dependencias | `new` manual o fábrica | Constructores de controllers, services y repositories |
| DataSource + pool HikariCP | `DriverManager` y abrir/cerrar conexiones | `application.properties`, `config/DatabaseConfig` |
| `JdbcTemplate` (conexión, `PreparedStatement`, `ResultSet`, cierre) | JDBC manual con `try/catch/finally` | Métodos `SELECT` de `*RepositoryImpl` |
| `SimpleJdbcCall` (parámetros IN/OUT) | `CallableStatement` + `registerOutParameter` | Métodos de crear/actualizar/eliminar |
| Traducción de excepciones | Interpretar códigos de MySQL (1062) | Servicio y `GlobalExceptionHandler` |
| Enrutamiento y verbos (`@GetMapping`, etc.) | Servidor HTTP y parseo de rutas propios | Controllers |
| Serialización JSON (Jackson) | Convertir a mano objeto ↔ JSON | Modelo y controllers |
| `@RestControllerAdvice` | `try/catch` en cada endpoint | `exception/GlobalExceptionHandler` |
| Servidor embebido (Tomcat) | Instalar y desplegar un servidor | README: ejecución con `mvn spring-boot:run` |

Y una sección final: **qué NO resuelve Spring y lo implementamos nosotros** (diseño en capas, validaciones, SQL, procedimientos, modelo, datos, CSV, diagrama). Opcional: comentario breve en cada clase clave indicando qué hace Spring ahí.

## 7. Contenido de `README.md`

- Descripción, integrantes y bonificación elegida (Java + MySQL).
- **Requisitos:** JDK (versión), Maven/Gradle, MySQL **8.0.16 o superior** (no funciona en MariaDB ni en MySQL 5.7: CHECK ignorados y collation distinta), cliente `mysql`.
- **Advertencia:** `01_schema.sql` ejecuta `DROP DATABASE IF EXISTS casa_dragon` (destructivo).
- Orden de ejecución: `01_schema.sql` → `02_datos.sql` → `03_procedimientos.sql` (este último con el cliente `mysql`, por el `DELIMITER`).
- Cómo levantar el contenedor MySQL (comando real usado) y cómo configurar usuario/contraseña por variables de entorno.
- Cómo compilar y ejecutar la API y en qué puerto queda.
- Contrato JSON y ejemplos `curl` de cada endpoint.
- Tabla de códigos de error.
- Enlace a `docs/SPRING.md`, al diagrama y a la colección.
- Decisiones de diseño y **limitaciones conocidas** (ver §1), útiles para la sustentación.

## 8. Calendario sugerido

| Día | Objetivo |
|---|---|
| **Vie 16** | Fases 0 y 1; arrancar Fase 4 (`GET /api/dragones` de punta a punta como prueba de concepto) |
| **Sáb 17** | Fases 2 y 3; terminar los 7 endpoints de Dragones |
| **Dom 18** | Fase 5 (Jinetes y Guaridas) |
| **Lun 19** | Fase 6 (pruebas, errores), diagrama JPG |
| **Mar 20** | Fase 7: README, `SPRING.md`, revisión de commits, entrega y correo |
| **Mié 21** | **Solo margen.** No planificar trabajo nuevo |

**Alerta de calendario:** si la prueba de concepto del viernes (`GET /api/dragones` con controller → service → repository contra MySQL) no funciona, avisar al estudiante de inmediato.

## 9. Riesgos conocidos a vigilar

1. **`SimpleJdbcCall` con MySQL** puede fallar al descubrir los parámetros (catálogo/esquema). Solución: parámetros declarados explícitamente.
2. **Valores nulos y espacios:** una cadena en blanco debe dar 400 antes de llegar a la BD (el CHECK de la BD es la segunda línea de defensa, no la primera).
3. **UUID en mayúsculas:** la collation es insensible a mayúsculas; el CHECK de formato no la distingue. Normalizar a minúsculas en el servicio.
4. **Dependencia del `ROW_COUNT`:** ver Fase 3.
5. **Credenciales:** nunca en commits.
6. **Commit final gigante:** evitarlo (afecta el 20 % de repositorio y commits).
