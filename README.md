# Business Sales Core

Internal management system for a small/medium business: sales, expenses, inventory, and supplier/customer records, managed through a JavaFX desktop interface connected to an Azure SQL database. It also includes a console fallback, automated notifications via WhatsApp and email, ad-hoc SQL querying, Excel export, and a Docker-based run flow.

## What does this project do?

It's a Java desktop application built so the business owner/admin can use the graphical interface to:

- Log in securely before accessing any functionality (limited login attempts).
- Record sales tied to a product and, optionally, a customer — regardless of payment method (cash, SINPE, card).
- Log business expenses and supplier purchases in a single place (materials, fuel, utilities, etc.), optionally linked to a product and/or supplier.
- Keep inventory in sync automatically: every sale reduces stock, with validation to prevent overselling.
- Query, update, and delete sales, expenses, products, suppliers, and inventory records.
- Send notifications (e.g. low-stock alerts, reminders) via WhatsApp and/or email.
- Run ad-hoc SQL queries from the interface or the console, with destructive commands blocked by default.
- Export any query result to a formatted Excel file, or toggle automatic Excel export for every `SELECT` run for the rest of the session.
- Wipe all table data (with confirmation) to reset the database without dropping its structure.

**Why build this instead of using a commercial ERP?** To have full control over the data, no recurring subscription cost, and exactly the features the business needs — nothing more. The trade-off is that maintenance and support are on us. See the [Considerations](#considerations-and-limitations) section below.

## Tech stack

| Component | Tool |
|---|---|
| Language | Java 21 |
| Build / dependencies | Maven, packaged as a fat jar via `maven-shade-plugin` |
| Desktop interface | JavaFX + FXML |
| Containerization | Docker (multi-stage build) |
| Database | Azure SQL Database (relational) |
| Connection driver | `mssql-jdbc` (Microsoft JDBC Driver for SQL Server) |
| Environment variables | `dotenv-java` (reads a local `.env`, no Spring) |
| Password hashing | BCrypt |
| WhatsApp notifications | Green API (`whatsapp-api-client-java`) |
| Email notifications | Jakarta Mail API + Eclipse Angus Mail (implementation) |
| Console table formatting | AsciiTable (`de.vandermeer:asciitable`) |
| Excel export | Apache POI (`poi-ooxml`) |
| Cloud management | Azure CLI |

## Project structure

Organized by business domain, one package per area:

```
src/main/java/io/github/joshua/
├── admin/            → SqlConsoleService (ad-hoc SQL), DatabaseResetService (wipe all tables)
├── costs/            → BusinessExpense (business expenses & supplier purchases)
├── customer/         → CustomerService (customer records)
├── database/         → DBConnection (connection management)
├── expensecategory/  → ExpenseCategory (expense categories)
├── inventory/        → InventoryManager (stock management)
├── notification/     → NotificationSender (interface), WhatsAppNotificationSender, EmailNotificationSender
├── product/          → ProductService (insert/update/delete products, auto-creates inventory row)
├── sales/            → SalesService (insert/query/delete sales)
├── supplier/         → SupplierService (insert/query/update/delete suppliers)
├── ui/               → LoginController, DashboardController, FXML views and CSS
├── user/             → UserAuth, UserMenu
├── util/             → AppConfig, AppSettings, ExcelExportService, query result helpers
└── Main.java           → application entry point
```

**Pattern followed in each domain:**
- A **service** class per domain (business logic + database access), most of it delegating to SQL Server **stored procedures** rather than building `INSERT`/`UPDATE` statements ad-hoc in Java.
- Plain model classes are only introduced where the app actually needs to pass a fully-assembled object between methods (e.g. `Receipt`); domains that only insert/query loose fields (like `Product`, `Supplier`) skip the model class to avoid unnecessary layers.
- For notifications specifically, a shared `NotificationSender` interface with one implementation per channel (WhatsApp, email), so the rest of the app can send a notification without knowing which channel handles it.
- `util/` holds cross-cutting helpers used by any domain: pretty-printing a `ResultSet` to the console, exporting a `ResultSet` to Excel, and a small in-memory settings toggle (`AppSettings`) for session-wide behavior like "export every SELECT to Excel".

## Database design

The schema is a **fact constellation** (galaxy schema): multiple fact tables share the same dimension tables, rather than each fact having its own private dimensions as in a plain star schema.

**Dimensions:**
- `Dim_Date` — standard date dimension (day, month, year, day of week, etc.). New rows are created on demand by the stored procedures whenever a sale or expense happens on a date not yet in the table.
- `Dim_Product` — product catalog (name, category, unit price, unit of measure).
- `Dim_Customer` — customer directory (name, phone, email). Nullable on sales, since not every sale has an identified customer (e.g. walk-in sales).
- `Dim_Supplier` — supplier directory (name, contact, phone, email). Nullable on expenses, since not every expense has a formal supplier (e.g. fuel).
- `Dim_ExpenseCategory` — expense categories (material, fuel, utilities, rent, other).

**Facts:**
- `Fact_Sales` — one row per sale (product, optional customer, quantity, unit price, payment method). `total_price` is a computed, persisted column.
- `Fact_MaterialExpenses` — one row per expense or supplier purchase (category, optional product, optional supplier, quantity, description, amount, payment method). This single table covers both formal supplier purchases and informal operating expenses — the difference is just whether `supplier_id`/`product_id` are populated.

**Supporting table:**
- `Inventory` — current stock snapshot per product (`quantity_on_hand`, `minimum_stock`), kept separate from `Dim_Product` since stock changes far more often than product details. A row is created automatically whenever a new product is inserted.

`Dim_Date` and `Dim_Product` are shared by both fact tables — that shared usage is what makes this a constellation rather than a single star. A full ER diagram of the schema is included in the repo (`business_schema.drawio`, viewable at [app.diagrams.net](https://app.diagrams.net)).

### Indexes

Beyond the primary keys (which get a clustered index automatically), non-clustered indexes are added on the foreign key columns and on the columns most frequently filtered by, since SQL Server does not index foreign keys automatically:

```sql
CREATE INDEX IX_Product_Name ON Dim_Product(product_name);
CREATE INDEX IX_Customer_Email ON Dim_Customer(email);
CREATE INDEX IX_Supplier_Email ON Dim_Supplier(email);

CREATE INDEX IX_Sales_ProductId ON Fact_Sales(product_id);
CREATE INDEX IX_Sales_CustomerId ON Fact_Sales(customer_id);
CREATE INDEX IX_Sales_DateId ON Fact_Sales(date_id);

CREATE INDEX IX_Expenses_ProductId ON Fact_MaterialExpenses(product_id);
CREATE INDEX IX_Expenses_SupplierId ON Fact_MaterialExpenses(supplier_id);
CREATE INDEX IX_Expenses_CategoryId ON Fact_MaterialExpenses(category_id);
CREATE INDEX IX_Expenses_DateId ON Fact_MaterialExpenses(date_id);
```

## Business logic in stored procedures

Rather than building multi-step inserts/updates in Java, the core write operations live in SQL Server stored procedures, called from Java via `CallableStatement`. Each one validates its inputs and wraps its writes in a transaction, rolling back on any failure:

- **`InsertSale`** — looks up the product and (optional) customer, creates today's row in `Dim_Date` if missing, inserts the sale, and calls `ReduceStock` — all inside one transaction. Fails with a clear error if the product doesn't exist, the quantity is invalid, or stock runs out.
- **`ReduceStock`** — validates that enough stock exists before deducting it; throws if the product has no inventory row or if the requested quantity exceeds what's on hand.
- **`InsertExpense`** — looks up (or creates) the expense category, looks up the optional product/supplier, creates today's `Dim_Date` row if missing, and inserts the expense.
- **`InsertProductAndCreateInventory`** — inserts a new product and its corresponding `Inventory` row (starting at zero stock) in a single transaction, so a product can never exist without an inventory record.

Validation failures use `THROW` (not `PRINT`), so they surface to Java as a real `SQLException` that the calling service can catch and show to the user — rather than silently failing.

## Notifications

The app can send notifications (e.g. low-stock alerts) through two channels, both implementing the same `NotificationSender` interface:

- **WhatsApp**, via [Green API](https://green-api.com) — connects to your own WhatsApp number by scanning a QR code (same mechanism as WhatsApp Web), no Meta Business verification required.
- **Email**, via Gmail SMTP using Jakarta Mail — requires a Gmail app password (not your regular password).

### WhatsApp setup (Green API)

1. Create a free developer account at [green-api.com](https://green-api.com) and create a new instance.
2. From your instance dashboard, copy the `idInstance` and `apiTokenInstance`.
3. Generate the QR code from the dashboard and scan it from your phone: WhatsApp → Settings → **Linked Devices** → **Link a Device**.
4. Once linked, the instance status should show `authorized` — it's ready to send messages from code.
5. Recipients are addressed as `<countrycode><number>@c.us` for an individual chat (e.g. `50671413393@c.us`), or `<group_id>@g.us` for a group.

### Email setup (Gmail)

1. Enable **2-Step Verification** on your Google account, if not already active.
2. Go to [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords) and generate an app password.
3. Copy the 16-character password **without spaces** — that's what goes in `.env`, not your regular Gmail password.

## Administration tools

Available from the dashboard after logging in, and through the console fallback where noted:

- **Ad-hoc SQL queries** (`SqlConsoleService`) — run any `SELECT` and see it pretty-printed to the console (or exported to Excel, see below). `DROP`, `TRUNCATE`, and `DELETE` are blocked from this option as a safety net.
- **Toggle Excel export** (`AppSettings`) — an in-memory, session-wide switch: once turned on, every `SELECT` result (from the SQL console or from other queries in the app) is written to an `.xlsx` file under `exports/` instead of being printed, until turned off again. This resets to off every time the program restarts.
- **Wipe database** (`DatabaseResetService`) — deletes all rows from every table (structure and stored procedures are kept) and resets identity counters. Requires typing `CONFIRMAR` to proceed, since it's irreversible.

Excel exports are written to an `exports/` folder at the project root (auto-created if missing, and excluded from git), with filenames timestamped to avoid overwriting previous exports.

## Local setup

### 1. Environment variables

Create a `.env` file at the project root (never committed to git — already in `.gitignore`):

```
# Database
DB_URL=jdbc:sqlserver://joshuaroot.database.windows.net:1433
DB_NAME=business-sales-db
DB_USER=rootjoshua
DB_PASSWORD=your_password_here
APP_USER=admin
APP_PASSWORD=change_this_password

# WhatsApp (Green API)
GREENAPI_INSTANCE_ID=your_instance_id
GREENAPI_TOKEN=your_api_token

# Email (Gmail)
GMAIL_ADDRESS=your_email@gmail.com
GMAIL_APP_PASSWORD=your16charapppassword
```

If you want anyone cloning the repo to know which variables to set, keep a versioned `.env.example` (with no real values).

### 2. Azure SQL Database

- Database: `business-sales-db`
- Server: `joshuaroot.database.windows.net` (West US region, General Purpose - Serverless tier, within Azure's free tier)
- Authentication: SQL Authentication (username/password, not Microsoft Entra)
- Run the table and stored procedure scripts in order: `Dim_Date`, `Dim_Product`, `Dim_Customer`, `Dim_Supplier`, `Dim_ExpenseCategory`, `Fact_Sales`, `Fact_MaterialExpenses`, `Inventory`, then `ReduceStock`, `InsertSale`, `InsertExpense`, `InsertProductAndCreateInventory`, then the indexes above.

### 3. Azure CLI (for the firewall step)

Azure SQL only accepts connections from authorized IPs. Since many residential connections have a dynamic IP, `run.sh` (see below) checks and updates the firewall rule automatically every time you run the project.

**Install Azure CLI** (one-time):
```bash
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
```

**Log in** (once per work session, or whenever the token expires):
```bash
az login
```

## Docker

The app is packaged as a fat jar (`maven-shade-plugin`, all dependencies bundled, `io.github.joshua.Main` set as the entry point) and built into a Docker image via a multi-stage build (`maven:3.9-eclipse-temurin-21` to compile, `eclipse-temurin:21-jre` to run).

Because the app is interactive (reads username/password and menu choices from `System.in`), it's always run with `-it`. `.env` is never baked into the image — it's supplied at container start, and `exports/` is mounted as a volume so Excel files generated inside the container land on the host filesystem.

### Desktop GUI (JavaFX + FXML)

The default entry point now opens the JavaFX desktop panel. The layouts live in
`src/main/resources/io/github/joshua/ui/` and can be opened directly in Scene Builder:

- `Login.fxml` — administrator login screen.
- `Dashboard.fxml` — navigation, business metrics, and database tables.

Create or update `.env` with `APP_USER` and `APP_PASSWORD`, then run:

```bash
./run.sh gui
```

You can also start the GUI directly with `mvn clean javafx:run`.

After logging in, use the left navigation to load each database table. The
`Actualizar` button reloads the current view. The window opens even when Azure
SQL is unavailable; the connection badge reports the problem instead of hiding
it.

The GUI also includes the terminal administration workflows:

- Create, edit and delete records for products, customers, suppliers, categories and inventory.
- Register sales and expenses through the existing `InsertSale` and `InsertExpense` procedures.
- Delete sales and expenses from their selected rows.
- Export the active table or custom query results to `exports/*.xlsx`.
- Enable automatic Excel export for custom `SELECT` queries.
- Execute custom SQL while destructive `DROP`, `TRUNCATE` and `DELETE` statements remain blocked.
- Empty the database from the dedicated action, requiring the exact confirmation `CONFIRMAR`.

To test the GUI manually:

1. Confirm that `.env` contains valid `APP_USER`, `APP_PASSWORD`, and database values.
2. Run `mvn clean javafx:run`.
3. Log in with those credentials.
4. Confirm that the connection badge becomes `Base de datos conectada`.
5. Open Productos, Clientes, Inventario, Ventas, and Gastos and press `Actualizar`.
6. Change one record through the existing console workflow or SQL tool, then refresh the corresponding GUI view and verify the new value appears.

Scene Builder can preview either FXML file without starting the database. Use the
FXML controller names already declared in each file; no generated controller code
is required.

### Console fallback

The original interactive menu remains available for deployments or operations
that still require the console:

```bash
./run.sh console
```

The console mode builds and starts the Docker image with `--console`.

## Running the project

The default mode launches the JavaFX desktop interface. The script also checks and updates the Azure SQL firewall rule before starting the application:

```bash
./run.sh
```

To use the legacy console workflow inside Docker instead:

```bash
./run.sh console
```

**What `run.sh` does in GUI mode:**

1. Gets the machine's current public IP (`curl ifconfig.me`).
2. Updates the Azure SQL `MiPC` firewall rule when the IP has changed.
3. Starts the JavaFX application with `mvn clean javafx:run`.

In console mode, the script builds the Docker image, creates the local `exports/` folder, and runs the container interactively with `.env` and `exports/` mounted.

Enter the admin username and password when prompted (3-attempt limit before the program closes).

## Considerations and limitations

- This system **does not replace** a commercial ERP's advanced features (tax compliance, multi-branch support, granular permissions) — it's a tailored solution for the business's current needs.
- The database connection depends on having the IP authorized in the Azure SQL firewall; in production this would be solved by locking access to a single machine or backend, rather than changing personal IPs. `run.sh` automates the check/update, but still requires a valid `az login` session on the host.
- Azure SQL's free Serverless tier auto-pauses once the monthly usage limit is exceeded — no charges are incurred, but the database becomes inaccessible until the next month unless overage billing is enabled.
- WhatsApp notifications depend on the linked phone staying online and connected to the internet (same requirement as WhatsApp Web). Green API is an unofficial bridge (not Meta's Cloud API), so it carries some risk of the number being flagged for automated behavior if used at high volume.
- Email sending depends on the Gmail app password remaining valid; if 2-Step Verification is disabled or the app password is revoked, `EmailNotificationSender` will fail authentication.
- The ad-hoc SQL console and database wipe are powerful, low-guardrail tools by design (meant for a single trusted admin); they should stay behind the login and not be exposed to untrusted users.
- The Azure CLI step runs on the host, not inside the container — Docker does not solve the dynamic-IP problem, since the container's outbound traffic still goes through the host's network connection.