-- =====================================================
-- Backlog final: celular e identidad verificados, seguimientos,
-- intereses por tipo de evento, devoluciones, video de artículos
-- y mensajes de voz.
-- =====================================================

-- ---------- Usuario: celular e identidad ----------
ALTER TABLE usuario ADD COLUMN telefono VARCHAR(20) NULL;
ALTER TABLE usuario ADD COLUMN telefono_verificado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE usuario ADD COLUMN identidad_verificada BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS telefono_verificaciones (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  telefono VARCHAR(20) NOT NULL,
  codigo_hash VARCHAR(64) NOT NULL,
  expira_en DATETIME NOT NULL,
  intentos INT NOT NULL DEFAULT 0,
  usado BOOLEAN NOT NULL DEFAULT FALSE,
  creado_en DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_tel_verif_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_tel_verif_usuario ON telefono_verificaciones(usuario_id);

-- Solo se guarda el resultado del análisis, nunca las imágenes del documento
CREATE TABLE IF NOT EXISTS verificaciones_identidad (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  estado VARCHAR(20) NOT NULL,
  tipo_documento VARCHAR(100) NULL,
  confianza INT NULL,
  motivo VARCHAR(1000) NULL,
  nota_revision VARCHAR(1000) NULL,
  revisado_por_id BIGINT NULL,
  creado_en DATETIME NOT NULL,
  resuelto_en DATETIME NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_verif_identidad_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
  CONSTRAINT fk_verif_identidad_revisor FOREIGN KEY (revisado_por_id) REFERENCES usuario (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_verif_identidad_usuario ON verificaciones_identidad(usuario_id);
CREATE INDEX idx_verif_identidad_estado ON verificaciones_identidad(estado);

-- ---------- Seguir oferentes y eventos ----------
CREATE TABLE IF NOT EXISTS seguimientos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  tipo VARCHAR(20) NOT NULL,
  objetivo_id BIGINT NOT NULL,
  creado_en DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_seguimientos_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
  CONSTRAINT uk_seguimiento UNIQUE KEY (usuario_id, tipo, objetivo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_seguimientos_objetivo ON seguimientos(tipo, objetivo_id);

CREATE TABLE IF NOT EXISTS intereses_eventos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  tipo_evento VARCHAR(30) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_intereses_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
  CONSTRAINT uk_interes_evento UNIQUE KEY (usuario_id, tipo_evento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- Devoluciones ----------
CREATE TABLE IF NOT EXISTS devoluciones (
  id BIGINT NOT NULL AUTO_INCREMENT,
  solicitud_id BIGINT NOT NULL,
  solicitante_id BIGINT NOT NULL,
  motivo VARCHAR(1000) NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
  respuesta VARCHAR(1000) NULL,
  creado_en DATETIME NOT NULL,
  resuelto_en DATETIME NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_devoluciones_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitudes_intercambio (id) ON DELETE CASCADE,
  CONSTRAINT fk_devoluciones_solicitante FOREIGN KEY (solicitante_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_devoluciones_solicitud ON devoluciones(solicitud_id);

ALTER TABLE politicas MODIFY COLUMN tipo
  ENUM('POLITICA_PRIVACIDAD','TERMINOS_USO','REGLAS_COMUNIDAD','FAQ','DEVOLUCIONES','OTRO') NOT NULL;

-- ---------- Video del artículo ----------
CREATE TABLE IF NOT EXISTS videos_articulos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  articulo_id BIGINT NOT NULL,
  nombre_archivo VARCHAR(255) NULL,
  content_type VARCHAR(100) NOT NULL,
  tamano_bytes BIGINT NOT NULL,
  con_audio BOOLEAN NOT NULL DEFAULT TRUE,
  contenido LONGBLOB NOT NULL,
  creado_en DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_videos_articulo FOREIGN KEY (articulo_id) REFERENCES articulos (id) ON DELETE CASCADE,
  CONSTRAINT uk_video_articulo UNIQUE KEY (articulo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------- Mensajes de voz ----------
ALTER TABLE mensajes ADD COLUMN tipo VARCHAR(10) NOT NULL DEFAULT 'TEXTO';
ALTER TABLE mensajes ADD COLUMN audio_duracion_seg INT NULL;

CREATE TABLE IF NOT EXISTS mensajes_audio (
  id BIGINT NOT NULL AUTO_INCREMENT,
  mensaje_id BIGINT NOT NULL,
  content_type VARCHAR(100) NOT NULL,
  contenido LONGBLOB NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_mensajes_audio_mensaje FOREIGN KEY (mensaje_id) REFERENCES mensajes (id) ON DELETE CASCADE,
  CONSTRAINT uk_mensaje_audio UNIQUE KEY (mensaje_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
