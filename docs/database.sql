-- ==========================================
-- SECTION 1: DDL (Data Definition Language)
-- ==========================================
DROP DATABASE IF EXISTS erp_frescura_total;
CREATE DATABASE erp_frescura_total CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_frescura_total;

-- TABLAS DE CATÁLOGO (LOOKUP TABLES)

CREATE TABLE estados_lote (
                              id_estado INT AUTO_INCREMENT PRIMARY KEY,
                              nombre VARCHAR(50) NOT NULL UNIQUE,
                              descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE tipos_cliente (
                               id_tipo INT AUTO_INCREMENT PRIMARY KEY,
                               nombre VARCHAR(50) NOT NULL UNIQUE,
                               descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE estados_venta (
                               id_estado INT AUTO_INCREMENT PRIMARY KEY,
                               nombre VARCHAR(50) NOT NULL UNIQUE,
                               descripcion TEXT
) ENGINE=InnoDB;

-- INFORMACIÓN DE LA EMPRESA

CREATE TABLE informacion_empresa (
                                     id_empresa INT AUTO_INCREMENT PRIMARY KEY,
                                     nombre_empresa VARCHAR(255) NOT NULL,
                                     nit VARCHAR(20) NOT NULL UNIQUE,
                                     direccion TEXT NOT NULL,
                                     ciudad VARCHAR(100),
                                     telefono VARCHAR(20),
                                     email VARCHAR(100)
) ENGINE=InnoDB;

-- MÓDULO: PROVEEDORES

CREATE TABLE proveedores (
                             id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
                             nombre VARCHAR(255) NOT NULL,
                             nit VARCHAR(20) NOT NULL UNIQUE,
                             persona_contacto VARCHAR(100),
                             email VARCHAR(100) NOT NULL,
                             telefono VARCHAR(20),
                             direccion TEXT,
                             activo BOOLEAN DEFAULT TRUE,
                             fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- MÓDULO: PRODUCTOS

CREATE TABLE categorias_producto (
                                     id_categoria INT AUTO_INCREMENT PRIMARY KEY,
                                     nombre VARCHAR(100) NOT NULL UNIQUE,
                                     descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE productos (
                           id_producto INT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(255) NOT NULL,
                           descripcion TEXT,
                           id_categoria INT NOT NULL,
                           unidad_medida VARCHAR(20) NOT NULL COMMENT 'Ej: kg, litro, unidad',
                           precio_base DECIMAL(10, 2),
                           FOREIGN KEY (id_categoria) REFERENCES categorias_producto(id_categoria)
) ENGINE=InnoDB;

CREATE TABLE productos_proveedor (
                                     id_proveedor INT NOT NULL,
                                     id_producto INT NOT NULL,
                                     PRIMARY KEY (id_proveedor, id_producto),
                                     FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor) ON DELETE CASCADE,
                                     FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE
) ENGINE=InnoDB;

-- MÓDULO: INVENTARIO Y CALIDAD

CREATE TABLE lotes_inventario (
                                  id_lote INT AUTO_INCREMENT PRIMARY KEY,
                                  numero_lote VARCHAR(50) NOT NULL UNIQUE,
                                  id_producto INT NOT NULL,
                                  id_proveedor INT NOT NULL,
                                  fecha_ingreso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  fecha_vencimiento DATE NOT NULL,
                                  cantidad_inicial DECIMAL(10, 2) NOT NULL,
                                  cantidad DECIMAL(10, 2) NOT NULL,
                                  precio_compra DECIMAL(10, 2) NOT NULL,
                                  precio_venta DECIMAL(10, 2) NOT NULL,
                                  pasillo VARCHAR(50),
                                  estante VARCHAR(50),
                                  id_estado INT NOT NULL,
                                  FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
                                  FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor),
                                  FOREIGN KEY (id_estado) REFERENCES estados_lote(id_estado),
                                  INDEX idx_fecha_vencimiento (fecha_vencimiento),
                                  INDEX idx_estado (id_estado),
                                  INDEX idx_producto (id_producto)
) ENGINE=InnoDB;

CREATE TABLE registro_cuarentena (
                                     id_registro INT AUTO_INCREMENT PRIMARY KEY,
                                     id_lote INT NOT NULL,
                                     motivo TEXT NOT NULL,
                                     usuario VARCHAR(100) NOT NULL,
                                     fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     resuelta BOOLEAN DEFAULT FALSE,
                                     fecha_resolucion DATETIME NULL,
                                     notas_resolucion TEXT,
                                     FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote),
                                     INDEX idx_resuelta (resuelta)
) ENGINE=InnoDB;

CREATE TABLE inspecciones_calidad (
                                      id_inspeccion INT AUTO_INCREMENT PRIMARY KEY,
                                      id_lote INT NOT NULL,
                                      fecha_inspeccion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      resultado ENUM('Aprobado', 'Rechazado') NOT NULL,
                                      nombre_inspector VARCHAR(100) NOT NULL,
                                      motivo_rechazo TEXT COMMENT 'Motivo detallado si el resultado es Rechazado',
                                      notas TEXT,
                                      FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote),
                                      INDEX idx_resultado (resultado)
) ENGINE=InnoDB;

CREATE TABLE registro_mermas (
                                 id_registro INT AUTO_INCREMENT PRIMARY KEY,
                                 id_lote INT NOT NULL,
                                 id_inspeccion INT COMMENT 'Inspección que generó esta merma (si aplica)',
                                 cantidad_perdida DECIMAL(10, 2) NOT NULL,
                                 motivo TEXT NOT NULL,
                                 fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 usuario_registro VARCHAR(100),
                                 FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote),
                                 FOREIGN KEY (id_inspeccion) REFERENCES inspecciones_calidad(id_inspeccion) ON DELETE SET NULL,
                                 INDEX idx_fecha (fecha_registro)
) ENGINE=InnoDB;

-- MÓDULO: PROMOCIONES

CREATE TABLE promociones (
                             id_promocion INT AUTO_INCREMENT PRIMARY KEY,
                             nombre VARCHAR(255) NOT NULL,
                             descripcion TEXT,
                             tasa_descuento DECIMAL(5, 2) NOT NULL COMMENT 'Ej: 0.50 para 50%',
                             aplica_estado_lote INT,
                             aplica_categoria_producto INT,
                             activa BOOLEAN DEFAULT TRUE,
                             fecha_inicio DATE,
                             fecha_fin DATE,
                             FOREIGN KEY (aplica_estado_lote) REFERENCES estados_lote(id_estado),
                             FOREIGN KEY (aplica_categoria_producto) REFERENCES categorias_producto(id_categoria),
                             INDEX idx_activa (activa)
) ENGINE=InnoDB;

-- MÓDULO: CLIENTES Y VENTAS

CREATE TABLE clientes (
                          id_cliente INT AUTO_INCREMENT PRIMARY KEY,
                          nombre VARCHAR(255) NOT NULL,
                          documento VARCHAR(20) NOT NULL UNIQUE,
                          email VARCHAR(100),
                          telefono VARCHAR(20),
                          direccion TEXT,
                          id_tipo INT NOT NULL,
                          fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (id_tipo) REFERENCES tipos_cliente(id_tipo)
) ENGINE=InnoDB;

CREATE TABLE ventas (
                        id_venta INT AUTO_INCREMENT PRIMARY KEY,
                        numero_venta VARCHAR(50) NOT NULL UNIQUE,
                        id_cliente INT NOT NULL,
                        fecha_venta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        subtotal DECIMAL(12, 2) NOT NULL,
                        total_descuento DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
                        total_impuesto DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
                        monto_total DECIMAL(12, 2) NOT NULL,
                        id_estado INT NOT NULL,
                        usuario_registro VARCHAR(100) COMMENT 'Usuario que registró la venta',
                        FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
                        FOREIGN KEY (id_estado) REFERENCES estados_venta(id_estado),
                        INDEX idx_estado (id_estado),
                        INDEX idx_fecha (fecha_venta)
) ENGINE=InnoDB;

CREATE TABLE detalles_venta (
                                id_detalle INT AUTO_INCREMENT PRIMARY KEY,
                                id_venta INT NOT NULL,
                                id_lote INT NOT NULL,
                                cantidad_vendida DECIMAL(10, 2) NOT NULL,
                                precio_unitario DECIMAL(10, 2) NOT NULL,
                                descuento_aplicado DECIMAL(5, 2) DEFAULT 0.00,
                                monto_descuento DECIMAL(10, 2) DEFAULT 0.00,
                                tasa_impuesto DECIMAL(5, 2) DEFAULT 0.00,
                                monto_impuesto DECIMAL(10, 2) DEFAULT 0.00,
                                subtotal DECIMAL(10, 2) NOT NULL,
                                total_linea DECIMAL(10, 2) NOT NULL,
                                FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE,
                                FOREIGN KEY (id_lote) REFERENCES lotes_inventario(id_lote)
) ENGINE=InnoDB;

CREATE TABLE facturas (
                          id_factura INT AUTO_INCREMENT PRIMARY KEY,
                          id_venta INT NOT NULL,
                          numero_factura VARCHAR(50) NOT NULL UNIQUE,
                          cufe VARCHAR(255) NOT NULL UNIQUE COMMENT 'Código simulado para factura electrónica',
                          enlace_qr VARCHAR(255) COMMENT 'URL o datos para generar código QR',
                          fecha_emision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          subtotal DECIMAL(12, 2) NOT NULL,
                          total_descuento DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
                          monto_impuesto DECIMAL(12, 2) NOT NULL,
                          monto_total DECIMAL(12, 2) NOT NULL,
                          FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
                          INDEX idx_numero (numero_factura)
) ENGINE=InnoDB;

-- MÓDULO: LOGÍSTICA Y ENTREGAS

