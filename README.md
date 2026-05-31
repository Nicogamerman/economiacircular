# Economía Circular — Backend REST API

Backend de la plataforma **Economía Circular**, desarrollado como proyecto de tesis para la carrera de Analista de Sistemas. Es una API REST que facilita el intercambio, donación y venta de artículos entre usuarios con el objetivo de reducir el desperdicio y promover el consumo responsable.

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 8 |
| Framework | Spring Boot 2.7.0 |
| Seguridad | Spring Security + JWT (jjwt 0.9.1) |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MySQL 8 (producción) / H2 (tests) |
| Migraciones | Liquibase |
| Documentación API | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |
| Deploy | Google App Engine Flexible |
| CI/CD | Google Cloud Build |
| Contenedores | Docker |

---

## Arquitectura

```
src/main/java/com/pp/economia_circular/
├── config/          # SecurityConfig, JWTFilter, WebConfiguration
├── controller/      # 17 controladores REST
├── service/         # Lógica de negocio (18 servicios)
├── repositories/    # Interfaces Spring Data JPA (18 repositorios)
├── entity/          # Entidades JPA (18 entidades)
└── DTO/             # Data Transfer Objects (37 DTOs)
```

---

## Requisitos previos

- Java 8 (JDK)
- Maven 3.6+
- MySQL 8.x (para desarrollo local)

---

## Configuración local

### 1. Clonar el repositorio

```bash
git clone https://github.com/nicogamerman/economiacircular.git
cd economiacircular
```

### 2. Crear la base de datos

```sql
CREATE DATABASE IF NOT EXISTS eco_circular
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 3. Configurar credenciales

Copiar el archivo de ejemplo y completar los datos:

```bash
cp src/main/resources/application-example.properties src/main/resources/application-dev.properties
```

Editar `application-dev.properties` con tu usuario y contraseña de MySQL local.

### 4. Levantar la aplicación

```bash
# Con Maven Wrapper (recomendado)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# En Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

