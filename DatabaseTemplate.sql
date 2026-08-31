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