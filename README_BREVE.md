# Economia Circular - Documentacion Breve

API REST para una plataforma de economia circular: intercambio de articulos, talleres, eventos, centros de reciclaje, mensajeria y reportes.

## Stack
- Java 8
- Spring Boot 2.7 + Spring Security
- JPA/Hibernate + MySQL
- Liquibase

## Estructura principal
- `src/main/java/com/pp/economia_circular`: controladores, servicios, entidades y repositorios.
- `src/main/resources/application.properties`: configuracion general.
- `src/main/resources/db/changelog`: migraciones y datos semilla con Liquibase.
- `src/test/java`: pruebas unitarias/integracion.

## Puesta en marcha
1. Crear base de datos:
```sql
CREATE DATABASE IF NOT EXISTS eco_circular CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
2. Configurar credenciales DB en `application.properties`.
3. Ejecutar:
```bash
mvn spring-boot:run
```
4. API disponible en `http://localhost:8081`.

## Endpoints base
- `/api/auth`
- `/api/articles`
- `/api/usuarios`
- `/api/talleres`
- `/api/events`
- `/api/recycling-centers`
- `/api/mensajes`
- `/api/reports`
- `/api/admin/dashboard`

## Testing
```bash
mvn test
```

## Nota
El frontend React/Vite del proyecto se encuentra en un repositorio/carpeta separada.
