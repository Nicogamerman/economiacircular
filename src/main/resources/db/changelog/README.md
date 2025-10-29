# Liquibase - Gestión de Base de Datos

## 📋 Descripción

Este proyecto utiliza **Liquibase** para gestionar automáticamente la estructura y datos iniciales de la base de datos. Cuando la aplicación se levanta, Liquibase ejecuta los scripts necesarios de forma automática.

## 🎯 ¿Qué hace Liquibase?

- ✅ Crea automáticamente las tablas al iniciar la aplicación
- ✅ Inserta datos iniciales según el ambiente (dev/prod)
- ✅ Mantiene un historial de cambios aplicados (tabla `databasechangelog`)
- ✅ No re-ejecuta cambios ya aplicados
- ✅ Permite hacer rollback si es necesario

## 📁 Estructura de Archivos

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml          # Archivo maestro que coordina todos los cambios
└── sql/
    ├── 01-schema.sql                 # Estructura de tablas
    ├── 02-seed-data-produccion.sql   # Datos para producción
    └── 02-seed-data-desarrollo.sql   # Datos para desarrollo
```

## 🚀 Uso Básico

### Ejecutar en Desarrollo (datos mínimos)

```bash
# Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Java
java -jar -Dspring.profiles.active=dev economia-circular.jar

# En IDE (Eclipse/IntelliJ)
# Agregar VM argument: -Dspring.profiles.active=dev
```

### Ejecutar en Producción (datos completos)

```bash
# Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Java
java -jar -Dspring.profiles.active=prod economia-circular.jar

# Google Cloud (app.yaml ya tiene configurado el profile prod)
gcloud app deploy
```

### Ejecutar sin datos iniciales (solo estructura)

Si solo quieres crear las tablas sin insertar datos:

```bash
# Deshabilitar Liquibase temporalmente
java -jar -Dspring.liquibase.enabled=false economia-circular.jar
```

## 🔄 Perfiles y Contextos

### Perfil `dev` (Desarrollo)
- Base de datos: MySQL local
- Datos: Mínimos (4 usuarios, 5 artículos, etc.)
- Propósito: Testing rápido y desarrollo local

### Perfil `prod` (Producción)
- Base de datos: Google Cloud SQL
- Datos: Completos (11 usuarios, 36 artículos, eventos, etc.)
- Propósito: Ambiente de producción/staging con datos realistas

## 📝 ChangeLog Maestro

El archivo `db.changelog-master.yaml` define qué scripts ejecutar:

```yaml
databaseChangeLog:
  - changeSet:
      id: 1-create-schema
      # Se ejecuta SIEMPRE en todos los ambientes
      
  - changeSet:
      id: 2-seed-data-produccion
      context: prod  # Solo se ejecuta en producción
      
  - changeSet:
      id: 2-seed-data-desarrollo
      context: dev   # Solo se ejecuta en desarrollo
```

## 🔧 Configuración

### application.properties (Producción por defecto)

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
spring.liquibase.contexts=${liquibase.context:prod}
```

### application-dev.properties (Desarrollo)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/economia_circular
spring.liquibase.contexts=dev
```

### application-prod.properties (Producción)

```properties
spring.liquibase.contexts=prod
```

## 🛠️ Comandos Útiles de Liquibase

### Ver estado de migraciones

```bash
mvn liquibase:status
```

### Validar changelog

```bash
mvn liquibase:validate
```

### Ver SQL que se ejecutará (sin aplicar)

```bash
mvn liquibase:updateSQL
```

### Rollback del último changeSet

```bash
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

### Limpiar checksums (si hay conflictos)

```bash
mvn liquibase:clearCheckSums
```

## 🔍 Tablas de Control de Liquibase

Liquibase crea automáticamente dos tablas:

- **`databasechangelog`**: Historial de cambios aplicados
- **`databasechangeloglock`**: Lock para evitar ejecuciones concurrentes