CREATE TABLE conductores (
                             id_conductor INT AUTO_INCREMENT PRIMARY KEY,
                             nombre VARCHAR(255) NOT NULL,
                             numero_licencia VARCHAR(50) NOT NULL UNIQUE,
                             telefono VARCHAR(20),
                             email VARCHAR(100),
                             activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE vehiculos (
                           id_vehiculo INT AUTO_INCREMENT PRIMARY KEY,
                           placa VARCHAR(20) NOT NULL UNIQUE,
                           modelo VARCHAR(100),
                           capacidad DECIMAL(10, 2) COMMENT 'Capacidad en kg o unidades',
                           estado ENUM('Activo', 'Inactivo', 'Mantenimiento') DEFAULT 'Activo'
) ENGINE=InnoDB;

CREATE TABLE rutas_entrega (
                               id_ruta INT AUTO_INCREMENT PRIMARY KEY,
                               nombre_ruta VARCHAR(255) NOT NULL,
                               fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                               usuario_creacion VARCHAR(100) COMMENT 'Usuario logístico que creó la ruta',
                               hora_salida_estimada DATETIME,
                               hora_llegada_estimada DATETIME,
                               hora_salida_real DATETIME,
                               hora_llegada_real DATETIME,
                               estado ENUM('Planificada', 'En Transito', 'Completada', 'Cancelada') DEFAULT 'Planificada',
                               INDEX idx_estado (estado),
                               INDEX idx_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB;

CREATE TABLE asignaciones_entrega (
                                      id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
                                      id_ruta INT NOT NULL,
                                      id_vehiculo INT NOT NULL,
                                      id_conductor INT NOT NULL,
                                      fecha_asignacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                                      usuario_asignacion VARCHAR(100) COMMENT 'Usuario que realizó la asignación',
                                      notas TEXT,
                                      FOREIGN KEY (id_ruta) REFERENCES rutas_entrega(id_ruta),
                                      FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo),
                                      FOREIGN KEY (id_conductor) REFERENCES conductores(id_conductor),
                                      INDEX idx_ruta (id_ruta)
) ENGINE=InnoDB;

CREATE TABLE ventas_ruta (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             id_ruta INT NOT NULL,
                             id_venta INT NOT NULL,
                             orden_entrega INT COMMENT 'Orden de entrega en la ruta',
                             direccion_destino TEXT NOT NULL,
                             fecha_entrega DATETIME,
                             recibido_por VARCHAR(255),
                             observaciones TEXT,
                             FOREIGN KEY (id_ruta) REFERENCES rutas_entrega(id_ruta),
                             FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
                             UNIQUE KEY (id_ruta, id_venta),
                             INDEX idx_ruta (id_ruta),
                             INDEX idx_venta (id_venta)
) ENGINE=InnoDB;

-- DATOS INICIALES (SOLO CATÁLOGOS ESENCIALES)

INSERT INTO estados_lote (nombre, descripcion) VALUES
                                                   ('Disponible', 'Lote disponible para venta'),
                                                   ('Proximo a Vencer', 'Lote próximo a vencer (7 días o menos)'),
                                                   ('Vencido', 'Lote vencido, no apto para venta'),
                                                   ('En Cuarentena', 'Lote en cuarentena pendiente de inspección');

INSERT INTO tipos_cliente (nombre, descripcion) VALUES
                                                    ('Mayorista', 'Cliente que compra en grandes cantidades'),
                                                    ('Minorista', 'Cliente que compra en pequeñas cantidades');

INSERT INTO estados_venta (nombre, descripcion) VALUES
                                                    ('Registrada', 'Venta registrada, pendiente de preparación'),
                                                    ('En Preparacion', 'Pedido en preparación para despacho'),
                                                    ('En Transito', 'Pedido en tránsito hacia el cliente'),
                                                    ('Entregada', 'Pedido entregado al cliente'),
                                                    ('Cancelada', 'Venta cancelada');

INSERT INTO informacion_empresa (nombre_empresa, nit, direccion, ciudad, telefono, email) VALUES
    ('Frescura Total S.A.S.', '900123456-7', 'Calle 123 #45-67', 'Bogotá', '(601) 234-5678', 'contacto@frescuratotal.com');

INSERT INTO categorias_producto (nombre, descripcion) VALUES
                                                          ('Frutas', 'Frutas frescas'),
                                                          ('Verduras', 'Verduras y hortalizas'),
                                                          ('Lacteos', 'Productos lácteos'),
                                                          ('Carnes', 'Carnes y derivados'),
                                                          ('Granos', 'Granos y cereales'),
                                                          ('Bebidas', 'Bebidas y líquidos'),
                                                          ('Enlatados', 'Productos enlatados'),
                                                          ('Panaderia', 'Productos de panadería');

-- ==========================================
-- SECTION 2: DML (Data Manipulation Language)
-- ==========================================
-- DML: proveedores (50)
-- El proveedor con id = 33 lleva el NIT sintetico exigido por el enunciado
-- ------------------------------------------
INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) VALUES
                                                                                        ('Frutas del Valle', '900100001-1', 'Carlos Pérez', 'ventas@frutasdelvalle.com', '3001000001', 'Calle 10 #5-20, Cali'),
                                                                                        ('Agrícola La Esperanza', '900100002-2', 'María Gómez', 'contacto@laesperanza.com', '3001000002', 'Km 5 vía Tunja, Boyacá'),
                                                                                        ('Lácteos del Sur', '900100003-3', 'Andrés Rojas', 'info@lacteosdelsur.com', '3001000003', 'Carrera 20 #12-30, Pasto'),
                                                                                        ('Hortalizas Andinas', '900100004-4', 'Luisa Fernández', 'ventas@hortalizasandinas.com', '3001000004', 'Vereda El Salitre, Facatativá'),
                                                                                        ('Carnes Premium del Llano', '900100005-5', 'Jorge Ramírez', 'pedidos@carnesllano.com', '3001000005', 'Calle 30 #8-15, Villavicencio'),
                                                                                        ('Granos y Cereales Tolima', '900100006-6', 'Patricia Díaz', 'granos@cerealestolima.com', '3001000006', 'Carrera 5 #22-10, Ibagué'),
                                                                                        ('Bebidas Naturales Cafetal', '900100007-7', 'Camilo Torres', 'ventas@bebidascafetal.com', '3001000007', 'Calle 15 #10-40, Manizales'),
                                                                                        ('Panadería Industrial Pan Real', '900100008-8', 'Sandra Molina', 'pedidos@panreal.com', '3001000008', 'Carrera 45 #60-12, Medellín'),
                                                                                        ('Finca Los Cerezos', '900100009-9', 'Héctor Vargas', 'fincacerezos@correo.com', '3001000009', 'Vereda La Palma, Chía'),
                                                                                        ('Cooperativa Campesina Boyacá', '900100010-1', 'Rosa Cárdenas', 'coop@campesinaboyaca.com', '3001000010', 'Plaza Principal, Ramiriquí'),
                                                                                        ('Frutas Tropicales del Caribe', '900100011-2', 'Diego Herrera', 'ventas@frutascaribe.com', '3001000011', 'Calle 72 #43-20, Barranquilla'),
                                                                                        ('Distribuidora La Sabana', '900100012-3', 'Natalia Ortiz', 'info@dlasabana.com', '3001000012', 'Autopista Norte Km 12, Bogotá'),
                                                                                        ('Verduras Frescas Cundinamarca', '900100013-4', 'Felipe Castro', 'ventas@verdurascundi.com', '3001000013', 'Vereda San José, Funza'),
                                                                                        ('Lácteos San Isidro', '900100014-5', 'Marcela Ruiz', 'pedidos@lacteossanisidro.com', '3001000014', 'Carrera 7 #3-25, Zipaquirá'),
                                                                                        ('Embutidos Santander', '900100015-6', 'Ricardo Peña', 'ventas@embutidossantander.com', '3001000015', 'Calle 35 #20-18, Bucaramanga'),
                                                                                        ('Avícola El Dorado', '900100016-7', 'Claudia Soto', 'info@avicolaeldorado.com', '3001000016', 'Km 8 vía Girón, Santander'),
                                                                                        ('Arrocera Casanare', '900100017-8', 'Mauricio León', 'ventas@arroceracasanare.com', '3001000017', 'Carrera 14 #9-30, Yopal'),
                                                                                        ('Enlatados La Cosecha', '900100018-9', 'Paola Mejía', 'contacto@enlatadoslacosecha.com', '3001000018', 'Zona Industrial, Cartago'),
                                                                                        ('Jugos del Huila', '900100019-1', 'Sergio Navarro', 'ventas@jugoshuila.com', '3001000019', 'Calle 8 #6-45, Neiva'),
                                                                                        ('Panificadora Santa Rita', '900100020-2', 'Elena Acosta', 'pedidos@santarita.com', '3001000020', 'Carrera 27 #45-10, Bucaramanga'),
                                                                                        ('Cítricos del Quindío', '900100021-3', 'Julián Mora', 'ventas@citricosquindio.com', '3001000021', 'Vereda Barcelona, Calarcá'),
                                                                                        ('Papas de Nariño', '900100022-4', 'Gloria Benítez', 'info@papasnarino.com', '3001000022', 'Vereda Catambuco, Pasto'),
                                                                                        ('Quesos Campesinos Antioquia', '900100023-5', 'Óscar Zapata', 'quesos@campesinosantioquia.com', '3001000023', 'Calle 50 #30-22, Santa Rosa de Osos'),
                                                                                        ('Frigorífico del Norte', '900100024-6', 'Tatiana Linares', 'ventas@frigorificonorte.com', '3001000024', 'Zona Franca, Cúcuta'),
                                                                                        ('Granos Andinos', '900100025-7', 'Iván Salazar', 'info@granosandinos.com', '3001000025', 'Carrera 9 #14-35, Duitama'),
                                                                                        ('Aguas y Gaseosas del Café', '900100026-8', 'Liliana Cano', 'pedidos@aguascafe.com', '3001000026', 'Avenida 30 de Agosto, Pereira'),
                                                                                        ('Conservas Pacífico', '900100027-9', 'Hernán Quintero', 'ventas@conservaspacifico.com', '3001000027', 'Puerto Pesquero, Buenaventura'),
                                                                                        ('Horno de Leña', '900100028-1', 'Beatriz Arango', 'hornodelena@correo.com', '3001000028', 'Calle 12 #8-14, Villa de Leyva'),
                                                                                        ('Banano Urabá', '900100029-2', 'Gustavo Palacios', 'ventas@bananouraba.com', '3001000029', 'Zona Bananera, Apartadó'),
                                                                                        ('Tomates del Valle del Cauca', '900100030-3', 'Angélica Rey', 'info@tomatesvalle.com', '3001000030', 'Vía Cali-Palmira Km 6'),
                                                                                        ('Lácteos La Pradera', '900100031-4', 'Esteban Vega', 'pedidos@lacteospradera.com', '3001000031', 'Carrera 3 #7-50, Ubaté'),
                                                                                        ('Carnes Frías Medellín', '900100032-5', 'Daniela Restrepo', 'ventas@carnesfriasmed.com', '3001000032', 'Calle 10 Sur #50-33, Medellín'),
                                                                                        ('Orgánicos Sierra Nevada', '900427038-7', 'Valentina Cruz', 'info@organicossierra.com', '3001000033', 'Km 3 vía Minca, Santa Marta'),
                                                                                        ('Frijoles y Lentejas del Cauca', '900100034-7', 'Nelson Bravo', 'ventas@frijolescauca.com', '3001000034', 'Carrera 4 #5-20, Popayán'),
                                                                                        ('Refrescos Costa Azul', '900100035-8', 'Yesenia Ibarra', 'pedidos@refrescosazul.com', '3001000035', 'Calle 31 #12-08, Cartagena'),
                                                                                        ('Atún y Sardinas Tumaco', '900100036-9', 'Wilson Caicedo', 'ventas@atunsardinas.com', '3001000036', 'Muelle Pesquero, Tumaco'),
                                                                                        ('Pan Artesanal Bucaramanga', '900100037-1', 'Karen Duarte', 'panartesanal@correo.com', '3001000037', 'Carrera 33 #52-14, Bucaramanga'),
                                                                                        ('Mora y Fresa Pamplona', '900100038-2', 'Edwin Contreras', 'ventas@morafresa.com', '3001000038', 'Vereda Cuchilla, Pamplona'),
                                                                                        ('Zanahorias Ocaña', '900100039-3', 'Mónica Páez', 'info@zanahoriasocana.com', '3001000039', 'Calle 11 #9-17, Ocaña'),
                                                                                        ('Yogures del Campo', '900100040-4', 'Rodrigo Medina', 'pedidos@yogurescampo.com', '3001000040', 'Carrera 6 #2-30, Sogamoso'),
                                                                                        ('Pollo Fresco Santander', '900100041-5', 'Adriana Silva', 'ventas@pollofresco.com', '3001000041', 'Km 12 vía Lebrija, Santander'),
                                                                                        ('Maíz y Harinas Córdoba', '900100042-6', 'Fabián Marín', 'info@maizharinas.com', '3001000042', 'Calle 27 #4-12, Montería'),
                                                                                        ('Té y Aromáticas Andes', '900100043-7', 'Lorena Gil', 'ventas@teandes.com', '3001000043', 'Vereda El Edén, Jardín'),
                                                                                        ('Frutas en Conserva Tolima', '900100044-8', 'Cristian Parra', 'info@frutasconserva.com', '3001000044', 'Zona Industrial, Espinal'),
                                                                                        ('Galletas y Bizcochos Valle', '900100045-9', 'Juliana Rincón', 'pedidos@galletasvalle.com', '3001000045', 'Calle 5 #38-20, Cali'),
                                                                                        ('Piñas Lebrija', '900100046-1', 'Álvaro Mantilla', 'ventas@pinaslebrija.com', '3001000046', 'Vereda La Esperanza, Lebrija'),
                                                                                        ('Cebolla Aquitania', '900100047-2', 'Teresa Pineda', 'info@cebollaaquitania.com', '3001000047', 'Lago de Tota, Aquitania'),
                                                                                        ('Mantequillas Sabana', '900100048-3', 'Bernardo Cuéllar', 'pedidos@mantequillassabana.com', '3001000048', 'Carrera 10 #15-40, Chiquinquirá'),
                                                                                        ('Cerdo Selecto Eje Cafetero', '900100049-4', 'Alejandra Giraldo', 'ventas@cerdoselecto.com', '3001000049', 'Km 4 vía Armenia, Quindío'),
                                                                                        ('Cereales Integrales Huila', '900100050-5', 'Santiago Ledesma', 'info@cerealeshuila.com', '3001000050', 'Calle 9 #11-25, Pitalito');


