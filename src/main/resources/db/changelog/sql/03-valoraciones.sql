-- =====================================================
-- Tabla: valoraciones
-- Sistema de comentarios y puntajes para oferentes
-- =====================================================
CREATE TABLE IF NOT EXISTS valoraciones (
  id BIGINT NOT NULL AUTO_INCREMENT,
  puntaje INT NOT NULL,
  comentario TEXT,
  valorador_id BIGINT NOT NULL,
  valorado_id BIGINT NOT NULL,
  articulo_id BIGINT,
  creado_en DATETIME,
  actualizado_en DATETIME,
  PRIMARY KEY (id),
  KEY fk_valoraciones_valorador (valorador_id),
  KEY fk_valoraciones_valorado (valorado_id),
  KEY fk_valoraciones_articulo (articulo_id),
  CONSTRAINT fk_valoraciones_valorador FOREIGN KEY (valorador_id) REFERENCES usuario (id) ON DELETE CASCADE,
  CONSTRAINT fk_valoraciones_valorado FOREIGN KEY (valorado_id) REFERENCES usuario (id) ON DELETE CASCADE,
  CONSTRAINT fk_valoraciones_articulo FOREIGN KEY (articulo_id) REFERENCES articulos (id) ON DELETE SET NULL,
  CONSTRAINT uk_valoracion_valorador_valorado_articulo UNIQUE KEY (valorador_id, valorado_id, articulo_id),
  CONSTRAINT chk_valoraciones_puntaje CHECK (puntaje BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_valoraciones_valorado ON valoraciones(valorado_id);
CREATE INDEX idx_valoraciones_creado_en ON valoraciones(creado_en);
