CREATE TABLE IF NOT EXISTS notificaciones (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  articulo_id BIGINT,
  mensaje VARCHAR(500) NOT NULL,
  tipo VARCHAR(80) NOT NULL,
  leida BOOLEAN NOT NULL DEFAULT false,
  creado_en DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_notificaciones_usuario (usuario_id),
  KEY idx_notificaciones_leida (leida),
  KEY fk_notificaciones_articulo (articulo_id),
  CONSTRAINT fk_notificaciones_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
  CONSTRAINT fk_notificaciones_articulo FOREIGN KEY (articulo_id) REFERENCES articulos (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
