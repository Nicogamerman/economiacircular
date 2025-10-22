-- =====================================================
-- Schema de Base de Datos - Economía Circular
-- Este script crea solo la estructura de tablas
-- =====================================================

-- Nota: JPA ya crea las tablas con ddl-auto=update
-- Este script es para tener control explícito del schema

-- =====================================================
-- Tabla: usuario
-- =====================================================
CREATE TABLE IF NOT EXISTS usuario (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255),
  apellido VARCHAR(255),
  email VARCHAR(255),
  contrasena VARCHAR(255),
  rol VARCHAR(255),
  domicilio VARCHAR(255),
  foto LONGBLOB,
  activo BOOLEAN DEFAULT TRUE,
  creado_en DATETIME,
  actualizado_en DATETIME,
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Tabla: taller
-- =====================================================
CREATE TABLE IF NOT EXISTS taller (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255),
  direccion VARCHAR(255),
  email VARCHAR(255),
  telefono VARCHAR(255),
  tipo_servicio VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Tabla: articulos
-- =====================================================
CREATE TABLE IF NOT EXISTS articulos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  titulo VARCHAR(200) NOT NULL,
  descripcion VARCHAR(1000) NOT NULL,
  categoria VARCHAR(50),
  condicion VARCHAR(50),
  estado VARCHAR(50) DEFAULT 'DISPONIBLE',
  usuario_id BIGINT NOT NULL,
  creado_en DATETIME,
  actualizado_en DATETIME,
  PRIMARY KEY (id),
  KEY fk_articulos_usuario (usuario_id),
  CONSTRAINT fk_articulos_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Tabla: imagenes_articulos
-- =====================================================
CREATE TABLE IF NOT EXISTS imagenes_articulos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  url_imagen VARCHAR(500) NOT NULL,
  nombre_archivo VARCHAR(255) NOT NULL,
  descripcion VARCHAR(500),
  articulo_id BIGINT,
  creado_en DATETIME,
  PRIMARY KEY (id),
  KEY fk_imagenes_articulo (articulo_id),
  CONSTRAINT fk_imagenes_articulo FOREIGN KEY (articulo_id) REFERENCES articulos (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Tabla: events
-- =====================================================
CREATE TABLE IF NOT EXISTS events (
  id BIGINT NOT NULL AUTO_INCREMENT,
  event_name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  event_date DATETIME NOT NULL,
  location VARCHAR(255) NOT NULL,
  latitude DOUBLE,
  longitude DOUBLE,
  event_type VARCHAR(50),
  status VARCHAR(50) DEFAULT 'ACTIVE',
  organizer_id BIGINT NOT NULL,
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY (id),
  KEY fk_events_organizer (organizer_id),
  CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Tabla: recycling_centers
-- =====================================================
CREATE TABLE IF NOT EXISTS recycling_centers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  address VARCHAR(255) NOT NULL,
  latitude DOUBLE NOT NULL,
  longitude DOUBLE NOT NULL,
  phone VARCHAR(50) NOT NULL,
  email VARCHAR(255) NOT NULL,
  center_type VARCHAR(50),
  status VARCHAR(50) DEFAULT 'ACTIVE',
  opening_hours VARCHAR(255),
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Tabla: mensajes
-- =====================================================
CREATE TABLE IF NOT EXISTS mensajes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  contenido TEXT NOT NULL,
  remitente_id BIGINT NOT NULL,
  destinatario_id BIGINT NOT NULL,
  articulo_id BIGINT,
  estado VARCHAR(50) DEFAULT 'NO_LEIDO',
  creado_en DATETIME,
  leido_en DATETIME,
  PRIMARY KEY (id),
  KEY fk_mensajes_remitente (remitente_id),
  KEY fk_mensajes_destinatario (destinatario_id),
  KEY fk_mensajes_articulo (articulo_id),
  CONSTRAINT fk_mensajes_remitente FOREIGN KEY (remitente_id) REFERENCES usuario (id),
  CONSTRAINT fk_mensajes_destinatario FOREIGN KEY (destinatario_id) REFERENCES usuario (id),
  CONSTRAINT fk_mensajes_articulo FOREIGN KEY (articulo_id) REFERENCES articulos (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Tabla: solicitudes_intercambio
-- =====================================================
CREATE TABLE IF NOT EXISTS solicitudes_intercambio (
  id BIGINT NOT NULL AUTO_INCREMENT,
  articulo_solicitado_id BIGINT NOT NULL,
  articulo_ofrecido_id BIGINT NOT NULL,
  solicitante_id BIGINT NOT NULL,
  estado VARCHAR(50) DEFAULT 'PENDIENTE',
  creado_en DATETIME,
  actualizado_en DATETIME,
  PRIMARY KEY (id),
  KEY fk_solicitudes_articulo_solicitado (articulo_solicitado_id),
  KEY fk_solicitudes_articulo_ofrecido (articulo_ofrecido_id),
  KEY fk_solicitudes_solicitante (solicitante_id),
  CONSTRAINT fk_solicitudes_articulo_solicitado FOREIGN KEY (articulo_solicitado_id) REFERENCES articulos (id),
  CONSTRAINT fk_solicitudes_articulo_ofrecido FOREIGN KEY (articulo_ofrecido_id) REFERENCES articulos (id),
  CONSTRAINT fk_solicitudes_solicitante FOREIGN KEY (solicitante_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Tabla: vistas_articulos
-- =====================================================
CREATE TABLE IF NOT EXISTS vistas_articulos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  articulo_id BIGINT NOT NULL,
  usuario_id BIGINT,
  direccion_ip VARCHAR(50),
  agente_usuario VARCHAR(255),
  visto_en DATETIME,
  PRIMARY KEY (id),
  KEY fk_vistas_articulo (articulo_id),
  KEY fk_vistas_usuario (usuario_id),
  CONSTRAINT fk_vistas_articulo FOREIGN KEY (articulo_id) REFERENCES articulos (id) ON DELETE CASCADE,
  CONSTRAINT fk_vistas_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Índices adicionales para optimización
-- =====================================================

-- Nota: No usamos IF NOT EXISTS porque MySQL no lo soporta para índices
-- Liquibase controla si ya fueron ejecutados (no se repiten)

-- Índices para búsquedas frecuentes en artículos
CREATE INDEX idx_articulos_categoria ON articulos(categoria);
CREATE INDEX idx_articulos_estado ON articulos(estado);
CREATE INDEX idx_articulos_creado_en ON articulos(creado_en);

-- Índices para mensajes
CREATE INDEX idx_mensajes_estado ON mensajes(estado);
CREATE INDEX idx_mensajes_creado_en ON mensajes(creado_en);

-- Índices para solicitudes
CREATE INDEX idx_solicitudes_estado ON solicitudes_intercambio(estado);

-- Índices para eventos
CREATE INDEX idx_events_date ON events(event_date);
CREATE INDEX idx_events_status ON events(status);

-- Índices para centros
CREATE INDEX idx_centers_status ON recycling_centers(status);

