# SGD_BACKEND

Backend del Sistema de Gestion Docente. Expone una API REST construida con Spring Boot para administrar calendarios, actividades, necesidades, asignaciones, materias, planes, usuarios, reportes y documentos asociados al proceso de labor docente.

## Resumen

- Framework: Spring Boot 3.3.4
- Lenguaje: Java 17
- Build tool: Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Base de datos: PostgreSQL
- Migraciones: Liquibase
- Documentacion API: Springdoc OpenAPI / Swagger UI
- Seguridad: Spring Security con JWT
- Zona horaria por defecto: `America/Bogota`
- Version de referencia: `v1.0.0`

## Estructura general

El proyecto sigue una estructura clasica por capas:

- `src/main/java/co/edu/unicauca/sgd/api/controller`: controladores REST
- `src/main/java/co/edu/unicauca/sgd/api/service`: logica de negocio
- `src/main/java/co/edu/unicauca/sgd/api/repository`: acceso a datos con Spring Data JPA
- `src/main/java/co/edu/unicauca/sgd/api/domain`: entidades del dominio
- `src/main/java/co/edu/unicauca/sgd/api/dto`: contratos de entrada y salida
- `src/main/resources/db/changelog`: migraciones Liquibase
- `src/main/resources/formatos`: plantillas HTML y Excel para reportes y exportaciones
- `src/test`: pruebas unitarias y de capa web/repositorio

## Modulos funcionales

La API esta organizada alrededor de estos dominios principales:

- Calendarios y fechas academicas
- Actividades y tipos de actividad
- Cargos y labor docente
- Necesidades y asignaciones
- Materias, planes, programas y departamentos
- Usuarios, roles y detalle de usuario
- Configuracion general
- Reportes PDF y exportaciones Excel

Controladores principales detectados en el proyecto:

- `/api/calendarios`
- `/api/fechas`
- `/api/nombre-fechas`
- `/api/actividades`
- `/api/tipo-actividad`
- `/api/estado-actividad`
- `/api/cargos-actividad`
- `/api/usuario-actividad-calendario`
- `/api/necesidades`
- `/api/necesidades/estado`
- `/api/necesidades/asignaciones`
- `/api/necesidades/documentos`
- `/api/departamentos`
- `/api/departamentos/usuarios`
- `/api/programas`
- `/api/planes`
- `/api/planes/documentos`
- `/api/materias`
- `/api/usuarios`
- `/api/usuario-detalle`
- `/api/roles`
- `/api/configuraciones`
- `/api/rld`
- `/api/estadisticas`

## Requisitos

Antes de ejecutar el proyecto asegure:

- JDK 17 instalado y configurado
- PostgreSQL disponible
- Variables de entorno requeridas configuradas
- Maven no es obligatorio si usa el wrapper incluido

## Variables de entorno

### Obligatorias

- `DB_URL`: URL JDBC de PostgreSQL. Ejemplo: `jdbc:postgresql://localhost:5432/sgd`
- `DB_USER`: usuario de base de datos
- `DB_PASSWORD`: contrasena de base de datos
- `JWT_SECRET`: secreto JWT en Base64

### Recomendadas para entorno local

- `SPRING_PROFILES_ACTIVE=dev`
- `SERVER_PORT=8080`
- `SERVER_CONTEXT_PATH=/sgd-back`
- `DB_SCHEMA=sgd`
- `LIQUIBASE_ENABLED=true`

### Opcionales

- `PORT`: puerto del servidor, util en despliegues administrados
- `LIQUIBASE_CHANGELOG`: ruta alternativa del changelog
- `MULTIPART_MAX_FILE_SIZE`: por defecto `10MB`
- `MULTIPART_MAX_REQUEST_SIZE`: por defecto `10MB`
- `FILES_BASE_PATH`: ruta base de archivos
- `DOCUMENT_UPLOAD_DIR`: directorio de carga de documentos
- `NOTIFICATION_SERVICE_URL`: URL del servicio de notificaciones
- `NOTIFICACION_CRON`: cron de ejecucion de notificaciones
- `NOTIFICACION_HABILITADA`: habilita o deshabilita la tarea programada
- `NOTIFICACION_DIAS_MIN`
- `NOTIFICACION_DIAS_MAX`
- `PERIODO_ACADEMICO_API_URL`: servicio externo de periodo academico
- `LOG_LEVEL_ROOT`
- `LOG_LEVEL_API`
- `LOG_LEVEL_LIQUIBASE`

## Ejecucion local

### 1. Configurar variables de entorno

