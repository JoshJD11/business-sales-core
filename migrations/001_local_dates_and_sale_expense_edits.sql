-- Migration 001: local (Costa Rica) dates, sale/expense editing, stock-safe sale deletion.
-- Safe to run more than once (CREATE OR ALTER). Batches are separated by GO.

-- Creates today's Dim_Date row (Costa Rica time, not the server's UTC clock) and returns its id.
CREATE OR ALTER PROCEDURE EnsureTodayDate
    @today_id INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @today DATE = CAST(SYSDATETIMEOFFSET() AT TIME ZONE 'Central America Standard Time' AS DATE);
    SET @today_id = CAST(FORMAT(@today, 'yyyyMMdd') AS INT);

    IF NOT EXISTS (SELECT 1 FROM Dim_Date WHERE date_id = @today_id)
    BEGIN
        INSERT INTO Dim_Date (date_id, full_date, day, month, month_name, year, day_of_week, is_weekend)
        VALUES (
            @today_id,
            @today,
            DAY(@today),
            MONTH(@today),
            DATENAME(MONTH, @today),
            YEAR(@today),
            DATENAME(WEEKDAY, @today),
            CASE WHEN DATENAME(WEEKDAY, @today) IN ('Saturday', 'Sunday') THEN 1 ELSE 0 END
        );
    END
END;
GO

CREATE OR ALTER PROCEDURE InsertSale
    @product_name VARCHAR(150),
    @quantity INT,
    @payment_method VARCHAR(30),
    @customer_email VARCHAR(150)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @product_id INT;
    DECLARE @unit_price DECIMAL(10,2);
    DECLARE @customer_id INT;
    DECLARE @today_id INT;

    BEGIN TRY

        IF @quantity <= 0
            THROW 50001, 'La cantidad debe ser mayor a cero.', 1;

        SELECT @product_id = p.product_id, @unit_price = p.unit_price
        FROM dbo.Dim_Product p
        WHERE p.product_name = @product_name;

        IF @product_id IS NULL
            THROW 50002, 'Producto no encontrado.', 1;

        IF @customer_email IS NOT NULL
        BEGIN
            SELECT @customer_id = c.customer_id FROM dbo.Dim_Customer c WHERE c.email = @customer_email;
            IF @customer_id IS NULL
                THROW 50006, 'No existe un cliente con ese correo.', 1;
        END

        BEGIN TRANSACTION;

        EXEC EnsureTodayDate @today_id = @today_id OUTPUT;

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
GO

