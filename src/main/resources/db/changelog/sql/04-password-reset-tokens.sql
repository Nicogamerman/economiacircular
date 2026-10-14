-- =====================================================
-- Tabla: password_reset_tokens
-- Tokens de recuperación de contraseña (expiración corta)
-- =====================================================
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id BIGINT NOT NULL AUTO_INCREMENT,
  token_hash VARCHAR(128) NOT NULL,
  usuario_id BIGINT NOT NULL,
  creado_en DATETIME NOT NULL,
  expira_en DATETIME NOT NULL,
  usado_en DATETIME NULL,
  PRIMARY KEY (id),
  KEY fk_password_reset_usuario (usuario_id),
  CONSTRAINT fk_password_reset_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
  CONSTRAINT uk_password_reset_token_hash UNIQUE KEY (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_password_reset_token_hash ON password_reset_tokens(token_hash);
CREATE INDEX idx_password_reset_usuario ON password_reset_tokens(usuario_id);
CREATE INDEX idx_password_reset_expira_en ON password_reset_tokens(expira_en);