Ejemplo en PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:DB_URL="jdbc:postgresql://localhost:5432/sgd"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
$env:DB_SCHEMA="sgd"
$env:JWT_SECRET="BASE64_SECRET"
$env:SERVER_PORT="8080"
$env:SERVER_CONTEXT_PATH="/sgd-back"
$env:LIQUIBASE_ENABLED="true"
$env:FILES_BASE_PATH="C:/tmp/sgd/uploads"
$env:DOCUMENT_UPLOAD_DIR="C:/tmp/sgd/uploads"
```

Si `FILES_BASE_PATH` y `DOCUMENT_UPLOAD_DIR` no se definen en `dev`, algunas operaciones relacionadas con documentos pueden fallar.

### 2. Ejecutar la aplicacion

En Windows:

```powershell
cd SGD_BACKEND
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
cd SGD_BACKEND
./mvnw spring-boot:run
```

### 3. Verificar el arranque

Con la configuracion por defecto:

- Base URL: `http://localhost:8080/sgd-back`
- OpenAPI JSON: `http://localhost:8080/sgd-back/v3/api-docs`
- Swagger UI: `http://localhost:8080/sgd-back/swagger-ui/index.html`

## Compilacion y pruebas

Compilar el proyecto:

```powershell
cd SGD_BACKEND
.\mvnw.cmd clean package
```

Ejecutar pruebas:

```powershell
cd SGD_BACKEND
.\mvnw.cmd test
```

Notas sobre pruebas:

- Las pruebas usan configuracion en `src/test/resources/application.yml`
- En pruebas, `liquibase` esta deshabilitado
- Se incluye H2 como dependencia de test

## Base de datos y migraciones

La aplicacion usa Liquibase para administrar el esquema y los datos semilla.

- Changelog principal: `src/main/resources/db/changelog/db.changelog-master.xml`
- Esquema por defecto: `sgd`
- En `prod`, Hibernate usa `ddl-auto: none`

Si `LIQUIBASE_ENABLED=true`, las migraciones se ejecutan al iniciar la aplicacion.

## Archivos, exportaciones y reportes

El backend incluye soporte para:

- Generacion de PDF de calendario
- Generacion de PDF RLD
- Generacion de PDF de estadisticas
- Exportacion de necesidades a Excel
- Carga y descarga de documentos asociados a planes

Recursos relevantes:

- Plantillas HTML: `src/main/resources/formatos/*.html`
- Plantillas Excel: `src/main/resources/formatos/*.xlsx`
- Directorio de carga configurable con `FILES_BASE_PATH` y `DOCUMENT_UPLOAD_DIR`

## Seguridad

La aplicacion usa Spring Security y un filtro JWT.

Puntos importantes del estado actual:

- Swagger UI y OpenAPI estan habilitados
- Existe soporte para autenticacion JWT mediante `JWT_SECRET`
- En la configuracion actual, `/api/**` esta marcado como `permitAll()` con un comentario `TODO`, por lo que la API no esta completamente restringida en produccion

Revise `src/main/java/co/edu/unicauca/sgd/api/config/SecurityConfig.java` antes de desplegar en un entorno productivo.

## Docker

El proyecto incluye `Dockerfile` multietapa:

1. Compila el proyecto con Maven y Java 17
2. Genera una imagen final basada en Eclipse Temurin 17
3. Crea `/app/uploads` como directorio de trabajo para documentos

Construir imagen:

```powershell
cd SGD_BACKEND
docker build -t sgd-backend .
```

Ejecutar contenedor:

```powershell
docker run --rm -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/sgd `
  -e DB_USER=postgres `
  -e DB_PASSWORD=postgres `
  -e JWT_SECRET=BASE64_SECRET `
  -e FILES_BASE_PATH=/app/uploads `
  -e DOCUMENT_UPLOAD_DIR=/app/uploads `
  -v sgd_uploads:/app/uploads `
  sgd-backend
```

## Despliegue en Railway

Railway es una opcion valida cuando se despliega con Docker.

### Variables de entorno obligatorias

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `PORT` si el proveedor no la inyecta automaticamente

### Variables opcionales recomendadas

- `DB_SCHEMA=sgd`
- `SERVER_CONTEXT_PATH=/sgd-back`
- `LIQUIBASE_ENABLED=true`
- `FILES_BASE_PATH=/app/uploads`
- `DOCUMENT_UPLOAD_DIR=/app/uploads`
- `NOTIFICATION_SERVICE_URL`
- `PERIODO_ACADEMICO_API_URL`

### Pasos sugeridos

1. Crear el proyecto en Railway y conectar el repositorio.
2. Configurar despliegue mediante `Dockerfile`.
3. Provisionar PostgreSQL y cargar las credenciales en `DB_*`.
4. Registrar las variables de entorno restantes.
5. Desplegar y validar `/<context-path>/v3/api-docs`.

### Notas operativas

- Si se usan uploads, configure almacenamiento persistente. El filesystem efimero no garantiza permanencia.
- Mantenga `LIQUIBASE_ENABLED=true` si desea que el esquema se sincronice automaticamente al arrancar.

## Observaciones utiles

- El contexto por defecto no es `/`, sino `/sgd-back`
- La zona horaria de la aplicacion esta fijada en `America/Bogota`
- El proyecto incluye tareas programadas y ejecucion asincrona (`@EnableScheduling`, `@EnableAsync`)
- La dependencia `dotenv-java` existe en `pom.xml`, pero la configuracion principal usa variables de entorno de Spring; no hay un bootstrap visible en este repositorio que cargue `.env` automaticamente