-- ------------------------------------------
-- DML: productos (50)
-- Categorias: 1 Frutas, 2 Verduras, 3 Lacteos, 4 Carnes,
--             5 Granos, 6 Bebidas, 7 Enlatados, 8 Panaderia
-- ------------------------------------------
INSERT INTO productos (nombre, descripcion, id_categoria, unidad_medida, precio_base) VALUES
                                                                                          ('Manzana roja', 'Manzana roja importada', 1, 'kg', 6500.00),
                                                                                          ('Banano', 'Banano criollo maduro', 1, 'kg', 3200.00),
                                                                                          ('Naranja', 'Naranja para jugo', 1, 'kg', 2800.00),
                                                                                          ('Mango', 'Mango Tommy', 1, 'kg', 4500.00),
                                                                                          ('Fresa', 'Fresa fresca de Pamplona', 1, 'kg', 9000.00),
                                                                                          ('Piña', 'Piña oro miel', 1, 'unidad', 5500.00),
                                                                                          ('Mora', 'Mora de Castilla', 1, 'kg', 8000.00),
                                                                                          ('Limón', 'Limón Tahití', 1, 'kg', 3500.00),
                                                                                          ('Uchuva', 'Uchuva con capuchón', 1, 'kg', 12000.00),
                                                                                          ('Papaya', 'Papaya maradol', 1, 'kg', 3800.00),
                                                                                          ('Tomate chonto', 'Tomate chonto maduro', 2, 'kg', 3500.00),
                                                                                          ('Cebolla cabezona', 'Cebolla cabezona blanca', 2, 'kg', 2800.00),
                                                                                          ('Zanahoria', 'Zanahoria lavada', 2, 'kg', 2500.00),
                                                                                          ('Papa pastusa', 'Papa pastusa lavada', 2, 'kg', 2200.00),
                                                                                          ('Lechuga crespa', 'Lechuga crespa fresca', 2, 'unidad', 2500.00),
                                                                                          ('Pimentón', 'Pimentón rojo y verde', 2, 'kg', 5000.00),
                                                                                          ('Cilantro', 'Cilantro en manojo', 2, 'manojo', 1500.00),
                                                                                          ('Habichuela', 'Habichuela fresca', 2, 'kg', 4500.00),
                                                                                          ('Brócoli', 'Brócoli fresco', 2, 'kg', 6000.00),
                                                                                          ('Ajo', 'Ajo cabeza nacional', 2, 'kg', 14000.00),
                                                                                          ('Leche entera', 'Leche entera pasteurizada', 3, 'litro', 3800.00),
                                                                                          ('Queso campesino', 'Queso campesino fresco', 3, 'kg', 18000.00),
                                                                                          ('Yogur natural', 'Yogur natural sin azúcar', 3, 'litro', 7500.00),
                                                                                          ('Mantequilla', 'Mantequilla con sal', 3, 'kg', 16000.00),
                                                                                          ('Queso mozzarella', 'Queso mozzarella para pizza', 3, 'kg', 22000.00),
                                                                                          ('Crema de leche', 'Crema de leche pasteurizada', 3, 'litro', 9500.00),
                                                                                          ('Kumis', 'Kumis tradicional', 3, 'litro', 6000.00),
                                                                                          ('Cuajada', 'Cuajada fresca', 3, 'kg', 15000.00),
                                                                                          ('Pechuga de pollo', 'Pechuga sin hueso', 4, 'kg', 14000.00),
                                                                                          ('Carne molida de res', 'Carne molida especial', 4, 'kg', 22000.00),
                                                                                          ('Lomo de cerdo', 'Lomo de cerdo fresco', 4, 'kg', 19000.00),
                                                                                          ('Chorizo', 'Chorizo santarrosano', 4, 'kg', 18000.00),
                                                                                          ('Costilla de res', 'Costilla de res', 4, 'kg', 17000.00),
                                                                                          ('Pollo entero', 'Pollo entero fresco', 4, 'kg', 9500.00),
                                                                                          ('Jamón', 'Jamón de cerdo', 4, 'kg', 20000.00),
                                                                                          ('Salchicha', 'Salchicha tipo Viena', 4, 'kg', 12000.00),
                                                                                          ('Arroz blanco', 'Arroz blanco premium', 5, 'kg', 3800.00),
                                                                                          ('Frijol cargamanto', 'Frijol cargamanto rojo', 5, 'kg', 9000.00),
                                                                                          ('Lenteja', 'Lenteja seleccionada', 5, 'kg', 5500.00),
                                                                                          ('Maíz', 'Maíz amarillo seco', 5, 'kg', 3000.00),
                                                                                          ('Harina de trigo', 'Harina de trigo todo uso', 5, 'kg', 3200.00),
                                                                                          ('Jugo de naranja', 'Jugo de naranja natural', 6, 'litro', 5000.00),
                                                                                          ('Agua en botella', 'Agua sin gas 600 ml', 6, 'unidad', 1800.00),
                                                                                          ('Gaseosa 1.5 L', 'Gaseosa sabor cola', 6, 'unidad', 4200.00),
                                                                                          ('Té frío', 'Té frío de limón', 6, 'litro', 4800.00),
                                                                                          ('Atún en lata', 'Atún en aceite 160 g', 7, 'unidad', 5800.00),
                                                                                          ('Sardinas en lata', 'Sardinas en salsa de tomate', 7, 'unidad', 4500.00),
                                                                                          ('Maíz tierno en lata', 'Maíz tierno 300 g', 7, 'unidad', 4200.00),
                                                                                          ('Pan tajado', 'Pan tajado integral', 8, 'unidad', 5500.00),
                                                                                          ('Croissant', 'Croissant de mantequilla', 8, 'unidad', 2500.00);

-- ------------------------------------------
-- DML: productos_proveedor (catalogo de cada proveedor)
-- ------------------------------------------
INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES
                                                                (1,1),(29,2),(21,3),(11,4),(38,5),(46,6),(38,7),(21,8),(10,9),(1,10),
                                                                (30,11),(47,12),(39,13),(22,14),(13,15),(4,16),(13,17),(2,18),(9,19),(12,20),
                                                                (3,21),(23,22),(40,23),(48,24),(14,25),(31,26),(14,27),(23,28),
                                                                (16,29),(5,30),(49,31),(15,32),(24,33),(41,34),(32,35),(15,36),
                                                                (17,37),(34,38),(6,39),(25,40),(42,41),(19,42),(26,43),(35,44),(43,45),
                                                                (36,46),(27,47),(18,48),(37,49),(8,50),
                                                                (11,2),(11,10),(2,1),(2,14),(10,12),(10,13),(12,14),(12,11),(4,11),(4,17),
                                                                (13,11),(9,15),(31,21),(14,21),(23,21),(48,23),(40,21),(5,29),(24,30),(24,29),
                                                                (16,34),(41,29),(17,39),(6,37),(34,39),(25,37),(7,42),(7,45),(26,42),(18,46),
                                                                (27,46),(20,49),(20,50),(37,50),(28,49),(44,48),(44,4);

-- ------------------------------------------
-- DML: clientes (50)  tipo 1 = Mayorista, 2 = Minorista
-- ------------------------------------------
INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) VALUES
                                                                                  ('Supermercados La Canasta', '800200001-1', 'compras@lacanasta.com', '6011000001', 'Av. Boyacá #80-15, Bogotá', 1),
                                                                                  ('Restaurante El Fogón Criollo', '800200002-2', 'admin@fogoncriollo.com', '6011000002', 'Calle 85 #14-20, Bogotá', 1),
                                                                                  ('Hotel Plaza Real', '800200003-3', 'compras@hotelplazareal.com', '6011000003', 'Carrera 7 #32-10, Bogotá', 1),
                                                                                  ('Almacenes Éxito Express Norte', '800200004-4', 'abastecimiento@exitonorte.com', '6011000004', 'Autopista Norte #120-40, Bogotá', 1),
                                                                                  ('Cafetería Universitaria UNAL', '800200005-5', 'cafeteria@unal.edu.co', '6011000005', 'Ciudad Universitaria, Bogotá', 1),
                                                                                  ('Restaurante Sabor Andino', '800200006-6', 'pedidos@saborandino.com', '6011000006', 'Calle 63 #10-25, Bogotá', 1),
                                                                                  ('Clínica Santa Fe Alimentación', '800200007-7', 'nutricion@clinicasantafe.com', '6011000007', 'Calle 119 #7-75, Bogotá', 1),
                                                                                  ('Panadería y Pastelería Delicias', '800200008-8', 'compras@deliciaspan.com', '6011000008', 'Carrera 15 #45-30, Bogotá', 1),
                                                                                  ('Catering Eventos Premium', '800200009-9', 'logistica@cateringpremium.com', '6011000009', 'Calle 100 #19-50, Bogotá', 1),
                                                                                  ('Supermercado Mercatodo', '800200010-1', 'compras@mercatodo.com', '6011000010', 'Calle 53 #25-12, Bogotá', 1),
                                                                                  ('Restaurante La Brasa Roja', '800200011-2', 'gerencia@brasaroja.com', '6011000011', 'Carrera 11 #93-20, Bogotá', 1),
                                                                                  ('Casino Industrial Metalúrgica', '800200012-3', 'casino@metalurgica.com', '6011000012', 'Zona Industrial Puente Aranda, Bogotá', 1),
                                                                                  ('Fruver Don Pepe', '800200013-4', 'donpepe@fruver.com', '6011000013', 'Plaza Paloquemao, Bogotá', 1),
                                                                                  ('Colegio Gimnasio Moderno', '800200014-5', 'servicios@gimnasiomoderno.edu.co', '6011000014', 'Carrera 9 #74-99, Bogotá', 1),
                                                                                  ('Hospital San Ignacio Cocina', '800200015-6', 'cocina@husi.org', '6011000015', 'Carrera 7 #40-62, Bogotá', 1),
                                                                                  ('Tiendas D1 Zona Sur', '800200016-7', 'abastecimiento@d1sur.com', '6011000016', 'Autopista Sur #38-10, Bogotá', 1),
                                                                                  ('Restaurante Mar y Tierra', '800200017-8', 'compras@marytierra.com', '6011000017', 'Calle 70 #5-30, Bogotá', 1),
                                                                                  ('Hotel Casa Boutique', '800200018-9', 'admin@casaboutique.com', '6011000018', 'Calle 12 #3-45, Bogotá', 1),
                                                                                  ('Distribuidora Minimercados Unidos', '800200019-1', 'pedidos@minimercadosunidos.com', '6011000019', 'Calle 13 #68-20, Bogotá', 1),
                                                                                  ('Pizzería Napoli', '800200020-2', 'compras@pizzerianapoli.com', '6011000020', 'Carrera 13 #85-10, Bogotá', 1),
                                                                                  ('Cadena Asadero Pollo Dorado', '800200021-3', 'compras@pollodorado.com', '6011000021', 'Avenida 68 #24-30, Bogotá', 1),
                                                                                  ('Restaurante Comida Típica Doña Rosa', '800200022-4', 'donarosa@correo.com', '6011000022', 'Calle 24 #19-12, Bogotá', 1),
                                                                                  ('Heladería y Postres Dulce Luna', '800200023-5', 'compras@dulceluna.com', '6011000023', 'Carrera 5 #61-18, Bogotá', 1),
                                                                                  ('Cooperativa de Consumo Sabana', '800200024-6', 'coop@consumosabana.com', '6011000024', 'Calle 80 #90-14, Bogotá', 1),
                                                                                  ('Restaurante Vegetariano Raíces', '800200025-7', 'raices@correo.com', '6011000025', 'Calle 66 #7-22, Bogotá', 1),
                                                                                  ('Tienda de Barrio La Esquina', '1010100001', 'laesquina@correo.com', '3101000001', 'Calle 45 #12-10, Bogotá', 2),
                                                                                  ('Mini Market Don Julio', '1010100002', 'donjulio@correo.com', '3101000002', 'Carrera 30 #20-15, Bogotá', 2),
                                                                                  ('Carmen Rosa Pineda', '1010100003', 'carmenpineda@correo.com', '3101000003', 'Calle 50 #8-33, Bogotá', 2),
                                                                                  ('Tienda La Bendición', '1010100004', 'labendicion@correo.com', '3101000004', 'Carrera 68 #40-20, Bogotá', 2),
                                                                                  ('Luis Alberto Murcia', '1010100005', 'lamurcia@correo.com', '3101000005', 'Calle 72 #22-14, Bogotá', 2),
                                                                                  ('Ana María Cifuentes', '1010100006', 'amcifuentes@correo.com', '3101000006', 'Carrera 24 #55-08, Bogotá', 2),
                                                                                  ('Tienda El Vecino', '1010100007', 'elvecino@correo.com', '3101000007', 'Calle 36 #14-25, Bogotá', 2),
                                                                                  ('Jorge Enrique Castaño', '1010100008', 'jecastano@correo.com', '3101000008', 'Carrera 50 #12-30, Bogotá', 2),
                                                                                  ('Miscelánea Los Ángeles', '1010100009', 'losangeles@correo.com', '3101000009', 'Calle 19 #4-20, Bogotá', 2),
                                                                                  ('Paola Andrea Ríos', '1010100010', 'paolarios@correo.com', '3101000010', 'Carrera 9 #18-44, Bogotá', 2),
                                                                                  ('Tienda Mi Casita', '1010100011', 'micasita@correo.com', '3101000011', 'Calle 100 #15-12, Bogotá', 2),
                                                                                  ('Héctor Fabio Duque', '1010100012', 'hfduque@correo.com', '3101000012', 'Carrera 45 #26-30, Bogotá', 2),
                                                                                  ('Mini Mercado Santa Lucía', '1010100013', 'santalucia@correo.com', '3101000013', 'Calle 7 #32-18, Bogotá', 2),
                                                                                  ('Sandra Milena Garzón', '1010100014', 'smgarzon@correo.com', '3101000014', 'Carrera 17 #63-22, Bogotá', 2),
                                                                                  ('Tienda Económica El Ahorro', '1010100015', 'elahorro@correo.com', '3101000015', 'Calle 134 #52-10, Bogotá', 2),
                                                                                  ('Fernando Augusto Lara', '1010100016', 'falara@correo.com', '3101000016', 'Carrera 80 #5-45, Bogotá', 2),
                                                                                  ('Tienda La Fortuna', '1010100017', 'lafortuna@correo.com', '3101000017', 'Calle 22 #28-14, Bogotá', 2),
                                                                                  ('Diana Carolina Beltrán', '1010100018', 'dcbeltran@correo.com', '3101000018', 'Carrera 14 #70-08, Bogotá', 2),
                                                                                  ('Minimercado Sol y Luna', '1010100019', 'solyluna@correo.com', '3101000019', 'Calle 57 #9-40, Bogotá', 2),
                                                                                  ('Ricardo Alonso Peña', '1010100020', 'rapena@correo.com', '3101000020', 'Carrera 22 #44-12, Bogotá', 2),
                                                                                  ('Tienda Doña Marta', '1010100021', 'donamarta@correo.com', '3101000021', 'Calle 40 #16-25, Bogotá', 2),
                                                                                  ('Yolanda Patricia Soler', '1010100022', 'ypsoler@correo.com', '3101000022', 'Carrera 33 #8-17, Bogotá', 2),
                                                                                  ('Autoservicio El Progreso', '1010100023', 'elprogreso@correo.com', '3101000023', 'Calle 26 #50-30, Bogotá', 2),
                                                                                  ('Mauricio Andrés Villa', '1010100024', 'mavilla@correo.com', '3101000024', 'Carrera 7 #112-20, Bogotá', 2),
                                                                                  ('Tienda Buen Precio', '1010100025', 'buenprecio@correo.com', '3101000025', 'Calle 68 #30-15, Bogotá', 2);

