-- =====================================================
-- Tabla: notificaciones
-- Notificaciones in-app genéricas (mensajes, valoraciones, etc.)
-- =====================================================
CREATE TABLE IF NOT EXISTS notificaciones (
  id BIGINT NOT NULL AUTO_INCREMENT,
  destinatario_id BIGINT NOT NULL,
  emisor_id BIGINT NULL,
  tipo VARCHAR(50) NOT NULL,
  titulo VARCHAR(200),
  mensaje TEXT,
  referencia_tipo VARCHAR(50),
  referencia_id BIGINT,
  leida BOOLEAN NOT NULL DEFAULT FALSE,
  creado_en DATETIME NOT NULL,
  leido_en DATETIME NULL,
  PRIMARY KEY (id),
  KEY fk_notificaciones_destinatario (destinatario_id),
  KEY fk_notificaciones_emisor (emisor_id),
  CONSTRAINT fk_notificaciones_destinatario FOREIGN KEY (destinatario_id) REFERENCES usuario (id) ON DELETE CASCADE,
  CONSTRAINT fk_notificaciones_emisor FOREIGN KEY (emisor_id) REFERENCES usuario (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notificaciones_destinatario ON notificaciones(destinatario_id);
CREATE INDEX idx_notificaciones_leida ON notificaciones(leida);
CREATE INDEX idx_notificaciones_creado_en ON notificaciones(creado_en);
