# Deploy gratuito: Render (backend) + Aiven (MySQL) + Vercel (front)

Cada push a `main` en GitHub corre los tests (GitHub Actions) y, si pasan, Render despliega solo.

```
GitHub (main) ──► GitHub Actions: ./mvnw test ──► Render: build del Dockerfile + deploy
                                                         │
Vercel (front) ──── VITE_API_URL ───────────────────────►│──► Aiven MySQL (SSL)
```

Archivos involucrados: `Dockerfile`, `.dockerignore`, `render.yaml`, `.github/workflows/ci.yml`.

---

## 1. Base de datos en Aiven

1. Crear cuenta en <https://aiven.io> y un servicio **MySQL** con el plan **Free**.
   Elegir una región cercana a la de Render (el `render.yaml` usa `oregon`, EE. UU. oeste).
2. Cuando el servicio esté en *Running*, en **Overview → Connection information** anotar:
   `Host`, `Port`, `User` (`avnadmin`), `Password` y `Database name` (`defaultdb`).
3. **Importante:** en **Service settings → Advanced configuration** agregar
   `mysql.sql_require_primary_key` = **false** y guardar.
   Aiven lo trae activado y Liquibase falla al crear su tabla de control (`DATABASECHANGELOG` no tiene clave primaria).
4. Armar la URL JDBC (Aiven exige SSL):

   ```
   jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED&serverTimezone=UTC
   ```

La base arranca vacía: al primer arranque Liquibase (`LIQUIBASE_ENABLED=true`) crea todas las tablas y carga los datos iniciales del contexto `prod` (usuarios de ejemplo, políticas, etc.).

---

## 2. Backend en Render

1. Crear cuenta en <https://render.com> e iniciar sesión con GitHub
   (quien tenga permisos sobre el repo `economiacircular` tiene que autorizar la app de Render).
2. **New → Blueprint** → elegir el repo. Render lee `render.yaml` y propone el servicio `economia-circular-api`.
3. Completar las variables marcadas como secretas:

   | Variable | Valor |
   |---|---|
   | `DB_URL` | la URL JDBC del paso 1.4 |
   | `DB_USERNAME` | `avnadmin` |
   | `DB_PASSWORD` | la contraseña de Aiven |
   | `PASSWORD_RESET_URL_BASE` | `https://TU-FRONT.vercel.app/restablecer-contrasena` |
   | `EMAIL_VERIFY_URL_BASE` | `https://TU-FRONT.vercel.app/verificar-email` |
   | `GOOGLE_CLIENT_ID` | el mismo que usa el front (o vacío para desactivar Google) |
   | `ANTHROPIC_API_KEY` | opcional; sin key la IA queda oculta |
   | `SMTP_USERNAME`, `SMTP_PASSWORD`, `EMAIL_FROM` | opcional; para enviar mails poner además `EMAIL_ENABLED=true` |

4. **Apply**. El primer build tarda varios minutos (descarga dependencias de Maven).
5. Probar: `https://economia-circular-api.onrender.com/ping` debe responder `OK`.
   La documentación de la API queda en `/swagger-ui.html`.

> Si Render no reconoce `autoDeployTrigger: checksPass` en `render.yaml`, reemplazarlo por `autoDeploy: true`
> (despliega en cada push sin esperar los tests) o configurarlo en **Settings → Build & Deploy → Auto-Deploy → After CI checks pass**.

### Limitaciones del plan gratuito
- El servicio **se duerme tras ~15 minutos sin tráfico**; la primera petición después tarda 30–60 s en despertarlo.
- 512 MB de RAM (el `Dockerfile` limita la JVM al 75 %).
- Aiven Free tiene poco almacenamiento: fotos, videos y audios se guardan **dentro de MySQL**, así que unos pocos videos lo llenan. Para la demo alcanza.

---

## 3. Front en Vercel

1. En el proyecto de Vercel del front: **Settings → Environment Variables**:
   - `VITE_API_URL` = `https://economia-circular-api.onrender.com/api`
   - `VITE_GOOGLE_CLIENT_ID` = el client ID de Google
2. **Redeploy** (las variables `VITE_*` se leen al compilar).
3. Si se usa login con Google: en Google Cloud Console → *Credentials* → el OAuth client, agregar el dominio de Vercel en **Authorized JavaScript origins**.

---

## 4. Flujo de trabajo

- Trabajar en una rama, abrir pull request a `main`: GitHub Actions corre los tests en el PR.
- Al mergear a `main`: corren los tests y, si pasan, Render despliega.
- Logs del backend: Render → servicio → **Logs**.

---

## 5. Seguridad

- La contraseña de la base anterior (Cloud SQL) quedó en el **historial de git** de este repo (`cloudbuild.yaml` / `app.yaml`, ya borrados).
  No reutilizarla. Las credenciales nuevas van solo en el panel de Render, nunca en archivos del repo.
- El código de verificación de celular hoy se escribe en el log (`LogSmsSender`); para producción hay que conectar un proveedor de SMS.
