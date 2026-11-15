# sala-reuniones-backend

API REST para reservar salas de reuniones, con Java 17 y Spring Boot.

## Requisitos

- Java 17 o superior
- Docker

## Cómo ejecutarlo

```bash
cp .env.example .env        # opcional: los valores por defecto ya funcionan
docker compose up -d        # levanta PostgreSQL
./mvnw spring-boot:run      # arranca la API en http://localhost:8080
```

Al arrancar, Liquibase crea las tablas automáticamente.
