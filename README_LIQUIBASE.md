# 🚀 Economía Circular - Gestión Automática de Base de Datos

## ✨ ¿Qué tiene ahora tu aplicación?

Tu aplicación ahora cuenta con un sistema profesional de gestión de base de datos usando **Liquibase**. Esto significa que **ya no necesitas ejecutar scripts SQL manualmente** - todo se hace automáticamente cuando levantas la aplicación.

## 🎯 Funcionalidades Implementadas

### ✅ Gestión Automática de Base de Datos
- La aplicación crea automáticamente todas las tablas al iniciar
- Inserta datos de prueba según el ambiente (desarrollo o producción)
- Mantiene un control de versiones de la base de datos
- Evita ejecutar dos veces el mismo script

### ✅ Dos Ambientes Configurados

#### 🔧 Desarrollo (`dev`)
- Datos mínimos para testing rápido
- 4 usuarios, 5 artículos, 2 eventos
- Base de datos MySQL local

#### 🌐 Producción (`prod`)
- Datos completos y realistas
- 11 usuarios, 36 artículos, 8 eventos, 10 centros de reciclaje
- Google Cloud SQL

### ✅ Datos Completos y Realistas

**36 Artículos** en todas las categorías:
- 📱 Electrónicos (laptops, iPhones, tablets, TVs)
- 👕 Ropa (camperas, zapatillas, vestidos)
- 📚 Libros (Harry Potter, programación, enciclopedias)
- 🪑 Muebles (escritorios, sillas, bibliotecas)
- 🔨 Herramientas (taladros, sierras, sets)
- ⚽ Deportes (bicicletas, tablas de surf, pesas)
- 🖼️ Decoración (cuadros, lámparas, espejos)
- 🍳 Cocina (batidoras, ollas, cafeteras)
- 🌱 Jardín (cortadoras de césped, herramientas)
- 🧸 Juguetes (Lego, bicicletas infantiles, PS4)
- 🎸 Instrumentos Musicales (guitarras, teclados)
- 🚗 Automotriz (neumáticos, portaequipajes)

**8 Eventos** futuros:
- Ferias de intercambio
- Talleres de reparación
- Conferencias sobre economía circular
- Meetups comunitarios

**10 Centros de Reciclaje** con ubicaciones reales de Buenos Aires

**Mensajes, Solicitudes de Intercambio y Estadísticas**

## 🎮 Cómo Usar

### Inicio Rápido (3 pasos)

1. **Crear la base de datos** (solo la primera vez)
```sql
CREATE DATABASE economia_circular CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **Iniciar la aplicación**
```bash
# Desarrollo (datos mínimos)
.\run-app.ps1 dev

# Producción (datos completos)
.\run-app.ps1 prod
```

3. **¡Listo!** Abre tu navegador en `http://localhost:8080`

### Credenciales de Prueba

**Desarrollo:**
- Admin: `admin@test.com` / `Test123!`
- Usuario: `test@test.com` / `Test123!`

**Producción:**
- Admin: `admin@economiacircular.com` / `Test123!`
- Usuario: `maria.gonzalez@email.com` / `Test123!`

## 📁 Estructura del Proyecto

```
economiacircular/
│
├── 📘 QUICKSTART.md              ← ¡Empieza aquí! Guía rápida
├── 📗 LIQUIBASE_SETUP.md         ← Guía completa de configuración
├── 📙 CHANGELOG_LIQUIBASE.md     ← Lista de cambios realizados
├── 📜 run-app.ps1                ← Script para iniciar fácilmente
│
├── pom.xml                       ← Configurado con Liquibase
├── app.yaml                      ← Configurado para Google Cloud
│
├── src/main/resources/
│   ├── application.properties              ← Config principal
│   ├── application-dev.properties          ← Config desarrollo
│   ├── application-prod.properties         ← Config producción
│   │
│   └── db/changelog/
│       ├── 📖 README.md                    ← Documentación técnica
│       ├── db.changelog-master.yaml        ← Changelog maestro
│       │
│       └── sql/
│           ├── 01-schema.sql               ← Estructura de tablas
│           ├── 02-seed-data-produccion.sql ← Datos completos
│           └── 02-seed-data-desarrollo.sql ← Datos mínimos
│
└── economia_circular.sql/
    ├── 📄 README.md                        ← Info sobre scripts
    ├── seed_data_produccion.sql            ← Backup
    └── seed_data_desarrollo.sql            ← Backup
```

## 🎯 Comandos Útiles

### Iniciar la Aplicación
```bash
.\run-app.ps1 dev          # Desarrollo
.\run-app.ps1 prod         # Producción
.\run-app.ps1 test         # Ejecutar tests
.\run-app.ps1 help         # Ver ayuda
```

