# ClinicaDent Frontend

SPA modular en Angular 22 para el backend de gestión de la clínica dental.

## Requisitos

- Node.js compatible con Angular 22 y npm.
- Backend activo en `http://localhost:8080`.
- Oracle disponible según la configuración del backend.

## Ejecutar

En una terminal, inicia el backend:

```powershell
cd CLINICADENT-backend
.\mvnw.cmd spring-boot:run
```

En otra terminal, inicia Angular:

```powershell
cd frontend
npm start
```

Abre `http://localhost:4200`. La URL base de la API se define en
`src/environments/environment.ts`. El interceptor agrega un `X-Trace-ID` a las
peticiones y el backend debe permitir CORS desde `http://localhost:4200`.

## Módulos de interfaz

- **Citas y admisión:** pacientes y citas.
- **Catálogo y tratamientos:** especialidades, procedimientos y planes.
- **Atenciones clínicas:** odontólogos y atenciones con varios detalles de piezas dentales.
- **Pagos y caja:** métodos de pago y abonos.

Un plan se crea asignándolo a un paciente y agregando uno o más procedimientos.
El frontend envía cada procedimiento y cantidad; el backend calcula importes y
saldo. Desde el listado también se pueden agregar procedimientos y actualizar el
estado del plan.

## Compilar

```powershell
npm run build
```

El build usa la configuración de producción de Angular: carga por rutas, división
de chunks, nombres con hash y presupuestos de tamaño definidos en `angular.json`.
La compilación muestra el tamaño del bundle inicial y de los chunks diferidos.

## Preparación local para producción

```powershell
npm run build
```

La configuración local de producción usa los servicios de desarrollo en
`localhost`. Antes de desplegar en otro entorno, actualiza
`src/environments/environment.prod.ts` con las URL públicas del backend y de
Keycloak, y registra la URL de callback correspondiente en el cliente
`clinicadent-frontend`. Estos valores son configuración pública del cliente; no
deben contener secretos.

El backend debe iniciarse con `SPRING_PROFILES_ACTIVE=prod` y las variables
requeridas por `application-prod.yml`. El esquema debe tener la tabla de
auditoría creada antes de arrancar; el DDL está en
`CLINICADENT-backend/src/main/resources/db/manual/V1__auditoria_eventos.sql`.
