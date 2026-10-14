-- =====================================================
-- Recuperación de contraseña y login con Google
-- =====================================================

-- Tabla para tokens de recuperación de contraseña
CREATE TABLE IF NOT EXISTS password_reset_token (
  id BIGINT NOT NULL AUTO_INCREMENT,
  token VARCHAR(255) NOT NULL,
  usuario_id BIGINT NOT NULL,
  expira_en DATETIME NOT NULL,
  usado BOOLEAN DEFAULT FALSE,
  creado_en DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reset_token (token),
  KEY fk_reset_token_usuario (usuario_id),
  CONSTRAINT fk_reset_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Columnas en usuario para OAuth (Google)
ALTER TABLE usuario ADD COLUMN google_id VARCHAR(255) NULL;
ALTER TABLE usuario ADD COLUMN auth_provider VARCHAR(50) DEFAULT 'local';

-- Índice para búsqueda por google_id (único para vincular cuenta)
CREATE UNIQUE INDEX uk_usuario_google_id ON usuario(google_id);
CREATE INDEX idx_reset_token_expira ON password_reset_token(expira_en);
CREATE INDEX idx_reset_token_usado ON password_reset_token(usado);
