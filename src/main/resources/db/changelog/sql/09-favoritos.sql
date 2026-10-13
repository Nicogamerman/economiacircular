-- =====================================================
-- Artículos Favoritos
-- Permite a usuarios guardar artículos como favoritos
-- =====================================================

CREATE TABLE IF NOT EXISTS favoritos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  articulo_id BIGINT NOT NULL,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_favoritos_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
  CONSTRAINT fk_favoritos_articulo FOREIGN KEY (articulo_id) REFERENCES articulos (id) ON DELETE CASCADE,
  CONSTRAINT uk_favorito_usuario_articulo UNIQUE KEY (usuario_id, articulo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_favoritos_usuario ON favoritos(usuario_id);
CREATE INDEX idx_favoritos_articulo ON favoritos(articulo_id);
