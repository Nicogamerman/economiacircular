# Economía Circular - API Endpoints

Documentación de los endpoints REST del backend, pensada para el consumo desde el frontend.

- **Base URL (dev):** `http://localhost:8080`
- **Autenticación:** JWT en header `Authorization: Bearer <token>`. Se obtiene desde `POST /api/auth/login`.
- **CORS:** habilitado para todos los orígenes.
- **Convención de errores:** los endpoints devuelven `400 Bad Request` con el mensaje en el body para errores de validación/negocio, `401` cuando el JWT falta o es inválido, `403` cuando el rol no alcanza.

---

## Tabla de contenidos

1. [Autenticación y registro](#1-autenticación-y-registro)
2. [Usuarios](#2-usuarios)
3. [Artículos](#3-artículos)
4. [Imágenes de artículos](#4-imágenes-de-artículos)
5. [Mensajería](#5-mensajería)
6. [Notificaciones](#6-notificaciones)
7. [Valoraciones (reputación del oferente)](#7-valoraciones-reputación-del-oferente)
8. [Eventos](#8-eventos)
9. [Centros de reciclaje](#9-centros-de-reciclaje)
10. [Talleres](#10-talleres)
11. [Reportes y métricas](#11-reportes-y-métricas)
12. [Dashboard de métricas (gráficos y mapas)](#12-dashboard-de-métricas-gráficos-y-mapas)
13. [Artículos favoritos](#13-artículos-favoritos)
14. [Intercambios](#14-intercambios)
15. [Chat de soporte](#15-chat-de-soporte)
16. [Políticas y reglas](#16-políticas-y-reglas)
17. [Health-check](#17-health-check)

---

## 1. Autenticación y registro

Todos los endpoints de esta sección son **públicos** (no requieren JWT).

### `POST /api/registrar/registrar`
Registra un nuevo usuario. Si la verificación de email está activa, se envía un email con un enlace a `GET /api/auth/verify-email?token=...`.

**Body:**
```json
{
  "nombre": "Ana",
  "apellido": "García",
  "email": "ana@example.com",
  "contrasena": "password123",
  "rol": "USER",
  "domicilio": "Calle 123",
  "fotoBase64": "iVBORw0KGgoAA..."
}
```
**Respuesta:** `200 OK` (vacío). El usuario queda con `emailVerificado=false` hasta confirmar.

### `GET /api/registrar/{id}/foto`
Devuelve la foto del usuario como `image/jpeg`.

### `POST /api/auth/login`
**Body:**
```json
{ "email": "ana@example.com", "contrasena": "password123" }
```
**Respuesta `200 OK`:**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```
**Respuestas de error:**
- `400` — email/contraseña vacíos
- `401` — credenciales inválidas, usuario inactivo, o email no verificado (si `EMAIL_VERIFICATION_REQUIRED=true`)

### `GET /api/auth/verify-email?token=<rawToken>`
Confirma el email del usuario usando el token recibido por mail.

**Respuesta `200 OK`:**
```json
{ "message": "Email verificado correctamente", "verified": true }
```

### `POST /api/auth/resend-verification`
**Body:** `{ "email": "ana@example.com" }`
Respuesta uniforme (no revela si el email existe).

### `POST /api/auth/forgot-password`
**Body:** `{ "email": "ana@example.com" }`
Envía un email con enlace de recuperación. Siempre responde `200 OK` con mensaje uniforme.

### `GET /api/auth/validate-reset-token?token=<rawToken>`
**Respuesta:** `{ "valid": true | false }`

### `POST /api/auth/confirm-reset-password`
**Body:**
```json
{ "token": "<rawToken>", "newPassword": "nuevaPass123" }
```
**Respuesta `200 OK`:** `{ "message": "Contraseña actualizada exitosamente" }`

### `POST /api/auth/reset-password`
**Legacy**, cambio de contraseña con la contraseña actual (sin email).
**Body:** `{ "email": "...", "oldPassword": "...", "newPassword": "..." }`

### `POST /api/auth/check-email`
**Body:** `{ "email": "..." }`
**Respuesta:** `{ "exists": true, "active": true }`

### `POST /api/auth/decode-token`
Endpoint de debug. **Body:** `{ "token": "..." }`. Devuelve las claims si es válido.

---

## 2. Usuarios

### `GET /api/usuarios/{id}`
**Auth:** USER/ADMIN. Devuelve los datos del usuario.

### `PUT /api/usuarios/{id}`
**Auth:** USER/ADMIN. Actualiza el perfil del usuario.

---

## 3. Artículos

### `GET /api/articles?page=0&size=10`  *(público)*
Lista paginada de artículos.

### `GET /api/articles/search`  *(público)*
Búsqueda con filtros opcionales (combinables, paginada).

**Query params:**
- `title` — busca solo en título (modo legacy)
- `q` — búsqueda libre en título, descripción, subcategoría, marca, modelo y etiquetas
- `category`: `ELECTRONICOS | ROPA | LIBROS | MUEBLES | HERRAMIENTAS | DEPORTES | DECORACION_HOGAR | COCINA | JARDIN | AUTOMOTRIZ | JUGUETES | SUMINISTROS_ARTE | INSTRUMENTOS_MUSICALES | OTROS`
- `condition`: `NUEVO | USADO | REACONDICIONADO | AVERIADO`
- `subcategoria` — texto exacto (ej. `celulares`)
- `marca` — texto exacto (ej. `samsung`)
- `tag` — etiqueta exacta (ej. `vintage`)
- `page`, `size` (default 0, 10)

> Si se pasa cualquiera de `q`, `subcategoria`, `marca` o `tag`, se usa el motor avanzado (incluye joins con etiquetas).

### `GET /api/articles/subcategories?category=ELECTRONICOS`  *(público)*
Lista de subcategorías distintas usadas. Si `category` se omite, devuelve todas.
Devuelve: `["celulares", "notebooks", "tablets"]`

### `GET /api/articles/brands?category=ELECTRONICOS`  *(público)*
Marcas distintas usadas. Útil para autocomplete en el formulario.

### `GET /api/articles/tags?limit=20`  *(público)*
Top etiquetas más usadas en artículos `DISPONIBLE`.
**Respuesta:** `[{ "etiqueta": "vintage", "cantidad": 12 }, ...]`

### `GET /api/articles/category/{category}`  *(público)*

### `GET /api/articles/most-viewed?page=0&size=10`  *(público)*

### `GET /api/articles/user/{userId}`  *(público)*

### `GET /api/articles/{id}`  *(público)*

### `GET /api/articles/my-articles`  *(USER/ADMIN)*

### `GET /api/articles/articulos-usuario?email=...`  *(USER/ADMIN)*

### `POST /api/articles`  *(USER/ADMIN)*
**Body:**
```json
{
  "title": "iPhone 12 64GB",
  "description": "Funciona perfecto, con caja y cargador.",
  "category": "ELECTRONICOS",
  "subcategoria": "celulares",
  "marca": "Apple",
  "modelo": "iPhone 12",
  "condition": "USADO",
  "estado": "DISPONIBLE",
  "etiquetas": ["smartphone", "ios", "64gb"]
}
```

Los campos `subcategoria`, `marca`, `modelo` y `etiquetas` son opcionales pero recomendados — habilitan filtros y búsqueda libre. Las etiquetas se normalizan a lowercase y se deduplican automáticamente.

### `PUT /api/articles/{id}`  *(USER/ADMIN; solo el dueño)*

### `DELETE /api/articles/{id}`  *(USER/ADMIN; solo el dueño)*

### `PUT /api/article-status/{id}`  *(USER/ADMIN)*
Cambia el estado del artículo (`DISPONIBLE | INTERCAMBIADO | RESERVADO | DONADO | CANCELADO | VENDIDO | PAUSADO`).

---

## 4. Imágenes de artículos

Hasta **5 imágenes por artículo** (configurable), tamaño máximo **5 MB** cada una, content-type `image/*`.

### `POST /api/articles/{articuloId}/images`  *(USER/ADMIN; solo el dueño)*
**Content-Type:** `multipart/form-data`
- `file`: archivo
- `descripcion`: texto opcional

**Respuesta `201`:**
```json
{
  "id": 12,
  "articuloId": 4,
  "nombreArchivo": "bici-frente.jpg",
  "contentType": "image/jpeg",
  "tamanoBytes": 458231,
  "url": "http://localhost:8080/api/articles/4/images/12/file",
  "creadoEn": "2026-05-27T22:00:00"
}
```

### `GET /api/articles/{articuloId}/images`  *(público)*
Lista de metadata con URL al archivo.

### `GET /api/articles/{articuloId}/images/{imagenId}/file`  *(público)*
Devuelve los bytes con el content-type correcto.

### `DELETE /api/articles/{articuloId}/images/{imagenId}`  *(USER/ADMIN; solo el dueño)*

---

## 5. Mensajería

### `POST /api/mensajes/enviar`  *(USER/ADMIN)*
**Body:**
```json
{ "destinatarioId": 5, "articuloId": 3, "contenido": "Hola, sigue disponible?" }
```
Al enviar el mensaje se crea automáticamente una notificación al destinatario.

### `GET /api/mensajes/conversacion?usuarioId1=...&usuarioId2=...`  *(USER/ADMIN)*

### `GET /api/mensajes/articulo/{articuloId}`  *(USER/ADMIN)*

### `PUT /api/mensajes/leer/{mensajeId}`  *(USER/ADMIN)*

### `GET /api/mensajes/no-leidos`  *(USER/ADMIN)*

### `GET /api/mensajes/no-leidos/count`  *(USER/ADMIN)*

---

## 6. Notificaciones

Sistema in-app que se dispara automáticamente con:
- Nuevos mensajes recibidos (`MENSAJE_NUEVO`)
- Valoraciones recibidas (`VALORACION_NUEVA`)
- Reservados para futuro: `SOLICITUD_INTERCAMBIO`, `SISTEMA`

Todos los endpoints requieren **USER/ADMIN**.

### `GET /api/notificaciones?soloNoLeidas=false`
**Respuesta (array):**
```json
[{
  "id": 1,
  "tipo": "MENSAJE_NUEVO",
  "titulo": "Nuevo mensaje sobre \"Bici rodado 26\"",
  "mensaje": "Hola, sigue disponible?",
  "referenciaTipo": "MENSAJE",
  "referenciaId": 42,
  "leida": false,
  "emisorId": 3,
  "emisorNombre": "Juan Pérez",
  "creadoEn": "2026-05-27T22:01:30",
  "leidoEn": null
}]
```

### `GET /api/notificaciones/no-leidas/count`
**Respuesta:** `{ "count": 7 }`

### `PUT /api/notificaciones/{id}/leer`

### `PUT /api/notificaciones/leer-todas`
**Respuesta:** `{ "actualizadas": 5 }`

### `DELETE /api/notificaciones/{id}`

**Tip de polling:** llamar `GET /api/notificaciones/no-leidas/count` cada 30–60 s para refrescar el badge.

---

## 7. Valoraciones (reputación del oferente)

Cada usuario puede ser valorado con un puntaje de **1 a 5** y un comentario opcional. Una valoración por par (valorador, valorado, artículo).

### `POST /api/valoraciones`  *(USER/ADMIN)*
**Body:**
```json
{ "valoradoId": 5, "articuloId": 3, "puntaje": 5, "comentario": "Muy buen trato!" }
```
`articuloId` es opcional. No se permite autovalorarse.

### `PUT /api/valoraciones/{id}`  *(USER/ADMIN; solo autor)*

### `DELETE /api/valoraciones/{id}`  *(USER/ADMIN; autor o ADMIN)*

### `GET /api/valoraciones/usuario/{usuarioId}`  *(público)*
Lista de valoraciones recibidas por el usuario.

### `GET /api/valoraciones/usuario/{usuarioId}/resumen`  *(público)*
**Respuesta:**
```json
{
  "usuarioId": 5,
  "promedio": 4.6,
  "totalValoraciones": 10,
  "cantidad5": 7, "cantidad4": 2, "cantidad3": 1, "cantidad2": 0, "cantidad1": 0
}
```

### `GET /api/valoraciones/articulo/{articuloId}`  *(público)*

### `GET /api/valoraciones/mis-valoraciones`  *(USER/ADMIN)*
Las dadas por el usuario autenticado.

---

## 8. Eventos

### `GET /api/events`  *(público)*

### `GET /api/events/upcoming`  *(público)*

### `GET /api/events/type/{eventType}`  *(público)*
Tipos: `FAIR | WORKSHOP | CONFERENCE | MEETUP | EXCHANGE_EVENT`.

### `GET /api/events/nearby?latitude=&longitude=&radiusKm=`  *(público)*

### `GET /api/events/{id}`  *(público)*

### `POST /api/events`  *(ADMIN)*

### `PUT /api/events/{id}`  *(ADMIN)*

### `DELETE /api/events/{id}`  *(ADMIN)*

---

## 9. Centros de reciclaje

### `GET /api/recycling-centers`  *(público)*

### `GET /api/recycling-centers/type/{centerType}`  *(público)*
Tipos: `RECYCLING_CENTER | WASTE_COLLECTION_POINT | NGO | REPAIR_WORKSHOP`.

### `GET /api/recycling-centers/nearby?latitude=&longitude=&radiusKm=`  *(público)*

### `GET /api/recycling-centers/{id}`  *(público)*

### `POST /api/recycling-centers`  *(ADMIN)*

### `PUT /api/recycling-centers/{id}`  *(ADMIN)*

### `DELETE /api/recycling-centers/{id}`  *(ADMIN)*

---

## 10. Talleres

### `GET /api/talleres`  *(auth)*

### `GET /api/talleres/{id}`  *(auth)*

### `POST /api/talleres`  *(auth)*

### `PUT /api/talleres/{id}`  *(auth)*

### `DELETE /api/talleres/{id}`  *(auth)*

---

## 11. Reportes y métricas

Endpoints agregados para dashboards. Todos requieren auth.

- `GET /api/reports/users` — usuarios activos, registros por mes, etc.
- `GET /api/reports/articles` — artículos por categoría, condición, estado.
- `GET /api/reports/top-users` — ranking de usuarios.
- `GET /api/reports/top-articles` — ranking de artículos.
- `GET /api/reports/social` — métricas sociales (mensajes, valoraciones, etc.).
- `GET /api/reports/environmental` — impacto ambiental estimado.

> Estos endpoints están preparados para futuro: pueden devolver más detalle conforme se amplíe el `ReportService`.

---

## 12. Dashboard de métricas (gráficos y mapas)

Endpoints diseñados para alimentar un panel administrativo con tarjetas resumen, gráficos de líneas/barras/donuts y mapas. **Todos requieren rol ADMIN.**

### `GET /api/metrics/dashboard`
Resumen consolidado con todas las métricas clave del producto.

**Respuesta:**
```json
{
  "generatedAt": "2026-05-27T22:00:00",
  "usuariosTotales": 124,
  "usuariosActivos": 110,
  "usuariosNuevosUltimos30Dias": 18,
  "crecimientoUsuariosPctMesActualVsAnterior": 15.0,
  "articulosTotales": 412,
  "articulosDisponibles": 287,
  "articulosPublicadosUltimos30Dias": 53,
  "crecimientoArticulosPctMesActualVsAnterior": 8.2,
  "intercambiosTotales": 90,
  "intercambiosPendientes": 12,
  "intercambiosCompletados": 64,
  "intercambiosUltimos30Dias": 22,
  "eventosTotales": 14,
  "eventosActivos": 6,
  "centrosReciclajeTotales": 10,
  "centrosReciclajeActivos": 9,
  "mensajesTotales": 1287,
  "valoracionesTotales": 78,
  "promedioValoraciones": 4.6
}
```

### `GET /api/metrics/timeline/users?months=12`
Serie temporal de **usuarios nuevos por mes** para los últimos N meses (default 12, máx 60). Pensado para gráficos de línea/barras.

**Respuesta:**
```json
[
  { "periodo": "2025-06", "valor": 4 },
  { "periodo": "2025-07", "valor": 9 },
  { "periodo": "2025-08", "valor": 12 }
]
```

### `GET /api/metrics/timeline/articles?months=12`
Misma estructura. Publicaciones nuevas por mes.

### `GET /api/metrics/timeline/exchanges?months=12`
Solicitudes de intercambio creadas por mes.

### `GET /api/metrics/distribution/articles-by-category`
Distribución de artículos por categoría (donut/pie chart).

**Respuesta:**
```json
[
  { "label": "ELECTRONICOS", "cantidad": 42, "porcentaje": 10.2 },
  { "label": "ROPA", "cantidad": 78, "porcentaje": 18.9 }
]
```

### `GET /api/metrics/distribution/articles-by-status`
Distribución por estado (`DISPONIBLE`, `INTERCAMBIADO`, etc).

### `GET /api/metrics/geo/events`
Eventos con coordenadas (para mapa). Filtra los que tienen `latitude/longitude`.

**Respuesta:**
```json
[
  {
    "id": 4,
    "nombre": "Feria de intercambio en Belgrano",
    "descripcion": "Encuentro mensual de la comunidad...",
    "latitud": -34.5631,
    "longitud": -58.4544,
    "tipo": "EVENTO",
    "categoria": "FAIR",
    "estado": "ACTIVE"
  }
]
```

### `GET /api/metrics/geo/recycling-centers`
Centros de reciclaje con coordenadas. Misma estructura, `tipo` = `"CENTRO_RECICLAJE"`.

---

## 13. Artículos favoritos

Base path: `/api/favoritos` | Requieren JWT salvo donde se indica.

### `POST /api/favoritos/{articuloId}` *(USER / ADMIN)*
Agrega un artículo a los favoritos del usuario autenticado.

**Respuesta 201:**
```json
{
  "favoritoId": 7,
  "guardadoEn": "2025-03-01T12:00:00",
  "articulo": {
    "id": 42,
    "title": "Bicicleta de montaña",
    "category": "DEPORTE",
    "status": "DISPONIBLE",
    ...
  }
}
```

### `DELETE /api/favoritos/{articuloId}` *(USER / ADMIN)*
Quita un artículo de los favoritos del usuario autenticado.

**Respuesta 200:** `"Artículo eliminado de favoritos"`

### `GET /api/favoritos` *(USER / ADMIN)*
Lista los artículos favoritos del usuario autenticado (paginado).

| Param | Tipo | Default |
|---|---|---|
| `page` | int | 0 |
| `size` | int | 10 |

**Respuesta 200:** Page de `FavoritoResponseDto` (misma estructura que POST).

### `GET /api/favoritos/{articuloId}/estado` *(USER / ADMIN)*
Indica si el artículo está en los favoritos del usuario autenticado.

**Respuesta 200:**
```json
{ "articuloId": 42, "esFavorito": true }
```

### `GET /api/favoritos/{articuloId}/count` *(público)*
Devuelve cuántos usuarios tienen el artículo como favorito.

**Respuesta 200:**
```json
{ "articuloId": 42, "total": 15 }
```

---

## 14. Intercambios

Base path: `/api/intercambios` | Todos requieren JWT.

### `POST /api/intercambios` *(USER / ADMIN)*
Crea una solicitud de intercambio. El solicitante ofrece su artículo a cambio del artículo de otro usuario.

**Body:**
```json
{ "articuloSolicitadoId": 10, "articuloOfrecidoId": 5 }
```

**Respuesta 201:** `SolicitudResponseDto` con todos los detalles.

**Validaciones:** artículos DISPONIBLE, el ofrecido debe ser del solicitante, no puede solicitar el propio artículo, no puede haber una solicitud pendiente duplicada.

### `GET /api/intercambios/mis-solicitudes` *(USER / ADMIN)*
Lista las solicitudes enviadas por el usuario autenticado (paginado).

| Param | Default |
|---|---|
| `page` | 0 |
| `size` | 10 |

### `GET /api/intercambios/recibidas` *(USER / ADMIN)*
Lista las solicitudes recibidas para los artículos del usuario autenticado (paginado).

### `GET /api/intercambios/historial` *(USER / ADMIN)*
Lista todas las solicitudes en las que participa el usuario (como solicitante o propietario), ordenadas por fecha descendente. Ideal para el historial completo.

### `GET /api/intercambios/{id}` *(USER / ADMIN)*
Obtiene el detalle de una solicitud específica. Solo accesible por el solicitante, el propietario del artículo o un ADMIN.

**Respuesta 200:**
```json
{
  "id": 1,
  "estado": "PENDIENTE",
  "articuloSolicitadoId": 10,
  "articuloSolicitadoTitulo": "Bicicleta de montaña",
  "propietarioArticuloSolicitadoId": 3,
  "propietarioArticuloSolicitadoEmail": "juan@example.com",
  "articuloOfrecidoId": 5,
  "articuloOfrecidoTitulo": "Raqueta de tenis",
  "solicitanteId": 7,
  "solicitanteEmail": "ana@example.com",
  "solicitanteNombre": "Ana García",
  "creadoEn": "2025-03-01T10:00:00",
  "actualizadoEn": "2025-03-01T10:00:00"
}
```

### `PUT /api/intercambios/{id}/estado` *(USER / ADMIN)*
Cambia el estado de una solicitud. Las transiciones válidas son:

| Quién | Puede cambiar a |
|---|---|
| Solicitante | `CANCELADO` |
| Propietario del artículo | `ACEPTADO`, `RECHAZADO` (desde PENDIENTE) |
| Propietario del artículo | `COMPLETADO` (desde ACEPTADO) |
| ADMIN | Cualquier transición válida |

**Body:**
```json
{ "nuevoEstado": "ACEPTADO" }
```
Estados posibles: `PENDIENTE`, `ACEPTADO`, `RECHAZADO`, `COMPLETADO`, `CANCELADO`.

Al cambiar estado se genera una notificación automática a la otra parte.

---

## 15. Chat de soporte

Base path: `/api/soporte` | Todos requieren JWT salvo donde se indica.

### `POST /api/soporte` *(USER / ADMIN)*
Abre un nuevo chat de soporte con un mensaje inicial.

**Body:**
```json
{
  "asunto": "No puedo subir imágenes",
  "mensajeInicial": "Cuando intento subir una foto me da error 400.",
  "prioridad": "ALTA"
}
```
Prioridades: `BAJA`, `MEDIA` (default), `ALTA`.

**Respuesta 201:** `SoporteChatResponseDto`.

### `POST /api/soporte/{chatId}/mensajes` *(USER / ADMIN)*
Envía un mensaje en un chat existente. El usuario solo puede escribir en sus propios chats; los admins pueden escribir en cualquiera.

**Body:**
```json
{ "contenido": "Gracias, ya lo revisamos." }
```
**Respuesta 201:** `MensajeSoporteResponseDto` con `esAdmin: true/false` para distinguir quién escribe.

Al responder un admin, el chat pasa automáticamente a `EN_PROGRESO`.

### `GET /api/soporte/mis-chats` *(USER / ADMIN)*
Lista los chats del usuario autenticado, ordenados por última actividad. Incluye `mensajesNoLeidos`.

| Param | Default |
|---|---|
| `page` | 0 |
| `size` | 10 |

### `GET /api/soporte` *(solo ADMIN)*
Lista todos los chats de soporte. Acepta filtro por estado.

| Param | Tipo | Descripción |
|---|---|---|
| `estado` | string | Filtra por `ABIERTO`, `EN_PROGRESO`, `RESUELTO`, `CERRADO` |
| `page` | int | 0 |
| `size` | int | 20 |

### `GET /api/soporte/{chatId}/mensajes` *(USER / ADMIN)*
Lista todos los mensajes de un chat en orden cronológico. Accesible solo por el dueño del chat o un admin.

**Respuesta 200:** Array de `MensajeSoporteResponseDto`.

### `PUT /api/soporte/{chatId}/estado` *(solo ADMIN)*
Cambia el estado de un chat.

**Body:**
```json
{ "estado": "RESUELTO" }
```
Estados: `ABIERTO`, `EN_PROGRESO`, `RESUELTO`, `CERRADO`.

Al cerrar/resolver se envía una notificación automática al usuario.

### `PUT /api/soporte/{chatId}/mensajes/leer` *(USER / ADMIN)*
Marca como leídos los mensajes del chat que no fueron enviados por el usuario autenticado.

**Respuesta 200:**
```json
{ "mensajesMarcados": 3 }
```

---

## 16. Políticas y reglas

Base path: `/api/politicas` | Los GETs son públicos; escritura requiere rol ADMIN.

**Tipos disponibles:** `POLITICA_PRIVACIDAD`, `TERMINOS_USO`, `REGLAS_COMUNIDAD`, `FAQ`, `OTRO`

### `GET /api/politicas` *(público)*
Lista todas las políticas activas. Acepta filtro por tipo.

| Param | Tipo | Descripción |
|---|---|---|
| `tipo` | string | Filtra por tipo (opcional) |

**Respuesta 200:** Array de `PoliticaResponseDto`.

### `GET /api/politicas/todas` *(solo ADMIN)*
Lista todas las políticas (activas e inactivas), útil para el panel de administración.

### `GET /api/politicas/{id}` *(público)*
Obtiene el detalle de una política por ID.

**Respuesta 200:**
```json
{
  "id": 1,
  "tipo": "TERMINOS_USO",
  "titulo": "Términos y condiciones de uso",
  "contenido": "...",
  "activo": true,
  "orden": 1,
  "creadoEn": "2025-01-01T00:00:00",
  "actualizadoEn": "2025-01-01T00:00:00"
}
```

### `POST /api/politicas` *(solo ADMIN)*
Crea una nueva política o regla.

**Body:**
```json
{
  "tipo": "REGLAS_COMUNIDAD",
  "titulo": "Reglas de publicación",
  "contenido": "Solo se permiten artículos en buen estado...",
  "activo": true,
  "orden": 2
}
```

### `PUT /api/politicas/{id}` *(solo ADMIN)*
Actualiza una política existente.

### `DELETE /api/politicas/{id}` *(solo ADMIN)*
Elimina permanentemente una política.

### `PATCH /api/politicas/{id}/toggle` *(solo ADMIN)*
Activa o desactiva una política sin eliminarla. Útil para publicar/despublicar temporalmente.

**Respuesta 200:** `PoliticaResponseDto` con el nuevo valor de `activo`.

---

## 17. Health-check

### `GET /ping`  *(público)*
Devuelve `pong` para verificar que la API está viva.

---

## Variables de entorno relevantes

| Variable | Default | Para qué sirve |
|---|---|---|
| `EMAIL_ENABLED` | `false` | Activa el envío real de emails por SMTP |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` | gmail/587 | Credenciales del servidor SMTP |
| `EMAIL_FROM` | `no-reply@economia-circular.local` | Remitente de los emails |
| `EMAIL_VERIFICATION_REQUIRED` | `false` | Si es `true`, el login rechaza usuarios con email no verificado |
| `EMAIL_VERIFY_URL_BASE` | `http://localhost:8080/api/auth/verify-email` | Base del enlace que va en el email de verificación |
| `PASSWORD_RESET_URL_BASE` | `http://localhost:8080/reset-password` | Base del enlace de recuperación que recibe el usuario |
| `ARTICLE_IMAGES_MAX` | `5` | Máximo de imágenes por artículo |
| `ARTICLE_IMAGES_MAX_SIZE_BYTES` | `5242880` (5 MB) | Tamaño máximo por imagen |

---

## Documentación interactiva

El proyecto incluye **Swagger UI** en `GET /swagger-ui.html` y el descriptor OpenAPI en `GET /v3/api-docs`. Es la forma recomendada para probar endpoints durante el desarrollo del frontend.