-- ------------------------------------------
-- DML: lotes_inventario (55)
-- Estados: 1 Disponible, 2 Proximo a Vencer, 3 Vencido, 4 En Cuarentena
-- Referencia de "hoy": 2026-11-01
-- Lotes 51-55: segundo lote de un producto, para probar FEFO
-- ------------------------------------------
INSERT INTO lotes_inventario
(numero_lote, id_producto, id_proveedor, fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, precio_compra, precio_venta, pasillo, estante, id_estado) VALUES
                                                                                                                                                                     ('L-20261025-0001', 1, 1,  '2026-10-25 08:00:00', '2026-11-20', 200, 200, 4800, 6500, 'A', '01', 1),
                                                                                                                                                                     ('L-20261028-0002', 2, 29, '2026-10-28 08:00:00', '2026-11-06', 300, 300, 2300, 3200, 'A', '02', 2),
                                                                                                                                                                     ('L-20261020-0003', 3, 21, '2026-10-20 08:00:00', '2026-11-15', 250, 250, 1900, 2800, 'A', '03', 1),
                                                                                                                                                                     ('L-20261026-0004', 4, 11, '2026-10-26 08:00:00', '2026-11-05', 180, 180, 3200, 4500, 'A', '04', 2),
                                                                                                                                                                     ('L-20261027-0005', 5, 38, '2026-10-27 08:00:00', '2026-11-04', 80,  80,  6500, 9000, 'A', '05', 2),
                                                                                                                                                                     ('L-20261018-0006', 6, 46, '2026-10-18 08:00:00', '2026-11-12', 120, 120, 3800, 5500, 'A', '06', 1),
                                                                                                                                                                     ('L-20261014-0007', 7, 38, '2026-10-14 08:00:00', '2026-10-30', 60,  60,  5800, 8000, 'A', '07', 3),
                                                                                                                                                                     ('L-20261022-0008', 8, 21, '2026-10-22 08:00:00', '2026-11-18', 200, 200, 2400, 3500, 'A', '08', 1),
                                                                                                                                                                     ('L-20261029-0009', 9, 10, '2026-10-29 08:00:00', '2026-11-10', 50,  50,  8500, 12000, 'A', '09', 1),
                                                                                                                                                                     ('L-20261024-0010', 10, 1, '2026-10-24 08:00:00', '2026-11-07', 150, 150, 2600, 3800, 'A', '10', 2),
                                                                                                                                                                     ('L-20261027-0011', 11, 30, '2026-10-27 08:00:00', '2026-11-09', 220, 220, 2400, 3500, 'B', '01', 1),
                                                                                                                                                                     ('L-20261015-0012', 12, 47, '2026-10-15 08:00:00', '2026-11-25', 400, 400, 1900, 2800, 'B', '02', 1),
                                                                                                                                                                     ('L-20261020-0013', 13, 39, '2026-10-20 08:00:00', '2026-11-14', 300, 300, 1700, 2500, 'B', '03', 1),
                                                                                                                                                                     ('L-20261010-0014', 14, 22, '2026-10-10 08:00:00', '2026-11-30', 600, 600, 1500, 2200, 'B', '04', 1),
                                                                                                                                                                     ('L-20261029-0015', 15, 13, '2026-10-29 08:00:00', '2026-11-05', 100, 100, 1700, 2500, 'B', '05', 2),
                                                                                                                                                                     ('L-20261023-0016', 16, 4, '2026-10-23 08:00:00', '2026-11-13', 90,  90,  3500, 5000, 'B', '06', 1),
                                                                                                                                                                     ('L-20261026-0017', 17, 13, '2026-10-26 08:00:00', '2026-10-31', 150, 150, 900,  1500, 'B', '07', 3),
                                                                                                                                                                     ('L-20261025-0018', 18, 2, '2026-10-25 08:00:00', '2026-11-08', 70,  70,  3100, 4500, 'B', '08', 2),
                                                                                                                                                                     ('L-20261027-0019', 19, 9, '2026-10-27 08:00:00', '2026-11-11', 60,  60,  4200, 6000, 'B', '09', 4),
                                                                                                                                                                     ('L-20261005-0020', 20, 12, '2026-10-05 08:00:00', '2026-12-30', 100, 100, 10000, 14000, 'B', '10', 1),
                                                                                                                                                                     ('L-20261028-0021', 21, 3, '2026-10-28 08:00:00', '2026-11-07', 500, 500, 2700, 3800, 'C', '01', 2),
                                                                                                                                                                     ('L-20261024-0022', 22, 23, '2026-10-24 08:00:00', '2026-11-16', 90,  90,  12500, 18000, 'C', '02', 1),
                                                                                                                                                                     ('L-20261026-0023', 23, 40, '2026-10-26 08:00:00', '2026-11-12', 200, 200, 5200, 7500, 'C', '03', 1),
                                                                                                                                                                     ('L-20261012-0024', 24, 48, '2026-10-12 08:00:00', '2026-12-10', 60,  60,  11000, 16000, 'C', '04', 1),
                                                                                                                                                                     ('L-20261021-0025', 25, 14, '2026-10-21 08:00:00', '2026-11-20', 80,  80,  15500, 22000, 'C', '05', 1),
                                                                                                                                                                     ('L-20261025-0026', 26, 31, '2026-10-25 08:00:00', '2026-11-03', 100, 100, 6700, 9500, 'C', '06', 2),
                                                                                                                                                                     ('L-20261022-0027', 27, 14, '2026-10-22 08:00:00', '2026-10-29', 120, 120, 4200, 6000, 'C', '07', 3),
                                                                                                                                                                     ('L-20261028-0028', 28, 23, '2026-10-28 08:00:00', '2026-11-09', 70,  70,  10500, 15000, 'C', '08', 4),
                                                                                                                                                                     ('L-20261029-0029', 29, 16, '2026-10-29 08:00:00', '2026-11-08', 150, 150, 9800, 14000, 'D', '01', 2),
                                                                                                                                                                     ('L-20261027-0030', 30, 5, '2026-10-27 08:00:00', '2026-11-10', 120, 120, 15500, 22000, 'D', '02', 1),
                                                                                                                                                                     ('L-20261026-0031', 31, 49, '2026-10-26 08:00:00', '2026-11-12', 100, 100, 13000, 19000, 'D', '03', 1),
                                                                                                                                                                     ('L-20261020-0032', 32, 15, '2026-10-20 08:00:00', '2026-11-25', 80,  80,  12500, 18000, 'D', '04', 1),
                                                                                                                                                                     ('L-20261024-0033', 33, 24, '2026-10-24 08:00:00', '2026-11-06', 90,  90,  11800, 17000, 'D', '05', 2),
                                                                                                                                                                     ('L-20261030-0034', 34, 41, '2026-10-30 08:00:00', '2026-11-10', 200, 200, 6600, 9500, 'D', '06', 1),
                                                                                                                                                                     ('L-20261015-0035', 35, 32, '2026-10-15 08:00:00', '2026-11-28', 60,  60,  14000, 20000, 'D', '07', 1),
                                                                                                                                                                     ('L-20261010-0036', 36, 15, '2026-10-10 08:00:00', '2026-10-28', 70,  70,  8400, 12000, 'D', '08', 3),
                                                                                                                                                                     ('L-20260915-0037', 37, 17, '2026-09-15 08:00:00', '2027-03-15', 1000, 1000, 2700, 3800, 'E', '01', 1),
                                                                                                                                                                     ('L-20260920-0038', 38, 34, '2026-09-20 08:00:00', '2027-02-20', 500, 500, 6300, 9000, 'E', '02', 1),
                                                                                                                                                                     ('L-20260925-0039', 39, 6, '2026-09-25 08:00:00', '2027-04-10', 400, 400, 3800, 5500, 'E', '03', 1),
                                                                                                                                                                     ('L-20261001-0040', 40, 25, '2026-10-01 08:00:00', '2027-01-30', 600, 600, 2100, 3000, 'E', '04', 1),
                                                                                                                                                                     ('L-20261005-0041', 41, 42, '2026-10-05 08:00:00', '2027-02-28', 700, 700, 2200, 3200, 'E', '05', 1),
                                                                                                                                                                     ('L-20261025-0042', 42, 19, '2026-10-25 08:00:00', '2026-11-08', 300, 300, 3500, 5000, 'F', '01', 2),
                                                                                                                                                                     ('L-20260930-0043', 43, 26, '2026-09-30 08:00:00', '2027-06-30', 800, 800, 1200, 1800, 'F', '02', 1),
                                                                                                                                                                     ('L-20261002-0044', 44, 35, '2026-10-02 08:00:00', '2027-03-31', 400, 400, 2900, 4200, 'F', '03', 1),
                                                                                                                                                                     ('L-20261020-0045', 45, 43, '2026-10-20 08:00:00', '2026-11-30', 250, 250, 3300, 4800, 'F', '04', 1),
                                                                                                                                                                     ('L-20260910-0046', 46, 36, '2026-09-10 08:00:00', '2028-01-15', 600, 600, 4000, 5800, 'G', '01', 1),
                                                                                                                                                                     ('L-20260912-0047', 47, 27, '2026-09-12 08:00:00', '2027-12-20', 500, 500, 3100, 4500, 'G', '02', 1),
                                                                                                                                                                     ('L-20260918-0048', 48, 18, '2026-09-18 08:00:00', '2027-11-30', 450, 450, 2900, 4200, 'G', '03', 1),
                                                                                                                                                                     ('L-20261029-0049', 49, 37, '2026-10-29 08:00:00', '2026-11-06', 200, 200, 3800, 5500, 'H', '01', 2),
                                                                                                                                                                     ('L-20261027-0050', 50, 8, '2026-10-27 08:00:00', '2026-10-31', 150, 150, 1700, 2500, 'H', '02', 3),
                                                                                                                                                                     ('L-20261030-0051', 11, 30, '2026-10-30 08:00:00', '2026-11-14', 180, 180, 2400, 3500, 'B', '11', 1),
                                                                                                                                                                     ('L-20261030-0052', 21, 3, '2026-10-30 08:00:00', '2026-11-12', 400, 400, 2700, 3800, 'C', '09', 1),
                                                                                                                                                                     ('L-20261030-0053', 1, 1, '2026-10-30 08:00:00', '2026-12-05', 150, 150, 4800, 6500, 'A', '11', 1),
                                                                                                                                                                     ('L-20261030-0054', 29, 16, '2026-10-30 08:00:00', '2026-11-15', 100, 100, 9800, 14000, 'D', '09', 1),
                                                                                                                                                                     ('L-20261020-0055', 37, 17, '2026-10-20 08:00:00', '2027-05-20', 800, 800, 2700, 3800, 'E', '06', 1);