La API queda disponible en `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Usuarios de prueba (perfil dev)

| Rol | Email | Contraseña |
|---|---|---|
| Admin | `admin@test.com` | `Test123!` |
| Usuario | `test@test.com` | `Test123!` |

---

## Migraciones de base de datos (Liquibase)

Las migraciones se ejecutan automáticamente al iniciar la aplicación. El archivo maestro es `src/main/resources/db/changelog/db.changelog-master.yaml`.

| # | Archivo | Descripción |
|---|---|---|
| 1 | `01-schema.sql` | Esquema completo (todas las tablas principales) |
| 2a | `02-seed-data-desarrollo.sql` | Datos de prueba — contexto `dev` |
| 2b | `02-seed-data-produccion.sql` | Datos iniciales — contexto `prod` |
| 3 | `03-valoraciones.sql` | Tabla `valoraciones` |
| 4 | `04-password-reset-tokens.sql` | Tabla de tokens de recuperación de contraseña |
| 5 | `05-imagenes-articulos-contenido.sql` | Almacenamiento de imágenes en BD |
| 6 | `06-notificaciones.sql` | Tabla `notificaciones` |
| 7 | `07-email-verification.sql` | Tokens de verificación de email |
| 8 | `08-mejor-categorizacion.sql` | Subcategoría, marca, modelo y etiquetas |
| 9 | `09-favoritos.sql` | Tabla `favoritos` |
| 10 | `10-soporte-chat.sql` | Chat de soporte usuario-admin |
| 11 | `11-politicas.sql` | Tabla de políticas y términos |
| 12 | `12-seed-politicas.sql` | Seed: Términos, Privacidad, Reglas, FAQ — contexto `prod` |

---

## Endpoints principales

### Autenticación — `/api/auth`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `/login` | Iniciar sesión → devuelve JWT | No |
| POST | `/forgot-password` | Solicitar reset de contraseña | No |
| POST | `/confirm-reset-password` | Confirmar nueva contraseña | No |
| GET | `/verify-email` | Verificar email con token | No |
| POST | `/resend-verification` | Reenviar email de verificación | No |

### Registro — `/api/registrar`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `/registrar` | Registrar nuevo usuario | No |

### Usuarios — `/api/usuarios`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| GET | `/me` | Perfil del usuario autenticado | JWT |
| PUT | `/me` | Actualizar nombre/apellido/domicilio | JWT |
| POST | `/me/foto` | Subir foto de perfil (multipart) | JWT |
| DELETE | `/me/foto` | Eliminar foto de perfil | JWT |
| GET | `/perfil/{id}` | Ver perfil público de un usuario | No |
| GET | `/{id}/foto` | Obtener foto de perfil | No |
| GET | `` | Listar todos los usuarios | ADMIN |
| DELETE | `/{id}` | Eliminar usuario | ADMIN |

### Artículos — `/api/articles`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| GET | `` | Listar artículos disponibles (paginado, filtrable) | No |
| GET | `/search` | Búsqueda por texto | No |
| GET | `/{id}` | Detalle de un artículo | No |
| GET | `/most-viewed` | Artículos más vistos | No |
| GET | `/category/{category}` | Por categoría | No |
| POST | `` | Publicar artículo | JWT |
| PUT | `/{id}` | Editar artículo | JWT |
| DELETE | `/{id}` | Cancelar publicación | JWT |
| GET | `/my-articles` | Mis artículos | JWT |
| GET | `/user/{userId}` | Artículos de un usuario | No |

**Categorías disponibles:** ELECTRONICA, ROPA, MUEBLES, HOGAR, DEPORTES, LIBROS, JUGUETES, HERRAMIENTAS, VEHICULOS, ALIMENTOS, JARDINERIA, MASCOTAS, ARTE, MUSICA, OTRO

**Condición:** NUEVO, USADO, REACONDICIONADO, AVERIADO

### Imágenes de artículos — `/api/articles/{articuloId}/images`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `` | Subir imagen (multipart, máx. 5 por artículo) | JWT |
| GET | `` | Listar imágenes del artículo | No |
| GET | `/{imagenId}/file` | Obtener imagen | No |
| DELETE | `/{imagenId}` | Eliminar imagen | JWT |

### Intercambios — `/api/intercambios`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `` | Crear solicitud de intercambio | JWT |
| GET | `/mis-solicitudes` | Solicitudes enviadas | JWT |
| GET | `/recibidas` | Solicitudes recibidas | JWT |
| GET | `/historial` | Historial completo | JWT |
| GET | `/{id}` | Detalle de solicitud | JWT |
| PUT | `/{id}/estado` | Cambiar estado (ACEPTADA/RECHAZADA/CANCELADA/COMPLETADA) | JWT |

### Favoritos — `/api/favoritos`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `/{articuloId}` | Agregar a favoritos | JWT |
| DELETE | `/{articuloId}` | Quitar de favoritos | JWT |
| GET | `` | Mis favoritos (paginado) | JWT |
| GET | `/{articuloId}/estado` | ¿Es favorito? | JWT |
| GET | `/{articuloId}/count` | Cantidad de favoritos del artículo | No |

### Mensajes — `/api/mensajes`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `/enviar` | Enviar mensaje | JWT |
| GET | `/conversacion` | Historial de conversación | JWT |
| GET | `/articulo/{articuloId}` | Mensajes sobre un artículo | JWT |
| PUT | `/leer/{mensajeId}` | Marcar como leído | JWT |
| GET | `/no-leidos/count` | Cantidad de no leídos | JWT |

### Valoraciones — `/api/valoraciones`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `` | Crear valoración (1-5) | JWT |
| PUT | `/{id}` | Editar valoración | JWT |
| DELETE | `/{id}` | Eliminar valoración | JWT |
| GET | `/usuario/{usuarioId}` | Valoraciones de un usuario | No |
| GET | `/usuario/{usuarioId}/resumen` | Promedio y cantidad | No |

### Notificaciones — `/api/notificaciones`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| GET | `` | Listar notificaciones | JWT |
| GET | `/no-leidas/count` | Cantidad no leídas | JWT |
| PUT | `/{id}/leer` | Marcar como leída | JWT |
| PUT | `/leer-todas` | Marcar todas como leídas | JWT |
| DELETE | `/{id}` | Eliminar notificación | JWT |

### Chat de soporte — `/api/soporte`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `` | Abrir nuevo chat | JWT |
| POST | `/{chatId}/mensajes` | Enviar mensaje | JWT |
| GET | `/mis-chats` | Mis chats | JWT |
| GET | `/{chatId}/mensajes` | Mensajes del chat | JWT |
| PUT | `/{chatId}/mensajes/leer` | Marcar mensajes como leídos | JWT |
| GET | `` | Todos los chats | ADMIN |
| PUT | `/{chatId}/estado` | Cambiar estado del chat | ADMIN |

### Políticas — `/api/politicas`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| GET | `` | Políticas activas (filtrable por tipo) | No |
| GET | `/{id}` | Detalle de política | No |
| GET | `/todas` | Todas (incluye inactivas) | ADMIN |
| POST | `` | Crear política | ADMIN |
| PUT | `/{id}` | Editar política | ADMIN |
| DELETE | `/{id}` | Eliminar política | ADMIN |
| PATCH | `/{id}/toggle` | Activar/desactivar | ADMIN |

**Tipos:** TERMINOS_USO, POLITICA_PRIVACIDAD, REGLAS_COMUNIDAD, FAQ, OTRO

### Eventos — `/api/events`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| GET | `` | Listar eventos | No |
| GET | `/upcoming` | Próximos eventos | No |
| GET | `/nearby` | Eventos cercanos (lat/lon/radio) | No |
| GET | `/type/{eventType}` | Por tipo | No |
| GET | `/{id}` | Detalle | No |
| POST | `` | Crear evento | JWT |
| PUT | `/{id}` | Editar evento | JWT |
| DELETE | `/{id}` | Eliminar evento | JWT |

### Centros de reciclaje — `/api/recycling-centers`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| GET | `` | Listar centros | No |
| GET | `/nearby` | Centros cercanos | No |
| GET | `/type/{centerType}` | Por tipo | No |
| GET | `/{id}` | Detalle | No |
| POST | `` | Crear centro | ADMIN |
| PUT | `/{id}` | Editar centro | ADMIN |
| DELETE | `/{id}` | Eliminar centro | ADMIN |

### Talleres — `/api/talleres`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| GET | `` | Listar talleres | No |
| GET | `/{id}` | Detalle | No |
| POST | `` | Crear taller | JWT |
| PUT | `/{id}` | Editar taller | JWT |
| DELETE | `/{id}` | Eliminar taller | JWT |

### Métricas y Reportes — `/api/metrics`, `/api/reports`

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| GET | `/api/metrics/dashboard` | Resumen general | ADMIN |
| GET | `/api/metrics/timeline/users` | Línea temporal de registros | ADMIN |
| GET | `/api/metrics/distribution/articles-by-category` | Distribución por categoría | ADMIN |
| GET | `/api/reports/users` | Reporte de usuarios | ADMIN |
| GET | `/api/reports/articles` | Reporte de artículos | ADMIN |
| GET | `/api/reports/top-users` | Usuarios más activos | ADMIN |

---

## Seguridad

- Autenticación mediante **JWT Bearer Token**
- Contraseñas cifradas con **BCrypt**
- Roles: `USUARIO` y `ADMIN`
- Verificación de email opcional (configurable via `EMAIL_VERIFICATION_REQUIRED`)
- Todos los endpoints públicos no requieren token; el resto requieren cabecera `Authorization: Bearer <token>`

---

## Tests

```bash
# Ejecutar todos los tests
./mvnw test

