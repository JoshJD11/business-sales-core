-- Tables for the business sales database, you can also create your own database

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


CREATE TABLE Dim_Customer (
    customer_id INT IDENTITY(1,1) PRIMARY KEY,
    customer_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(150),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);


CREATE TABLE Fact_Sales (
    sale_id INT IDENTITY(1,1) PRIMARY KEY,
    date_id INT NOT NULL,
    product_id INT NOT NULL,
    customer_id INT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price AS (quantity * unit_price) PERSISTED,
    payment_method VARCHAR(30) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Sales_Date FOREIGN KEY (date_id) REFERENCES Dim_Date(date_id) ON DELETE CASCADE,
    CONSTRAINT FK_Sales_Product FOREIGN KEY (product_id) REFERENCES Dim_Product(product_id) ON DELETE CASCADE,
    CONSTRAINT FK_Sales_Customer FOREIGN KEY (customer_id) REFERENCES Dim_Customer(customer_id) ON DELETE SET NULL
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
    quantity INT NULL,

    CONSTRAINT FK_Expenses_Date FOREIGN KEY (date_id) REFERENCES Dim_Date(date_id) ON DELETE CASCADE,
    CONSTRAINT FK_Expenses_Category FOREIGN KEY (category_id) REFERENCES Dim_ExpenseCategory(category_id) ON DELETE CASCADE,
    CONSTRAINT FK_Expenses_Product FOREIGN KEY (product_id) REFERENCES Dim_Product(product_id) ON DELETE SET NULL,
    CONSTRAINT FK_Expenses_Supplier FOREIGN KEY (supplier_id) REFERENCES Dim_Supplier(supplier_id) ON DELETE SET NULL
);


CREATE TABLE Inventory (
    inventory_id INT IDENTITY(1,1) PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    quantity_on_hand INT NOT NULL DEFAULT 0,
    minimum_stock INT NOT NULL DEFAULT 0,
    last_updated DATETIME2 NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Inventory_Product FOREIGN KEY (product_id) REFERENCES Dim_Product(product_id) ON DELETE CASCADE
);

-- Insert initial data into Dim_Date

INSERT INTO Dim_Date (date_id, full_date, day, month, month_name, year, day_of_week, is_weekend) VALUES
(20260824, '2026-08-24', 24, 8, 'August', 2026, 'Monday', 0),
(20260825, '2026-08-25', 25, 8, 'August', 2026, 'Tuesday', 0),
(20260826, '2026-08-26', 26, 8, 'August', 2026, 'Wednesday', 0),
(20260827, '2026-08-27', 27, 8, 'August', 2026, 'Thursday', 0),
(20260828, '2026-08-28', 28, 8, 'August', 2026, 'Friday', 0),
(20260829, '2026-08-29', 29, 8, 'August', 2026, 'Saturday', 1),
(20260830, '2026-08-30', 30, 8, 'August', 2026, 'Sunday', 1);

INSERT INTO Dim_Product (product_name, category, unit_of_measure, unit_price, is_active) VALUES
('Taza blanca personalizada', 'Producto terminado', 'unit', 4500.00, 1),
('Taza negra personalizada', 'Producto terminado', 'unit', 4800.00, 1),
('Plato decorativo personalizado', 'Producto terminado', 'unit', 6500.00, 1),
('Arcilla cruda', 'Materia prima', 'kg', 850.00, 1),
('Pegamento cerámico especial', 'Materia prima', 'unit', 3500.00, 1),
('Esmalte transparente', 'Materia prima', 'liter', 5200.00, 1);

INSERT INTO Dim_Supplier (supplier_name, contact_name, phone, email) VALUES
('Cerámicas Alajuela', 'Marco Vindas', '24401122', 'ventas@ceramicasalajuela.cr'),
('Insumos Creativos CR', 'Laura Solano', '22331144', 'contacto@insumoscreativos.cr'),
('Ferretería El Roble', 'Danilo Araya', '24509988', NULL);

INSERT INTO Dim_ExpenseCategory (category_name) VALUES
('material'),
('combustible'),
('servicios'),
('renta'),
('otro');

INSERT INTO Dim_Customer (customer_name, phone, email) VALUES
('Ana Rodríguez', '88112233', 'ana.rodriguez@gmail.com'),
('Carlos Méndez', '87654321', 'carlos.mendez@hotmail.com'),
('Sofía Castro', '89901122', 'sofia.castro@gmail.com'),
('Pedro Jiménez', '86543210', NULL);

INSERT INTO Fact_Sales (date_id, product_id, customer_id, quantity, unit_price, payment_method) VALUES
(20260824, 1, 1, 2, 4500.00, 'SINPE'),
(20260825, 2, 2, 1, 4800.00, 'efectivo'),
(20260826, 3, 3, 1, 6500.00, 'tarjeta'),
(20260827, 1, NULL, 3, 4500.00, 'efectivo'),
(20260828, 2, 4, 2, 4800.00, 'SINPE'),
(20260829, 1, 1, 1, 4500.00, 'SINPE'),
(20260830, 3, 2, 2, 6500.00, 'tarjeta');

INSERT INTO Fact_MaterialExpenses (date_id, category_id, product_id, supplier_id, description, amount, payment_method, quantity) VALUES
(20260824, 1, 4, 1, 'Compra de arcilla cruda para producción', 42500.00, 'SINPE', 50),
(20260825, 2, NULL, NULL, 'Gasolina para entregas', 15000.00, 'efectivo', NULL),
(20260826, 1, 5, 2, 'Compra de pegamento cerámico especial', 7000.00, 'efectivo', 2),
(20260827, 3, NULL, NULL, 'Pago de electricidad del taller', 28000.00, 'SINPE', NULL),
(20260828, 1, 6, 2, 'Compra de esmalte transparente', 15600.00, 'tarjeta', 3),
(20260829, 5, NULL, 3, 'Compra de herramientas menores', 9800.00, 'efectivo', NULL),
(20260830, 4, NULL, NULL, 'Pago de renta del taller', 180000.00, 'SINPE', NULL);

INSERT INTO Inventory (product_id, quantity_on_hand, minimum_stock) VALUES
(1, 18, 10),
(2, 6, 10),
(3, 12, 5),
(4, 50, 20),
(5, 8, 5),
(6, 15, 8);

-- Procedures

CREATE PROCEDURE ReduceStock
    @ProductId INT,
    @QuantitySold INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @CurrentStock INT;

    IF @QuantitySold <= 0
    BEGIN
        THROW 50003, 'La cantidad a reducir debe ser mayor a cero.', 1;
    END

    SELECT @CurrentStock = quantity_on_hand
    FROM Inventory
    WHERE product_id = @ProductId;

    IF @CurrentStock IS NULL
    BEGIN
        THROW 50004, 'El producto no tiene registro de inventario.', 1;
    END

    IF @CurrentStock < @QuantitySold
    BEGIN
        THROW 50005, 'No hay suficiente stock disponible para esta venta.', 1;
    END

    UPDATE Inventory
    SET quantity_on_hand = @CurrentStock - @QuantitySold,
        last_updated = GETDATE()
    WHERE product_id = @ProductId;

END;

CREATE PROCEDURE InsertSale
    @product_name VARCHAR(150),
    @quantity INT,
    @payment_method VARCHAR(30),
    @customer_email VARCHAR(150)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY

        IF @quantity <= 0
        BEGIN
            THROW 50001, 'La cantidad debe ser mayor a cero.', 1;
        END

        DECLARE @product_id INT;
        DECLARE @unit_price DECIMAL(10,2);
        DECLARE @customer_id INT;
        DECLARE @today_id INT;

        SELECT @product_id = p.product_id, @unit_price = p.unit_price
        FROM dbo.Dim_Product p
        WHERE p.product_name = @product_name;

        IF @product_id IS NULL
        BEGIN
            THROW 50002, 'Producto no encontrado.', 1;
        END

        SELECT @customer_id = c.customer_id
        FROM dbo.Dim_Customer c
        WHERE c.email = @customer_email;

        SET @today_id = CAST(FORMAT(GETDATE(), 'yyyyMMdd') AS INT);

        BEGIN TRANSACTION;

        IF NOT EXISTS (SELECT 1 FROM Dim_Date WHERE date_id = @today_id)
        BEGIN
            INSERT INTO Dim_Date (date_id, full_date, day, month, month_name, year, day_of_week, is_weekend)
            VALUES (
                @today_id,
                CAST(GETDATE() AS DATE),
                DAY(GETDATE()),
                MONTH(GETDATE()),
                DATENAME(MONTH, GETDATE()),
                YEAR(GETDATE()),
                DATENAME(WEEKDAY, GETDATE()),
                CASE WHEN DATENAME(WEEKDAY, GETDATE()) IN ('Saturday', 'Sunday') THEN 1 ELSE 0 END
            );
        END

        INSERT INTO Fact_Sales (date_id, product_id, customer_id, quantity, unit_price, payment_method)
        VALUES (@today_id, @product_id, @customer_id, @quantity, @unit_price, @payment_method);

        EXEC ReduceStock @ProductId = @product_id, @QuantitySold = @quantity;

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH

        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;

    END CATCH
END;

CREATE PROCEDURE InsertExpense
    @category_name VARCHAR(50),
    @product_name VARCHAR(150),
    @supplier_email VARCHAR(150),
    @description VARCHAR(200),
    @amount DECIMAL(10,2),
    @payment_method VARCHAR(30),
    @quantity INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @category_id INT;
    DECLARE @product_id INT;
    DECLARE @supplier_id INT;
    DECLARE @today_id INT;

    BEGIN TRY

        IF @quantity <= 0
        BEGIN
            THROW 50001, 'La cantidad debe ser mayor a cero.', 1;
        END

        IF @amount < 0
        BEGIN
            THROW 50002, 'El monto no puede ser negativo.', 1;
        END

        SET @today_id = CAST(FORMAT(GETDATE(), 'yyyyMMdd') AS INT);

        BEGIN TRANSACTION;

        IF NOT EXISTS (SELECT 1 FROM Dim_Date WHERE date_id = @today_id)
        BEGIN
            INSERT INTO Dim_Date (date_id, full_date, day, month, month_name, year, day_of_week, is_weekend)
            VALUES (
                @today_id,
                CAST(GETDATE() AS DATE),
                DAY(GETDATE()),
                MONTH(GETDATE()),
                DATENAME(MONTH, GETDATE()),
                YEAR(GETDATE()),
                DATENAME(WEEKDAY, GETDATE()),
                CASE WHEN DATENAME(WEEKDAY, GETDATE()) IN ('Saturday', 'Sunday') THEN 1 ELSE 0 END
            );
        END

        IF NOT EXISTS (SELECT 1 FROM dbo.Dim_ExpenseCategory c WHERE c.category_name = @category_name)
        BEGIN
            INSERT INTO dbo.Dim_ExpenseCategory (category_name) VALUES (@category_name);
            SET @category_id = SCOPE_IDENTITY();
        END
        ELSE
        BEGIN
            SELECT @category_id = c.category_id FROM dbo.Dim_ExpenseCategory c WHERE c.category_name = @category_name;
        END

        SELECT @product_id = p.product_id FROM dbo.Dim_Product p WHERE p.product_name = @product_name;
        SELECT @supplier_id = s.supplier_id FROM dbo.Dim_Supplier s WHERE s.email = @supplier_email;

        INSERT INTO dbo.Fact_MaterialExpenses (date_id, category_id, product_id, supplier_id, description, amount, payment_method, quantity)
        VALUES (@today_id, @category_id, @product_id, @supplier_id, @description, @amount, @payment_method, @quantity);

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH

        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;

    END CATCH
END;

CREATE PROCEDURE InsertProductAndCreateInventory
    @product_name VARCHAR(150),
    @category VARCHAR(80),
    @unit_of_measure VARCHAR(20),
    @unit_price DECIMAL(10,2)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @product_id INT;

    BEGIN TRY

        BEGIN TRANSACTION;

        INSERT INTO Dim_Product (product_name, category, unit_of_measure, unit_price)
        VALUES (@product_name, @category, @unit_of_measure, @unit_price);

        SET @product_id = SCOPE_IDENTITY();

        INSERT INTO Inventory (product_id, quantity_on_hand, minimum_stock)
        VALUES (@product_id, 0, 0);

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH

        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;

    END CATCH
END;
