-- =====================================================
-- Script de datos iniciales para Economía Circular
-- Base de datos de producción con datos de prueba
-- =====================================================

-- Deshabilitar verificaciones temporalmente para facilitar la carga
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";

-- =====================================================
-- 1. USUARIOS
-- =====================================================
-- Nota: Las contraseñas están encriptadas con BCrypt
-- Contraseña para todos los usuarios de prueba: "Test123!"

INSERT INTO usuario (nombre, apellido, email, contrasena, rol, domicilio, activo, creado_en, actualizado_en) VALUES
-- Administradores
('Admin', 'Sistema', 'admin@economiacircular.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'ADMIN', 'Av. Principal 123, Buenos Aires', true, NOW(), NOW()),

-- Usuarios regulares
('María', 'González', 'maria.gonzalez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Calle Corrientes 456, CABA', true, NOW(), NOW()),
('Juan', 'Pérez', 'juan.perez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Av. Rivadavia 789, Buenos Aires', true, NOW(), NOW()),
('Ana', 'Martínez', 'ana.martinez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Calle San Martín 321, La Plata', true, NOW(), NOW()),
('Carlos', 'López', 'carlos.lopez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Av. Libertador 555, Vicente López', true, NOW(), NOW()),
('Laura', 'Fernández', 'laura.fernandez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Calle Belgrano 888, San Isidro', true, NOW(), NOW()),
('Pedro', 'Rodríguez', 'pedro.rodriguez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Av. Córdoba 999, CABA', true, NOW(), NOW()),
('Sofía', 'Sánchez', 'sofia.sanchez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Calle Mitre 147, Quilmes', true, NOW(), NOW()),
('Diego', 'Torres', 'diego.torres@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Av. del Libertador 258, Olivos', true, NOW(), NOW()),
('Valentina', 'Ramírez', 'valentina.ramirez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Calle Lavalle 369, Morón', true, NOW(), NOW()),
('Martín', 'Giménez', 'martin.gimenez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'USER', 'Av. Santa Fe 741, CABA', true, NOW(), NOW());

-- =====================================================
-- 2. ARTÍCULOS
-- =====================================================

INSERT INTO articulos (titulo, descripcion, categoria, condicion, estado, usuario_id, creado_en, actualizado_en) VALUES
-- Electrónicos
('Laptop Dell Inspiron 15', 'Laptop en excelente estado, 8GB RAM, 256GB SSD. Ideal para estudiantes o trabajo remoto. Batería con buena duración.', 'ELECTRONICOS', 'BUENO', 'DISPONIBLE', 2, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('iPhone 11 64GB', 'Teléfono en buen estado, pantalla sin rayones. Incluye cargador original. Batería al 85%.', 'ELECTRONICOS', 'BUENO', 'DISPONIBLE', 3, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('Smart TV Samsung 32"', 'Televisor inteligente funcionando perfectamente. Incluye control remoto. Ideal para habitación o cocina.', 'ELECTRONICOS', 'COMO_NUEVO', 'DISPONIBLE', 4, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
('Auriculares Bluetooth Sony', 'Auriculares inalámbricos con cancelación de ruido. Muy poco uso. Con estuche original.', 'ELECTRONICOS', 'COMO_NUEVO', 'DISPONIBLE', 5, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('Tablet Samsung Galaxy Tab', 'Tablet de 10 pulgadas, perfecta para leer o ver películas. Incluye funda protectora.', 'ELECTRONICOS', 'BUENO', 'RESERVADO', 6, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Ropa
('Campera de cuero negra', 'Campera de cuero genuino talle M. En excelente estado, casi sin uso. Muy abrigada.', 'ROPA', 'COMO_NUEVO', 'DISPONIBLE', 7, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('Zapatillas Nike Running', 'Zapatillas deportivas talle 42, usadas pocas veces. Perfectas para running o gym.', 'ROPA', 'BUENO', 'DISPONIBLE', 8, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
('Vestido de fiesta', 'Vestido elegante color azul talle S, usado una sola vez. Perfecto estado.', 'ROPA', 'COMO_NUEVO', 'DISPONIBLE', 9, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),

-- Libros
('Colección Harry Potter', 'Colección completa de los 7 libros en español. Buen estado de conservación.', 'LIBROS', 'BUENO', 'DISPONIBLE', 2, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
('Libros de programación', 'Pack de 5 libros sobre JavaScript, Python y desarrollo web. Ideales para programadores.', 'LIBROS', 'BUENO', 'DISPONIBLE', 10, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
('Enciclopedia Britannica', 'Colección completa 30 tomos. Excelente estado. Ideal para coleccionistas o estudiantes.', 'LIBROS', 'BUENO', 'DISPONIBLE', 3, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),

-- Muebles
('Escritorio de madera', 'Escritorio sólido de madera de 120x60cm. Perfecto para home office. Muy robusto.', 'MUEBLES', 'BUENO', 'DISPONIBLE', 4, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY)),
('Silla de oficina ergonómica', 'Silla con respaldo alto y apoyabrazos ajustables. Muy cómoda para trabajo prolongado.', 'MUEBLES', 'BUENO', 'DISPONIBLE', 5, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('Biblioteca de roble', 'Biblioteca de 5 estantes en madera de roble. Medidas: 180cm alto x 90cm ancho.', 'MUEBLES', 'COMO_NUEVO', 'DISPONIBLE', 6, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
('Mesa ratona vintage', 'Mesa de centro estilo vintage, restaurada. Perfecta para living. 100x50cm.', 'MUEBLES', 'BUENO', 'INTERCAMBIADO', 7, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Herramientas
('Set de herramientas', 'Caja completa con herramientas variadas. Incluye destornilladores, llaves, martillo, etc.', 'HERRAMIENTAS', 'BUENO', 'DISPONIBLE', 8, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
('Taladro eléctrico Bosch', 'Taladro profesional con percutor. Incluye set de mechas y maletín. Poco uso.', 'HERRAMIENTAS', 'COMO_NUEVO', 'DISPONIBLE', 9, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('Sierra circular', 'Sierra eléctrica en buen estado. Ideal para carpintería. Incluye disco de corte nuevo.', 'HERRAMIENTAS', 'BUENO', 'DISPONIBLE', 10, DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY)),

-- Deportes
('Bicicleta mountain bike', 'Bicicleta rodado 26, 21 velocidades. Excelente estado, recién hecha la puesta a punto.', 'DEPORTES', 'BUENO', 'DISPONIBLE', 2, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
('Tabla de surf', 'Tabla de surf 6\'2", ideal para olas medianas. Buen estado general, algunas marcas de uso.', 'DEPORTES', 'REGULAR', 'DISPONIBLE', 3, DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),
('Set de pesas ajustables', 'Juego de mancuernas ajustables de 2kg a 20kg. Perfecto para entrenar en casa.', 'DEPORTES', 'COMO_NUEVO', 'DISPONIBLE', 4, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- Decoración
('Cuadros decorativos', 'Set de 3 cuadros abstractos modernos. Medidas variadas. Perfectos para living o dormitorio.', 'DECORACION_HOGAR', 'COMO_NUEVO', 'DISPONIBLE', 5, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
('Lámpara de pie moderna', 'Lámpara de diseño minimalista, luz LED regulable. Color negro mate. 180cm altura.', 'DECORACION_HOGAR', 'BUENO', 'DISPONIBLE', 6, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY)),
('Espejo de pared grande', 'Espejo rectangular con marco de madera. 120x80cm. Perfecto para entrada o dormitorio.', 'DECORACION_HOGAR', 'BUENO', 'DISPONIBLE', 7, DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY)),

-- Cocina
('Batidora KitchenAid', 'Batidora de pie profesional color rojo. Incluye 3 accesorios. Muy poco uso.', 'COCINA', 'COMO_NUEVO', 'DISPONIBLE', 8, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('Juego de ollas acero', 'Set completo de 8 piezas en acero quirúrgico. Excelente calidad y estado.', 'COCINA', 'BUENO', 'DISPONIBLE', 9, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
('Cafetera Express Nespresso', 'Cafetera automática con espumador de leche. Funciona perfectamente. Incluye cápsulas.', 'COCINA', 'BUENO', 'DISPONIBLE', 10, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),

-- Jardín
('Cortadora de césped eléctrica', 'Cortadora de césped Bauker 1200W. Buen estado, ideal para jardines pequeños/medianos.', 'JARDIN', 'BUENO', 'DISPONIBLE', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
('Set de herramientas de jardín', 'Incluye pala, rastrillo, tijera de podar, regadera. Todo en buen estado.', 'JARDIN', 'BUENO', 'DISPONIBLE', 3, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
('Manguera con carretel', 'Manguera extensible 30 metros con carretel de pared. Como nueva.', 'JARDIN', 'COMO_NUEVO', 'DISPONIBLE', 4, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- Juguetes
('Lego Star Wars', 'Set grande Lego Star Wars Halcón Milenario. Completo con todas las piezas y manual.', 'JUGUETES', 'COMO_NUEVO', 'DISPONIBLE', 5, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
('Bicicleta infantil', 'Bicicleta rodado 16 con rueditas. Color rosa. Buen estado, lista para usar.', 'JUGUETES', 'BUENO', 'DISPONIBLE', 6, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
('Consola PlayStation 4', 'PS4 Slim 500GB con 2 joysticks y 5 juegos. Excelente estado, poco uso.', 'JUGUETES', 'BUENO', 'DISPONIBLE', 7, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- Instrumentos musicales
('Guitarra acústica Yamaha', 'Guitarra acústica excelente sonido. Incluye funda acolchada y afinador.', 'INSTRUMENTOS_MUSICALES', 'BUENO', 'DISPONIBLE', 8, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY)),
('Teclado Casio 61 teclas', 'Teclado electrónico con 200 sonidos y 100 ritmos. Perfecto para aprender.', 'INSTRUMENTOS_MUSICALES', 'COMO_NUEVO', 'DISPONIBLE', 9, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),

-- Automotriz
('Neumáticos Pirelli 185/65R15', 'Set de 4 neumáticos con 70% de vida útil. Excelente estado.', 'AUTOMOTRIZ', 'BUENO', 'DISPONIBLE', 10, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('Portaequipaje de techo', 'Portaequipaje universal para auto. Capacidad 50kg. Completo con sistema de sujeción.', 'AUTOMOTRIZ', 'BUENO', 'DISPONIBLE', 2, DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY));

-- =====================================================
-- 3. IMÁGENES DE ARTÍCULOS
-- =====================================================

INSERT INTO imagenes_articulos (url_imagen, nombre_archivo, descripcion, articulo_id, creado_en) VALUES
-- Imágenes para Laptop
('https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800', 'laptop_dell_1.jpg', 'Vista frontal de la laptop', 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
('https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=800', 'laptop_dell_2.jpg', 'Detalle del teclado', 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- Imágenes para iPhone
('https://images.unsplash.com/photo-1592286927505-4fb44a5d8ed8?w=800', 'iphone11_1.jpg', 'iPhone 11 frontal', 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Imágenes para Smart TV
('https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=800', 'tv_samsung_1.jpg', 'Smart TV Samsung', 3, DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- Imágenes para otros artículos
('https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800', 'auriculares_1.jpg', 'Auriculares Sony', 4, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('https://images.unsplash.com/photo-1561154464-82e9adf32764?w=800', 'tablet_1.jpg', 'Tablet Samsung', 5, DATE_SUB(NOW(), INTERVAL 10 DAY)),
('https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800', 'campera_1.jpg', 'Campera de cuero', 6, DATE_SUB(NOW(), INTERVAL 4 DAY)),
('https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800', 'zapatillas_1.jpg', 'Zapatillas Nike', 7, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800', 'vestido_1.jpg', 'Vestido de fiesta', 8, DATE_SUB(NOW(), INTERVAL 8 DAY)),
('https://images.unsplash.com/photo-1589998059171-988d887df646?w=800', 'libros_hp_1.jpg', 'Colección Harry Potter', 9, DATE_SUB(NOW(), INTERVAL 12 DAY)),
('https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800', 'libros_prog_1.jpg', 'Libros de programación', 10, DATE_SUB(NOW(), INTERVAL 9 DAY)),
('https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800', 'escritorio_1.jpg', 'Escritorio de madera', 12, DATE_SUB(NOW(), INTERVAL 11 DAY)),
('https://images.unsplash.com/photo-1580480055273-228ff5388ef8?w=800', 'silla_1.jpg', 'Silla ergonómica', 13, DATE_SUB(NOW(), INTERVAL 5 DAY)),
('https://images.unsplash.com/photo-1486946255434-2466348c2166?w=800', 'bicicleta_1.jpg', 'Bicicleta mountain bike', 18, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=800', 'guitarra_1.jpg', 'Guitarra acústica', 32, DATE_SUB(NOW(), INTERVAL 11 DAY));

-- =====================================================
-- 4. EVENTOS
-- =====================================================

INSERT INTO events (event_name, description, event_date, location, latitude, longitude, event_type, status, organizer_id, created_at, updated_at) VALUES
('Feria de Intercambio Sustentable', 'Gran feria de intercambio de objetos y productos sustentables. Traé lo que no uses y llevate algo nuevo. Entrada libre y gratuita.', DATE_ADD(NOW(), INTERVAL 15 DAY), 'Plaza San Martín, Buenos Aires', -34.5959, -58.3745, 'FAIR', 'ACTIVE', 1, NOW(), NOW()),

('Taller de Reparación de Electrónicos', 'Aprende a reparar tus propios dispositivos electrónicos. Traé tus aparatos en desuso y te ayudamos a repararlos. Materiales incluidos.', DATE_ADD(NOW(), INTERVAL 7 DAY), 'Centro Cultural Konex, CABA', -34.6019, -58.4003, 'WORKSHOP', 'ACTIVE', 1, NOW(), NOW()),

('Conferencia: Economía Circular en Argentina', 'Expertos nacionales e internacionales hablarán sobre el futuro de la economía circular. Incluye panel de preguntas y networking.', DATE_ADD(NOW(), INTERVAL 30 DAY), 'Centro de Convenciones Buenos Aires', -34.5833, -58.4167, 'CONFERENCE', 'ACTIVE', 1, NOW(), NOW()),

('Meetup de Reciclaje Comunitario', 'Encuentro informal para compartir experiencias sobre reciclaje y economía circular. Café y merienda incluida.', DATE_ADD(NOW(), INTERVAL 10 DAY), 'Café Tortoni, Av. de Mayo 825', -34.6089, -58.3751, 'MEETUP', 'ACTIVE', 1, NOW(), NOW()),

('Evento de Intercambio de Ropa', 'Intercambia prendas que ya no uses por otras que necesites. Cada persona puede traer hasta 5 prendas en buen estado.', DATE_ADD(NOW(), INTERVAL 5 DAY), 'Club de Innovación, Palermo', -34.5889, -58.4250, 'EXCHANGE_EVENT', 'ACTIVE', 2, NOW(), NOW()),

('Taller: Compostaje en Casa', 'Aprende a hacer compost casero y reducir tus residuos orgánicos. Incluye kit inicial de compostaje.', DATE_ADD(NOW(), INTERVAL 12 DAY), 'Jardín Botánico Buenos Aires', -34.5799, -58.4192, 'WORKSHOP', 'ACTIVE', 3, NOW(), NOW()),

('Feria de Muebles Restaurados', 'Exhibición y venta de muebles restaurados y reciclados. Únicos y con historia.', DATE_ADD(NOW(), INTERVAL 20 DAY), 'Mercado de San Telmo', -34.6211, -58.3724, 'FAIR', 'ACTIVE', 4, NOW(), NOW()),

('Jornada de Limpieza de Playas', 'Actividad comunitaria de limpieza y concientización ambiental. Equipamiento provisto.', DATE_ADD(NOW(), INTERVAL 8 DAY), 'Playa Bristol, Mar del Plata', -38.0055, -57.5426, 'MEETUP', 'ACTIVE', 5, NOW(), NOW());

-- =====================================================
-- 5. CENTROS DE RECICLAJE
-- =====================================================

INSERT INTO recycling_centers (name, description, address, latitude, longitude, phone, email, center_type, status, opening_hours, created_at, updated_at) VALUES
('EcoPunto Palermo', 'Centro de reciclaje integral que recibe papel, cartón, plástico, vidrio y electrónicos. Personal capacitado para asesoramiento.', 'Av. Santa Fe 3501, Palermo', -34.5889, -58.4186, '+54 11 4567-8901', 'palermo@ecopunto.com', 'RECYCLING_CENTER', 'ACTIVE', 'Lunes a Viernes 9:00-18:00, Sábados 10:00-14:00', NOW(), NOW()),

('Punto Verde Belgrano', 'Punto de recolección especializado en residuos electrónicos y baterías. Certificado por normas ambientales.', 'Av. Cabildo 2202, Belgrano', -34.5621, -58.4567, '+54 11 4789-0123', 'belgrano@puntoverde.com', 'WASTE_COLLECTION_POINT', 'ACTIVE', 'Lunes a Sábado 8:00-20:00', NOW(), NOW()),

('Fundación Reciclar', 'ONG dedicada al reciclaje y educación ambiental. Recibimos todo tipo de materiales reciclables y damos talleres gratuitos.', 'Calle Defensa 456, San Telmo', -34.6211, -58.3699, '+54 11 4890-1234', 'info@fundacionreciclar.org', 'NGO', 'ACTIVE', 'Lunes a Viernes 10:00-17:00', NOW(), NOW()),

('Taller Eco Reparaciones', 'Taller especializado en reparación de electrodomésticos y electrónicos. Promovemos la reparación antes que el descarte.', 'Av. Corrientes 2345, Once', -34.6037, -58.4006, '+54 11 4901-2345', 'contacto@ecoreparaciones.com', 'REPAIR_WORKSHOP', 'ACTIVE', 'Lunes a Viernes 9:00-19:00, Sábados 10:00-15:00', NOW(), NOW()),

('Centro Verde La Plata', 'Centro municipal de reciclaje. Aceptamos residuos domiciliarios separados y brindamos asesoramiento sobre separación en origen.', 'Calle 7 entre 41 y 42, La Plata', -34.9204, -57.9544, '+54 221 453-6789', 'centroverde@laplata.gob.ar', 'RECYCLING_CENTER', 'ACTIVE', 'Lunes a Domingo 7:00-19:00', NOW(), NOW()),

('Eco Punto Móvil Zona Norte', 'Punto de recolección itinerante que recorre Vicente López, San Isidro y Olivos. Consultar cronograma en web.', 'Varios puntos - Ver sitio web', -34.5264, -58.4784, '+54 11 5012-3456', 'movil@ecopunto.com', 'WASTE_COLLECTION_POINT', 'ACTIVE', 'Ver cronograma en www.ecopuntomovil.com', NOW(), NOW()),

('ReUse Workshop', 'Taller de reparación comunitario. Traé tus objetos rotos y te ayudamos a repararlos. Filosofía: reparar, reutilizar, reciclar.', 'Av. Rivadavia 5678, Caballito', -34.6177, -58.4368, '+54 11 5123-4567', 'hola@reusebs.org', 'REPAIR_WORKSHOP', 'ACTIVE', 'Miércoles y Viernes 15:00-20:00, Sábados 11:00-18:00', NOW(), NOW()),

('EcoReciclajes Quilmes', 'Centro de reciclaje con planta de separación. Compramos materiales reciclables a particulares y empresas.', 'Calle Mitre 890, Quilmes', -34.7203, -58.2630, '+54 11 5234-5678', 'ventas@ecoreciclajes.com', 'RECYCLING_CENTER', 'ACTIVE', 'Lunes a Viernes 7:00-17:00', NOW(), NOW()),

('Fundación Manos Verdes', 'ONG que promueve el reciclaje inclusivo. Trabajamos con cooperativas de recuperadores urbanos.', 'Av. Boedo 1234, Boedo', -34.6319, -58.4114, '+54 11 5345-6789', 'info@manosverdes.org.ar', 'NGO', 'ACTIVE', 'Lunes a Viernes 9:00-18:00', NOW(), NOW()),

('Punto Limpio Avellaneda', 'Centro municipal de recepción de residuos voluminosos, electrónicos y especiales. Servicio gratuito para residentes.', 'Av. Mitre 750, Avellaneda', -34.6617, -58.3657, '+54 11 5456-7890', 'puntolimpio@avellaneda.gob.ar', 'WASTE_COLLECTION_POINT', 'ACTIVE', 'Martes a Domingo 8:00-16:00', NOW(), NOW());

-- =====================================================
-- 6. TALLERES
-- =====================================================

INSERT INTO taller (nombre, direccion, email, telefono, tipo_servicio) VALUES
('Reparaciones Don José', 'Av. San Martín 1234, Villa Crespo', 'reparaciones.jose@email.com', '+54 11 4567-1111', 'Reparación de electrodomésticos'),
('TecnoFix Express', 'Calle Corrientes 567, Almagro', 'info@tecnofix.com', '+54 11 4567-2222', 'Reparación de electrónicos'),
('Carpintería El Roble', 'Av. Warnes 890, Villa Ortúzar', 'elroble@carpinteria.com', '+54 11 4567-3333', 'Restauración de muebles'),
('Taller Mecánico Rodriguez', 'Calle Gaona 456, Flores', 'mecanica.rodriguez@email.com', '+54 11 4567-4444', 'Reparación de bicicletas'),
('Electrónica Martínez', 'Av. Rivadavia 2345, Caballito', 'electronica.martinez@email.com', '+54 11 4567-5555', 'Reparación de computadoras'),
('Costurería La Aguja de Oro', 'Calle Acoyte 123, Caballito', 'costura.oro@email.com', '+54 11 4567-6666', 'Reparación y arreglos de ropa'),
('Taller MultiService', 'Av. Nazca 789, Villa del Parque', 'multiservice@email.com', '+54 11 4567-7777', 'Reparaciones generales del hogar'),
('Herrería y Soldaduras Norte', 'Av. Cabildo 3456, Belgrano', 'herreria.norte@email.com', '+54 11 4567-8888', 'Herrería y reparación metálica');

-- =====================================================
-- 7. MENSAJES
-- =====================================================

INSERT INTO mensajes (contenido, remitente_id, destinatario_id, articulo_id, estado, creado_en, leido_en) VALUES
-- Mensajes sobre artículos
('Hola! Me interesa tu laptop. ¿Sigue disponible? ¿Podríamos coordinar para verla?', 3, 2, 1, 'LEIDO', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('Si, está disponible! Podemos encontrarnos mañana en la tarde si te queda bien.', 2, 3, 1, 'LEIDO', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('Perfecto! Te escribo mañana para confirmar. Gracias!', 3, 2, 1, 'LEIDO', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

('Me encanta la bicicleta! ¿Aceptarías intercambiarla por mi tablet?', 5, 2, 18, 'LEIDO', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('Hola! Podría ser, déjame ver la tablet. ¿Tenés fotos?', 2, 5, 18, 'LEIDO', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

('Consulta sobre la campera de cuero: ¿qué talle es exactamente?', 4, 7, 6, 'LEIDO', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('Hola! Es talle M, equivalente a 48. Te puedo pasar las medidas exactas si querés.', 7, 4, 6, 'LEIDO', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('Si, me serviría! Gracias!', 4, 7, 6, 'NO_LEIDO', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL),

('Buenas! El iPhone sigue disponible? Me interesa mucho.', 6, 3, 2, 'LEIDO', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('Hola! Sí, está disponible. ¿Querés que coordinemos para que lo veas?', 3, 6, 2, 'NO_LEIDO', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),

('Los auriculares Sony se ven muy buenos. ¿Cuánto tiempo los usaste?', 8, 5, 4, 'LEIDO', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('Los usé apenas 3 meses, prácticamente nuevos!', 5, 8, 4, 'NO_LEIDO', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),

-- Mensajes generales entre usuarios
('Hola María! Gracias por el intercambio, me encantó la mesa ratona!', 8, 2, NULL, 'LEIDO', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('Gracias a vos! La silla quedó perfecta en mi oficina. Éxitos!', 2, 8, NULL, 'LEIDO', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

('Bienvenido a la plataforma! Si tenés alguna duda sobre cómo funciona, avisame.', 1, 11, NULL, 'NO_LEIDO', DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL);

-- =====================================================
-- 8. SOLICITUDES DE INTERCAMBIO
-- =====================================================

INSERT INTO solicitudes_intercambio (articulo_solicitado_id, articulo_ofrecido_id, solicitante_id, estado, creado_en, actualizado_en) VALUES
-- Intercambio completado
(15, 5, 6, 'COMPLETADO', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Intercambios aceptados pendientes de completar
(1, 10, 3, 'ACEPTADO', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(18, 20, 4, 'ACEPTADO', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Intercambios pendientes
(2, 7, 6, 'PENDIENTE', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 19, 8, 'PENDIENTE', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(12, 16, 9, 'PENDIENTE', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Intercambios rechazados
(3, 6, 5, 'RECHAZADO', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
(13, 8, 7, 'RECHAZADO', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),

-- Intercambios cancelados
(9, 21, 10, 'CANCELADO', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY));

-- =====================================================
-- 9. VISTAS DE ARTÍCULOS (para estadísticas)
-- =====================================================

INSERT INTO vistas_articulos (articulo_id, usuario_id, direccion_ip, agente_usuario, visto_en) VALUES
-- Artículos más vistos
(1, 3, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 4, '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 5, '192.168.1.102', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 6, '192.168.1.103', 'Mozilla/5.0 (Linux; Android 11)', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 7, '192.168.1.104', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 2 DAY)),

(2, 3, '192.168.1.105', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 5, '192.168.1.106', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 6, '192.168.1.107', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 8, '192.168.1.108', 'Mozilla/5.0 (Linux; Android 11)', DATE_SUB(NOW(), INTERVAL 1 DAY)),

(3, 2, '192.168.1.109', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, 4, '192.168.1.110', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 7, '192.168.1.111', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', DATE_SUB(NOW(), INTERVAL 4 DAY)),

(4, 2, '192.168.1.112', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 8, '192.168.1.113', 'Mozilla/5.0 (Linux; Android 11)', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 9, '192.168.1.114', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 1 DAY)),

(18, 3, '192.168.1.115', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(18, 4, '192.168.1.116', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(18, 5, '192.168.1.117', 'Mozilla/5.0 (Linux; Android 11)', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(18, 6, '192.168.1.118', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 3 DAY)),

(6, 4, '192.168.1.119', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 5, '192.168.1.120', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', DATE_SUB(NOW(), INTERVAL 2 DAY)),

(12, 3, '192.168.1.121', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(12, 8, '192.168.1.122', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(12, 9, '192.168.1.123', 'Mozilla/5.0 (Linux; Android 11)', DATE_SUB(NOW(), INTERVAL 8 DAY)),

(32, 2, '192.168.1.124', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(32, 5, '192.168.1.125', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 9 DAY)),

-- Vistas anónimas (sin usuario)
(1, NULL, '200.45.123.45', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, NULL, '200.45.123.46', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, NULL, '200.45.123.47', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0)', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, NULL, '200.45.123.48', 'Mozilla/5.0 (Linux; Android 11)', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(18, NULL, '200.45.123.49', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- =====================================================
-- RESTAURAR CONFIGURACIONES
-- =====================================================

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- VERIFICACIÓN DE DATOS INSERTADOS
-- =====================================================

SELECT 'Datos insertados correctamente:' as '';
SELECT CONCAT('Usuarios: ', COUNT(*)) as total FROM usuario;
SELECT CONCAT('Artículos: ', COUNT(*)) as total FROM articulos;
SELECT CONCAT('Imágenes: ', COUNT(*)) as total FROM imagenes_articulos;
SELECT CONCAT('Eventos: ', COUNT(*)) as total FROM events;
SELECT CONCAT('Centros de Reciclaje: ', COUNT(*)) as total FROM recycling_centers;
SELECT CONCAT('Talleres: ', COUNT(*)) as total FROM taller;
SELECT CONCAT('Mensajes: ', COUNT(*)) as total FROM mensajes;
SELECT CONCAT('Solicitudes de Intercambio: ', COUNT(*)) as total FROM solicitudes_intercambio;
SELECT CONCAT('Vistas de Artículos: ', COUNT(*)) as total FROM vistas_articulos;

-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================

