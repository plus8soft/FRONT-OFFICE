# Integration Points & Database Architecture

Short guide for adding integrations and understanding the current structure. For the high-level "what works / what is stubbed" overview, see [README.md](README.md). For a detailed feature list with screenshots, see [FEATURES.md](FEATURES.md). For a business-oriented product description, see [project-description.md](project-description.md).

---

## TL;DR — where to integrate

The Community Edition ships with full UI for client management, currency exchange, transfers, and WOA payments, but **does not include a banking core**. To make teller "Process" actions actually post a transaction, you replace one or more services in `web.service.back.*` (or extend `web.integration.fineract.*`). Everything else — CRM, admin, dictionaries, audit, reports rendered by the app — runs on the Front Office database on its own.

| Layer | Package / file | What it does |
|-------|----------------|--------------|
| **Core-banking adapter (REPLACE ME)** | `web/service/back/CurrencyExchangeBackService.java`, `TransferBackService.java`, `WoaPaymentBackService.java`, `PersonBackService.java` | Thin Spring services the UI calls when it needs to post / read from the core. Currently throw a clear "Core banking is not connected" message when `integration.back.enabled=false`. |
| **Dictionary stubs** | `web/service/stubs/...` | Sample data for banks and FX rates. Replace with real APIs. |
| **Payment-system data** | `web/service/pat/payment/PaymentTransferService.java` | Test payment points, fees, transfer lookup. Replace with real gateway APIs. |
| **Apache Fineract** | `web/integration/fineract/...` | Working client-sync connector. Can be extended to cover accounts / loans / transfers — see [FINERACT_INTEGRATION.md](FINERACT_INTEGRATION.md). |
| **Back DTOs** | `web/repository/back/{account,ce,crm,transfer,woa}/...` | `Input*` / `Output*` data carriers used by the back services as their input/return shapes. Reuse them when wiring your adapter or replace them with your own DTOs. |

---

## Features overview (quick reference)

| Feature | Description | UI / notes |
|---------|-------------|------------|
| **Client management** | Client file (card), contracts & supplementary agreements, document expiry tracking, webcam still-photo capture | CRM schema; client forms, address autocomplete |
| **Currency exchange** | Operations, orders, rates, reporting (UI ready; posting needs adapter) | [Currencies](docs/screenshots/currencies.png), [Exchange rates](docs/screenshots/exchange%20rates.png) |
| **Transfers** | Send/receive, payment systems, fee calculation (UI ready; posting needs adapter) | Flows in `single-window/transfer`; extend via `PaymentSystemName` and flow XML |
| **WOA payments** | Counterparty payments (UI ready; posting needs adapter) | Flow in `single-window/payment-woa` |
| **Administration** | Users, roles, groups, departments, audit | [User management](docs/screenshots/user-management.png), [User edit](docs/screenshots/user-edit.png), [Audit logs](docs/screenshots/audit-logs.png) |
| **Dictionaries** | Countries, currencies, banks, payment systems, addresses | Administration → Dictionaries / Extended Dictionaries; some support "Update" (stub or real API) |
| **Reports** | App-rendered PDFs work; core-stored-procedure reports need core | Data from stored procedures → `OutputOperationData` (or your mapping) |
| **Hardware** | Webcam still photo, printer (via browser) | Scanner integration is your own (no built-in TWAIN) |

---

## Replacing the back layer (your own adapter)

This is the single most important extension task for any non-trivial deployment.

### 1. The contract

Each `*BackService` exposes a small set of methods used by the UI:

| Service | Methods of interest |
|---------|---------------------|
| `CurrencyExchangeBackService` | `receiveAccountRest`, `isWorkday`, `installRates`, `processOperation`, `receiveOperationData` |
| `TransferBackService` | `processTransfer` |
| `WoaPaymentBackService` | `transferPayment` |
| `PersonBackService` | `findPerson`, `createPerson`, `updatePerson`, `checkOnTerrorist`, `checkPassport`, `findDeposits`, `findCredits` |

All current implementations unconditionally throw `BackException(BackIntegrationMessages.CORE_NOT_CONNECTED)`. The `integration.back.enabled` flag controls whether **callers** even attempt the back step (when `false`, callers like CRM person sync, currency-exchange flows, etc. take a no-back code path and never invoke these services). Wiring your adapter is a matter of replacing the method bodies in these classes.

