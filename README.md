# Business Sales Core

Internal management system for a small/medium business: sales, inventory, expenses, production, and receipts, handled through a Java admin console connected to an Azure SQL database. Includes automated notifications via WhatsApp and email.

## What does this project do?

It's a command-line application (no GUI, for now) built so the business owner/admin can:

- Log in securely before accessing any functionality.
- Record sales, regardless of payment method (cash, SINPE, card).
- Keep inventory up to date: deduct stock, get low-stock alerts, generate reorder lists.
- Log business expenses (materials, fuel, utilities, etc.).
- Track production times, to calculate averages and estimate delivery times.
- Generate purchase receipts for customers.
- Send notifications (low-stock alerts, receipts, reminders) via WhatsApp and/or email.
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
| WhatsApp notifications | Green API (`whatsapp-api-client-java`) |
| Email notifications | Jakarta Mail API + Eclipse Angus Mail (implementation) |
| Cloud management | Azure CLI |

## Project structure

Organized by business domain, one package per area:

```
src/main/java/io/github/joshua/
├── costs/          → BusinessExpense (business expenses)
├── database/         → DBConnection, EnvConfig (connection and configuration)
├── inventory/         → InventoryManager, InventoryReportService
├── notification/      → NotificationSender (interface), WhatsAppNotificationSender, EmailNotificationSender
├── production/        → ProductionRecord, ProductionService
├── receipt/           → ReceiptService
├── user/              → UserAuth, UserMenu
└── Main.java            → application entry point
```

**Pattern followed in each domain:**
- A **model** class (plain data: `Product`, `Sale`, `MaterialExpense`).
- A **service** class (business logic + database access).
- Where it applies, a separate class for **exporting/formatting** output (e.g. receipts, reports), so "what data is fetched" stays separate from "how it's displayed".
- For notifications specifically, a shared `NotificationSender` interface with one implementation per channel (WhatsApp, email), so the rest of the app can send a notification without knowing which channel handles it.

## Database design

The schema is a **fact constellation** (galaxy schema): two fact tables share the same dimension tables, rather than each fact having its own private dimensions as in a plain star schema.

**Dimensions:**
- `Dim_Date` — standard date dimension (day, month, year, day of week, etc.)
- `Dim_Product` — product catalog (name, category, unit price, unit of measure)
- `Dim_ExpenseCategory` — expense categories (material, fuel, utilities, rent, other)

**Facts:**
- `Fact_Purchases` — one row per purchased line item (product, quantity, unit cost, supplier, payment method)
- `Fact_MaterialExpenses` — broader expense log (fuel, utilities, small material buys), optionally linked to a product

**Supporting table:**
- `Inventory` — current stock snapshot per product (`quantity_on_hand`, `minimum_stock`), updated whenever a purchase or sale changes stock. Kept separate from `Dim_Product` since stock changes far more often than product details.

`Dim_Date` and `Dim_Product` are shared by both fact tables — that shared usage is what makes this a constellation rather than a single star. A full ER diagram of the schema is included in the repo (`business_schema.drawio`, viewable at [app.diagrams.net](https://app.diagrams.net)).

Every purchase and every expense includes a `payment_method` field (SINPE, cash, card) for traceability, without that affecting how a receipt or report is generated.

## Notifications

The app can send automated notifications (low-stock alerts, receipts, reminders) through two channels, both implementing the same `NotificationSender` interface:

- **WhatsApp**, via [Green API](https://green-api.com) — connects to your own WhatsApp number by scanning a QR code (same mechanism as WhatsApp Web), no Meta Business verification required.
- **Email**, via Gmail SMTP using Jakarta Mail — requires a Gmail app password (not your regular password).

### WhatsApp setup (Green API)

1. Create a free developer account at [green-api.com](https://green-api.com) and create a new instance.
2. From your instance dashboard, copy the `idInstance` and `apiTokenInstance`.
3. Generate the QR code from the dashboard and scan it from your phone: WhatsApp → Settings → **Linked Devices** → **Link a Device**.
4. Once linked, the instance status should show `authorized` — it's ready to send messages from code.

### Email setup (Gmail)

1. Enable **2-Step Verification** on your Google account, if not already active.
2. Go to [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords) and generate an app password.
3. Copy the 16-character password **without spaces** — that's what goes in `.env`, not your regular Gmail password.

## Local setup

### 1. Environment variables

Create a `.env` file at the project root (never committed to git — already in `.gitignore`):

```
# Database
DB_URL=jdbc:sqlserver://joshuaroot.database.windows.net:1433
DB_NAME=business-sales-db
DB_USER=rootjoshua
DB_PASSWORD=your_password_here

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
2. Create the `.env` file with the database, WhatsApp, and email credentials (see above).
3. Run `./update-firewall.sh` to authorize your current IP.
4. Make sure your WhatsApp number is linked in Green API (status `authorized`).
5. Compile and run:
   ```bash
   mvn compile
   mvn exec:java -Dexec.mainClass="io.github.joshua.Main"
   ```
   (or run `Main.java` directly from your IDE)
6. Enter the admin username and password when prompted (3-attempt limit before the program closes).

## Considerations and limitations

- This system **does not replace** a commercial ERP's advanced features (tax compliance, multi-branch support, granular permissions) — it's a tailored solution for the business's current needs.
- The database connection depends on having the IP authorized in the Azure SQL firewall; in production this would be solved by locking access to a single machine or backend, rather than changing personal IPs.
- Azure SQL's free Serverless tier auto-pauses once the monthly usage limit is exceeded — no charges are incurred, but the database becomes inaccessible until the next month unless overage billing is enabled.
- WhatsApp notifications depend on the linked phone staying online and connected to the internet (same requirement as WhatsApp Web). Green API is an unofficial bridge (not Meta's Cloud API), so it carries some risk of the number being flagged for automated behavior if used at high volume.
- Email sending depends on the Gmail app password remaining valid; if 2-Step Verification is disabled or the app password is revoked, `EmailNotificationSender` will fail authentication.