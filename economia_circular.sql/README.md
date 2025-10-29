# Scripts de Base de Datos - Economía Circular

## Descripción General

Este directorio contiene scripts SQL para inicializar y poblar la base de datos de la aplicación Economía Circular.

## Archivos Disponibles

### 1. `seed_data_produccion.sql`
Script completo con datos realistas para ambiente de producción/staging.

**Contenido:**
- ✅ 11 Usuarios (1 admin + 10 usuarios regulares)
- ✅ 36 Artículos de todas las categorías
- ✅ 15+ Imágenes de artículos
- ✅ 8 Eventos futuros
- ✅ 10 Centros de reciclaje
- ✅ 8 Talleres de reparación
- ✅ 13 Mensajes entre usuarios
- ✅ 9 Solicitudes de intercambio (varios estados)
- ✅ 30+ Vistas de artículos (estadísticas)

**Características:**
- Fechas relativas (se ajustan automáticamente al momento de ejecución)
- Datos realistas y coherentes
- Incluye diferentes estados de artículos (disponible, reservado, intercambiado)
- Eventos futuros programados
- Relaciones completas entre entidades

### 2. `economia_circular_usuario.sql` y `economia_circular_taller.sql`
Scripts de estructura de base de datos (dumps existentes).

## Uso

### Instalación Completa en Producción

```bash
# 1. Crear la base de datos
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS economia_circular CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. Ejecutar el script de datos
mysql -u root -p economia_circular < economia_circular.sql/seed_data_produccion.sql
```

### Instalación en Google Cloud SQL

```bash
# Conectar a Cloud SQL
gcloud sql connect [INSTANCE_NAME] --user=root --quiet

# Dentro de MySQL
CREATE DATABASE IF NOT EXISTS economia_circular CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE economia_circular;
SOURCE seed_data_produccion.sql;
```

### Reinstalación (limpiar datos anteriores)

```bash
# CUIDADO: Esto eliminará TODOS los datos existentes
mysql -u root -p economia_circular -e "
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE vistas_articulos;
TRUNCATE TABLE solicitudes_intercambio;
TRUNCATE TABLE mensajes;
TRUNCATE TABLE imagenes_articulos;
TRUNCATE TABLE articulos;
TRUNCATE TABLE events;
TRUNCATE TABLE recycling_centers;
TRUNCATE TABLE taller;
TRUNCATE TABLE usuario;
SET FOREIGN_KEY_CHECKS = 1;
"

# Luego ejecutar el script de nuevo
mysql -u root -p economia_circular < economia_circular.sql/seed_data_produccion.sql
```

## Credenciales de Prueba

### Usuarios de Prueba

Todos los usuarios tienen la misma contraseña para facilitar las pruebas:

**Contraseña:** `Test123!`

#### Usuario Administrador
- Email: `admin@economiacircular.com`
- Rol: ADMIN

#### Usuarios Regulares
- `maria.gonzalez@email.com`
- `juan.perez@email.com`
- `ana.martinez@email.com`
- `carlos.lopez@email.com`
- `laura.fernandez@email.com`
- `pedro.rodriguez@email.com`
- `sofia.sanchez@email.com`
- `diego.torres@email.com`
- `valentina.ramirez@email.com`
- `martin.gimenez@email.com`

## Verificación

Después de ejecutar el script, verifica que los datos se insertaron correctamente:

```sql
USE economia_circular;

-- Ver totales
SELECT 'Usuarios' as tabla, COUNT(*) as total FROM usuario
UNION ALL
SELECT 'Artículos', COUNT(*) FROM articulos
UNION ALL
SELECT 'Eventos', COUNT(*) FROM events
UNION ALL
SELECT 'Centros de Reciclaje', COUNT(*) FROM recycling_centers
UNION ALL
SELECT 'Talleres', COUNT(*) FROM taller
UNION ALL
SELECT 'Mensajes', COUNT(*) FROM mensajes
UNION ALL
SELECT 'Solicitudes', COUNT(*) FROM solicitudes_intercambio;

-- Ver algunos datos de ejemplo
SELECT id, titulo, categoria, estado FROM articulos LIMIT 5;
SELECT id, email, nombre FROM usuario LIMIT 5;
SELECT id, event_name, event_date FROM events LIMIT 5;
```

## Personalización

### Modificar Contraseñas

Las contraseñas están encriptadas con BCrypt. Para generar una nueva contraseña encriptada:

**Usando Java (en tu aplicación):**
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode("TuNuevaContraseña");
System.out.println(hashedPassword);
```

**Usando Online:**
- Visita: https://bcrypt-generator.com/
- Ingresa tu contraseña
- Usa el hash generado en el script SQL

### Agregar Más Datos

Puedes editar el archivo `seed_data_produccion.sql` y agregar más registros siguiendo el mismo formato. Recuerda:

1. Mantener las relaciones entre tablas (foreign keys)
2. Usar fechas relativas con `DATE_SUB()` y `DATE_ADD()`
3. Respetar los tipos de enums definidos en las entidades Java

## Datos Incluidos por Categoría

### Artículos por Categoría
- **Electrónicos:** Laptops, smartphones, tablets, auriculares, TVs
- **Ropa:** Camperas, zapatillas, vestidos
- **Libros:** Ficción, programación, enciclopedias
- **Muebles:** Escritorios, sillas, bibliotecas, mesas
- **Herramientas:** Sets de herramientas, taladros, sierras
- **Deportes:** Bicicletas, tablas de surf, pesas
- **Decoración:** Cuadros, lámparas, espejos
- **Cocina:** Batidoras, ollas, cafeteras
- **Jardín:** Cortadoras de césped, herramientas de jardín
- **Juguetes:** Lego, bicicletas infantiles, consolas
- **Instrumentos Musicales:** Guitarras, teclados
- **Automotriz:** Neumáticos, portaequipajes

### Tipos de Eventos
- Ferias de intercambio
- Talleres de reparación
- Conferencias
- Meetups comunitarios

### Tipos de Centros
- Centros de reciclaje
- Puntos de recolección
- ONGs ambientales
- Talleres de reparación

## Mantenimiento

### Backup de Datos

```bash
# Hacer backup de la base de datos completa
mysqldump -u root -p economia_circular > backup_$(date +%Y%m%d).sql

# Backup solo de los datos (sin estructura)
mysqldump -u root -p --no-create-info economia_circular > backup_data_$(date +%Y%m%d).sql
```

### Actualizar Datos Existentes

Si ya tienes datos en producción y solo quieres agregar nuevos registros:

1. Abre el script `seed_data_produccion.sql`
2. Comenta las líneas `TRUNCATE TABLE` si las hay
3. Modifica los IDs para que no colisionen con los existentes
4. Ejecuta solo las secciones que necesites

## Troubleshooting

### Error: Duplicate entry for key 'PRIMARY'
**Causa:** Ya existen registros con esos IDs  
**Solución:** Limpia las tablas primero o modifica los IDs en el script

### Error: Cannot add foreign key constraint
**Causa:** Intentas insertar un registro que referencia a otro que no existe  
**Solución:** Verifica que las foreign keys apunten a registros existentes

### Error: Data too long for column
**Causa:** Algún campo excede el tamaño máximo definido  
**Solución:** Reduce el tamaño del texto o modifica la estructura de la tabla

## Contacto y Soporte

Para problemas o sugerencias, contactar al equipo de desarrollo.

---

**Última actualización:** Octubre 2025  
**Versión del script:** 1.0

