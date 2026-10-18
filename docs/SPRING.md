# Análisis de Spring Boot en el Proyecto: Qué resuelve y dónde se evidencia

Este documento complementa el desarrollo de la API **Casa del Dragón** explicando detalladamente qué problemas arquitectónicos e infraestructurales resuelve el framework Spring Boot, cómo se abordaría dicha problemática en Java estándar sin framework, y en qué archivo o clase específica de nuestro proyecto queda evidenciado.

---

## 1. Tabla Comparativa: Qué hace Spring vs Sin Framework

| Qué hace Spring | Sin framework (Java puro / Servlets) | Dónde queda en nuestro código (Ruta real) |
|---|---|---|
| **Inyección de Dependencias (IoC / DI)** | Instanciación manual con `new`, fábricas singleton o patrones de servicio complejos con alto acoplamiento. | Constructores con inyección automática en:<br>• `com.andres.casa_dragones.controller.*`<br>• `com.andres.casa_dragones.service.*`<br>• `com.andres.casa_dragones.repository.*` |
| **DataSource + Pool de Conexiones (HikariCP)** | Carga manual de `DriverManager.getConnection()`, gestión de hilos, límites de conexiones concurrentes y cierre de sockets manual. | • `src/main/resources/application.properties`<br>• [`DatabaseConfig.java`](file:///c:/Users/arroy/OneDrive%20-%20UPB/Documentos/GitHub/Casa_Dragones/src/main/java/com/andres/casa_dragones/config/DatabaseConfig.java) |
| **JdbcTemplate (Gestión de recursos JDBC)** | `PreparedStatement`, apertura/cierre de `ResultSet`, manejo repetitivo de `try-with-resources` y `SQLException`. | Consultas `SELECT` en:<br>• [`DragonRepositoryImpl.java`](file:///c:/Users/arroy/OneDrive%20-%20UPB/Documentos/GitHub/Casa_Dragones/src/main/java/com/andres/casa_dragones/repository/DragonRepositoryImpl.java)<br>• [`JineteRepositoryImpl.java`](file:///c:/Users/arroy/OneDrive%20-%20UPB/Documentos/GitHub/Casa_Dragones/src/main/java/com/andres/casa_dragones/repository/JineteRepositoryImpl.java)<br>• [`GuaridaRepositoryImpl.java`](file:///c:/Users/arroy/OneDrive%20-%20UPB/Documentos/GitHub/Casa_Dragones/src/main/java/com/andres/casa_dragones/repository/GuaridaRepositoryImpl.java) |
| **SimpleJdbcCall (Llamada a Procedimientos)** | `CallableStatement`, registro manual de tipos `registerOutParameter`, mapeo posicional de índices y lectura de cursores. | Métodos `insertar()`, `actualizar()` y `eliminar()` en:<br>• `DragonRepositoryImpl.java`<br>• `JineteRepositoryImpl.java`<br>• `GuaridaRepositoryImpl.java` |
| **Traducción Jerárquica de Excepciones** | Inspeccionar códigos de error específicos del motor MySQL (ej. Error `1062` para duplicados o `1451` para FKs). | Captura de `DuplicateKeyException` y `DataIntegrityViolationException` en:<br>• [`GlobalExceptionHandler.java`](file:///c:/Users/arroy/OneDrive%20-%20UPB/Documentos/GitHub/Casa_Dragones/src/main/java/com/andres/casa_dragones/exception/GlobalExceptionHandler.java)<br>• Servicios (`*Service.java`) |
| **Enrutamiento HTTP declarativo** | Servlets HTTP pesados, lectura de `HttpServletRequest.getPathInfo()`, `request.getMethod()`, expresiones regulares para extraer paths. | Anotaciones `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` en:<br>• `DragonController.java`<br>• `JineteController.java`<br>• `GuaridaController.java` |
| **Serialización / Deserialización JSON (Jackson)** | Lectura de streams de bytes del request, parseo manual con librerías de bajo nivel a POJOs y formateo manual del stream de salida. | Records del modelo con anotaciones como `@JsonAlias("tamaño")` en:<br>• [`Dragon.java`](file:///c:/Users/arroy/OneDrive%20-%20UPB/Documentos/GitHub/Casa_Dragones/src/main/java/com/andres/casa_dragones/model/Dragon.java)<br>• [`Jinete.java`](file:///c:/Users/arroy/OneDrive%20-%20UPB/Documentos/GitHub/Casa_Dragones/src/main/java/com/andres/casa_dragones/model/Jinete.java)<br>• [`Guarida.java`](file:///c:/Users/arroy/OneDrive%20-%20UPB/Documentos/GitHub/Casa_Dragones/src/main/java/com/andres/casa_dragones/model/Guarida.java) |
| **Gestión Global de Errores (@RestControllerAdvice)** | Bloques `try/catch` envolviendo cada método de cada Servlet, generando JSONs de error manualmente con respuestas HTTP adecuadas. | [`GlobalExceptionHandler.java`](file:///c:/Users/arroy/OneDrive%20-%20UPB/Documentos/GitHub/Casa_Dragones/src/main/java/com/andres/casa_dragones/exception/GlobalExceptionHandler.java) centralizando códigos 400, 404, 409 y 500 con estructura uniforme. |
| **Servidor Web Embebido (Apache Tomcat)** | Instalar un servidor de aplicaciones externo (Tomcat/WildFly), empaquetar archivos WAR, configurar descriptores `web.xml` y desplegar. | Ejecución autónoma como JAR o ejecutable Gradle con `.\gradlew.bat bootRun`. |

---

## 2. Qué NO resuelve Spring y fue implementado

Aunque Spring Boot proporciona el andamiaje y la infraestructura, las reglas centrales de la solución fueron diseñadas e implementadas por el desarrollador:

1. **Diseño Arquitectónico en Capas Limpias:**
   - Separación estricta de responsabilidades:
     - **Controlador:** Trata peticiones y respuestas HTTP.
     - **Servicio:** Reglas de validación, comprobación de existencia previa, reglas de negocio e integridad referencial.
     - **Repositorio:** Implementación del patrón repositorio con `JdbcTemplate` y `SimpleJdbcCall`.
     - **Modelo:** Entidades modeladas mediante Java Records inmutables.
     - **Contexto:** Configuración explícita en `DatabaseConfig`.

2. **Validaciones de Reglas de Negocio en la Capa de Servicio:**
   - Validación sintáctica del formato UUID antes de interactuar con la base de datos (evitando conversión opaca de Spring y permitiendo un código 400 con mensaje descriptivo).
   - Verificación de duplicidad de nombres con respuesta 409.
   - Normalización de cadenas (`trim`) y validación de longitudes máximas según el esquema DDL (`VARCHAR(50)`, `VARCHAR(100)`, `VARCHAR(150)`).
   - Verificación de existencia previa en búsquedas por ID y consultas cruzadas (evitando devolver arreglos vacíos `[]` cuando el padre no existe, respondiendo en su lugar 404).
   - Bloqueo de eliminación con 409 al detectar relaciones foráneas activas (`tieneRelaciones()`).

3. **Esquema Relacional y Procedimientos Almacenados:**
   - Diseño del DDL en `sql/01_schema.sql` con restricciones `CHECK`, `UNIQUE` e integridad foránea con `RESTRICT`.
   - Implementación de los 9 procedimientos almacenados en `sql/03_procedimientos.sql` (`sp_*_insertar`, `sp_*_actualizar`, `sp_*_eliminar`), encargados de generar los UUIDs para nuevas entidades y ejecutar las mutaciones sin sentencias INSERT/UPDATE/DELETE directas desde Java.

4. **Sincronización de Datos y Archivos Planos CSV:**
   - Generación de `sql/02_datos.sql` y de los 5 archivos CSV en la carpeta `datos/` sincronizados al 100% en identificadores y asociaciones muchos a muchos.
