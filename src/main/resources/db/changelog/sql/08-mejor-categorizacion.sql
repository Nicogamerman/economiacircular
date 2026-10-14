-- =====================================================
-- Mejor categorización de artículos
-- Agrega subcategoría, marca, modelo y etiquetas (tags)
-- =====================================================

ALTER TABLE articulos
  ADD COLUMN subcategoria VARCHAR(100) NULL AFTER categoria;

ALTER TABLE articulos
  ADD COLUMN marca VARCHAR(100) NULL AFTER subcategoria;

ALTER TABLE articulos
  ADD COLUMN modelo VARCHAR(100) NULL AFTER marca;

CREATE INDEX idx_articulos_subcategoria ON articulos(subcategoria);
CREATE INDEX idx_articulos_marca ON articulos(marca);

CREATE TABLE IF NOT EXISTS etiquetas_articulos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  articulo_id BIGINT NOT NULL,
  etiqueta VARCHAR(60) NOT NULL,
  PRIMARY KEY (id),
  KEY fk_etiquetas_articulos_articulo (articulo_id),
  CONSTRAINT fk_etiquetas_articulos_articulo FOREIGN KEY (articulo_id) REFERENCES articulos (id) ON DELETE CASCADE,
  CONSTRAINT uk_etiqueta_articulo UNIQUE KEY (articulo_id, etiqueta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_etiquetas_articulo ON etiquetas_articulos(articulo_id);
CREATE INDEX idx_etiquetas_etiqueta ON etiquetas_articulos(etiqueta);