### 2. Steps for your own adapter

1. **Pick a transport** for your core — REST, SOAP, JDBC, message broker — and add the client(s) under a new package, e.g. `web.integration.mycore`.
2. **Inject the client** into the relevant `*BackService` and replace the method bodies so they call your core. Keep the same method signatures so the UI does not change.
3. **Map your responses** to the existing `Output*` DTOs (e.g. `OutputOperationData`, `OutputTransfer`) or update the DTOs and the entity mapping.
4. **Configure** `integration.back.enabled=true` (and remove or keep the guard depending on whether you want a "demo mode" fallback).
5. **Optionally** replace `web/service/stubs/...` (banks, rates) and `PaymentTransferService` (payment-system data) with real APIs.

### 3. Spring/transaction notes

- All Spring services (including the `*BackService`s) use the primary JPA `transactionManager` against the Front Office DB. If your adapter needs its own transactional boundary (e.g. JDBC into a separate ledger DB), define a second `DataSource` + `TransactionManager` and qualify your back-only methods accordingly.
- The DTOs under `web/repository/back/...` are plain Java objects — feel free to keep, extend, or replace them with your own request/response shapes.

### 4. Friendly errors

`BackIntegrationMessages.CORE_NOT_CONNECTED` is the single message used by the UI when a posting step has no integration. Keep it (or replace it with something equally clear) so that running the application without a wired core still produces a clean user experience instead of stack traces.

---

## Where to add integrations

### 1. Configuration (URLs, API keys)

- **File:** `web/src/main/java/web/configuration/Settings.java`
- Add new `@Value("${your.property}")` fields for URLs, API keys, feature flags.
- Document keys in `front-office.properties.example` (no real secrets).

### 2. Dictionary / data updates (banks, rates, payment systems)

- **File:** `web/src/main/java/web/service/dict/DictionaryParameterService.java`
- The `update(DictionaryParameter)` switch drives which provider runs (BANKS, RATES, PAYMENT_TRANSFER_INFO).
- To add a new updatable dictionary: add an enum value in `DictionarySystemName`, implement a service that returns `DictionaryUpdateResult`, and add a `case` in `DictionaryParameterService.update(...)` that calls it.

**Existing integration points:**

| Dictionary      | Service / stub to replace or extend        | Location |
|-----------------|--------------------------------------------|----------|
| **BANKS**       | `BankService` (stub)                       | `web/service/stubs/bankstub/BankService.java` |
| **RATES**       | `ExtRateService` → `ExternalRateService`, `MarketRateService` (stubs) | `web/service/dict/rate/ExtRateService.java`, `web/service/stubs/ratestub/` |
| **PAYMENT_TRANSFER_INFO** | `PaymentTransferService` (test data) | `web/service/pat/payment/PaymentTransferService.java` |

### 3. Payment systems (send / receive transfers)

- **Receive (payout) flow:**
  - `web/src/main/java/web/view/transfer/get/StepOneView.java` — `next(...)` switch: add a `case` for your payment system and set the correct `payoutTransfer` / view.
  - Flow definitions: `web/src/main/webapp/WEB-INF/flow/pages/single-window/transfer/get/get-flow.xml` (add subflow or view-state for the new system).
- **Send flow:**
  - `web/src/main/java/web/view/transfer/send/StepTwoView.java` — `init(...)` switch: add a `case` for fee calculation for your system.
  - Flow: `web/src/main/webapp/WEB-INF/flow/pages/single-window/transfer/send/send-flow.xml`.
- **Enum:** Add the new system to `web/entity/dict/PaymentSystemName.java` and wire it in the flows and XHTML (step-one, step-two, etc.).

### 4. Operation reporting from the core

- **DTO:** `web/repository/back/ce/OutputOperationData.java` — return shape used by `CurrencyExchangeBackService.receiveOperationData(...)` to render operation reports in the UI.
- When wiring your adapter, populate this DTO from your core's response (or change the DTO to match your data and update the report views accordingly).

### 5. Apache Fineract (open-source core banking API)

