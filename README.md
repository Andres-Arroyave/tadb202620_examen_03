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

## 7. Documentación Adicional y Colecciones

- **Análisis de Spring Boot:** Consulta el archivo [`docs/SPRING.md`](file:///c:/Users/arroy/OneDrive%20-%20UPB/Documentos/GitHub/Casa_Dragones/docs/SPRING.md) para la explicación sobre qué resuelve el framework, qué haríamos sin él y qué fue implementado a mano.
- **Colección de Pruebas (Postman / Bruno):** Ubicada en [`docs/coleccion/Casa_Dragones.postman_collection.json`](file:///c:/Users/arroy/OneDrive%20-%20UPB/Documentos/GitHub/Casa_Dragones/docs/coleccion/Casa_Dragones.postman_collection.json), lista para importar y probar los 19 endpoints y los casos de error.
- **Datos y CSV:** Los archivos planos sincronizados con la BD se encuentran en `datos/` (`dragon.csv`, `jinete.csv`, `guarida.csv`, `jinete_dragon.csv`, `dragon_guarida.csv`).
- **Diagrama Relacional:** Ubicado en `docs/diagrama_relacional.jpg`.

---

## 8. Decisiones de Diseño y Limitaciones Conocidas

1. **Integridad Foránea `RESTRICT` (Decisión D1):**  
   Las tablas intermedias no tienen `ON DELETE CASCADE`. Como la API no expone endpoints para re-asociar relaciones muchos a muchos, un registro con relaciones no puede borrarse por la API y retornará un código **409 Conflict** explicativo. Solo se pueden borrar registros creados por la API que no tengan relaciones asignadas.
2. **Validaciones en Servicio vs Controladores:**  
   Las validaciones de sintaxis UUID, campos vacíos y duplicados se realizan expresamente en la capa Service para asegurar que no se omita ninguna regla de negocio independientemente del cliente HTTP.
3. **Compatibilidad con `tamaño` y `tamano`:**  
   El modelo `Dragon` soporta tanto el campo `tamano` como el alias `"tamaño"` mediante `@JsonAlias("tamaño")` en Jackson.
