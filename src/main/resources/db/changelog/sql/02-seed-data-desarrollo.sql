-- =====================================================
-- Script de datos mínimos para Desarrollo Local
-- Versión ligera para pruebas rápidas
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";

-- =====================================================
-- USUARIOS (versión mínima)
-- Contraseña para todos: "Test123!"
-- =====================================================

INSERT INTO usuario (nombre, apellido, email, contrasena, rol, domicilio, activo, creado_en, actualizado_en) VALUES
('Admin', 'Sistema', 'admin@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'ADMIN', 'Av. Principal 123', true, NOW(), NOW()),
('Test', 'User', 'test@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Calle Test 456', true, NOW(), NOW()),
('María', 'González', 'maria@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Av. Corrientes 789', true, NOW(), NOW()),
('Juan', 'Pérez', 'juan@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Calle Falsa 321', true, NOW(), NOW());

-- =====================================================
-- ARTÍCULOS (versión mínima)
-- =====================================================

INSERT INTO articulos (titulo, descripcion, categoria, condicion, estado, usuario_id, creado_en, actualizado_en) VALUES
('Laptop de Prueba', 'Laptop para testing de la aplicación', 'ELECTRONICOS', 'BUENO', 'DISPONIBLE', 2, NOW(), NOW()),
('iPhone Test', 'Smartphone para pruebas', 'ELECTRONICOS', 'BUENO', 'DISPONIBLE', 3, NOW(), NOW()),
('Libro de Programación', 'Libro de JavaScript', 'LIBROS', 'COMO_NUEVO', 'DISPONIBLE', 2, NOW(), NOW()),
('Silla de Oficina', 'Silla ergonómica', 'MUEBLES', 'BUENO', 'DISPONIBLE', 4, NOW(), NOW()),
('Bicicleta MTB', 'Bicicleta mountain bike', 'DEPORTES', 'BUENO', 'RESERVADO', 3, NOW(), NOW());

-- =====================================================
-- IMÁGENES DE ARTÍCULOS
-- =====================================================

INSERT INTO imagenes_articulos (url_imagen, nombre_archivo, descripcion, articulo_id, creado_en) VALUES
('https://via.placeholder.com/800x600', 'laptop_test.jpg', 'Imagen de prueba laptop', 1, NOW()),
('https://via.placeholder.com/800x600', 'iphone_test.jpg', 'Imagen de prueba iphone', 2, NOW()),
('https://via.placeholder.com/800x600', 'libro_test.jpg', 'Imagen de prueba libro', 3, NOW());

-- =====================================================
-- EVENTOS
-- =====================================================

INSERT INTO events (event_name, description, event_date, location, latitude, longitude, event_type, status, usuario_id, created_at, updated_at) VALUES
('Feria de Prueba', 'Evento de testing', DATE_ADD(NOW(), INTERVAL 7 DAY), 'Plaza Test', -34.6037, -58.3816, 'FAIR', 'ACTIVE', 1, NOW(), NOW()),
('Taller Test', 'Taller de prueba', DATE_ADD(NOW(), INTERVAL 14 DAY), 'Centro Test', -34.6037, -58.3816, 'WORKSHOP', 'ACTIVE', 1, NOW(), NOW());

-- =====================================================
-- CENTROS DE RECICLAJE
-- =====================================================

INSERT INTO recycling_centers (name, description, address, latitude, longitude, phone, email, center_type, status, opening_hours, created_at, updated_at) VALUES
('Centro Test 1', 'Centro de reciclaje de prueba', 'Calle Test 123', -34.6037, -58.3816, '+54 11 1234-5678', 'test@centro.com', 'RECYCLING_CENTER', 'ACTIVE', 'Lun-Vie 9-18', NOW(), NOW()),
('ONG Test', 'ONG de prueba', 'Av. Test 456', -34.6037, -58.3816, '+54 11 8765-4321', 'test@ong.com', 'NGO', 'ACTIVE', 'Lun-Vie 10-17', NOW(), NOW());

-- =====================================================
-- TALLERES
-- =====================================================

INSERT INTO taller (nombre, direccion, email, telefono, tipo_servicio) VALUES
('Taller Test 1', 'Calle Test 100', 'taller1@test.com', '+54 11 1111-1111', 'Reparación general'),
('Taller Test 2', 'Av. Test 200', 'taller2@test.com', '+54 11 2222-2222', 'Electrónica');

-- =====================================================
-- MENSAJES
-- =====================================================

INSERT INTO mensajes (contenido, remitente_id, destinatario_id, articulo_id, estado, creado_en, leido_en) VALUES
('Hola! Me interesa tu laptop', 3, 2, 1, 'LEIDO', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('Claro! Está disponible', 2, 3, 1, 'LEIDO', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('Me interesa el iPhone', 4, 3, 2, 'NO_LEIDO', NOW(), NULL);

-- =====================================================
-- SOLICITUDES DE INTERCAMBIO
-- =====================================================

INSERT INTO solicitudes_intercambio (articulo_solicitado_id, articulo_ofrecido_id, solicitante_id, estado, creado_en, actualizado_en) VALUES
(1, 3, 3, 'PENDIENTE', NOW(), NOW()),
(2, 4, 4, 'ACEPTADO', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- VISTAS DE ARTÍCULOS
-- =====================================================

INSERT INTO vistas_articulos (articulo_id, usuario_id, direccion_ip, agente_usuario, visto_en) VALUES
(1, 3, '127.0.0.1', 'Mozilla/5.0 Test', NOW()),
(1, 4, '127.0.0.2', 'Mozilla/5.0 Test', NOW()),
(2, 2, '127.0.0.3', 'Mozilla/5.0 Test', NOW());

-- =====================================================
-- RESTAURAR CONFIGURACIONES
-- =====================================================

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- VERIFICACIÓN
-- =====================================================

SELECT '===== Datos de Desarrollo Cargados =====' as '';
SELECT CONCAT('✓ Usuarios: ', COUNT(*)) as resultado FROM usuario;
SELECT CONCAT('✓ Artículos: ', COUNT(*)) as resultado FROM articulos;
SELECT CONCAT('✓ Eventos: ', COUNT(*)) as resultado FROM events;
SELECT CONCAT('✓ Centros: ', COUNT(*)) as resultado FROM recycling_centers;
SELECT CONCAT('✓ Talleres: ', COUNT(*)) as resultado FROM taller;

SELECT '' as '';
SELECT 'Credenciales de prueba:' as '';
SELECT 'Admin: admin@test.com / Test123!' as '';
SELECT 'User: test@test.com / Test123!' as '';

