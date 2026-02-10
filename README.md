# SGD_BACKEND
Back-End para el sistema de gestion de labor docente.

## Despliegue en Railway (recomendado con Docker)

### Variables de entorno obligatorias
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL` (jdbc:postgresql://host:puerto/db)
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET` (base64)
- `PORT` (Railway lo provee)

### Variables de entorno opcionales
- `DB_SCHEMA` (por defecto: `sgd`)
- `SERVER_CONTEXT_PATH` (por defecto: `/sed-back`)
- `LIQUIBASE_ENABLED` (por defecto: `true`)
- `LIQUIBASE_CHANGELOG`
- `MULTIPART_MAX_FILE_SIZE` (por defecto: `10MB`)
- `MULTIPART_MAX_REQUEST_SIZE` (por defecto: `10MB`)
- `FILES_BASE_PATH` (por defecto: `/app/uploads`)
- `DOCUMENT_UPLOAD_DIR` (por defecto: `/app/uploads`)
- `NOTIFICATION_SERVICE_URL`
- `NOTIFICACION_CRON`, `NOTIFICACION_HABILITADA`, `NOTIFICACION_DIAS_MIN`, `NOTIFICACION_DIAS_MAX`
- `PERIODO_ACADEMICO_API_URL`
- `LOG_LEVEL_ROOT`, `LOG_LEVEL_API`, `LOG_LEVEL_LIQUIBASE`

### Pasos en Railway
1. Crear proyecto y conectar el repo.
2. Seleccionar despliegue por Dockerfile.
3. Crear un servicio de Postgres y copiar credenciales en las variables `DB_*`.
4. Agregar las variables de entorno obligatorias.
5. Desplegar y verificar el endpoint `/v3/api-docs` o el endpoint base configurado.

### Notas
- Si usas uploads, configura un volumen persistente o un storage externo. Railway no garantiza persistencia del filesystem.
- Liquibase corre con `LIQUIBASE_ENABLED=true`. Desactivalo si quieres controlar migraciones manualmente.