- **Package:** `web/integration/fineract/`
- **Config:** `integration.fineract.*` in `front-office.properties` (see `front-office.properties.example`)
- **Details:** [FINERACT_INTEGRATION.md](FINERACT_INTEGRATION.md)
- The shipped connector covers **clients only**. You can extend it (accounts, deposits, loans, transfers) and call it from the `*BackService` methods above to provide real posting without writing a custom core integration.

### 6. External rate / market rate providers

- **Stubs:**
  - `web/service/stubs/ratestub/ExternalRateService.java`
  - `web/service/stubs/ratestub/MarketRateService.java`
- Replace or implement these to call real APIs (e.g. ECB, Fixer.io, ExchangeRate-API).
- Optional: in `Settings.java`, uncomment and use `integration.external.rate.url` and `integration.market.rate.url` (and add corresponding properties in the example config) if you want URL-driven configuration.

---

## Stubs and what to integrate

Out of the box, several features use **stubs** (hardcoded or demo data). For production you typically need to wire real APIs or backend services:

| Area | Current behaviour | What to integrate |
|------|-------------------|-------------------|
| **Core posting (exchange / transfer / WOA)** | Stub `BackException` with friendly message | Your own `*BackService` adapter (REST/SOAP/JDBC to your core) — or extend Fineract |
| **Banks** | Stub: fixed list of sample banks (`BankService`) | Your bank directory or registry API (SWIFT, national registry) |
| **Exchange rates** | Stub: fixed rates in `ExternalRateService` / `MarketRateService` | Real rate provider (ECB, Fixer.io, ExchangeRate-API, or your core banking) |
| **Countries** | No automatic update; data is manual or from initial scripts | Optional: country list API if you need auto-updates |
| **Payment systems (transfer)** | `PaymentTransferService` ships with test data | Your payment gateway(s) and fee/quote APIs |
| **Operation report** | Data comes from a stored procedure (`OutputOperationData`) | Backend stored procedure that returns the columns expected by the entity; or replace the report source |
| **Person products (deposits, credits)** | Empty without a core | Your core API for client product portfolio |
| **Sanctions / passport-registry checks** | Skipped without a core | Your sanctions screening / KYC provider |

The "Update" action in **Administration → Extended Dictionaries** runs the logic above per dictionary type; for Countries it does nothing (no auto-update). Configure URLs and credentials in `Settings.java` and `front-office.properties` when you add real integrations.

---

## Current Database Structure (Liquibase)

Schema migrations are in `web/src/main/resources/db/2.0.0/`. Master list: `changelog.xml` includes `core`, `dict`, `core-data`, `log`, `crm`, `dict-data`, `ce`, `ps`, `pay`, and `demo-data` (sample counterparty / bank / pay-action data used by the demo flows).

| Schema | Purpose | Examples |
|--------|---------|----------|
| **CORE** | Org structure, users, security | DEPARTMENTS, USERS, ROLES, RIGHTS, SECURITY_PROFILES |
| **DICT** | Reference data | COUNTRIES, CURRENCIES, BANKS, ACCOUNTS, ADDRESS_ELEMENTS, DICTIONARY_PARAMS |
| **CE**   | Currency exchange | DEPARTMENT_CURRENCIES, RULES, ORDERS, CURRENCY_OPERATIONS |
| **CRM**  | Clients | PERSONS, CONTACTS, ADDRESSES, DOCUMENTS |
| **LOG**  | Operations & audit | OPERATIONS, CONNECTION_EVENTS |
| **PS**   | Payment systems (transfer) | RECIPIENTS |
| **PAY**  | Payment operations | PAYMENT_OPERATIONS (extends LOG.OPERATIONS) |

JPA entities live under `web/entity/` (e.g. `core`, `dict`, `ce`, `log`). Backend-only result sets (e.g. report output) are mapped in `web/repository/back/` (e.g. `OutputOperationData`).

---

## Customising the schema for your jurisdiction

Some table, column, and field names in Liquibase changeSets and JPA entities reflect general banking-domain conventions that may not match your local naming, regulatory requirements, or internal data model. Adjust the changeSets under `web/src/main/resources/db/...`, update the corresponding `@Entity` mappings, and regenerate static metamodels as needed.
