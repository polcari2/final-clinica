# LP2 · Producto de Unidad 3 de Clínica Dent

**Proyecto Integrador**  
Clínica Dent · Sistema de Gestión Clínica Odontológica  
Equipo InnovateX · Grupo 8 · Sección G-2  
UPeU · Filial Juliaca · Semestre 2026-2  
[Repositorio de la entrega](https://github.com/polcari2/final-clinica)

## Asignación de módulos y responsabilidades

| Integrante | Módulo | Alcance |
|---|---|---|
| Eddy Brayan Huaman Taco | Citas y Admisión | Registro de pacientes y programación de citas con procedimientos. |
| Paul Cariapaza Sacachipana | Catálogo y Tratamientos | Catálogo de especialidades y procedimientos. Creación de planes con cabecera, detalles, costo total y saldo pendiente. |
| Erick Midwar Calsin Chura | Atenciones Clínicas | Registro de la atención clínica y cambio de estado de la cita. |
| Axcel Daniel Vargas Quispe | Pagos y Caja | Registro de abonos, distribución de pagos y control del saldo. |

## Producto evaluado

Clínica Dent utiliza Angular 22 para la interfaz, Spring Boot 4.0.8 con Java 21 para la API, Oracle para la persistencia y Keycloak para la autenticación. El flujo del sistema conecta admisión, cita, atención, tratamiento y pago. El código del [frontend](../frontend) y del [backend](../CLINICADENT-backend) está en este repositorio.

## 1. Acceso local y ejecución

| Servicio | Dirección de desarrollo | Uso |
|---|---|---|
| Angular | `http://localhost:4200` | Interfaz clínica |
| API REST | `http://localhost:8080` | Operaciones de los módulos |
| Keycloak | `http://localhost:38081` | Inicio de sesión en el realm `clinicadent` |
| Oracle | `localhost:1521/FREEPDB1` | Persistencia en el esquema `CLINICADENT_APP` |
| Actuator | `http://localhost:8080/actuator/health` | Consulta del estado del backend |

La configuración de los servicios locales está en [compose-dev.yml](../CLINICADENT-backend/compose-dev.yml). El perfil de producción se configura con variables de entorno.

## 2. Evidencias de Unidad 3

### Optimización, caché y observabilidad

- Las rutas de negocio usan `loadComponent` para cargar cada pantalla bajo demanda. La documentación del proyecto registra un bundle inicial de **356.18 kB sin comprimir**. Se puede repetir la compilación con `npm run build` en `frontend`.
- Los catálogos de especialidades y procedimientos usan `Cache-Control: private, max-age=300`. Por defecto, las demás respuestas de la API usan `Cache-Control: no-store`. La documentación deja Redis fuera de la caché de aplicación mientras no exista una medición que justifique su uso.
- El backend usa `X-Trace-ID` para correlacionar una petición con los logs y expone `/actuator/health` para consultar su estado.

### Paginación e integración

El padrón de pacientes consume `GET /api/v1/pacientes/paginado?page=0&size=10`. La API devuelve los registros de la página junto con `totalElements` y `totalPages`, ordenados por apellidos y nombres.

### Auditoría y reglas transaccionales

`AuditoriaService` registra operaciones en `AUDITORIA_EVENTOS` con actor, fecha, acción, recurso e identificador. El endpoint de consulta está reservado al rol `ADMINISTRADOR`. En Tratamientos, el plan guarda sus procedimientos como detalles y calcula el costo y el saldo. El servicio rechaza el intento de finalizar un plan con saldo pendiente.

## 3. Plan de pruebas

| Caso | Cómo comprobarlo | Resultado esperado |
|---|---|---|
| Compilación Angular | Ejecutar `npm run build` en `frontend`. | Se genera el bundle inicial y los chunks de rutas diferidas. |
| Paginación | Abrir el padrón de pacientes y cambiar de página. | La API mantiene un orden estable y devuelve los metadatos de paginación. |
| Auditoría | Crear un registro y consultar la auditoría como administrador. | El evento muestra el actor, la operación y el recurso. |
| Finalización de un plan | Ejecutar las pruebas de `TratamientoServiceImplTest`. | El servicio rechaza la finalización mientras exista saldo pendiente. |
| Flujo integral | Registrar un paciente, programar una cita, registrar atención, crear el plan y aplicar un abono. | El plan y sus detalles quedan guardados y el saldo se actualiza con el abono. |

Las pruebas del backend se ejecutan con `.\mvnw.cmd test` desde `CLINICADENT-backend`. El flujo integral se comprueba en el entorno local con la aplicación, Keycloak y Oracle activos.

## 4. Estabilización

| Caso revisado | Control aplicado | Evidencia en el repositorio |
|---|---|---|
| Finalización con saldo pendiente | Validación del cambio de estado en Tratamientos. | `noPermiteFinalizarTratamientoConSaldoPendiente` |
| Saltos entre estados de un plan | Validación de las transiciones permitidas. | `noPermiteSaltarEstadosDelTratamiento` |
| Auditoría de una operación | Registro del actor y la acción sin exponer datos clínicos. | `registraElActorYLaOperacionSinDatosClinicos` |
| Caché de las respuestas de la API | Política por defecto y excepción documentada para catálogos. | `ApiCacheControlFilterTest` |

## 5. Flujo de demostración

1. **Citas y Admisión.** Registrar un paciente y mostrar la cita programada.
2. **Atenciones Clínicas.** Registrar la atención y verificar el cambio de estado de la cita.
3. **Catálogo y Tratamientos.** Crear un plan, agregar procedimientos y comprobar el costo total y el saldo inicial.
4. **Pagos y Caja.** Registrar un abono y comprobar el saldo actualizado.
5. **Verificación.** Consultar el plan y su auditoría usando los datos de la demostración.
