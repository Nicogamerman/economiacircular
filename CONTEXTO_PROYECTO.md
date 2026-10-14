# Economía Circular — Contexto de ambos proyectos

_Última actualización: 2026-10-13_

Tesis "Economía Circular": plataforma para intercambiar, donar o vender artículos usados, con centros de reciclaje, talleres, eventos/ferias, chat, valoraciones, reportes y funciones de IA.

| Proyecto | Carpeta | Stack | Repo remoto |
|---|---|---|---|
| Backend | `economiacircular/` | Spring Boot 2.7, Java 8, MySQL, Liquibase, JWT, SDK de Claude | `git@github.com:Nicogamerman/economiacircular.git` |
| Frontend | `EconomiaCircularFront/` | React 19, Vite 7, MUI 7, Tailwind 4, react-hook-form, recharts, react-leaflet, zustand | `git@github.com:NicolasPotelJunot/EconomiaCircularFront.git` |

---

## 1. Estado de las ramas

Ambos repos están en **`feature/last_update`**, **sin pushear y con todos los cambios de esta sesión sin commitear**.

- **Backend** = `fix/full-project-audit` + `claude/beautiful-ptolemy-MBLEE`: notificaciones unificadas (modelo claude + compatibilidad `GET /no-leidas`, `PATCH /leer`, `articuloId`), recuperación de contraseña con `PasswordResetService` (se eliminó `PasswordRecoveryService`), changeset Liquibase `13-unificar-notificaciones`.
- **Front** = `fix/full-project-audit` + `feature/login` + fix del login con Google (manda `idToken`, client ID en `VITE_GOOGLE_CLIENT_ID`).

Pendiente: probar el front (`npm install`, `npm test`, `npm run build`), commitear y pushear.

---

## 2. Cómo correr

### Backend
```bash
cd economiacircular
./mvnw test        # 394 tests, todos en verde
./mvnw spring-boot:run   # puerto 8081
```
`JAVA_HOME` = `C:\Program Files\Java\jdk1.8.0_202` (java no está en el PATH; el wrapper usa JAVA_HOME). Tests usan H2 en memoria.

### Frontend
```bash
cd EconomiaCircularFront
npm install
npm test           # Vitest + Testing Library (13 archivos, nunca ejecutados todavía)
npm run dev
npm run build      # tarda ~7 min en OneDrive
```
Node 24.20.0 instalado con winget. npm bloquea el postinstall de `esbuild` (el build funciona igual).

---

## 3. Configuración / variables de entorno

### Backend (`application.properties`)
| Variable | Default | Uso |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | MySQL local `eco_circular`, `root`, `devpass` | Base de datos |
| `LIQUIBASE_ENABLED` | `false` | En local el esquema lo mantiene `ddl-auto=update`; en prod usar `true` |
| `ANTHROPIC_API_KEY` | vacío | Sin key, la IA queda desactivada y el front la oculta |
| `IA_MODEL` | `claude-opus-5-5` | Modelo de Claude |
| `IA_MAX_PEDIDOS_POR_HORA` | `30` | Límite de consultas IA por usuario |
| `EMAIL_ENABLED`, `SMTP_*`, `EMAIL_FROM` | `false` | Sin esto no se envían mails (recuperar contraseña, verificación) |
| `EMAIL_VERIFICATION_REQUIRED` | `false` | Exigir email verificado para loguear |
| `EMAIL_VERIFY_URL_BASE` | `http://localhost:5173/verificar-email` | Link del mail de verificación (front) |
| `PASSWORD_RESET_URL_BASE` | `http://localhost:5173/restablecer-contrasena` | Link del mail de recuperación (front) |
| `GOOGLE_CLIENT_ID` | vacío | Login con Google |
| `DEVOLUCIONES_PLAZO_DIAS` | `7` | Plazo para pedir devolución |
| `ARTICLE_IMAGES_MAX` / `_MAX_SIZE_BYTES` | 5 / 5 MB | Fotos por artículo |
| `ARTICLE_VIDEO_MAX_SIZE_BYTES` | 30 MB | Video por artículo |

Multipart: `max-file-size=35MB`, `max-request-size=40MB` (cada servicio valida su límite).

### Front (`.env.local`, ver `.env.example`)
- `VITE_API_URL` — URL del backend con `/api` (default `http://localhost:8081/api`). En Vercel apuntarlo a Cloud Run.
- `VITE_GOOGLE_CLIENT_ID` — mismo client ID que el backend.

### Dependencias agregadas
- Backend: `spring-boot-starter-validation` (antes **ninguna** validación `@Valid` funcionaba), `com.anthropic:anthropic-java:2.34.0`; se fijaron `jackson-bom.version=2.18.2`, `kotlin.version=1.8.0`, `okhttp.version=4.12.0` (lo requiere el SDK). Se quitó un `spring-boot-starter-mail` duplicado.
- Front (devDependencies, sin instalar aún): `vitest`, `jsdom`, `@testing-library/react|dom|jest-dom|user-event`. Scripts `test` y `test:watch`.