# Resultado esperado
Tests run: 281, Failures: 0, Errors: 0, Skipped: 0
```

**Cobertura:**

| Tipo | Cantidad |
|---|---|
| Tests de controladores (MockMvc) | ~100 |
| Tests de servicios (Mockito) | ~110 |
| Tests de repositorios | ~15 |
| Tests de JWT y utilidades | ~56 |
| **Total** | **281** |

Los tests usan **H2 en memoria** con Liquibase y `TestSecurityConfig` para aislar la seguridad.

---

## Despliegue en Google App Engine

### Prerrequisitos

- Google Cloud SDK instalado
- Proyecto de GCP configurado con Cloud SQL (MySQL 8)
- App Engine Flexible habilitado

### Configurar `app.yaml`

Editar `app.yaml` reemplazando los valores de Cloud SQL, credenciales SMTP y URLs:

```yaml
env_variables:
  SPRING_PROFILES_ACTIVE: "prod"
  SPRING_DATASOURCE_URL: "jdbc:mysql://google/economia_circular?cloudSqlInstance=PROJECT:REGION:INSTANCE&socketFactory=com.google.cloud.sql.mysql.SocketFactory"
  SPRING_DATASOURCE_USERNAME: "usuario_db"
  SPRING_DATASOURCE_PASSWORD: "password_db"