```sql
-- Ver qué cambios se han aplicado
SELECT * FROM databasechangelog ORDER BY dateexecuted DESC;

-- Ver si hay algún lock activo
SELECT * FROM databasechangeloglock;
```

## 🐛 Troubleshooting

### Error: "Validation Failed"

**Causa**: El checksum del archivo SQL cambió después de ser ejecutado.

**Solución**:
```bash
mvn liquibase:clearCheckSums
```

### Error: "Waiting for changelog lock"

**Causa**: Otro proceso tiene el lock o quedó bloqueado.

**Solución**:
```sql
-- Liberar el lock manualmente
UPDATE databasechangeloglock SET locked = 0;
```

### Error: "Table already exists"

**Causa**: Las tablas ya existen y el changeSet intenta crearlas de nuevo.

**Solución 1** (Marcar como ejecutado sin ejecutar):
```bash
mvn liquibase:changelogSync
```

**Solución 2** (Limpiar todo y empezar de cero):
```sql
DROP TABLE IF EXISTS databasechangeloglock;
DROP TABLE IF EXISTS databasechangelog;
-- Luego reinicia la aplicación
```

### Reiniciar completamente la base de datos

```sql
-- CUIDADO: Esto borra TODOS los datos
SET FOREIGN_KEY_CHECKS = 0;

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

-- Ahora reinicia la aplicación y Liquibase creará todo desde cero
```

## 🔐 Datos de Prueba

### Usuarios de Desarrollo (perfil dev)

```
admin@test.com / Test123!    (ADMIN)
test@test.com / Test123!     (USER)
maria@test.com / Test123!    (USER)
juan@test.com / Test123!     (USER)
```

### Usuarios de Producción (perfil prod)

```
admin@economiacircular.com / Test123!     (ADMIN)
maria.gonzalez@email.com / Test123!       (USER)
juan.perez@email.com / Test123!           (USER)
... (10 usuarios más)
```

## 📚 Agregar Nuevas Migraciones

### 1. Crear un nuevo archivo SQL

```bash
# Crear nuevo archivo de migración
src/main/resources/db/changelog/sql/03-add-new-table.sql
```

### 2. Agregar al changelog maestro

```yaml
# db.changelog-master.yaml
databaseChangeLog:
  # ... changeSets existentes ...
  
  - changeSet:
      id: 3-add-new-table
      author: tu-nombre
      changes:
        - sqlFile:
            path: db/changelog/sql/03-add-new-table.sql
      rollback:
        - sql: DROP TABLE IF EXISTS nueva_tabla;
```

### 3. Reiniciar la aplicación

La próxima vez que inicies la app, Liquibase aplicará automáticamente el nuevo changeSet.

## 🎓 Buenas Prácticas

1. ✅ **Nunca modifiques un changeSet ya aplicado**
   - Crea uno nuevo en su lugar

2. ✅ **Usa IDs descriptivos**
   - Ejemplo: `1-create-schema`, `2-seed-users`, `3-add-email-column`

3. ✅ **Incluye rollback cuando sea posible**
   - Facilita revertir cambios en caso de errores

4. ✅ **Usa contextos para diferentes ambientes**
   - `dev` para desarrollo
   - `prod` para producción
   - `test` para testing

5. ✅ **Documenta cambios complejos**
   - Agrega comentarios en los archivos SQL

6. ✅ **Versiona tus changeSets**
   - Usa Git para mantener historial de cambios

## 🔄 Ciclo de Vida

```
Inicio de Aplicación
        ↓
Liquibase se activa
        ↓
Lee db.changelog-master.yaml
        ↓
Compara con databasechangelog
        ↓
Ejecuta changeSets pendientes
        ↓
Actualiza databasechangelog
        ↓
Aplicación lista para usar
```

## 📞 Soporte

Para más información sobre Liquibase:
- Documentación oficial: https://docs.liquibase.com
- Repositorio del proyecto: https://github.com/liquibase/liquibase

---

**Última actualización**: Octubre 2025  
**Versión**: 1.0

