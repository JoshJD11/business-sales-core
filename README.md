# Business Sales Core

Internal management system for a small/medium business: sales, inventory, expenses, production, and receipts, handled through a Java admin console connected to an Azure SQL database.

## What does this project do?

It's a command-line application (no GUI, for now) built so the business owner/admin can:

- Log in securely before accessing any functionality.
- Record sales, regardless of payment method (cash, SINPE, card).
- Keep inventory up to date: deduct stock, get low-stock alerts, generate reorder lists.
- Log business expenses (materials, fuel, utilities, etc.).
- Track production times, to calculate averages and estimate delivery times.
- Generate purchase receipts for customers.
- Export reports to Excel for analysis in Power BI.

**Why build this instead of using a commercial ERP?** To have full control over the data, no recurring subscription cost, and exactly the features the business needs — nothing more. The trade-off is that maintenance and support are on us. See the [Considerations](#considerations-and-limitations) section below.

## Tech stack

| Component | Tool |
|---|---|
| Language | Java 17 |
| Build / dependencies | Maven |
| Database | Azure SQL Database (relational) |
| Connection driver | `mssql-jdbc` (Microsoft JDBC Driver for SQL Server) |
| Environment variables | `dotenv-java` (reads a local `.env`, no Spring) |
| Password hashing | BCrypt |
| Cloud management | Azure CLI |

## Project structure

Organized by business domain, one package per area:

```
src/main/java/io/github/joshua/
├── costs/          → BusinessExpense (business expenses)
├── database/         → DBConnection, EnvConfig (connection and configuration)
├── inventory/         → InventoryManager, InventoryReportService
├── production/        → ProductionRecord, ProductionService
├── receipt/           → ReceiptService
├── user/              → UserAuth, UserMenu
└── Main.java            → application entry point
```

**Pattern followed in each domain:**
- A **model** class (plain data: `Product`, `Sale`, `MaterialExpense`).
- A **service** class (business logic + database access).
- Where it applies, a separate class for **exporting/formatting** output (e.g. receipts, reports), so "what data is fetched" stays separate from "how it's displayed".

## Database design

Core tables:

- `customers`
- `products` / `inventory`
- `sales` + `sale_details` (junction table for multiple products per sale)
- `expenses` (with a `category` field: material, fuel, utilities, rent, etc.)
- `production` (real time-per-batch/product records, used to calculate averages)
- `users` (admin login, with password hashed via BCrypt)

Every sale and every expense includes a `payment_method` field (SINPE, cash, card) for traceability, without that affecting how a receipt or report is generated.

## Local setup

### 1. Environment variables

Create a `.env` file at the project root (never committed to git — already in `.gitignore`):

```
DB_URL=jdbc:sqlserver://joshuaroot.database.windows.net:1433
DB_NAME=business-sales-db
DB_USER=rootjoshua
DB_PASSWORD=your_password_here
```

If you want anyone cloning the repo to know which variables to set, keep a versioned `.env.example` (with no real values).

### 2. Azure SQL Database

- Database: `business-sales-db`
- Server: `joshuaroot.database.windows.net` (West US region, General Purpose - Serverless tier, within Azure's free tier)
- Authentication: SQL Authentication (username/password, not Microsoft Entra)

### 3. Azure SQL firewall

Azure SQL only accepts connections from authorized IPs. Since many residential connections have a dynamic IP, you'll need to re-authorize it whenever it changes.

**Install Azure CLI** (one-time):
```bash
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
```

**Log in** (once per work session, or whenever the token expires):
```bash
az login
```

**Update the firewall rule with the current IP** (whenever you switch networks: home, university, coffee shop, etc.):
```bash
./update-firewall.sh
```

This script runs the following internally:
```bash
az sql server firewall-rule create \
  --resource-group business-sales-rg \
  --server joshuaroot \
  --name MyPC \
  --start-ip-address $(curl -s ifconfig.me) \
  --end-ip-address $(curl -s ifconfig.me)
```

The change can take up to 5 minutes to take effect.

## How to run the project

1. Install Azure CLI and authenticate (`az login`) — only needed the first time or if the session expires.
2. Create the `.env` file with the database credentials (see above).
3. Run `./update-firewall.sh` to authorize your current IP.
4. Compile and run:
   ```bash
   mvn compile
   mvn exec:java -Dexec.mainClass="io.github.joshua.Main"
   ```
   (or run `Main.java` directly from your IDE)
5. Enter the admin username and password when prompted (3-attempt limit before the program closes).

## Considerations and limitations

- This system **does not replace** a commercial ERP's advanced features (tax compliance, multi-branch support, granular permissions) — it's a tailored solution for the business's current needs.
- The database connection depends on having the IP authorized in the Azure SQL firewall; in production this would be solved by locking access to a single machine or backend, rather than changing personal IPs.
- Azure SQL's free Serverless tier auto-pauses once the monthly usage limit is exceeded — no charges are incurred, but the database becomes inaccessible until the next month unless overage billing is enabled.