---

## 4. Funcionalidades (backlog) y dónde están

| Historia | Backend | Front |
|---|---|---|
| Confirmar email | `/api/auth/verify-email`, `/resend-verification` | `/verificar-email`, reenvío en Login, aviso al registrarse |
| Confirmar celular | `POST /api/usuarios/me/telefono`, `/me/telefono/verificar` (código 6 dígitos, 10 min, 5 intentos, hash SHA-256) | Configuración → "Verificar Celular" |
| Imágenes (hasta 5) | `/api/articles/{id}/images`; `imagenPrincipalId` en respuestas de artículos | `ImagenesArticuloInput` en los 3 formularios, galería en detalle |
| Video con opción de audio | `/api/articles/{id}/video` (GET meta, GET `/file` con soporte Range, POST, PATCH `conAudio`, DELETE) | `VideoArticuloInput`, reproductor en detalle |
| Mejor categorización | subcategoría, marca, modelo, etiquetas; `/articles/subcategories`, `/brands`, `/tags` | `CamposCategorizacion` en formularios + filtros en `/articulos` |
| IA al cargar imagen | `POST /api/ia/sugerir-articulo` | Botón "Completar con IA" |
| Comentarios/puntajes del producto | `/api/valoraciones/articulo/{id}`, `POST /api/valoraciones` (requiere intercambio completado) | Sección en detalle; "Valorar" en Intercambios |
| Valoraciones del oferente | `/api/oferentes/{id}/valoraciones` (moderadas) | Detalle del artículo; `/mis-valoraciones` |
| Chat con oferente | `/api/mensajes/*`, `GET /mis-mensajes` (nuevo) | `/mensajes` (bandeja + hilo) |
| Mensajes de audio | `POST /api/mensajes/enviar-audio`, `GET /mensajes/{id}/audio` (tabla `mensajes_audio`) | Grabador (MediaRecorder, máx 3 min) y reproductor |
| Notificaciones | `/api/notificaciones`; tipos nuevos: `NUEVO_ARTICULO_SEGUIDO`, `EVENTO_NUEVO`, `EVENTO_ACTUALIZADO`, `DEVOLUCION`, `VERIFICACION_IDENTIDAD` | Campanita con polling 30 s y navegación por tipo (`destinoNotificacion.js`) |
| Intercambios + historial | `/api/intercambios/*` | `/intercambios` (Recibidas/Enviadas/Historial) |
| Devolución | `POST /api/intercambios/{id}/devolucion`, `/api/devoluciones/{id}/responder`, `/cancelar`, `/mias`, `/plazo`, admin `GET /api/devoluciones`. Al aceptar: intercambio `DEVUELTO`, artículos `DISPONIBLE` | Botones en Intercambios; admin `/admin/devoluciones` |
| Favoritos | `/api/favoritos/*` | `/favoritos`, botón en detalle |
| Seguir oferentes/eventos | `/api/seguimientos` (GET, `/feed`, `/{oferentes|eventos}/{id}` GET/POST/DELETE, `/intereses` GET/PUT) | `BotonSeguir` (detalle y Eventos), `/siguiendo` |
| Avisos de eventos por interés / ferias seguidas | Hooks en `EventService` (crear, cambiar fecha/lugar, cancelar) y `ArticleService` (publicar/aprobar) | Preferencias en `/siguiendo` |
| Métricas del oferente y de mis intercambios | `GET /api/usuarios/me/metricas` | `/mis-metricas` (recharts) |
| Dashboard admin + mapas | `/api/reports/*`, `/api/metrics/geo/*`, `/api/admin/dashboard/**` (ahora solo ADMIN) | `/admin/reportes` + pestaña Mapa |
| Impacto ambiental | `/api/reports/environmental`; métricas personales reutilizan los factores | Reportes / Mis métricas |
| Chat con soporte | `/api/soporte/*` | `/soporte` (usuario), `/admin/soporte` |
| Políticas y reglas | `/api/politicas/*`; tipo nuevo `DEVOLUCIONES` | `/politicas` (pública, Markdown seguro), `/admin/politicas` |
| Verificación anti-fraude (IA) | `/api/verificacion/identidad` (POST doc+selfie, GET último, admin `/pendientes`, PUT resolver). No guarda imágenes | Configuración → "Verificar Identidad", admin `/admin/verificaciones`, insignia en detalle |
| Chatbot | `POST /api/ia/chat`, `GET /api/ia/estado` | Botón flotante `Chatbot` |
| Recuperar contraseña | `/api/auth/forgot-password`, `/validate-reset-token`, `/confirm-reset-password` | `/recuperar-contrasena`, `/restablecer-contrasena` |
| Configuración de cuenta | `/api/usuarios/me`, `/me/foto` | Perfil, foto, contraseña, celular, identidad |

Endpoint público nuevo para el Home: `GET /api/stats/public` (antes el Home usaba `/admin/dashboard`).

---

## 5. Decisiones de diseño relevantes

