-- =====================================================
-- Script para Limpiar Error de Liquibase
-- Ejecutar ANTES de volver a desplegar
-- =====================================================

-- Este script limpia el estado de Liquibase para que pueda
-- volver a intentar la migración desde cero

-- 1. Liberar cualquier lock activo
UPDATE databasechangeloglock SET locked = 0, lockgranted = NULL, lockedby = NULL WHERE id = 1;

-- 2. Eliminar el changeset fallido de la tabla de control
-- Esto permite que Liquibase lo intente de nuevo
DELETE FROM databasechangelog WHERE id = '1-create-schema';

-- 3. Opcional: Si quieres empezar completamente desde cero, 
--    descomenta y ejecuta las siguientes líneas:

/*
-- ADVERTENCIA: Esto eliminará TODAS las tablas y datos
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
*/

-- =====================================================
-- Después de ejecutar este script:
-- 1. Compila la app: mvn clean package
-- 2. Despliega de nuevo: gcloud app deploy
-- =====================================================