```

### Build y deploy

```bash
# Compilar
./mvnw clean package -DskipTests

# Desplegar
gcloud app deploy
```

### Docker (opcional)

```bash
# Build
docker build -t economia-circular .

# Run
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://... \
  economia-circular
```

---

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `PORT` | `8080` | Puerto del servidor |
| `SPRING_PROFILES_ACTIVE` | `dev` | Perfil activo (`dev` / `prod`) |
| `SPRING_DATASOURCE_URL` | localhost MySQL | URL de conexión a la base de datos |
| `SPRING_DATASOURCE_USERNAME` | `user` | Usuario de base de datos |
| `SPRING_DATASOURCE_PASSWORD` | `devpass` | Contraseña de base de datos |
| `EMAIL_ENABLED` | `false` | Activar envío de emails |
| `EMAIL_FROM` | — | Dirección remitente |
| `SMTP_HOST` | `smtp.gmail.com` | Host SMTP |
| `SMTP_PORT` | `587` | Puerto SMTP |
| `SMTP_USERNAME` | — | Usuario SMTP |
| `SMTP_PASSWORD` | — | Contraseña SMTP (App Password) |
| `PASSWORD_RESET_URL_BASE` | `http://localhost:8080/reset-password` | URL del frontend para reset |
| `EMAIL_VERIFY_URL_BASE` | `http://localhost:8080/api/auth/verify-email` | URL del backend para verificación |
| `EMAIL_VERIFICATION_REQUIRED` | `false` | Requerir verificación de email |
| `ARTICLE_IMAGES_MAX` | `5` | Máximo de imágenes por artículo |
| `ARTICLE_IMAGES_MAX_SIZE_BYTES` | `5242880` | Tamaño máximo de imagen (5MB) |

---

## Estructura del proyecto

```
economiacircular/
├── src/
│   ├── main/
│   │   ├── java/com/pp/economia_circular/
│   │   │   ├── EconomiaCircularApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/        (17 controladores)
│   │   │   ├── DTO/               (37 clases)
│   │   │   ├── entity/            (18 entidades)
│   │   │   ├── repositories/      (18 repositorios)
│   │   │   └── service/           (18 servicios)
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-example.properties
│   │       └── db/changelog/
│   │           ├── db.changelog-master.yaml
│   │           └── sql/           (13 archivos de migración)
│   └── test/
│       └── java/com/pp/economia_circular/
│           ├── config/            (TestSecurityConfig)
│           ├── controller/        (9 clases de test)
│           └── service/           (11 clases de test)
├── app.yaml                       (App Engine config)
├── cloudbuild.yaml                (CI/CD)
├── Dockerfile
├── pom.xml
└── ENDPOINTS.md                   (documentación detallada de endpoints)
```

---

## Documentación adicional

- [`ENDPOINTS.md`](ENDPOINTS.md) — Documentación completa de todos los endpoints con ejemplos de request/response
- [`QUICKSTART.md`](QUICKSTART.md) — Guía de inicio rápido en 3 pasos
- [`README_LIQUIBASE.md`](README_LIQUIBASE.md) — Guía de migraciones de base de datos

---

## Licencia

Proyecto académico — Tesis de Analista de Sistemas.