- **IA**: interfaz `IaClient` + implementación `ClaudeIaClient` (SDK oficial, structured outputs con POJOs). Servicios testeables con mocks. `LimitadorIa` en memoria (una sola instancia). Rechazos/errores → `IaException` (503 si no disponible).
- **Verificación de identidad**: la IA no compara rostros; evalúa autenticidad del documento, coincidencia del nombre y que la selfie muestre a la persona con el documento. Aprobación automática solo si todo da bien y confianza ≥ 70; dudas → revisión manual del admin.
- **SMS**: interfaz `SmsSender`; `LogSmsSender` escribe el código en el log. Para producción registrar otra implementación con `@Primary` (Twilio, SNS…).
- **Circularidad**: `SeguimientoService` inyecta `ArticleService` con `@Lazy`.
- **Base de datos**: local con `ddl-auto=update`; producción con Liquibase. Changesets nuevos: `14-backlog-final` (tablas `seguimientos`, `intereses_eventos`, `devoluciones`, `videos_articulos`, `mensajes_audio`, `telefono_verificaciones`, `verificaciones_identidad`; columnas nuevas en `usuario` y `mensajes`; enum de `politicas`) y `15-seed-politica-devoluciones` (contexto prod).
- **Front**: estilos compartidos en `src/assets/estilos.js` (verde `#92A48B`, hover `#7C8F73`, oscuro `#3E4C3D`), hook `useAviso`, `Cargando`, `Markdown` (no inserta HTML). Formularios de artículo usan `useExtrasArticulo` + `ExtrasArticulo`. Listas de categorías/condiciones en `opcionesArticulo.js` (incluye `DECORACION_HOGAR` y `SUMINISTROS_ARTE`).
- **MUI 7**: se migró `<Grid item xs>` a `<Grid size={{...}}>` en todo el front.
- **Pautas**: las pantallas nuevas mantienen el estilo existente; el usuario corre él mismo build y tests del front.

---

## 6. Tests

- **Backend**: 394 tests en verde (303 previos + 91 nuevos): `SeguimientoServiceTest`, `DevolucionServiceTest`, `MetricasUsuarioServiceTest`, `TelefonoVerificacionServiceTest`, `VideoArticuloServiceTest`, `MensajeAudioServiceTest`, `VerificacionIdentidadServiceTest`, `EventServiceSeguimientoTest`, `ia/IaServiceTest`, `ia/LimitadorIaTest`, `ia/ClaudeIaClientTest`, `controller/NuevasFuncionesControllerTest` (incluye 206 Partial Content del video). `TestSecurityConfig` replica las reglas públicas nuevas.
- **Front** (sin ejecutar aún): `destinoNotificacion`, `Markdown`, `useExtrasArticulo`, `RecuperacionContrasena` (recuperar/restablecer/verificar email), `IntercambiosPage`, `MensajesPage`, `BotonSeguir`, `Chatbot`, `ArticuloInputs` (IA, fotos, video), `VerificarCelular`, `PoliticasPage`, `FavoritosPage`, `SiguiendoPage`. Setup en `src/test/setup.js`, helpers en `src/test/utils.jsx`.

---

## 7. Pendientes y advertencias

1. **Probar el front**: `npm install`, `npm test`, `npm run build`, y prueba manual de los flujos.
2. **Commit y push** de `feature/last_update` en ambos repos.
3. **Tabla `notificaciones` local**: si tiene el esquema viejo (columna `usuario_id`), borrarla antes de arrancar (Hibernate no la corrige).
4. **Secreto en el repo**: `app.yaml` (backend) tiene versionada la contraseña de la base de producción. Sacarla y, si el repo fue público, rotarla.
5. **Producción**: configurar `VITE_API_URL`, `ANTHROPIC_API_KEY`, SMTP + `EMAIL_ENABLED=true`, `PASSWORD_RESET_URL_BASE` / `EMAIL_VERIFY_URL_BASE` con el dominio real, `LIQUIBASE_ENABLED=true`, proveedor de SMS.
6. **IA**: no se activó el fallback de modelo ante rechazos (el usuario ve un mensaje de error).
7. **Lint previo** (no introducido en esta sesión): `RegisterUsuario.jsx` (variable sin usar) y `MyArticlesPage.jsx` (dependencia de hook).
8. **Nombre "EcoCórdoba" (comentario del profesor)**: el nombre limita la plataforma a la ciudad de Córdoba; conviene uno más general. Referencias encontradas:
   - `README.md` raíz: URLs `https://eco-cordoba.vercel.app/...` (dominio de Vercel).
   - `IaService.java`: el prompt del chatbot dice "una plataforma de Córdoba (Argentina)".
   - `MapaReport.jsx`: el mapa del admin arranca centrado en Córdoba (es solo el centro inicial; luego encuadra los puntos).
   - Falta revisar el logo (`src/assets/Logo Economia Circular.svg`), el `<title>` de `index.html` y el nombre del proyecto en Vercel.
   - Los seeds mencionan "Av. Córdoba" como dirección: no es parte del nombre.
