# 📝 Changelog - Implementación de Liquibase

## 🎯 Resumen de Cambios

Se implementó **Liquibase** para la gestión automática de la base de datos. Ahora cuando la aplicación se levanta, automáticamente:

1. ✅ Crea todas las tablas necesarias
2. ✅ Inserta datos iniciales (desarrollo o producción)
3. ✅ Controla qué migraciones se ejecutaron
4. ✅ Evita duplicar datos o tablas

## 📦 Archivos Nuevos Creados

### 1. Configuración de Maven
- ✅ `pom.xml` - Agregada dependencia de Liquibase

### 2. Archivos de Configuración
- ✅ `src/main/resources/application-dev.properties` - Perfil desarrollo
- ✅ `src/main/resources/application-prod.properties` - Perfil producción
- ✅ `src/main/resources/application.properties` - Actualizado con config de Liquibase
- ✅ `src/test/resources/application-test.properties` - Liquibase deshabilitado en tests
- ✅ `app.yaml` - Agregado perfil prod para Google Cloud

### 3. Scripts de Base de Datos (Liquibase)
- ✅ `src/main/resources/db/changelog/db.changelog-master.yaml` - Changelog maestro
- ✅ `src/main/resources/db/changelog/sql/01-schema.sql` - Estructura de tablas
- ✅ `src/main/resources/db/changelog/sql/02-seed-data-produccion.sql` - Datos completos
- ✅ `src/main/resources/db/changelog/sql/02-seed-data-desarrollo.sql` - Datos mínimos

### 4. Scripts de Datos (Backup)
- ✅ `economia_circular.sql/seed_data_produccion.sql` - 36 artículos, 11 usuarios, eventos, etc.
- ✅ `economia_circular.sql/seed_data_desarrollo.sql` - Datos mínimos para testing

### 5. Documentación
- ✅ `LIQUIBASE_SETUP.md` - Guía completa de configuración y uso
- ✅ `QUICKSTART.md` - Guía rápida de inicio
- ✅ `economia_circular.sql/README.md` - Documentación de scripts SQL
- ✅ `src/main/resources/db/changelog/README.md` - Documentación técnica Liquibase
- ✅ `CHANGELOG_LIQUIBASE.md` - Este archivo

### 6. Scripts de Ayuda
- ✅ `run-app.ps1` - Script PowerShell para iniciar la app fácilmente

## 🔄 Archivos Modificados

- ✅ `pom.xml` - Dependencia de Liquibase
- ✅ `src/main/resources/application.properties` - Configuración de Liquibase
- ✅ `src/test/resources/application-test.properties` - Liquibase deshabilitado
- ✅ `app.yaml` - Perfil de producción

## 📊 Datos Generados

### Perfil Desarrollo (`dev`)
```
✅ 4 Usuarios (1 admin + 3 users)
✅ 5 Artículos (básicos para testing)
✅ 3 Imágenes de artículos
✅ 2 Eventos
✅ 2 Centros de reciclaje
✅ 2 Talleres
✅ 3 Mensajes
✅ 2 Solicitudes de intercambio
✅ 3 Vistas de artículos
```

### Perfil Producción (`prod`)
```
✅ 11 Usuarios (1 admin + 10 users)
✅ 36 Artículos (todas las categorías)
   - Electrónicos, Ropa, Libros, Muebles
   - Herramientas, Deportes, Decoración
   - Cocina, Jardín, Juguetes
   - Instrumentos Musicales, Automotriz
✅ 15+ Imágenes de artículos
✅ 8 Eventos futuros
✅ 10 Centros de reciclaje
✅ 8 Talleres de reparación
✅ 13 Mensajes entre usuarios
✅ 9 Solicitudes de intercambio (varios estados)
✅ 30+ Vistas de artículos (para estadísticas)
```

## 🎭 Categorías de Artículos Incluidas

- ✅ ELECTRONICOS - Laptops, teléfonos, tablets, TVs, auriculares
- ✅ ROPA - Camperas, zapatillas, vestidos
- ✅ LIBROS - Ficción, técnicos, enciclopedias
- ✅ MUEBLES - Escritorios, sillas, bibliotecas, mesas
- ✅ HERRAMIENTAS - Sets de herramientas, taladros, sierras
- ✅ DEPORTES - Bicicletas, tablas de surf, pesas
- ✅ DECORACION_HOGAR - Cuadros, lámparas, espejos
- ✅ COCINA - Batidoras, ollas, cafeteras
- ✅ JARDIN - Cortadoras de césped, herramientas
- ✅ JUGUETES - Lego, bicicletas infantiles, consolas
- ✅ INSTRUMENTOS_MUSICALES - Guitarras, teclados
- ✅ AUTOMOTRIZ - Neumáticos, portaequipajes

## 🔐 Credenciales de Prueba