-- ------------------------------------------
-- DML: ventas (50)  totales se calculan despues desde los detalles
-- Estados: 1 Registrada, 2 En Preparacion, 3 En Transito, 4 Entregada, 5 Cancelada
-- ------------------------------------------
INSERT INTO ventas (numero_venta, id_cliente, fecha_venta, subtotal, total_descuento, total_impuesto, monto_total, id_estado, usuario_registro) VALUES
                                                                                                                                                    ('V-2026-0001', 1,  '2026-10-30 09:10:00', 0, 0, 0, 0, 4, 'vendedor1'),
                                                                                                                                                    ('V-2026-0002', 2,  '2026-10-30 09:40:00', 0, 0, 0, 0, 4, 'vendedor2'),
                                                                                                                                                    ('V-2026-0003', 3,  '2026-10-30 10:05:00', 0, 0, 0, 0, 4, 'vendedor1'),
                                                                                                                                                    ('V-2026-0004', 4,  '2026-10-30 10:30:00', 0, 0, 0, 0, 4, 'vendedor3'),
                                                                                                                                                    ('V-2026-0005', 5,  '2026-10-30 11:00:00', 0, 0, 0, 0, 4, 'vendedor2'),
                                                                                                                                                    ('V-2026-0006', 6,  '2026-10-30 11:25:00', 0, 0, 0, 0, 4, 'vendedor1'),
                                                                                                                                                    ('V-2026-0007', 7,  '2026-10-30 13:00:00', 0, 0, 0, 0, 4, 'vendedor3'),
                                                                                                                                                    ('V-2026-0008', 8,  '2026-10-30 13:30:00', 0, 0, 0, 0, 4, 'vendedor2'),
                                                                                                                                                    ('V-2026-0009', 9,  '2026-10-30 14:00:00', 0, 0, 0, 0, 4, 'vendedor1'),
                                                                                                                                                    ('V-2026-0010', 10, '2026-10-30 14:40:00', 0, 0, 0, 0, 4, 'vendedor3'),
                                                                                                                                                    ('V-2026-0011', 11, '2026-10-30 15:10:00', 0, 0, 0, 0, 4, 'vendedor2'),
                                                                                                                                                    ('V-2026-0012', 12, '2026-10-30 15:45:00', 0, 0, 0, 0, 4, 'vendedor1'),
                                                                                                                                                    ('V-2026-0013', 13, '2026-10-31 08:15:00', 0, 0, 0, 0, 3, 'vendedor2'),
                                                                                                                                                    ('V-2026-0014', 14, '2026-10-31 08:40:00', 0, 0, 0, 0, 3, 'vendedor3'),
                                                                                                                                                    ('V-2026-0015', 15, '2026-10-31 09:05:00', 0, 0, 0, 0, 3, 'vendedor1'),
                                                                                                                                                    ('V-2026-0016', 16, '2026-10-31 09:30:00', 0, 0, 0, 0, 3, 'vendedor2'),
                                                                                                                                                    ('V-2026-0017', 17, '2026-10-31 10:00:00', 0, 0, 0, 0, 3, 'vendedor3'),
                                                                                                                                                    ('V-2026-0018', 18, '2026-10-31 10:25:00', 0, 0, 0, 0, 3, 'vendedor1'),
                                                                                                                                                    ('V-2026-0019', 19, '2026-10-31 10:50:00', 0, 0, 0, 0, 3, 'vendedor2'),
                                                                                                                                                    ('V-2026-0020', 20, '2026-10-31 11:15:00', 0, 0, 0, 0, 3, 'vendedor3'),
                                                                                                                                                    ('V-2026-0021', 21, '2026-10-31 11:40:00', 0, 0, 0, 0, 2, 'vendedor1'),
                                                                                                                                                    ('V-2026-0022', 22, '2026-10-31 12:10:00', 0, 0, 0, 0, 2, 'vendedor2'),
                                                                                                                                                    ('V-2026-0023', 23, '2026-10-31 13:00:00', 0, 0, 0, 0, 2, 'vendedor3'),
                                                                                                                                                    ('V-2026-0024', 24, '2026-10-31 13:30:00', 0, 0, 0, 0, 2, 'vendedor1'),
                                                                                                                                                    ('V-2026-0025', 25, '2026-10-31 14:00:00', 0, 0, 0, 0, 2, 'vendedor2'),
                                                                                                                                                    ('V-2026-0026', 26, '2026-10-31 14:20:00', 0, 0, 0, 0, 2, 'vendedor3'),
                                                                                                                                                    ('V-2026-0027', 27, '2026-10-31 14:45:00', 0, 0, 0, 0, 2, 'vendedor1'),
                                                                                                                                                    ('V-2026-0028', 28, '2026-10-31 15:10:00', 0, 0, 0, 0, 2, 'vendedor2'),
                                                                                                                                                    ('V-2026-0029', 29, '2026-10-31 15:35:00', 0, 0, 0, 0, 2, 'vendedor3'),
                                                                                                                                                    ('V-2026-0030', 30, '2026-10-31 16:00:00', 0, 0, 0, 0, 2, 'vendedor1'),
                                                                                                                                                    ('V-2026-0031', 31, '2026-10-31 16:20:00', 0, 0, 0, 0, 2, 'vendedor2'),
                                                                                                                                                    ('V-2026-0032', 32, '2026-10-31 16:40:00', 0, 0, 0, 0, 2, 'vendedor3'),
                                                                                                                                                    ('V-2026-0033', 33, '2026-10-31 17:00:00', 0, 0, 0, 0, 2, 'vendedor1'),
                                                                                                                                                    ('V-2026-0034', 34, '2026-10-31 17:20:00', 0, 0, 0, 0, 2, 'vendedor2'),
                                                                                                                                                    ('V-2026-0035', 35, '2026-10-31 17:40:00', 0, 0, 0, 0, 2, 'vendedor3'),
                                                                                                                                                    ('V-2026-0036', 36, '2026-11-01 08:00:00', 0, 0, 0, 0, 1, 'vendedor1'),
                                                                                                                                                    ('V-2026-0037', 37, '2026-11-01 08:20:00', 0, 0, 0, 0, 1, 'vendedor2'),
                                                                                                                                                    ('V-2026-0038', 38, '2026-11-01 08:40:00', 0, 0, 0, 0, 1, 'vendedor3'),
                                                                                                                                                    ('V-2026-0039', 39, '2026-11-01 09:00:00', 0, 0, 0, 0, 1, 'vendedor1'),
                                                                                                                                                    ('V-2026-0040', 40, '2026-11-01 09:20:00', 0, 0, 0, 0, 1, 'vendedor2'),
                                                                                                                                                    ('V-2026-0041', 41, '2026-11-01 09:40:00', 0, 0, 0, 0, 1, 'vendedor3'),
                                                                                                                                                    ('V-2026-0042', 42, '2026-11-01 10:00:00', 0, 0, 0, 0, 1, 'vendedor1'),
                                                                                                                                                    ('V-2026-0043', 43, '2026-11-01 10:20:00', 0, 0, 0, 0, 1, 'vendedor2'),
                                                                                                                                                    ('V-2026-0044', 44, '2026-11-01 10:40:00', 0, 0, 0, 0, 1, 'vendedor3'),
                                                                                                                                                    ('V-2026-0045', 45, '2026-11-01 11:00:00', 0, 0, 0, 0, 1, 'vendedor1'),
                                                                                                                                                    ('V-2026-0046', 46, '2026-11-01 11:20:00', 0, 0, 0, 0, 1, 'vendedor2'),
                                                                                                                                                    ('V-2026-0047', 47, '2026-11-01 11:40:00', 0, 0, 0, 0, 1, 'vendedor3'),
                                                                                                                                                    ('V-2026-0048', 48, '2026-11-01 12:00:00', 0, 0, 0, 0, 5, 'vendedor1'),
                                                                                                                                                    ('V-2026-0049', 49, '2026-11-01 12:20:00', 0, 0, 0, 0, 5, 'vendedor2'),
                                                                                                                                                    ('V-2026-0050', 50, '2026-11-01 12:40:00', 0, 0, 0, 0, 5, 'vendedor3');

