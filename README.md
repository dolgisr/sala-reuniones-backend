# sala-reuniones-backend

API REST para reservar salas de reuniones, con Java 17 y Spring Boot 3.5.

**Stack:** Java 17 · Spring Boot 3.5 · PostgreSQL 17 · Liquibase · Spring Security (JWT) · springdoc-openapi (Swagger) · Lombok · JUnit 5, Mockito y Testcontainers

## Requisitos

- Java 17 o superior
- Docker (para PostgreSQL y para los tests de integración)

## Cómo ejecutarlo

```bash
cp .env.example .env        # opcional: los valores por defecto ya funcionan
docker compose up -d        # levanta PostgreSQL en localhost:5432
./mvnw spring-boot:run      # arranca la API en http://localhost:8080
```

Al arrancar, Liquibase crea las tablas automáticamente.

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Especificación OpenAPI:** http://localhost:8080/v3/api-docs

### Configuración

Todo se lee del `.env`, que comparten la aplicación y `docker compose`. Si no existe, se usan estos valores por defecto:

| Variable | Por defecto | Para qué |
|---|---|---|
| `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_PORT` | `sala_reuniones`, `sala`, `sala`, `5432` | Conexión a PostgreSQL |
| `APP_USERNAME`, `APP_PASSWORD` | `admin`, `admin123` | Único usuario de la API |
| `JWT_SECRET` | Un secreto de desarrollo | Firma de los tokens (HS256). Mínimo 32 caracteres: si es más corto, la aplicación no arranca |
| `JWT_EXPIRATION` | `1h` | Caducidad del token |

### Tests

```bash
./mvnw test                 # necesita Docker en marcha
```

Hay 19 tests:

- **Unitarios**, sin Spring ni base de datos: reglas de la reserva (`ReservaTest`) y de los servicios con repositorios simulados (`ReservaServiceTest`, `SalaServiceTest`).
- **De integración**, con MockMvc contra un PostgreSQL real que Testcontainers levanta en Docker (`SalasIntegrationTest`, `ReservasIntegrationTest`). Recorren la API completa: autenticación, 409 y sus bordes, errores 400 por campo, filtros, orden, paginación y cancelación. No usan la base de desarrollo.

## Uso de la API

### Autenticación

Los endpoints que crean o cancelan exigen un token; los de consulta no.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username": "admin", "password": "admin123"}'
# {"accessToken":"eyJ...","tokenType":"Bearer","expiresIn":3600}
```

El token se envía en la cabecera `Authorization: Bearer <token>`. En Swagger: ejecuta el login, copia el `accessToken`, pulsa **Authorize** y pégalo.

### Endpoints

| Método | Ruta | Token | Respuestas |
|---|---|---|---|
| `POST` | `/api/auth/login` | No | 200 con el token · 401 credenciales incorrectas |
| `GET` | `/api/salas?page&size` | No | 200 paginado, ordenado por nombre |
| `POST` | `/api/salas` | **Sí** | 201 · 400 datos inválidos · 409 nombre repetido |
| `GET` | `/api/reservas?salaId&fecha&page&size` | No | 200 paginado, ordenado por inicio · 400 si falta `salaId` o `fecha` |
| `POST` | `/api/reservas` | **Sí** | 201 · 400 datos inválidos · 404 sala inexistente · 409 sala ocupada |
| `DELETE` | `/api/reservas/{id}` | **Sí** | 204 cancelada · 404 inexistente · 409 ya estaba cancelada |

Crear una reserva:

```bash
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"salaId": "<id de una sala>", "responsable": "Ana García",
       "inicio": "2026-11-16T10:00:00", "fin": "2026-11-16T11:00:00"}'
```

Listar las reservas de una sala en un día:

```bash
curl 'http://localhost:8080/api/reservas?salaId=<id>&fecha=2026-11-16&page=0&size=20'
```

### Paginación y errores

Los listados devuelven:

```json
{"data": [ ... ], "page": 0, "size": 20, "totalElements": 3, "totalPages": 1}
```

Las páginas empiezan en 0. El tamaño por defecto es 20 y el máximo, 100.

Todos los errores tienen el mismo formato. En los 400 se indica el error de cada campo:

```json
{"message": "La petición tiene campos inválidos",
 "errors": {"responsable": "El responsable es obligatorio", "fin": "El fin es obligatorio"}}