### Desarrollo
```
Admin:   admin@test.com / Test123!
Usuario: test@test.com / Test123!
Usuario: maria@test.com / Test123!
Usuario: juan@test.com / Test123!
```

### Producción
```
Admin: admin@economiacircular.com / Test123!

Usuarios:
- maria.gonzalez@email.com / Test123!
- juan.perez@email.com / Test123!
- ana.martinez@email.com / Test123!
- carlos.lopez@email.com / Test123!
- laura.fernandez@email.com / Test123!
- pedro.rodriguez@email.com / Test123!
- sofia.sanchez@email.com / Test123!
- diego.torres@email.com / Test123!
- valentina.ramirez@email.com / Test123!
- martin.gimenez@email.com / Test123!
```

## 🚀 Cómo Usar

### Desarrollo Local
```bash
# Opción 1: Script PowerShell
.\run-app.ps1 dev

# Opción 2: Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Opción 3: IDE
# Agregar VM argument: -Dspring.profiles.active=dev
```

### Producción (Google Cloud)
```bash
# Deploy automático
mvn clean package appengine:deploy

# O manualmente
gcloud app deploy
```

## ⚙️ Configuración Técnica

### Liquibase
- **Changelog:** `db/changelog/db.changelog-master.yaml`
- **Contextos:** `dev` y `prod`
- **Control:** Tabla `databasechangelog`
- **Locks:** Tabla `databasechangeloglock`

### JPA/Hibernate
- **ddl-auto:** `validate` (Liquibase controla la estructura)
- **Dialect:** MySQL8Dialect
- **Show SQL:** `true` en dev, `false` en prod

### Base de Datos
- **Desarrollo:** MySQL local (localhost:3306)
- **Producción:** Google Cloud SQL
- **Tests:** H2 en memoria

## 🔍 Verificación

### Después de iniciar la app, verifica:

```sql
-- Ver migraciones aplicadas
SELECT * FROM databasechangelog;
-- Debería mostrar 2 registros

-- Ver datos insertados
SELECT COUNT(*) FROM usuario;
SELECT COUNT(*) FROM articulos;
SELECT COUNT(*) FROM events;

-- Ver usuarios
SELECT email, nombre, rol FROM usuario;
```

### En los logs de la aplicación:
```
[INFO] Successfully acquired change log lock
[INFO] Reading from economia_circular.databasechangelog
[INFO] db/changelog/sql/01-schema.sql: Changeset executed
[INFO] db/changelog/sql/02-seed-data-produccion.sql: Changeset executed
[INFO] Successfully released change log lock
```

## 🎯 Beneficios

### Antes
- ❌ Scripts SQL manuales
- ❌ Sin control de versiones de BD
- ❌ Riesgo de inconsistencias
- ❌ Proceso manual de setup

### Ahora
- ✅ Automático al iniciar la app
- ✅ Control de versiones completo
- ✅ Diferentes datos por ambiente
- ✅ Robusto y profesional
- ✅ Fácil de mantener

## 📝 Próximos Pasos

Para agregar nuevas migraciones:

1. Crear archivo SQL: `src/main/resources/db/changelog/sql/03-nueva-migracion.sql`
2. Registrar en `db.changelog-master.yaml`
3. Reiniciar la app
4. ¡Listo! Liquibase lo aplica automáticamente

## 🐛 Solución de Problemas

Ver documentación completa en:
- `LIQUIBASE_SETUP.md` - Sección "Problemas Comunes"
- `src/main/resources/db/changelog/README.md` - Sección "Troubleshooting"

## 📚 Documentación

| Archivo | Descripción |
|---------|-------------|
| `QUICKSTART.md` | Guía rápida de inicio (3 pasos) |
| `LIQUIBASE_SETUP.md` | Guía completa de configuración |
| `economia_circular.sql/README.md` | Documentación de scripts SQL |
| `src/main/resources/db/changelog/README.md` | Documentación técnica |
| `run-app.ps1 help` | Ayuda del script PowerShell |

## ✅ Checklist Final

- [x] Liquibase configurado en pom.xml
- [x] Perfiles dev y prod creados
- [x] Scripts de estructura (schema)
- [x] Scripts de datos (seed data)
- [x] Documentación completa
- [x] Scripts de ayuda
- [x] Configuración de Google Cloud
- [x] Tests configurados (Liquibase deshabilitado)
- [x] Credenciales de prueba documentadas
- [x] Backup de scripts SQL originales

## 🎉 Estado: COMPLETADO

La implementación está lista para usar. Simplemente inicia la aplicación y Liquibase se encargará del resto.

---

**Fecha de implementación:** Octubre 2025  
**Versión:** 1.0  
**Tecnologías:** Spring Boot 2.7.0 + Liquibase + MySQL + Google Cloud SQL