### Maven
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev    # Desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=prod   # Producción
mvn test                                               # Tests
mvn clean package                                      # Compilar
```

### Liquibase
```bash
mvn liquibase:status          # Ver estado de migraciones
mvn liquibase:validate        # Validar changelogs
mvn liquibase:clearCheckSums  # Limpiar checksums
```

### Desplegar a Google Cloud
```bash
mvn clean package appengine:deploy
```

## 🔍 Verificar que Funciona

Después de iniciar la app, verifica en MySQL:

```sql
-- Ver que Liquibase funcionó
SELECT * FROM databasechangelog;

-- Ver usuarios creados
SELECT id, email, nombre, rol FROM usuario;

-- Ver artículos
SELECT id, titulo, categoria, estado FROM articulos LIMIT 10;

-- Ver eventos
SELECT id, event_name, event_date FROM events;
```

Deberías ver:
- ✅ 2 registros en `databasechangelog`
- ✅ 4 usuarios (dev) o 11 usuarios (prod)
- ✅ 5 artículos (dev) o 36 artículos (prod)
- ✅ Eventos, centros de reciclaje, etc.

## 📚 Documentación

| Documento | Descripción | Para quién |
|-----------|-------------|------------|
| `QUICKSTART.md` | Guía de inicio en 3 pasos | Todos |
| `LIQUIBASE_SETUP.md` | Guía completa y detallada | Desarrolladores |
| `CHANGELOG_LIQUIBASE.md` | Lista de todo lo implementado | Referencia |
| `economia_circular.sql/README.md` | Info sobre scripts SQL | DBA/Desarrolladores |
| `db/changelog/README.md` | Documentación técnica Liquibase | Desarrolladores |

## 🎓 ¿Cómo Funciona?

1. **Inicias la aplicación**
2. **Liquibase se activa automáticamente**
3. **Lee el archivo maestro** (`db.changelog-master.yaml`)
4. **Compara con la tabla de control** (`databasechangelog`)
5. **Ejecuta solo los cambios pendientes**
6. **Registra lo que ejecutó**
7. **La aplicación está lista para usar**

La próxima vez que inicies la app, Liquibase verá que ya ejecutó esos cambios y no los volverá a ejecutar. ¡Automático y seguro!

## ✨ Beneficios

| Antes | Ahora |
|-------|-------|
| ❌ Ejecutar scripts manualmente | ✅ Automático al iniciar |
| ❌ Sin control de versiones | ✅ Control completo |
| ❌ Riesgo de inconsistencias | ✅ Ambiente consistente |
| ❌ Configuración manual | ✅ Setup automático |
| ❌ Un solo conjunto de datos | ✅ Datos por ambiente |

## 🐛 Problemas Comunes

### "Can't connect to MySQL"
```bash
# Verifica que MySQL esté corriendo
# Revisa usuario/contraseña en application-dev.properties
```

### "Table already exists"
```bash
# Opción 1: Sincronizar
mvn liquibase:changelogSync

# Opción 2: Limpiar y recrear
DROP DATABASE economia_circular;
CREATE DATABASE economia_circular CHARACTER SET utf8mb4;
# Reinicia la app
```

### "Waiting for changelog lock"
```sql
-- Liberar el lock
UPDATE databasechangeloglock SET locked = 0;
```

Ver más soluciones en `LIQUIBASE_SETUP.md`

## 🔄 Agregar Nuevos Datos

### Opción 1: Crear nueva migración
```bash
# 1. Crear archivo SQL
echo "INSERT INTO usuario ..." > src/main/resources/db/changelog/sql/03-new-data.sql

# 2. Agregar a db.changelog-master.yaml
# 3. Reiniciar la app
```

### Opción 2: Modificar script existente (antes de aplicar)
```bash
# Edita: src/main/resources/db/changelog/sql/02-seed-data-produccion.sql
# Reinicia la app (solo funciona si no se aplicó antes)
```

## 📞 Soporte

- **Guía rápida:** Lee `QUICKSTART.md`
- **Guía completa:** Lee `LIQUIBASE_SETUP.md`
- **Problemas técnicos:** Lee `db/changelog/README.md`
- **Ayuda script:** `.\run-app.ps1 help`

## 🎉 ¡Todo Listo!

Tu aplicación está configurada con:
- ✅ Liquibase funcionando
- ✅ Datos de prueba completos
- ✅ Perfiles dev y prod
- ✅ Documentación completa
- ✅ Scripts de ayuda
- ✅ Google Cloud configurado

**Simplemente ejecuta `.\run-app.ps1 dev` y empieza a trabajar!** 🚀

---

**Versión:** 1.0  
**Última actualización:** Octubre 2025  
**Stack:** Spring Boot + Liquibase + MySQL + Google Cloud