-- Edits a sale and keeps inventory consistent: returns the old quantity to stock, then takes the new one.
CREATE OR ALTER PROCEDURE UpdateSale
    @sale_id INT,
    @product_name VARCHAR(150),
    @quantity INT,
    @payment_method VARCHAR(30),
    @customer_email VARCHAR(150)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @product_id INT;
    DECLARE @unit_price DECIMAL(10,2);
    DECLARE @customer_id INT;
    DECLARE @old_product_id INT;
    DECLARE @old_quantity INT;
    DECLARE @old_unit_price DECIMAL(10,2);
    DECLARE @message NVARCHAR(200);

    BEGIN TRY

        IF @quantity <= 0
            THROW 50001, 'La cantidad debe ser mayor a cero.', 1;

        SELECT @product_id = p.product_id, @unit_price = p.unit_price
        FROM dbo.Dim_Product p
        WHERE p.product_name = @product_name;

        IF @product_id IS NULL
            THROW 50002, 'Producto no encontrado.', 1;

        IF @customer_email IS NOT NULL
        BEGIN
            SELECT @customer_id = c.customer_id FROM dbo.Dim_Customer c WHERE c.email = @customer_email;
            IF @customer_id IS NULL
                THROW 50006, 'No existe un cliente con ese correo.', 1;
        END

        BEGIN TRANSACTION;

        SELECT @old_product_id = product_id, @old_quantity = quantity, @old_unit_price = unit_price
        FROM Fact_Sales WITH (UPDLOCK)
        WHERE sale_id = @sale_id;

        IF @old_product_id IS NULL
        BEGIN
            SET @message = CONCAT('No se encontró ninguna venta con el ID: ', @sale_id);
            THROW 50007, @message, 1;
        END

        -- Same product: keep the price the sale was originally made at.
        IF @old_product_id = @product_id
            SET @unit_price = @old_unit_price;

        UPDATE Inventory
        SET quantity_on_hand = quantity_on_hand + @old_quantity,
            last_updated = GETDATE()
        WHERE product_id = @old_product_id;

        EXEC ReduceStock @ProductId = @product_id, @QuantitySold = @quantity;

        UPDATE Fact_Sales
        SET product_id = @product_id,
            customer_id = @customer_id,
            quantity = @quantity,
            unit_price = @unit_price,
            payment_method = @payment_method
        WHERE sale_id = @sale_id;

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- Deletes a sale and returns its quantity to inventory.
CREATE OR ALTER PROCEDURE DeleteSale
    @sale_id INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @product_id INT;
    DECLARE @quantity INT;
    DECLARE @message NVARCHAR(200);

    BEGIN TRY

        BEGIN TRANSACTION;

        SELECT @product_id = product_id, @quantity = quantity
        FROM Fact_Sales WITH (UPDLOCK)
        WHERE sale_id = @sale_id;

        IF @product_id IS NULL
        BEGIN
            SET @message = CONCAT('No se encontró ninguna venta con el ID: ', @sale_id);
            THROW 50007, @message, 1;
        END

        DELETE FROM Fact_Sales WHERE sale_id = @sale_id;

        UPDATE Inventory
        SET quantity_on_hand = quantity_on_hand + @quantity,
            last_updated = GETDATE()
        WHERE product_id = @product_id;

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE InsertExpense
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

        IF @category_name IS NULL OR LTRIM(RTRIM(@category_name)) = ''
            THROW 50003, 'La categoría es obligatoria.', 1;

        IF @quantity <= 0
            THROW 50001, 'La cantidad debe ser mayor a cero.', 1;

        IF @amount < 0
            THROW 50002, 'El monto no puede ser negativo.', 1;

        IF @product_name IS NOT NULL
        BEGIN
            SELECT @product_id = p.product_id FROM dbo.Dim_Product p WHERE p.product_name = @product_name;
            IF @product_id IS NULL
                THROW 50004, 'Producto no encontrado.', 1;
        END

        IF @supplier_email IS NOT NULL
        BEGIN
            SELECT @supplier_id = s.supplier_id FROM dbo.Dim_Supplier s WHERE s.email = @supplier_email;
            IF @supplier_id IS NULL
                THROW 50005, 'No existe un proveedor con ese correo.', 1;
        END

        BEGIN TRANSACTION;

        EXEC EnsureTodayDate @today_id = @today_id OUTPUT;

        SELECT @category_id = c.category_id FROM dbo.Dim_ExpenseCategory c WHERE c.category_name = @category_name;
        IF @category_id IS NULL
        BEGIN
            INSERT INTO dbo.Dim_ExpenseCategory (category_name) VALUES (@category_name);
            SET @category_id = SCOPE_IDENTITY();
        END

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
GO

CREATE OR ALTER PROCEDURE UpdateExpense
    @expense_id INT,
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
    DECLARE @message NVARCHAR(200);

    BEGIN TRY

        IF @category_name IS NULL OR LTRIM(RTRIM(@category_name)) = ''
            THROW 50003, 'La categoría es obligatoria.', 1;

        IF @quantity <= 0
            THROW 50001, 'La cantidad debe ser mayor a cero.', 1;

        IF @amount < 0
            THROW 50002, 'El monto no puede ser negativo.', 1;

        IF @product_name IS NOT NULL
        BEGIN
            SELECT @product_id = p.product_id FROM dbo.Dim_Product p WHERE p.product_name = @product_name;
            IF @product_id IS NULL
                THROW 50004, 'Producto no encontrado.', 1;
        END

        IF @supplier_email IS NOT NULL
        BEGIN
            SELECT @supplier_id = s.supplier_id FROM dbo.Dim_Supplier s WHERE s.email = @supplier_email;
            IF @supplier_id IS NULL
                THROW 50005, 'No existe un proveedor con ese correo.', 1;
        END

        BEGIN TRANSACTION;

        IF NOT EXISTS (SELECT 1 FROM dbo.Fact_MaterialExpenses WITH (UPDLOCK) WHERE expense_id = @expense_id)
        BEGIN
            SET @message = CONCAT('No se encontró el gasto con ID: ', @expense_id);
            THROW 50008, @message, 1;
        END

        SELECT @category_id = c.category_id FROM dbo.Dim_ExpenseCategory c WHERE c.category_name = @category_name;
        IF @category_id IS NULL
        BEGIN
            INSERT INTO dbo.Dim_ExpenseCategory (category_name) VALUES (@category_name);
            SET @category_id = SCOPE_IDENTITY();
        END

        UPDATE dbo.Fact_MaterialExpenses
        SET category_id = @category_id,
            product_id = @product_id,
            supplier_id = @supplier_id,
            description = @description,
            amount = @amount,
            payment_method = @payment_method,
            quantity = @quantity
        WHERE expense_id = @expense_id;

        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO
