-- Tables for the business sales database

CREATE TABLE Dim_Date (
    date_id INT PRIMARY KEY,
    full_date DATE NOT NULL,
    day INT NOT NULL,
    month INT NOT NULL,
    month_name VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    day_of_week VARCHAR(15) NOT NULL,
    is_weekend BIT NOT NULL DEFAULT 0
);

CREATE TABLE Dim_Product (
    product_id INT IDENTITY(1,1) PRIMARY KEY,
    product_name VARCHAR(150) NOT NULL,
    category VARCHAR(80),
    unit_of_measure VARCHAR(20),
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

CREATE TABLE Dim_Supplier (
    supplier_id INT IDENTITY(1,1) PRIMARY KEY,
    supplier_name VARCHAR(150) NOT NULL,
    contact_name VARCHAR(100),
    phone VARCHAR(30),
    email VARCHAR(150),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

CREATE TABLE Dim_ExpenseCategory (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Fact_Purchases (
    purchase_id INT IDENTITY(1,1) PRIMARY KEY,
    date_id INT NOT NULL,
    product_id INT NOT NULL,
    supplier_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    total_cost AS (quantity * unit_cost) PERSISTED,
    payment_method VARCHAR(30) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Purchases_Date FOREIGN KEY (date_id) REFERENCES Dim_Date(date_id),
    CONSTRAINT FK_Purchases_Product FOREIGN KEY (product_id) REFERENCES Dim_Product(product_id),
    CONSTRAINT FK_Purchases_Supplier FOREIGN KEY (supplier_id) REFERENCES Dim_Supplier(supplier_id)
);

CREATE TABLE Fact_MaterialExpenses (
    expense_id INT IDENTITY(1,1) PRIMARY KEY,
    date_id INT NOT NULL,
    category_id INT NOT NULL,
    product_id INT NULL,
    supplier_id INT NULL,
    description VARCHAR(200) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Expenses_Date FOREIGN KEY (date_id) REFERENCES Dim_Date(date_id),
    CONSTRAINT FK_Expenses_Category FOREIGN KEY (category_id) REFERENCES Dim_ExpenseCategory(category_id),
    CONSTRAINT FK_Expenses_Product FOREIGN KEY (product_id) REFERENCES Dim_Product(product_id),
    CONSTRAINT FK_Expenses_Supplier FOREIGN KEY (supplier_id) REFERENCES Dim_Supplier(supplier_id)
);

CREATE TABLE Inventory (
    inventory_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    quantity_on_hand INT NOT NULL DEFAULT 0,
    minimum_stock INT NOT NULL DEFAULT 0,
    last_updated DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Inventory_Product FOREIGN KEY (product_id) REFERENCES Dim_Product(product_id)
);

-- INSERTION OF DIMENSIONAL DATA

INSERT INTO Dim_Date
(date_id, full_date, day, month, month_name, year, day_of_week, is_weekend)
VALUES
(20250102, '2025-01-02', 2, 1, 'Enero', 2025, 'Jueves', 0),
(20250103, '2025-01-03', 3, 1, 'Enero', 2025, 'Viernes', 0),
(20250104, '2025-01-04', 4, 1, 'Enero', 2025, 'Sábado', 1),
(20250106, '2025-01-06', 6, 1, 'Enero', 2025, 'Lunes', 0),
(20250108, '2025-01-08', 8, 1, 'Enero', 2025, 'Miércoles', 0),
(20250110, '2025-01-10', 10, 1, 'Enero', 2025, 'Viernes', 0),
(20250112, '2025-01-12', 12, 1, 'Enero', 2025, 'Domingo', 1),
(20250115, '2025-01-15', 15, 1, 'Enero', 2025, 'Miércoles', 0),
(20250118, '2025-01-18', 18, 1, 'Enero', 2025, 'Sábado', 1),
(20250120, '2025-01-20', 20, 1, 'Enero', 2025, 'Lunes', 0),
(20250122, '2025-01-22', 22, 1, 'Enero', 2025, 'Miércoles', 0),
(20250125, '2025-01-25', 25, 1, 'Enero', 2025, 'Sábado', 1),
(20250201, '2025-02-01', 1, 2, 'Febrero', 2025, 'Sábado', 1),
(20250203, '2025-02-03', 3, 2, 'Febrero', 2025, 'Lunes', 0),
(20250205, '2025-02-05', 5, 2, 'Febrero', 2025, 'Miércoles', 0),
(20250207, '2025-02-07', 7, 2, 'Febrero', 2025, 'Viernes', 0),
(20250210, '2025-02-10', 10, 2, 'Febrero', 2025, 'Lunes', 0),
(20250212, '2025-02-12', 12, 2, 'Febrero', 2025, 'Miércoles', 0),
(20250215, '2025-02-15', 15, 2, 'Febrero', 2025, 'Sábado', 1),
(20250218, '2025-02-18', 18, 2, 'Febrero', 2025, 'Martes', 0),
(20250220, '2025-02-20', 20, 2, 'Febrero', 2025, 'Jueves', 0),
(20250222, '2025-02-22', 22, 2, 'Febrero', 2025, 'Sábado', 1),
(20250224, '2025-02-24', 24, 2, 'Febrero', 2025, 'Lunes', 0),
(20250226, '2025-02-26', 26, 2, 'Febrero', 2025, 'Miércoles', 0),
(20250228, '2025-02-28', 28, 2, 'Febrero', 2025, 'Viernes', 0),
(20250301, '2025-03-01', 1, 3, 'Marzo', 2025, 'Sábado', 1),
(20250303, '2025-03-03', 3, 3, 'Marzo', 2025, 'Lunes', 0),
(20250305, '2025-03-05', 5, 3, 'Marzo', 2025, 'Miércoles', 0),
(20250308, '2025-03-08', 8, 3, 'Marzo', 2025, 'Sábado', 1),
(20250310, '2025-03-10', 10, 3, 'Marzo', 2025, 'Lunes', 0);

INSERT INTO Dim_Product
(product_name, category, unit_of_measure, unit_price, is_active)
VALUES
('Taza blanca personalizada', 'Tazas', 'Unidad', 3500.00, 1),
('Taza mágica personalizada', 'Tazas', 'Unidad', 5000.00, 1),
('Camisa blanca personalizada', 'Ropa', 'Unidad', 8500.00, 1),
('Camisa negra personalizada', 'Ropa', 'Unidad', 9500.00, 1),
('Gorra personalizada', 'Ropa', 'Unidad', 6500.00, 1),
('Medias personalizadas', 'Ropa', 'Par', 4000.00, 1),
('Sticker resistente al agua', 'Stickers', 'Unidad', 1200.00, 1),
('Sticker normal', 'Stickers', 'Unidad', 700.00, 1),
('Botella personalizada', 'Botellas', 'Unidad', 7500.00, 1),
('Termo personalizado', 'Termos', 'Unidad', 12000.00, 1),
('Llavero acrílico', 'Accesorios', 'Unidad', 2500.00, 1),
('Llavero de madera', 'Accesorios', 'Unidad', 3000.00, 1),
('Mousepad personalizado', 'Accesorios', 'Unidad', 5500.00, 1),
('Agenda personalizada', 'Papelería', 'Unidad', 5000.00, 1),
('Cuaderno personalizado', 'Papelería', 'Unidad', 4500.00, 1),
('Tarjeta personalizada', 'Papelería', 'Unidad', 500.00, 1),
('Vaso térmico', 'Bebidas', 'Unidad', 8500.00, 1),
('Portavasos personalizado', 'Hogar', 'Unidad', 2500.00, 1),
('Bolsa personalizada', 'Accesorios', 'Unidad', 4500.00, 1),
('Peluche personalizado', 'Regalos', 'Unidad', 7500.00, 0);

INSERT INTO Dim_Supplier
(supplier_name, contact_name, phone, email)
VALUES
('Distribuidora Tica', 'Carlos Rodríguez', '8888-1001', 'carlos@distribuidoratica.com'),
('Papeles del Valle', 'María González', '8888-1002', 'maria@papelesvalle.com'),
('Importadora Central', 'José Ramírez', '8888-1003', 'jose@importadoracentral.com'),
('Textiles CR', 'Ana López', '8888-1004', 'ana@textilescr.com'),
('Suministros Creativos', 'Daniel Vargas', '8888-1005', 'daniel@suministroscreativos.com'),
('Empaques Costa Rica', 'Laura Hernández', '8888-1006', 'laura@empaquescr.com'),
('Acrílicos y Más', 'Andrés Mora', '8888-1007', 'andres@acrilicosymas.com'),
('Insumos Digitales', 'Sofía Castro', '8888-1008', 'sofia@insumosdigitales.com'),
('Proveedora Nacional', 'Miguel Jiménez', '8888-1009', 'miguel@proveedoranacional.com'),
('Comercial San José', 'Valeria Sánchez', '8888-1010', 'valeria@comercialsj.com');

INSERT INTO Dim_ExpenseCategory
(category_name)
VALUES
('Electricidad'),
('Agua'),
('Internet'),
('Transporte'),
('Publicidad'),
('Empaque'),
('Mantenimiento'),
('Papelería');

INSERT INTO Fact_Purchases
(date_id, product_id, supplier_id, quantity, unit_cost, payment_method)
VALUES
(20250102, 1, 1, 20, 1800.00, 'Transferencia'),
(20250103, 3, 4, 10, 5200.00, 'Tarjeta'),
(20250104, 7, 5, 50, 500.00, 'Efectivo'),
(20250106, 2, 1, 15, 2700.00, 'Transferencia'),
(20250108, 5, 4, 12, 3800.00, 'SINPE'),
(20250110, 8, 5, 100, 250.00, 'Efectivo'),
(20250112, 10, 3, 8, 7500.00, 'Tarjeta'),
(20250115, 11, 7, 25, 1100.00, 'Transferencia'),
(20250118, 4, 4, 15, 6000.00, 'SINPE'),
(20250120, 6, 4, 20, 2300.00, 'Efectivo'),
(20250122, 9, 3, 10, 4300.00, 'Tarjeta'),
(20250125, 14, 2, 30, 2800.00, 'Transferencia'),

(20250201, 1, 1, 30, 1750.00, 'SINPE'),
(20250203, 3, 4, 20, 5100.00, 'Transferencia'),
(20250205, 7, 5, 80, 480.00, 'Efectivo'),
(20250207, 2, 1, 25, 2600.00, 'Tarjeta'),
(20250210, 5, 4, 15, 3900.00, 'SINPE'),
(20250212, 8, 5, 120, 230.00, 'Efectivo'),
(20250215, 10, 3, 12, 7300.00, 'Transferencia'),
(20250218, 11, 7, 30, 1050.00, 'Tarjeta'),
(20250220, 4, 4, 18, 5900.00, 'SINPE'),
(20250222, 6, 4, 25, 2250.00, 'Efectivo'),
(20250224, 9, 3, 12, 4200.00, 'Transferencia'),
(20250226, 14, 2, 40, 2700.00, 'Tarjeta'),
(20250228, 15, 2, 25, 2400.00, 'SINPE'),

(20250301, 1, 1, 25, 1800.00, 'Transferencia'),
(20250303, 3, 4, 15, 5300.00, 'Tarjeta'),
(20250305, 7, 5, 60, 520.00, 'Efectivo'),
(20250308, 2, 1, 20, 2750.00, 'SINPE'),
(20250310, 5, 4, 10, 4000.00, 'Transferencia'),
(20250310, 8, 5, 90, 240.00, 'Efectivo'),
(20250110, 13, 8, 10, 3500.00, 'Tarjeta'),
(20250120, 17, 3, 8, 5500.00, 'Transferencia'),
(20250205, 18, 6, 20, 1400.00, 'SINPE'),
(20250215, 19, 6, 15, 2600.00, 'Efectivo'),
(20250303, 12, 7, 20, 1500.00, 'Tarjeta'),
(20250305, 16, 2, 100, 250.00, 'Efectivo'),
(20250125, 9, 3, 5, 4500.00, 'Transferencia'),
(20250220, 10, 3, 6, 7200.00, 'SINPE'),
(20250308, 15, 2, 30, 2300.00, 'Tarjeta');

INSERT INTO Fact_MaterialExpenses
(date_id, category_id, product_id, supplier_id, description, amount, payment_method)
VALUES

(20250102, 1, NULL, NULL,
'Pago de electricidad del local',
28500.00, 'Transferencia'),

(20250103, 3, NULL, NULL,
'Servicio mensual de internet',
22000.00, 'Tarjeta'),

(20250104, 6, 7, 6,
'Compra de bolsas para stickers',
8500.00, 'Efectivo'),

(20250106, 4, NULL, NULL,
'Transporte para recoger materiales',
6500.00, 'Efectivo'),

(20250108, 5, NULL, NULL,
'Publicidad en redes sociales',
15000.00, 'Tarjeta'),

(20250110, 6, 3, 6,
'Bolsas para camisetas',
12000.00, 'Transferencia'),

(20250112, 7, NULL, 8,
'Mantenimiento de impresora',
18500.00, 'SINPE'),

(20250115, 8, NULL, 2,
'Compra de papel para diseños',
9500.00, 'Efectivo'),

(20250118, 1, NULL, NULL,
'Pago de electricidad',
31200.00, 'Transferencia'),

(20250120, 4, NULL, NULL,
'Transporte de materiales',
4800.00, 'Efectivo'),

(20250122, 5, NULL, NULL,
'Campaña publicitaria',
20000.00, 'Tarjeta'),

(20250125, 6, 1, 6,
'Empaque para tazas',
7500.00, 'SINPE'),

(20250201, 3, NULL, NULL,
'Pago de internet',
22000.00, 'Tarjeta'),

(20250203, 7, NULL, 8,
'Limpieza de impresora',
9000.00, 'Efectivo'),

(20250205, 4, NULL, NULL,
'Transporte para compra de insumos',
7200.00, 'Efectivo'),

(20250207, 5, NULL, NULL,
'Publicidad en Instagram',
18000.00, 'Tarjeta'),

(20250210, 6, 3, 6,
'Bolsas para camisas',
10500.00, 'Transferencia'),

(20250212, 8, 14, 2,
'Papel para agendas',
11500.00, 'Efectivo'),

(20250215, 1, NULL, NULL,
'Pago de electricidad',
29800.00, 'Transferencia'),

(20250218, 7, NULL, 8,
'Mantenimiento preventivo',
14000.00, 'SINPE'),

(20250220, 4, NULL, NULL,
'Transporte de pedidos',
5500.00, 'Efectivo'),

(20250222, 5, NULL, NULL,
'Publicidad de productos',
12500.00, 'Tarjeta'),

(20250224, 6, 10, 6,
'Empaque para termos',
9800.00, 'Transferencia'),

(20250226, 8, 15, 2,
'Material para cuadernos',
13200.00, 'Efectivo'),

(20250228, 3, NULL, NULL,
'Internet mensual',
22000.00, 'Tarjeta'),

(20250301, 1, NULL, NULL,
'Pago de electricidad',
30500.00, 'Transferencia'),

(20250303, 4, NULL, NULL,
'Transporte de materiales',
6300.00, 'Efectivo'),

(20250305, 5, NULL, NULL,
'Publicidad en redes sociales',
17500.00, 'Tarjeta'),

(20250308, 7, NULL, 8,
'Mantenimiento de equipo',
21000.00, 'SINPE'),

(20250310, 6, 9, 6,
'Empaque para botellas',
8700.00, 'Transferencia');

INSERT INTO Inventory
(product_id, quantity_on_hand, minimum_stock)
VALUES
(1, 45, 10),
(2, 18, 8),
(3, 32, 10),
(4, 12, 8),
(5, 20, 5),
(6, 35, 10),
(7, 120, 30),
(8, 250, 50),
(9, 15, 5),
(10, 8, 5),
(11, 40, 10),
(12, 25, 8),
(13, 14, 5),
(14, 50, 15),
(15, 35, 10),
(16, 200, 50),
(17, 10, 5),
(18, 28, 10),
(19, 22, 8),
(20, 3, 5);