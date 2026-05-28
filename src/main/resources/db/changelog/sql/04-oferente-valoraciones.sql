CREATE TABLE IF NOT EXISTS valoraciones_oferente (
  id BIGINT NOT NULL AUTO_INCREMENT,
  oferente_id BIGINT NOT NULL,
  autor_id BIGINT NOT NULL,
  articulo_id BIGINT NOT NULL,
  puntuacion INT NOT NULL,
  comentario VARCHAR(1000),
  creado_en DATETIME NOT NULL,
  actualizado_en DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT chk_valoraciones_puntuacion CHECK (puntuacion BETWEEN 1 AND 5),
  CONSTRAINT uk_valoracion_autor_articulo UNIQUE (autor_id, articulo_id),
  KEY idx_valoraciones_oferente (oferente_id),
  KEY idx_valoraciones_articulo (articulo_id),
  CONSTRAINT fk_valoraciones_oferente FOREIGN KEY (oferente_id) REFERENCES usuario(id),
  CONSTRAINT fk_valoraciones_autor FOREIGN KEY (autor_id) REFERENCES usuario(id),
  CONSTRAINT fk_valoraciones_articulo FOREIGN KEY (articulo_id) REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
