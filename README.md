1. Recuperación de contraseña (“Olvidé mi contraseña”)
POST /api/auth/forgot-password
Body: { "email": "usuario@ejemplo.com" }
Genera un token de un solo uso (válido 1 hora).
Envía un correo con el enlace (si está configurado el mail) o lo escribe en consola en desarrollo.
La respuesta es siempre la misma por seguridad (no se indica si el email existe).
POST /api/auth/reset-password-with-token
Body: { "token": "...", "newPassword": "NuevaPassword123!" }
O token en query: ?token=...
Comprueba el token, actualiza la contraseña y invalida el token.
Base de datos: tabla password_reset_token y migración en Liquibase (03-password-reset-and-oauth.sql).
Email: si en application.properties configuras spring.mail.*, se enviará el correo real; si no, solo se loguea el enlace en consola.
URL del enlace: configurable con app.password-reset.base-url (por defecto http://localhost:8080). En producción conviene poner la URL del frontend donde el usuario introduce la nueva contraseña.
2. Login con Google y vincular cuenta
POST /api/auth/google
Body: { "idToken": "..." }
El frontend obtiene idToken con Google Sign-In (por ejemplo con la librería de Google para web o móvil).
El backend verifica el token con Google, busca o crea el usuario y devuelve tu JWT.
Si el email ya existe (cuenta creada con email/contraseña), se vincula automáticamente esa cuenta con ese Google.
POST /api/auth/link-google (requiere estar logueado con JWT)
Headers: Authorization: Bearer <tu-jwt>
Body: { "idToken": "..." }
Asocia la cuenta de Google actual a la cuenta ya autenticada (mismo email).
A partir de ahí el usuario puede entrar con “Iniciar con Google” o con email/contraseña.
Usuario solo Google: si alguien entra solo con Google, no tiene contraseña local; si intenta login por email/contraseña se le indica que use “Iniciar con Google”.
Configuración: en application.properties (o variables de entorno) debes definir el Client ID de Google:
app.google.client-id=TU_CLIENT_ID.apps.googleusercontent.com
Para obtenerlo: Google Cloud Console → APIs & Services → Credentials → crear o usar un “OAuth 2.0 Client ID” (tipo “Web application” o “Android/iOS” según tu cliente). Si no configuras app.google.client-id, el login con Google responderá 503 (no configurado).
3. Cambios en el modelo y seguridad
Usuario: nuevos campos google_id y auth_provider ("local" o "google"). Los usuarios creados por registro normal tienen auth_provider = "local".
Seguridad:
/api/auth/link-google exige JWT.
El resto de /api/auth/** sigue siendo público (login, registro, forgot-password, reset-with-token, google).
4. Cómo probar en desarrollo
Recuperación de contraseña:
POST /api/auth/forgot-password con { "email": "test@test.com" }.
Revisar consola por el enlace (si no hay mail configurado).
POST /api/auth/reset-password-with-token con { "token": "<token_del_enlace>", "newPassword": "nueva123" }.
Google:
Configurar app.google.client-id con tu Client ID de Google.
En el frontend, integrar Google Sign-In, obtener el idToken y enviarlo a POST /api/auth/google.
Para vincular una cuenta ya existente: login con JWT y luego POST /api/auth/link-google con el idToken de Google (mismo email).
Si quieres, el siguiente paso puede ser un ejemplo mínimo de frontend (HTML/JS o React) para “Olvidé mi contraseña” y “Iniciar con Google” contra estos endpoints.