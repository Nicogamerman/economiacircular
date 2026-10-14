-- =====================================================
-- Ampliación de imagenes_articulos: contenido en BD
-- Permite upload directo de imágenes sin storage externo
-- =====================================================

ALTER TABLE imagenes_articulos
  ADD COLUMN contenido LONGBLOB NULL AFTER descripcion;

ALTER TABLE imagenes_articulos
  ADD COLUMN content_type VARCHAR(100) NULL AFTER contenido;

ALTER TABLE imagenes_articulos
  ADD COLUMN tamano_bytes BIGINT NULL AFTER content_type;

ALTER TABLE imagenes_articulos
  MODIFY url_imagen VARCHAR(500) NULL;

ALTER TABLE imagenes_articulos
  MODIFY nombre_archivo VARCHAR(255) NULL;