-- ------------------------------------------
-- DML: detalles_venta (60 lineas)
-- Cada fila: id_venta, id_lote, cantidad. Precio y descuento salen del lote:
-- lote Proximo a Vencer (estado 2) => 50% de descuento. IVA 19%.
-- ------------------------------------------
INSERT INTO detalles_venta
(id_venta, id_lote, cantidad_vendida, precio_unitario, descuento_aplicado, monto_descuento,
 tasa_impuesto, monto_impuesto, subtotal, total_linea)
SELECT t.id_venta, t.id_lote, t.qty, l.precio_venta,
       IF(l.id_estado = 2, 0.50, 0.00),
       ROUND(t.qty * l.precio_venta * IF(l.id_estado = 2, 0.50, 0), 2),
       0.19,
       ROUND(t.qty * l.precio_venta * (1 - IF(l.id_estado = 2, 0.50, 0)) * 0.19, 2),
       ROUND(t.qty * l.precio_venta, 2),
       ROUND(t.qty * l.precio_venta * (1 - IF(l.id_estado = 2, 0.50, 0)) * 1.19, 2)
FROM (
         SELECT 1 AS id_venta, 1 AS id_lote, 20 AS qty
         UNION ALL SELECT 2,2,30   UNION ALL SELECT 3,3,25   UNION ALL SELECT 4,4,15
         UNION ALL SELECT 5,5,10   UNION ALL SELECT 6,6,12   UNION ALL SELECT 7,8,20
         UNION ALL SELECT 8,9,5    UNION ALL SELECT 9,10,15  UNION ALL SELECT 10,11,22
         UNION ALL SELECT 11,12,40 UNION ALL SELECT 12,13,30 UNION ALL SELECT 13,14,60
         UNION ALL SELECT 14,15,10 UNION ALL SELECT 15,16,9  UNION ALL SELECT 16,18,7
         UNION ALL SELECT 17,20,10 UNION ALL SELECT 18,21,50 UNION ALL SELECT 19,22,9
         UNION ALL SELECT 20,23,20 UNION ALL SELECT 21,24,6  UNION ALL SELECT 22,25,8
         UNION ALL SELECT 23,26,10 UNION ALL SELECT 24,29,15 UNION ALL SELECT 25,30,12
         UNION ALL SELECT 26,31,10 UNION ALL SELECT 27,32,8  UNION ALL SELECT 28,33,9
         UNION ALL SELECT 29,34,20 UNION ALL SELECT 30,35,6  UNION ALL SELECT 31,37,100
         UNION ALL SELECT 32,38,50 UNION ALL SELECT 33,39,40 UNION ALL SELECT 34,40,60
         UNION ALL SELECT 35,41,70 UNION ALL SELECT 36,42,30 UNION ALL SELECT 37,43,80
         UNION ALL SELECT 38,44,40 UNION ALL SELECT 39,45,25 UNION ALL SELECT 40,46,60
         UNION ALL SELECT 41,47,50 UNION ALL SELECT 42,48,45 UNION ALL SELECT 43,49,20
         UNION ALL SELECT 44,51,18 UNION ALL SELECT 45,52,40 UNION ALL SELECT 46,53,15
         UNION ALL SELECT 47,54,10 UNION ALL SELECT 48,55,80 UNION ALL SELECT 49,1,10
         UNION ALL SELECT 50,2,15
         -- segundas lineas en 10 ventas (ventas con varios productos)
         UNION ALL SELECT 1,11,10  UNION ALL SELECT 2,21,20  UNION ALL SELECT 3,12,15
         UNION ALL SELECT 5,1,5    UNION ALL SELECT 8,30,5   UNION ALL SELECT 12,14,20
         UNION ALL SELECT 15,53,10 UNION ALL SELECT 20,3,10  UNION ALL SELECT 25,25,5
         UNION ALL SELECT 30,52,10
     ) AS t
         JOIN lotes_inventario l ON l.id_lote = t.id_lote;

-- ------------------------------------------
-- Totales de cada venta = suma de sus detalles
-- ------------------------------------------
UPDATE ventas v
    JOIN (
    SELECT id_venta,
    SUM(subtotal)        AS st,
    SUM(monto_descuento) AS md,
    SUM(monto_impuesto)  AS mi,
    SUM(total_linea)     AS tl
    FROM detalles_venta
    GROUP BY id_venta
    ) d ON d.id_venta = v.id_venta
    SET v.subtotal = d.st,
        v.total_descuento = d.md,
        v.total_impuesto = d.mi,
        v.monto_total = d.tl;

-- ------------------------------------------
-- DML: facturas (una por venta no cancelada) con CUFE y QR simulados
-- ------------------------------------------
INSERT INTO facturas
(id_venta, numero_factura, cufe, enlace_qr, fecha_emision, subtotal, total_descuento, monto_impuesto, monto_total)
SELECT v.id_venta,
       CONCAT('FE-', LPAD(v.id_venta, 6, '0')),
       SHA2(CONCAT('CUFE-FT-', v.numero_venta), 384),
       CONCAT('https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=',
              SHA2(CONCAT('CUFE-FT-', v.numero_venta), 384)),
       v.fecha_venta,
       v.subtotal, v.total_descuento, v.total_impuesto, v.monto_total
FROM ventas v
WHERE v.id_estado <> 5;

-- ------------------------------------------
-- Ajuste de stock: cantidad = cantidad_inicial - lo vendido (sin ventas canceladas)
-- ------------------------------------------
UPDATE lotes_inventario l
    JOIN (
    SELECT d.id_lote, SUM(d.cantidad_vendida) AS vendido
    FROM detalles_venta d
    JOIN ventas v ON v.id_venta = d.id_venta
    WHERE v.id_estado <> 5
    GROUP BY d.id_lote
    ) s ON s.id_lote = l.id_lote
    SET l.cantidad = l.cantidad_inicial - s.vendido;

-- ------------------------------------------
-- DML: promociones (el descuento de 50% se aplica a lotes Proximo a Vencer)
-- ------------------------------------------
INSERT INTO promociones (nombre, descripcion, tasa_descuento, aplica_estado_lote, aplica_categoria_producto, activa, fecha_inicio, fecha_fin) VALUES
                                                                                                                                                  ('Proximo a vencer 50%', 'Descuento automatico para lotes con 7 dias o menos para vencer', 0.50, 2, NULL, TRUE, '2026-01-01', '2026-12-31'),
                                                                                                                                                  ('Lacteos de temporada 10%', 'Descuento promocional en lacteos', 0.10, NULL, 3, FALSE, '2026-06-01', '2026-06-30'),
                                                                                                                                                  ('Frutas de fin de ano 5%', 'Descuento promocional en frutas', 0.05, NULL, 1, FALSE, '2026-12-01', '2026-12-31');

-- ------------------------------------------
-- DML: conductores (50). Los ultimos 5 estan inactivos
-- ------------------------------------------
INSERT INTO conductores (nombre, numero_licencia, telefono, email, activo)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50
)
SELECT CONCAT(
               ELT(1 + MOD(n - 1, 10), 'Carlos', 'Andrea', 'Luis', 'Jorge', 'Diana', 'Camilo', 'Felipe', 'Sandra', 'Julian', 'Mauricio'),
               ' ',
               ELT(1 + FLOOR((n - 1) / 10), 'Ramirez Soto', 'Gomez Prieto', 'Torres Mejia', 'Castro Vargas', 'Perez Duarte')
       ),
       CONCAT('2026', LPAD(n, 6, '0')),
       CONCAT('320', LPAD(n, 7, '0')),
       CONCAT('conductor', n, '@frescuratotal.com'),
       IF(n <= 45, TRUE, FALSE)
FROM seq
ORDER BY n;

-- ------------------------------------------
-- DML: vehiculos (50). 46-48 en mantenimiento, 49-50 inactivos
-- ------------------------------------------
INSERT INTO vehiculos (placa, modelo, capacidad, estado)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50
)
SELECT CONCAT('FTA', LPAD(100 + n, 3, '0')),
       ELT(1 + MOD(n, 5), 'Chevrolet NHR 2022', 'Hyundai HD65 2021', 'Kia K3000 2023', 'Foton Aumark 2022', 'JAC X250 2023'),
       ELT(1 + MOD(n, 4), 1500, 3500, 5000, 8000),
       CASE WHEN n <= 45 THEN 'Activo'
            WHEN n <= 48 THEN 'Mantenimiento'
            ELSE 'Inactivo' END
FROM seq
ORDER BY n;

