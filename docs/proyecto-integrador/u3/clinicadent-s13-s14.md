# ClinicaDent - Evidencias S13 y S14

Esta guía adapta las sesiones de optimización y estabilización al sistema
odontológico ClinicaDent. No reutiliza los módulos comerciales del ejemplo
BomERP.

## S13 - Optimización y preparación para producción

### Estado y medición del frontend

Las rutas de negocio de Angular ya usan `loadComponent`, por lo que Angular
genera chunks diferidos para pacientes, citas, pagos, tratamientos, atenciones y
reportes. La compilación de producción observada en este workspace reportó un
bundle inicial de **356.18 kB sin comprimir** y chunks separados por ruta. El
bundle inicial está por debajo del umbral de advertencia de 500 kB y del límite
de error de 1 MB configurados en `frontend/angular.json`; por eso se mantiene la
división existente en lugar de introducir optimizaciones no respaldadas por la
medición.

Comando para repetir la evidencia:

```powershell
cd frontend
npm run build
```

### Caché y decisión sobre Redis

- Los listados de especialidades y procedimientos usan `Cache-Control:
  private, max-age=300`, apropiado para catálogos de baja frecuencia de cambio.
- Las respuestas de API usan `Cache-Control: no-store` por defecto; los dos
  catálogos anteriores son las únicas excepciones con caché privada.
- **Redis se descarta por ahora**: no hay mediciones que demuestren una consulta
  repetida y costosa que compense la complejidad operativa de otra dependencia.
  Se reconsiderará con métricas de latencia y frecuencia de consultas; no se
  almacenarán en caché notas clínicas ni datos personales.

### Logging, monitoreo y perfil `prod`

- `TraceIdFilter` valida el `X-Trace-ID` recibido, genera uno cuando falta y lo
  incluye en la respuesta y en el MDC para correlacionar logs.
- `logback-spring.xml` mantiene el perfil `dev` más detallado y el perfil `prod`
  en nivel `INFO`.
- El perfil `prod` expone solo `/actuator/health` y `/actuator/info`; los
  detalles de salud se ocultan y Springdoc queda deshabilitado.
- `application-prod.yml` requiere credenciales y URL por variables de entorno,
  desactiva `show-sql` y usa `ddl-auto: validate`.
- La tabla `AUDITORIA_EVENTOS` debe crearse antes del primer arranque en
  producción ejecutando `CLINICADENT-backend/src/main/resources/db/manual/V1__auditoria_eventos.sql`
  en el esquema de la aplicación.

Variables requeridas por el perfil de producción:

| Variable | Propósito |
|---|---|
| `SPRING_PROFILES_ACTIVE=prod` | Seleccionar el perfil. |
| `DB_URL` | URL JDBC de Oracle. |
| `DB_USERNAME`, `DB_PASSWORD` | Credenciales del esquema de la aplicación. |
| `DB_SCHEMA` | Esquema Oracle usado por Hibernate. |
| `KEYCLOAK_ISSUER_URI` | Emisor OIDC del realm `clinicadent`. |
| `CORS_ALLOWED_ORIGINS` | Orígenes autorizados de la SPA. |
| `SERVER_PORT` | Puerto del backend; opcional, por defecto `8080`. |

El build Angular de producción usa `environment.prod.ts`, configurado para
ejecución local con los servicios de ClinicaDent. Para publicar en otro host,
hay que cambiar las URL públicas del backend y Keycloak y registrar la URL de
callback correspondiente en Keycloak. El archivo no contiene secretos.

## S14 - Integración y estabilización

### Paginación

El padrón de pacientes —el listado con mayor potencial de crecimiento— tiene un
endpoint paginado usado por la pantalla de pacientes:

```text
GET /api/v1/pacientes/paginado?page=0&size=10
```

La respuesta incluye `content`, `page`, `size`, `totalElements`, `totalPages`,
`first` y `last`. El tamaño permitido es de 1 a 100 y el orden es estable por
apellidos y nombres. El endpoint de lista anterior se conserva para no romper
los selectores de citas y atención que necesitan referencias de pacientes.

### Auditoría

El módulo `auditoria` persiste actor (claim `sub` de Keycloak), fecha, acción,
recurso, identificador y una descripción breve para operaciones de pacientes,
citas, planes de tratamiento, atenciones y pagos. No registra DNI, datos de
contacto, importes, diagnósticos ni notas clínicas. La consulta paginada
`GET /api/v1/auditoria?page=0&size=20` está restringida al rol
`ADMINISTRADOR`.

### Verificación y flujo integral

Validaciones repetibles:

```powershell
cd CLINICADENT-backend
.\mvnw.cmd test

cd ..\frontend
npm run build
```

La prueba integral funcional para ejecutar con Oracle, backend, Keycloak y SPA
disponibles es:

1. Iniciar sesión con un usuario recepcionista y registrar un paciente.
2. Programar una cita para ese paciente y confirmar que aparezca en la agenda.
3. Iniciar sesión como odontólogo, registrar la atención y verificar el cambio
   de estado de la cita.
4. Crear o actualizar el plan de tratamiento y registrar un abono con el rol de
   cajero.
5. Consultar la auditoría con el usuario administrador y comprobar actor,
   fecha, acción y entidad para cada operación, sin datos clínicos en el
   registro.
6. Confirmar que un usuario no administrador recibe `403` al consultar la
   auditoría y que los listados de pacientes cambian de página conservando el
   orden.

La evaluación automática actual cubre compilación Angular, contexto Spring y
pruebas unitarias de paginación, auditoría y reglas existentes. El flujo
anterior requiere las credenciales de los roles y servicios del entorno local;
debe registrarse como evidencia end-to-end al ejecutarlo.
