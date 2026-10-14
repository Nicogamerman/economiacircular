-- =====================================================
-- Verificación de email
-- Agrega columnas a usuario y crea tabla de tokens
-- =====================================================

ALTER TABLE usuario
  ADD COLUMN email_verificado BOOLEAN NOT NULL DEFAULT FALSE AFTER activo;

ALTER TABLE usuario
  ADD COLUMN email_verificado_en DATETIME NULL AFTER email_verificado;

-- Usuarios pre-existentes se consideran verificados (no romper login para legacy)
UPDATE usuario SET email_verificado = TRUE WHERE email_verificado = FALSE;

CREATE TABLE IF NOT EXISTS email_verification_tokens (
  id BIGINT NOT NULL AUTO_INCREMENT,
  token_hash VARCHAR(128) NOT NULL,
  usuario_id BIGINT NOT NULL,
  creado_en DATETIME NOT NULL,
  expira_en DATETIME NOT NULL,
  usado_en DATETIME NULL,
  PRIMARY KEY (id),
  KEY fk_email_verification_usuario (usuario_id),
  CONSTRAINT fk_email_verification_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
  CONSTRAINT uk_email_verification_token_hash UNIQUE KEY (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_email_verification_token_hash ON email_verification_tokens(token_hash);
CREATE INDEX idx_email_verification_usuario ON email_verification_tokens(usuario_id);