```

## Estructura

Cada módulo de negocio se organiza por capas:

```
com.salareuniones
├── sala/
│   ├── domain/                 Sala, SalaRepository, errors/
│   ├── application/            SalaService
│   └── infrastructure/http/    controlador, requests, responses y mapper
├── reserva/
│   ├── domain/                 Reserva, EstadoReserva, ReservaRepository, errors/
│   ├── application/            ReservaService
│   └── infrastructure/http/    controlador, requests, responses y mapper
├── auth/
│   ├── application/            AuthService, TokenGenerator
│   └── infrastructure/
│       ├── http/               login
│       └── security/           JwtTokenGenerator
└── shared/
    ├── domain/                 BaseEntity, errors/
    ├── application/            BaseService
    └── infrastructure/
        ├── http/               manejo global de errores, paginación, Swagger, BaseMapper
        ├── persistence/        auditoría JPA
        └── security/           Spring Security y validación del JWT
```

- **domain**: entidades, repositorios y errores de dominio.
- **application**: servicios con las reglas de negocio.
- **infrastructure**: adaptadores técnicos, agrupados por tipo. `http/` contiene lo que expone la API (controladores, requests, responses y mappers); `security/` y `persistence/`, la configuración de Spring Security y JPA.

Las dependencias van hacia dentro: infraestructura conoce a aplicación y a dominio, y aplicación solo conoce a dominio.

## Decisiones

**Arquitectura por capas, no hexagonal pura.** Conservé lo que aporta valor a este tamaño de proyecto: capas separadas, servicios que trabajan con entidades, controladores que convierten a DTOs, y errores de dominio que el manejador global traduce a HTTP. Simplifiqué lo que añadía ceremonia: las entidades JPA son el modelo de dominio (sin mappers entre dominio y persistencia), los repositorios de Spring Data no están detrás de puertos y no hay interfaces de casos de uso. La excepción es el login: `AuthService` usa la interfaz `TokenGenerator` para no depender de cómo se firma el JWT. La versión pura está en "Qué haría con más tiempo".

**`BaseService` como plantilla.** `create()` llama siempre a `validateCreate()`, que cada servicio sobrescribe con sus reglas. Así no se puede crear una entidad sin pasar por ellas.

**Base de datos.**
- **Migraciones con Liquibase**, en SQL y con su rollback, porque el esquema usa funciones propias de PostgreSQL. Hibernate solo valida que las entidades coincidan con el esquema (`ddl-auto: validate`).
- **Ids UUID** generados en el dominio, para que la entidad tenga identidad antes de guardarse.
- **Nombre de sala único sin distinguir mayúsculas**, con un índice único parcial sobre `lower(nombre)` que ignora las salas borradas.
- **Estado de la reserva como enum nativo de PostgreSQL** (`estado_reserva`): la base rechaza cualquier valor que no sea `ACTIVA` o `CANCELADA`.
- **`CHECK (fin > inicio)`** e índice `(sala_id, inicio)`, que sirve tanto a la comprobación de solapamiento como al listado.

**Fechas.** `inicio` y `fin` son `LocalDateTime`: una reserva "de 10 a 11" es la hora del reloj de la oficina, y el filtro por fecha es directo. La auditoría (`created_at`, `updated_at`) usa `Instant`, en UTC. Supuesto: todas las salas están en la misma zona horaria.

**Solapamiento (409).**
- **Intervalos semiabiertos**: dos reservas chocan si cada una empieza antes de que termine la otra. Una reserva puede empezar justo cuando termina otra.
- **Solo cuentan las reservas activas**: cancelar libera el horario.
- **Concurrencia**: antes de comprobar el solapamiento se bloquea la fila de la sala (`SELECT ... FOR UPDATE`) hasta el final de la transacción. Las reservas de una misma sala se procesan de una en una; las de salas distintas no se esperan. Con cinco peticiones simultáneas para el mismo horario, una da 201 y las demás 409.

**Cancelar no borra.** `DELETE /api/reservas/{id}` pasa la reserva a `CANCELADA`: conserva el historial y sigue apareciendo en el listado con su estado. La fila de la reserva también se bloquea, así que de dos cancelaciones simultáneas la segunda recibe 409.

**Listado de reservas.** Sala y fecha son obligatorias. Una fecha devuelve las reservas que ocupan algún momento de ese día, incluidas las que cruzan la medianoche.

**Orden fijo en los listados.** Salas por nombre y reservas por inicio (desempatadas por id). El cliente no elige el orden, así que la paginación es estable y no se puede ordenar por un campo inexistente.

**Respuestas de creación.** `POST` devuelve 201 sin cuerpo. El id se obtiene del listado.

**Autenticación con JWT.**
- **Con lo que trae Spring Security**: el login emite el token con `JwtEncoder`, y el *resource server* lo valida en cada petición. No hay librerías de JWT aparte ni filtros propios.
- **El caso de uso no conoce el JWT**: `AuthService` (application) comprueba las credenciales y pide el token a una interfaz `TokenGenerator`; la implementación que firma el JWT, `JwtTokenGenerator`, está en `auth/infrastructure/security`.
- **Usuario único** definido en la configuración, con la contraseña cifrada en memoria. El login no revela si falló el usuario o la contraseña.
- **Regla por método HTTP**: los `GET`, el login y Swagger son libres; el resto exige token. En las rutas públicas se ignora la cabecera `Authorization`, para que un token caducado no bloquee una consulta ni impida volver a iniciar sesión.

**Errores.** Formato propio y corto (`message` y `errors`), en lugar de `ProblemDetail`. Las reglas de negocio que se incumplen lanzan una subclase de `ConflictoException`, que el manejador traduce a 409; así, una regla nueva no exige tocarlo. Un recurso que no existe lanza `EntidadNoEncontradaException` (404). Los errores inesperados devuelven 500 con un mensaje genérico, y el detalle solo queda en el log.

**Idioma del código.** Lo de negocio en español (`Sala`, `Reserva`, `cancelar`); lo técnico y genérico en inglés (`BaseService`, `create`, `PaginatedResponse`).

## Qué haría con más tiempo

- **Arquitectura hexagonal pura**: modelo de dominio sin anotaciones JPA, puertos de salida para los repositorios con adaptadores y mappers, y ArchUnit para comprobar en los tests que el dominio no depende de Spring. Para una elevada tasa de transacciones sobre la base de datos, añadiría **CQRS** con un **bus de comandos y otro de consultas**: los comandos (crear sala, reservar, cancelar) y las consultas (listados) tendrían cada uno su handler, y las consultas podrían ir a réplicas de lectura o a una vista optimizada para el listado, escalando por separado de las escrituras.
- **Uso de value objects** para los conceptos del dominio que hoy son tipos primitivos. Por ejemplo:
  - **Ids tipados** (`SalaId`, `ReservaId`), para que el compilador impida confundirlos.
  - **Un `Horario`** que agrupe inicio y fin, valide que el fin sea posterior y sepa si se solapa con otro, en lugar de repetir esa regla en la consulta y en la validación del request.
- **UUID v7**, ordenados por fecha de creación: mantienen el tipo y la base, y mejoran la localidad del índice cuando crece el volumen. Para un sistema distribuido seguiría siendo la opción: Snowflake obligaría a migrar a enteros y a coordinar nodos sin necesidad.
- **Restricción de exclusión en PostgreSQL** (`EXCLUDE USING gist` con `btree_gist`), para que la propia base impida el solapamiento como segunda barrera.
- **409 en lugar de 500 cuando se crean a la vez dos salas con el mismo nombre**: hoy el índice único impide el duplicado, pero la segunda petición responde 500.
- **Usuarios en base de datos**, con roles (por ejemplo, que solo un administrador cree salas), *refresh tokens* y el secreto JWT en un gestor de secretos.
- **Varias zonas horarias** (una zona por sala) y **no permitir reservas en el pasado**.
- **Documentar en Swagger las respuestas de error** y ocultar el parámetro `sort`, que se ignora.
- **Observabilidad**: logs estructurados, métricas con Actuator y un `Dockerfile` de la aplicación.

## Cómo usé la IA

Usé Claude Code (Anthropic) como asistente durante todo el desarrollo: discutimos el plan antes de escribir código, generó la mayor parte de las clases y los tests a partir de mis instrucciones, y probó cada cambio arrancando la aplicación y lanzando peticiones reales. Revisé cada paso y decidí qué entraba en cada commit.

**Qué le pedí**
- Un plan inicial basado en el uso de arquitectura hexagonal.
- Cada pieza por separado: entidades, migraciones, servicios, endpoints, manejo de errores, login con JWT y tests.
- Revisiones antes de cada commit y una revisión de seguridad con un agente revisor.

**Qué acepté**
- Los intervalos semiabiertos de fecha y el bloqueo pesimista de la sala para las reservas simultáneas.
- Hacer la validación de JWT con lo que trae Spring Security, sin librerías adicionales.
- Tres hallazgos de la revisión de seguridad: un error interno se respondía como 401, un token caducado bloqueaba las rutas públicas y el login, y dos cancelaciones simultáneas podían responder 204 las dos.
- Testcontainers para los tests de integración, contra el mismo PostgreSQL que usa la aplicación.

**Qué corregí o cambié**
- **Arquitectura**: le propuse hexagonal; recomendó primero una versión ligera y luego la completa, y elegí la simplificada para el tamaño de la prueba.
- **Campos inventados**: añadió una capacidad a la sala que la tarea no pide, y la quité.
- **Base de datos**: para el estado de la reserva propuso una columna `VARCHAR` con un `CHECK` que limitara los valores; le pedí usar en su lugar un enum nativo de PostgreSQL. También cambié las migraciones de Flyway a Liquibase.
- **No reinventar**: implementó a mano un generador de UUID v7 porque Java 17 no lo trae, y lo descarté.
- **Simplificar**: le pedí recortar el manejador de errores y acortar el formato de error, sustituir una consulta derivada de nombre muy largo por un `findBy` simple, devolver 201 sin cuerpo y sin `Location`, y fijar el orden de los listados en el controlador.
- **Nombre de sala repetido**: le pedí que devolviera un error de dominio y no un error de la base; lo comprueba el servicio, y el índice único queda como respaldo.
- **Validaciones en su sitio**: la validación de formato estaba en la entidad; la moví a los requests, y dejé en el dominio solo las reglas de negocio.
- **Diseño de `BaseService`**: le pedí que crear pasara por el servicio base. Lo resolvió con un método plantilla y un gancho de validación por servicio.
- **Mappers y respuestas**: le pedí un `BaseMapper` con `toDto`, y que la conversión a la respuesta se hiciera en el controlador. Al principio el servicio devolvía ya el DTO; lo cambié para que el servicio devuelva la entidad y el controlador construya la respuesta con el mapper.
- **Estructura**: le pedí revisar clean code y estructura, y corregimos dependencias entre capas y módulos y los nombres de los DTOs; después le pedí `AuthService` en application y una carpeta `http` en la infraestructura de cada módulo, igual que en `shared`.

**Lo que no le delegué**
- **El alcance**: qué se implementaba y qué quedaba fuera, y cuándo una propuesta era sobreingeniería para esta prueba.
- **Las elecciones técnicas**: PostgreSQL con Docker, Liquibase, Lombok, Swagger, el login con JWT y el enum nativo para el estado.
- **El modelo de datos**: qué campos tiene cada entidad, la entidad base con UUID y fechas de auditoría, y la cancelación como cambio de estado.
- **El diseño de la API**: las rutas, que crear responda 201 sin cuerpo, que el listado de reservas exija sala y fecha en un único objeto de búsqueda, el orden fijo de los listados y los ejemplos de Swagger.
- **La estructura y los nombres**: los paquetes, las capas, los subpaquetes de errores y la regla de idioma del código.
- **Los commits**: los hice yo, decidiendo qué entraba en cada uno y con qué mensaje.
- **La revisión**: leí cada cambio antes de aceptarlo y probé la API desde Swagger e IntelliJ.
- **El contenido de este README**: qué decisiones y mejoras incluir, y cómo contar el uso de la IA.
