# Casa del Dragón - API REST con Patrón Repositorio

> **TADB 202620 - Examen 03**  
> **Bonificación:** Implementado en **Java 21 + Spring Boot 4.x** con base de datos **MySQL 8+** (lenguaje distinto de C# y motor distinto de PostgreSQL).

---

## 1. Integrantes
- **Estudiante:** Andrés Arroyo

---

## 2. Requisitos Previos

- **Java JDK:** Versión 21 o superior.
- **Gradle:** Wrapper incluido en el repositorio (`gradlew` / `gradlew.bat`).
- **MySQL:** Versión 8.0.16 o superior (necesaria para soporte de expresiones por defecto `UUID()` y evaluación de restricciones `CHECK`).
- **Docker:** Para levantar o gestionar el contenedor local MySQL (opcional si se dispone de MySQL Server nativo).
- **Cliente MySQL / Workbench / Postman / Bruno**.

---

## 3. Configuración y Despliegue de la Base de Datos

### 3.1. Contenedor MySQL
El proyecto se diseñó para conectarse a un contenedor MySQL local. Para crearlo y levantarlo con las credenciales esperadas:
```bash
docker run -d --name mysql-dev -p 3306:3306 -e MYSQL_ROOT_PASSWORD=Andres1018. -e MYSQL_DATABASE=casa_dragon -e MYSQL_USER=casa_dragon_andres -e MYSQL_PASSWORD=Dragon_Andres* mysql:latest
```

### 3.2. Orden de Ejecución de Scripts SQL
Los scripts se encuentran en la carpeta `sql/` y deben ejecutarse en el siguiente orden:

1. **`sql/01_schema.sql`**: Crea las tablas principales (`dragon`, `jinete`, `guarida`), las intermedias de relación muchos a muchos (`jinete_dragon`, `dragon_guarida`) con `RESTRICT`, llaves primarias en formato UUID `CHAR(36)`, restricciones de unicidad y restricciones `CHECK`.
2. **`sql/02_datos.sql`**: Inserta los datos iniciales de prueba con identificadores UUID fijos.
3. **`sql/03_procedimientos.sql`**: Crea los 9 procedimientos almacenados para las operaciones de mutación (`sp_*_insertar`, `sp_*_actualizar`, `sp_*_eliminar`).

> [!IMPORTANT]
> Debido al uso de la directiva `DELIMITER`, el script `03_procedimientos.sql` debe ejecutarse mediante el cliente de consola MySQL o MySQL Workbench:
> ```bash
> cmd.exe /c "docker exec -i mysql-dev mysql -u casa_dragon_andres -pDragon_Andres* casa_dragon < sql/03_procedimientos.sql"
> ```

---

## 4. Ejecución de la API

La aplicación se ejecuta a través del Gradle Wrapper. La configuración de conexión a la base de datos se toma de variables de entorno con valores por defecto preparados para el entorno de desarrollo:

- `DB_URL`: `${DB_URL:jdbc:mysql://localhost:3306/casa_dragon?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=America/Bogota}`
- `DB_USER`: `${DB_USER:casa_dragon_andres}`
- `DB_PASSWORD`: `${DB_PASSWORD:Dragon_Andres*}`

Para compilar y correr:
```bash
# Windows
.\gradlew.bat bootRun

# Linux / macOS
./gradlew bootRun
```
La API queda escuchando en el puerto estándar: **`http://localhost:8080`**.

---

## 5. Arquitectura del Proyecto (Patrón Repositorio en Capas)

El proyecto respeta estrictamente la separación de responsabilidades solicitada en la rúbrica:

```
com.andres.casa_dragones/
├── config/           # DatabaseConfig: Exposición explícita de la capa de contexto
├── controller/       # DragonController, JineteController, GuaridaController: Capa HTTP
├── service/          # DragonService, JineteService, GuaridaService: Validaciones y lógica de negocio
├── repository/       # Interfaces y DragonRepositoryImpl, JineteRepositoryImpl, GuaridaRepositoryImpl
│                     # (Consultas con JdbcTemplate y mutaciones con SimpleJdbcCall)
├── model/            # Records inmutables: Dragon, Jinete, Guarida
└── exception/        # RecursoNoEncontradoException (404), ConflictoException (409),
                      # ValidacionException (400) y GlobalExceptionHandler (@RestControllerAdvice)
```

- **Consultas (GET):** Realizadas mediante sentencias `SELECT` en el repositorio con `JdbcTemplate`.
- **Creación, actualización y eliminación (POST, PUT, DELETE):** Ejecutadas **únicamente a través de procedimientos almacenados** utilizando `SimpleJdbcCall`. No existen `INSERT`, `UPDATE` ni `DELETE` directos desde Java.
- **Sin Spring Data JPA ni JpaRepository:** Toda la interacción SQL y procedimientos es visible en el código.

---

## 6. Contrato de la API y Lista de Endpoints

### Formato de Errores Uniforme
Todos los errores devuelven un objeto JSON consistente:
```json
{
  "timestamp": "2026-10-16T14:46:14.3758472",
  "status": 404,
  "error": "No encontrado",
  "mensaje": "No existe un dragón con id 00000000-0000-0000-0000-000000000000",
  "ruta": "/api/dragones/00000000-0000-0000-0000-000000000000"
}
```

### Tabla de Códigos HTTP
| Código | Significado en esta API |
|---|---|
| **200 OK** | Solicitud exitosa (GET o PUT). |
| **201 Created** | Recurso creado exitosamente con su UUID (POST). |
| **204 No Content** | Recurso eliminado exitosamente (DELETE). |
| **400 Bad Request** | UUID con formato inválido, campos requeridos vacíos o JSON malformado. |
| **404 Not Found** | El recurso o su relación no existe. |
| **409 Conflict** | Nombre duplicado (violación de unicidad) o intento de borrado de un registro con relaciones activas (`RESTRICT`). |
| **500 Internal Error** | Error inesperado del servidor, sin exponer detalles internos ni SQL. |

---

### Los 19 Endpoints de la API

| # | Método | Ruta | Repositorio | Descripción |
|---|---|---|---|---|
| 1 | `GET` | `/api/dragones` | `SELECT` | Listar todos los dragones |
| 2 | `GET` | `/api/dragones/{dragon_id}` | `SELECT` | Buscar dragón por UUID |
| 3 | `GET` | `/api/dragones/{dragon_id}/jinetes` | `SELECT + JOIN` | Listar jinetes de un dragón |
| 4 | `GET` | `/api/dragones/{dragon_id}/guaridas` | `SELECT + JOIN` | Listar guaridas de un dragón |
| 5 | `POST` | `/api/dragones` | `sp_dragon_insertar` | Crear nuevo dragón |
| 6 | `PUT` | `/api/dragones` | `sp_dragon_actualizar` | Actualizar datos de un dragón |
| 7 | `DELETE`| `/api/dragones/{dragon_id}` | `sp_dragon_eliminar` | Eliminar dragón sin relaciones |
| 8 | `GET` | `/api/jinetes` | `SELECT` | Listar todos los jinetes |
| 9 | `GET` | `/api/jinetes/{jinete_id}` | `SELECT` | Buscar jinete por UUID |
| 10 | `GET` | `/api/jinetes/{jinete_id}/dragones` | `SELECT + JOIN` | Listar dragones montados por un jinete |
| 11 | `POST` | `/api/jinetes` | `sp_jinete_insertar` | Crear nuevo jinete |
| 12 | `PUT` | `/api/jinetes` | `sp_jinete_actualizar` | Actualizar datos de un jinete |
| 13 | `DELETE`| `/api/jinetes/{jinete_id}` | `sp_jinete_eliminar` | Eliminar jinete sin relaciones |
| 14 | `GET` | `/api/guaridas` | `SELECT` | Listar todas las guaridas |
| 15 | `GET` | `/api/guaridas/{guarida_id}` | `SELECT` | Buscar guarida por UUID |
| 16 | `GET` | `/api/guaridas/{guarida_id}/dragones`| `SELECT + JOIN` | Listar dragones alojados en la guarida |
| 17 | `POST` | `/api/guaridas` | `sp_guarida_insertar` | Crear nueva guarida |
| 18 | `PUT` | `/api/guaridas` | `sp_guarida_actualizar` | Actualizar datos de una guarida |
| 19 | `DELETE`| `/api/guaridas/{guarida_id}` | `sp_guarida_eliminar` | Eliminar guarida sin relaciones |

---

## 7. Documentación Adicional, Swagger y Colección de Pruebas

### 7.1. Documentación Interactiva OpenAPI / Swagger UI
La API cuenta con Swagger UI integrado mediante `springdoc-openapi-starter-webmvc-ui`. Una vez la aplicación esté corriendo, se puede interactuar con los 19 endpoints y consultar sus esquemas JSON directamente en el navegador:
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Especificación OpenAPI (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### 7.2. Colección de Pruebas (Postman / Bruno)
En la carpeta [`docs/coleccion/`](docs/coleccion/) se incluye la colección completa [`Casa_Dragones.postman_collection.json`](docs/coleccion/Casa_Dragones.postman_collection.json).

**Instrucciones de uso en Postman:**
1. Abrir Postman y hacer clic en **Import** (esquina superior izquierda).
2. Arrastrar o seleccionar el archivo `docs/coleccion/Casa_Dragones.postman_collection.json`.
3. La colección incluye variables preconfiguradas:
   - `baseUrl`: `http://localhost:8080`
   - `dragonId`, `jineteId`, `guaridaId`: UUIDs válidos correspondientes a los datos iniciales sembrados en `sql/02_datos.sql`.
4. Contiene 4 carpetas con peticiones listas para ejecutar:
   - **Dragones:** 7 peticiones (CRUD y consultas cruzadas con jinetes y guaridas).
   - **Jinetes:** 6 peticiones (CRUD y consulta cruzada con dragones).
   - **Guaridas:** 6 peticiones (CRUD y consulta cruzada con dragones).
   - **Casos de Error:** Peticiones para verificar respuestas `400 Bad Request` (UUID inválido, campos vacíos), `404 Not Found` (recursos inexistentes) y `409 Conflict` (nombres duplicados o borrado restringido).

### 7.3. Diagrama Entidad-Relación (Pata de Gallo)
El modelo relacional visual se encuentra disponible en:
- Archivo: [`docs/diagrama_relacional.jpg`](docs/diagrama_relacional.jpg)
- Notación: Pata de Gallo (*Crow's Foot*), con especificación de cardinalidades `0..N` y `1..1` para las tablas intermedias, restricciones de unicidad (`UK`), claves primarias (`PK`), foráneas (`FK`) y tipos de datos.

### 7.4. Análisis de Arquitectura Spring Boot (`SPRING.md`)
En cumplimiento de las instrucciones de la asignatura, se elaboró un análisis detallado en [`docs/SPRING.md`](docs/SPRING.md) que explica:
- Qué resuelve Spring Boot por nosotros (Inyección de dependencias, HikariCP, `JdbcTemplate`, `SimpleJdbcCall`, `@RestControllerAdvice`, etc.).
- Qué hubiese implicado programar todo esto sin framework a mano con JDBC puro y sockets.
- Dónde queda reflejado cada concepto en el código fuente de este repositorio.
- Qué componentes **NO resuelve Spring** y requirieron diseño artesanal de negocio (validaciones de servicio, scripts DDL/DML, DTOs y Stored Procedures).

---

## 8. Ejemplos de Peticiones con cURL

A continuación se presentan ejemplos reales para interactuar con la API por terminal:

```bash
# 1. Listar todos los dragones (200 OK)
curl -X GET http://localhost:8080/api/dragones

# 2. Consultar un dragón por UUID (200 OK)
curl -X GET http://localhost:8080/api/dragones/c01ce52a-6d84-4aea-a4d0-091eb54dbb7a

# 3. Consultar jinetes de un dragón (200 OK)
curl -X GET http://localhost:8080/api/dragones/c01ce52a-6d84-4aea-a4d0-091eb54dbb7a/jinetes

# 4. Crear un dragón mediante Stored Procedure (201 Created)
curl -X POST http://localhost:8080/api/dragones \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Balerion", "color": "Negro", "tamano": "Colosal"}'

# 5. Actualizar un dragón mediante Stored Procedure (200 OK)
curl -X PUT http://localhost:8080/api/dragones \
  -H "Content-Type: application/json" \
  -d '{"id": "c01ce52a-6d84-4aea-a4d0-091eb54dbb7a", "nombre": "Syrax", "color": "Amarillo Dorado", "tamano": "Grande"}'

# 6. Eliminar un dragón recién creado sin relaciones (204 No Content)
curl -X DELETE http://localhost:8080/api/dragones/<UUID_DEL_DRAGON>

# 7. Caso de Error: Nombre duplicado (409 Conflict)
curl -X POST http://localhost:8080/api/dragones \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Syrax", "color": "Dorado", "tamano": "Grande"}'
```

---

## 9. Decisiones de Diseño y Limitaciones Conocidas

1. **Integridad Foránea `RESTRICT` (Decisión D1):**  
   Las tablas intermedias no tienen `ON DELETE CASCADE`. Como la API no expone endpoints para re-asociar relaciones muchos a muchos, un registro con relaciones no puede borrarse por la API y retornará un código **409 Conflict** explicativo. Solo se pueden borrar registros creados por la API que no tengan relaciones asignadas.
2. **Validaciones en Servicio vs Controladores:**  
   Las validaciones de sintaxis UUID, campos vacíos y duplicados se realizan expresamente en la capa Service para asegurar que no se omita ninguna regla de negocio independientemente del cliente HTTP.
3. **Compatibilidad con `tamaño` y `tamano`:**  
   El modelo `Dragon` soporta tanto el campo `tamano` como el alias `"tamaño"` mediante `@JsonAlias("tamaño")` en Jackson.
4. **DTOs de Creación:**  
   Se diseñaron DTOs específicos (`DragonCrearDto`, `JineteCrearDto`, `GuaridaCrearDto`) para las peticiones `POST`, garantizando que el cliente no envíe un `id` y que este sea generado exclusivamente por el Stored Procedure en la base de datos.
