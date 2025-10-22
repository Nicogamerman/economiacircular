# 🚀 Guía de Configuración de Base de Datos con Liquibase

## 📋 Descripción General

Este proyecto utiliza **Liquibase** para gestionar automáticamente la base de datos. Cuando la aplicación se inicia, Liquibase:

1. ✅ Crea automáticamente todas las tablas necesarias
2. ✅ Inserta datos iniciales (según el ambiente)
3. ✅ Mantiene un control de versiones de la base de datos
4. ✅ Solo ejecuta cambios que no se hayan aplicado antes

## 🎯 ¿Qué cambió?

### Antes
- ❌ Había que ejecutar manualmente scripts SQL
- ❌ No había control de qué scripts se ejecutaron
- ❌ Podía haber inconsistencias entre ambientes

### Ahora
- ✅ Todo se ejecuta automáticamente al iniciar la app
- ✅ Liquibase controla qué cambios se aplicaron
- ✅ Diferentes datos para desarrollo y producción
- ✅ Sistema robusto y profesional

## 📁 Estructura del Proyecto

```
economiacircular/
├── pom.xml                                    # ✅ Incluye dependencia de Liquibase
├── app.yaml                                   # ✅ Configurado con perfil 'prod'
│
├── src/main/resources/
│   ├── application.properties                 # ✅ Configuración principal (prod)
│   ├── application-dev.properties             # ✅ Configuración desarrollo
│   ├── application-prod.properties            # ✅ Configuración producción
│   │
│   └── db/changelog/
│       ├── README.md                          # 📖 Documentación Liquibase
│       ├── db.changelog-master.yaml           # 🎯 Archivo maestro
│       │
│       └── sql/
│           ├── 01-schema.sql                  # Estructura de tablas
│           ├── 02-seed-data-produccion.sql    # Datos completos
│           └── 02-seed-data-desarrollo.sql    # Datos mínimos
│
└── economia_circular.sql/
    ├── README.md                              # Documentación de scripts
    ├── seed_data_produccion.sql               # Backup
    └── seed_data_desarrollo.sql               # Backup
```

## 🚀 Guía de Inicio Rápido

### 1️⃣ Desarrollo Local

```bash
# Asegúrate de tener MySQL corriendo en localhost:3306
# Usuario: root, Password: root

# Crear la base de datos
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS economia_circular CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Ejecutar la aplicación en modo desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# O desde tu IDE, agregar VM argument:
# -Dspring.profiles.active=dev
```

**¿Qué pasa?**
- Se crean todas las tablas
- Se insertan datos mínimos (4 usuarios, 5 artículos, etc.)
- Perfecto para desarrollo rápido

### 2️⃣ Producción (Google Cloud)

```bash
# Desplegar a Google Cloud
mvn clean package
gcloud app deploy

# O solo:
mvn clean package appengine:deploy
```

**¿Qué pasa?**
- Se crean todas las tablas
- Se insertan datos completos (11 usuarios, 36 artículos, eventos, etc.)
- Datos realistas para pruebas en producción

### 3️⃣ Solo crear tablas (sin datos)

Si solo quieres la estructura sin datos iniciales:

```bash
# Opción 1: Deshabilitar Liquibase
mvn spring-boot:run -Dspring.liquibase.enabled=false

# Opción 2: Usar JPA con ddl-auto=update (cambiar en application.properties)
spring.jpa.hibernate.ddl-auto=update
spring.liquibase.enabled=false
```

## 🎭 Perfiles de Ejecución

### Perfil: `dev` (Desarrollo)

```properties
# Base de datos
MySQL local (localhost:3306)

# Datos insertados
- 4 usuarios
- 5 artículos
- 2 eventos
- 2 centros de reciclaje
- 2 talleres
- Mensajes y solicitudes de prueba

# Usuarios
admin@test.com / Test123!
test@test.com / Test123!
maria@test.com / Test123!
juan@test.com / Test123!
```

### Perfil: `prod` (Producción)

```properties
# Base de datos
Google Cloud SQL

# Datos insertados
- 11 usuarios (1 admin + 10 users)
- 36 artículos (todas las categorías)
- 8 eventos futuros
- 10 centros de reciclaje
- 8 talleres
- 13 mensajes
- 9 solicitudes de intercambio
- 30+ vistas de artículos

# Usuario admin
admin@economiacircular.com / Test123!

# Usuarios regulares
maria.gonzalez@email.com / Test123!
juan.perez@email.com / Test123!
... y 8 más
```

## ⚙️ Configuraciones Importantes

### pom.xml

```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

### application.properties

```properties
# Importante: ddl-auto en 'validate' para que Liquibase controle la estructura
spring.jpa.hibernate.ddl-auto=validate

# Liquibase habilitado
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
spring.liquibase.contexts=${liquibase.context:prod}
```

## 🔍 Verificar que Funciona

### Desde la base de datos

```sql
-- Ver qué cambios aplicó Liquibase
SELECT * FROM databasechangelog;

-- Debería mostrar:
-- 1-create-schema (EXECUTED)
-- 2-seed-data-produccion (EXECUTED) o 2-seed-data-desarrollo (EXECUTED)

