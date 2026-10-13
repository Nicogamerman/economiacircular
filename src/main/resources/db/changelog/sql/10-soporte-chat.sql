-- =====================================================
-- Chat de soporte entre usuarios y administradores
-- =====================================================

CREATE TABLE IF NOT EXISTS soporte_chats (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  asunto VARCHAR(200) NOT NULL,
  estado ENUM('ABIERTO','EN_PROGRESO','RESUELTO','CERRADO') NOT NULL DEFAULT 'ABIERTO',
  prioridad ENUM('BAJA','MEDIA','ALTA') NOT NULL DEFAULT 'MEDIA',
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_soporte_chats_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_soporte_chats_usuario ON soporte_chats(usuario_id);
CREATE INDEX idx_soporte_chats_estado ON soporte_chats(estado);

CREATE TABLE IF NOT EXISTS mensajes_soporte (
  id BIGINT NOT NULL AUTO_INCREMENT,
  chat_id BIGINT NOT NULL,
  emisor_id BIGINT NOT NULL,
  contenido TEXT NOT NULL,
  leido TINYINT(1) NOT NULL DEFAULT 0,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_mensajes_soporte_chat FOREIGN KEY (chat_id) REFERENCES soporte_chats (id) ON DELETE CASCADE,
  CONSTRAINT fk_mensajes_soporte_emisor FOREIGN KEY (emisor_id) REFERENCES usuario (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_mensajes_soporte_chat ON mensajes_soporte(chat_id);
