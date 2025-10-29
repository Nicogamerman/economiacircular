# ⚡ Quick Start - Economía Circular

## 🚀 Inicio Rápido (3 pasos)

### 1️⃣ Crear la base de datos

```sql
-- En MySQL
CREATE DATABASE IF NOT EXISTS economia_circular 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2️⃣ Iniciar la aplicación

**Opción A: Usando el script**
```powershell
# Desarrollo (datos mínimos)
.\run-app.ps1 dev

# Producción (datos completos)
.\run-app.ps1 prod
```

**Opción B: Comando directo**
```bash
# Desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Producción
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 3️⃣ ¡Listo! 🎉

La aplicación estará en: `http://localhost:8080`

## 🔑 Usuarios de Prueba

### Desarrollo
- **Admin:** `admin@test.com` / `Test123!`
- **Usuario:** `test@test.com` / `Test123!`

### Producción
- **Admin:** `admin@economiacircular.com` / `Test123!`
- **Usuario:** `maria.gonzalez@email.com` / `Test123!`

## 📊 ¿Qué se crea automáticamente?

### Modo Desarrollo (`dev`)
- ✅ Todas las tablas
- ✅ 4 usuarios
- ✅ 5 artículos
- ✅ 2 eventos
- ✅ 2 centros de reciclaje
- ✅ Datos relacionados

### Modo Producción (`prod`)
- ✅ Todas las tablas
- ✅ 11 usuarios
- ✅ 36 artículos (todas las categorías)
- ✅ 8 eventos
- ✅ 10 centros de reciclaje
- ✅ 8 talleres
- ✅ Mensajes, solicitudes, estadísticas

## 🔍 Verificar que funcionó

```sql
-- Ver tablas creadas
SHOW TABLES;

-- Ver usuarios
SELECT id, email, nombre FROM usuario;

-- Ver artículos
SELECT id, titulo, categoria, estado FROM articulos;

-- Ver control de Liquibase
SELECT id, author, filename, exectype, dateexecuted 
FROM databasechangelog;
```

## ❓ ¿Problemas?

### "Can't connect to MySQL"
→ Verifica que MySQL esté corriendo
→ Verifica usuario/contraseña en `application-dev.properties`

### "Table already exists"
→ Sincroniza: `mvn liquibase:changelogSync`
→ O borra y recrea la base de datos

### "Waiting for changelog lock"
→ Ejecuta: `UPDATE databasechangeloglock SET locked = 0;`

## 📚 Más Información

- **Guía completa:** `LIQUIBASE_SETUP.md`
- **Documentación de scripts:** `economia_circular.sql/README.md`
- **Documentación técnica:** `src/main/resources/db/changelog/README.md`

## 🎯 Comandos Útiles

```bash
# Ver ayuda del script
.\run-app.ps1 help

# Ejecutar tests
.\run-app.ps1 test

# Ver estado de migraciones
mvn liquibase:status

# Limpiar y compilar
mvn clean package

# Desplegar a Google Cloud
mvn clean package appengine:deploy
```

---

¡Listo para usar! 🚀