-- Verificar datos
SELECT COUNT(*) FROM usuario;
SELECT COUNT(*) FROM articulos;
SELECT COUNT(*) FROM events;
```

### Desde los logs de la aplicación

```
[INFO] Successfully acquired change log lock
[INFO] Reading from economia_circular.databasechangelog
[INFO] db/changelog/sql/01-schema.sql: Changeset executed
[INFO] db/changelog/sql/02-seed-data-produccion.sql: Changeset executed
[INFO] Successfully released change log lock
```

## 🐛 Problemas Comunes

### 1. "Table 'usuario' already exists"

**Causa:** Las tablas ya existen de una ejecución anterior.

**Solución A** (Sincronizar sin ejecutar):
```bash
mvn liquibase:changelogSync
```

**Solución B** (Limpiar todo):
```sql
-- En MySQL
DROP DATABASE IF EXISTS economia_circular;
CREATE DATABASE economia_circular CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Reiniciar la app
```

### 2. "Waiting for changelog lock"

**Causa:** El lock quedó bloqueado de una ejecución anterior.

**Solución:**
```sql
-- Liberar el lock
UPDATE databasechangeloglock SET locked = 0, lockgranted = NULL, lockedby = NULL;
```

### 3. Cambié el archivo SQL y no se aplica

**Causa:** Liquibase detecta que el changeSet ya fue ejecutado (por el ID).

**Solución:** No modifiques changeSets ya aplicados. Crea uno nuevo:
```yaml
# En db.changelog-master.yaml
- changeSet:
    id: 3-mi-nuevo-cambio  # Nuevo ID
    author: tu-nombre
    changes:
      - sqlFile:
          path: db/changelog/sql/03-mi-cambio.sql
```

### 4. Quiero empezar desde cero

```sql
-- CUIDADO: Esto borra TODO
SET FOREIGN_KEY_CHECKS = 0;

-- Eliminar todas las tablas
DROP TABLE IF EXISTS vistas_articulos;
DROP TABLE IF EXISTS solicitudes_intercambio;
DROP TABLE IF EXISTS mensajes;
DROP TABLE IF EXISTS imagenes_articulos;
DROP TABLE IF EXISTS articulos;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS recycling_centers;
DROP TABLE IF EXISTS taller;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS databasechangelog;
DROP TABLE IF EXISTS databasechangeloglock;

SET FOREIGN_KEY_CHECKS = 1;

-- Reiniciar la aplicación
```

## 🔄 Flujo de Trabajo

### Primera vez (base de datos vacía)

```
1. Inicias la app
2. Liquibase detecta que la BD está vacía
3. Ejecuta 01-schema.sql → Crea tablas
4. Ejecuta 02-seed-data-*.sql → Inserta datos
5. Registra en databasechangelog que ya se ejecutó
6. App lista para usar ✅
```

### Segunda vez en adelante

```
1. Inicias la app
2. Liquibase revisa databasechangelog
3. Ve que todos los changeSets ya fueron ejecutados
4. No hace nada, solo continúa
5. App lista para usar ✅
```

### Si agregas un nuevo changeSet

```
1. Creas 03-nuevo-cambio.sql
2. Lo agregas a db.changelog-master.yaml
3. Inicias la app
4. Liquibase detecta el nuevo changeSet
5. Lo ejecuta
6. Lo registra en databasechangelog
7. App lista para usar ✅
```

## 📝 Agregar Nuevas Migraciones

### Paso 1: Crear archivo SQL

```bash
# Crear archivo
echo "ALTER TABLE usuario ADD COLUMN telefono VARCHAR(20);" > src/main/resources/db/changelog/sql/03-add-telefono.sql
```

### Paso 2: Registrar en changelog maestro

```yaml
# db.changelog-master.yaml
databaseChangeLog:
  # ... changeSets existentes ...
  
  - changeSet:
      id: 3-add-telefono
      author: tu-nombre
      changes:
        - sqlFile:
            path: db/changelog/sql/03-add-telefono.sql
      rollback:
        - sql: ALTER TABLE usuario DROP COLUMN telefono;
```

### Paso 3: Reiniciar la app

```bash
mvn spring-boot:run
```

## 🎓 Comandos Maven de Liquibase

```bash
# Ver estado de migraciones
mvn liquibase:status

# Validar changelog
mvn liquibase:validate

# Ver SQL sin ejecutar
mvn liquibase:updateSQL

# Sincronizar (marcar como ejecutado sin ejecutar)
mvn liquibase:changelogSync

# Limpiar checksums
mvn liquibase:clearCheckSums

# Rollback último cambio
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

## 📚 Recursos Adicionales

### Documentación
- `src/main/resources/db/changelog/README.md` - Documentación técnica de Liquibase
- `economia_circular.sql/README.md` - Documentación de scripts SQL
- Documentación oficial: https://docs.liquibase.com

### Scripts de Backup
Los scripts originales están en `economia_circular.sql/` por si necesitas ejecutarlos manualmente.

## ✅ Checklist de Verificación

Después de configurar, verifica que:

- [ ] La aplicación inicia sin errores
- [ ] Existen las tablas: usuario, articulos, events, etc.
- [ ] Existe la tabla databasechangelog con 2 registros
- [ ] Hay datos en usuario (4 en dev, 11 en prod)
- [ ] Hay datos en articulos (5 en dev, 36 en prod)
- [ ] Puedes hacer login con admin@test.com (dev) o admin@economiacircular.com (prod)

## 🎉 ¡Listo!

Tu aplicación ahora gestiona automáticamente la base de datos. Ya no necesitas:
- ❌ Ejecutar scripts SQL manualmente
- ❌ Recordar qué scripts ejecutaste
- ❌ Preocuparte por inconsistencias entre ambientes

Todo se maneja automáticamente al iniciar la aplicación. 🚀

---

**Creado:** Octubre 2025  
**Versión:** 1.0  
**Autor:** Sistema Economía Circular

