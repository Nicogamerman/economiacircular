-- =====================================================
-- Políticas y reglas de uso de la plataforma
-- =====================================================

CREATE TABLE IF NOT EXISTS politicas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tipo ENUM('POLITICA_PRIVACIDAD','TERMINOS_USO','REGLAS_COMUNIDAD','FAQ','OTRO') NOT NULL,
  titulo VARCHAR(200) NOT NULL,
  contenido LONGTEXT NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  orden INT NOT NULL DEFAULT 0,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_politicas_tipo ON politicas(tipo);
CREATE INDEX idx_politicas_activo ON politicas(activo);