-- ------------------------------------------
-- DML: rutas_entrega (50)
-- Rutas 1-12 Completada (ventas Entregadas), 13-20 En Transito,
-- 21-35 Planificada (ventas En Preparacion), 36-50 Cancelada (reasignadas)
-- ------------------------------------------
INSERT INTO rutas_entrega
(nombre_ruta, fecha_creacion, usuario_creacion, hora_salida_estimada, hora_llegada_estimada,
 hora_salida_real, hora_llegada_real, estado)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50
)
SELECT CONCAT('Ruta ', ELT(1 + MOD(n, 5), 'Norte', 'Sur', 'Centro', 'Occidente', 'Oriente'), ' ', LPAD(n, 3, '0')),
       IF(n <= 35, v.fecha_venta + INTERVAL 2 HOUR, v.fecha_venta + INTERVAL 1 HOUR),
       IF(MOD(n, 2) = 0, 'logistica1', 'logistica2'),
       CASE WHEN n <= 12 THEN '2026-10-31 08:00:00'
            WHEN n <= 20 THEN '2026-11-01 08:00:00'
            WHEN n <= 35 THEN '2026-11-02 08:00:00'
            ELSE '2026-11-01 07:00:00' END,
       CASE WHEN n <= 12 THEN '2026-10-31 12:00:00'
            WHEN n <= 20 THEN '2026-11-01 14:00:00'
            WHEN n <= 35 THEN '2026-11-02 14:00:00'
            ELSE '2026-11-01 12:00:00' END,
       CASE WHEN n <= 12 THEN DATE_ADD('2026-10-31 08:00:00', INTERVAL n MINUTE)
            WHEN n <= 20 THEN DATE_ADD('2026-11-01 08:00:00', INTERVAL n MINUTE)
            ELSE NULL END,
       CASE WHEN n <= 12 THEN DATE_ADD('2026-10-31 11:30:00', INTERVAL n MINUTE)
            ELSE NULL END,
       CASE WHEN n <= 12 THEN 'Completada'
            WHEN n <= 20 THEN 'En Transito'
            WHEN n <= 35 THEN 'Planificada'
            ELSE 'Cancelada' END
FROM seq
         JOIN ventas v ON v.id_venta = IF(n <= 35, n, n - 15)
ORDER BY n;

-- ------------------------------------------
-- DML: asignaciones_entrega (una por ruta, solo vehiculos y conductores activos 1-45)
-- ------------------------------------------
INSERT INTO asignaciones_entrega
(id_ruta, id_vehiculo, id_conductor, fecha_asignacion, usuario_asignacion, notas)
SELECT r.id_ruta,
       1 + MOD(r.id_ruta - 1, 45),
       1 + MOD(r.id_ruta + 6, 45),
       r.fecha_creacion + INTERVAL 30 MINUTE,
    r.usuario_creacion,
    CASE r.estado
    WHEN 'Cancelada'  THEN 'Ruta cancelada por cambio de vehiculo; el pedido fue reasignado'
    WHEN 'Completada' THEN 'Entrega realizada sin novedades'
    ELSE NULL END
FROM rutas_entrega r
ORDER BY r.id_ruta;

-- ------------------------------------------
-- DML: ventas_ruta (50): cada ruta lleva un pedido
-- Rutas 36-50 corresponden a las ventas 21-35 (primer intento cancelado)
-- ------------------------------------------
INSERT INTO ventas_ruta
(id_ruta, id_venta, orden_entrega, direccion_destino, fecha_entrega, recibido_por, observaciones)
SELECT r.id_ruta, v.id_venta, 1, c.direccion,
       CASE WHEN r.estado = 'Completada' THEN r.hora_llegada_real END,
       CASE WHEN r.estado = 'Completada'
                THEN ELT(1 + MOD(r.id_ruta, 4), 'Jefe de bodega', 'Recepcionista', 'Administrador', 'Chef de cocina') END,
       CASE r.estado
           WHEN 'Completada'  THEN 'Entrega completa sin novedades'
           WHEN 'En Transito' THEN 'Pedido en camino al cliente'
           WHEN 'Planificada' THEN 'Pendiente de despacho'
           ELSE 'Ruta cancelada; pedido reasignado a una nueva ruta' END
FROM rutas_entrega r
         JOIN ventas v ON v.id_venta = IF(r.id_ruta <= 35, r.id_ruta, r.id_ruta - 15)
         JOIN clientes c ON c.id_cliente = v.id_cliente
ORDER BY r.id_ruta;

-- ------------------------------------------
-- DML: registro_cuarentena
-- 51 cuarentenas ya resueltas (todos los lotes excepto 19, 28, 36 y 50)
-- Los lotes 7, 17 y 27 fueron rechazados; el resto aprobado
-- ------------------------------------------
INSERT INTO registro_cuarentena
(id_lote, motivo, usuario, fecha_registro, resuelta, fecha_resolucion, notas_resolucion)
SELECT x.id_lote,
       ELT(1 + MOD(x.rn, 6), 'Empaque danado', 'Olor extrano', 'Temperatura fuera de rango en transporte',
           'Presencia de humedad', 'Etiqueta ilegible o ausente', 'Golpes visibles en el producto'),
       ELT(1 + MOD(x.rn, 3), 'operario1', 'operario2', 'operario3'),
       x.fecha_ingreso + INTERVAL (2 + MOD(x.rn, 5)) HOUR,
    TRUE,
    x.fecha_ingreso + INTERVAL (5 + MOD(x.rn, 5)) HOUR,
    IF(x.id_lote IN (7, 17, 27), 'Lote rechazado: no apto para consumo', 'Revision completada: apto para la venta')
FROM (
    SELECT id_lote, fecha_ingreso, ROW_NUMBER() OVER (ORDER BY id_lote) AS rn
    FROM lotes_inventario
    WHERE id_lote NOT IN (19, 28, 36, 50)
    ) x
ORDER BY x.id_lote;

-- Cuarentenas abiertas: los dos lotes que estan hoy En Cuarentena
INSERT INTO registro_cuarentena
(id_lote, motivo, usuario, fecha_registro, resuelta, fecha_resolucion, notas_resolucion) VALUES
                                                                                             (19, 'Olor extrano detectado al abrir las cajas', 'operario1', '2026-10-31 09:15:00', FALSE, NULL, NULL),
                                                                                             (28, 'Empaque danado y producto con humedad', 'operario2', '2026-10-31 14:30:00', FALSE, NULL, NULL);

-- ------------------------------------------
-- DML: inspecciones_calidad (una por cada cuarentena resuelta)
-- ------------------------------------------
INSERT INTO inspecciones_calidad
(id_lote, fecha_inspeccion, resultado, nombre_inspector, motivo_rechazo, notas)
SELECT q.id_lote,
       q.fecha_resolucion,
       IF(q.id_lote IN (7, 17, 27), 'Rechazado', 'Aprobado'),
       ELT(1 + MOD(q.id_registro, 3), 'Laura Gomez', 'Pedro Sanchez', 'Marta Ibanez'),
       IF(q.id_lote IN (7, 17, 27), 'Producto en mal estado, no apto para consumo humano', NULL),
       IF(q.id_lote IN (7, 17, 27), 'Se ordena la baja total del lote', 'Producto en condiciones aceptables, vuelve a la venta')
FROM registro_cuarentena q
WHERE q.resuelta = TRUE
ORDER BY q.id_registro;

-- ------------------------------------------
-- DML: registro_mermas (50)
-- ------------------------------------------
-- A) 3 mermas por lotes rechazados en inspeccion (perdida total)
INSERT INTO registro_mermas (id_lote, id_inspeccion, cantidad_perdida, motivo, fecha_registro, usuario_registro)
SELECT i.id_lote, i.id_inspeccion, l.cantidad,
       CONCAT('Rechazado en inspeccion: ', i.motivo_rechazo),
       i.fecha_inspeccion, i.nombre_inspector
FROM inspecciones_calidad i
         JOIN lotes_inventario l ON l.id_lote = i.id_lote
WHERE i.resultado = 'Rechazado'
ORDER BY i.id_inspeccion;

-- B) 2 mermas por lotes vencidos (perdida total)
INSERT INTO registro_mermas (id_lote, id_inspeccion, cantidad_perdida, motivo, fecha_registro, usuario_registro) VALUES
                                                                                                                     (36, NULL, 70,  'Producto vencido: baja total del lote', '2026-10-29 08:00:00', 'operario1'),
                                                                                                                     (50, NULL, 150, 'Producto vencido: baja total del lote', '2026-11-01 07:30:00', 'operario2');

-- C) 45 mermas pequenas (2% del lote) por manipulacion
INSERT INTO registro_mermas (id_lote, id_inspeccion, cantidad_perdida, motivo, fecha_registro, usuario_registro)
SELECT x.id_lote, NULL, ROUND(x.cantidad_inicial * 0.02, 2),
       ELT(1 + MOD(x.rn, 5), 'Producto danado en manipulacion', 'Empaque roto', 'Golpes durante el almacenamiento',
           'Perdida de frescura', 'Derrame o fuga'),
       x.fecha_ingreso + INTERVAL 1 DAY + INTERVAL (MOD(x.rn, 6)) HOUR,
    ELT(1 + MOD(x.rn, 3), 'operario1', 'operario2', 'operario3')
FROM (
    SELECT id_lote, cantidad_inicial, fecha_ingreso, ROW_NUMBER() OVER (ORDER BY id_lote) AS rn
    FROM lotes_inventario
    WHERE id_lote NOT IN (7, 17, 19, 27, 28, 36, 50)
    ) x
WHERE x.rn <= 45
ORDER BY x.id_lote;

-- ------------------------------------------
-- Ajuste de stock: restar lo perdido en mermas a la cantidad de cada lote
-- ------------------------------------------
UPDATE lotes_inventario l
    JOIN (
    SELECT id_lote, SUM(cantidad_perdida) AS perdido
    FROM registro_mermas
    GROUP BY id_lote
    ) m ON m.id_lote = l.id_lote
    SET l.cantidad = l.cantidad - m.perdido;