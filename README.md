<!-- Copyright AxiomaSoft LLC (d/b/a Plus8Soft). Licensed under the Apache License 2.0. See LICENSE. -->

# Plus8Soft Front Office (Community Edition)

**Open-source web-based front-office platform for banks, MFOs, insurance companies, and in-person service points.**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Stack-Java%208%20%2F%20Spring%20%2F%20JSF-green)](https://spring.io/)
[![Build](https://img.shields.io/badge/build-Gradle-brightgreen)](https://gradle.org/)

> Production deployments with multi-branch, SSO, sanctions screening, biometric/video KYC, or pre-built core-banking connectors (Temenos, Finastra, Finacle, FIS, Fiserv, Mambu) are available in the **Enterprise Edition** — contact [sales@plus8soft.com](mailto:sales@plus8soft.com).

---

## About the project

Plus8Soft Front Office is a teller-facing web application for in-person banking and service operations: client management, currency exchange, transfers, counterparty payments, administration, audit, and reporting — all in a standard browser, no thick client.

Built with Java 8, Spring, JSF + PrimeFaces, and Microsoft SQL Server, it provides a production-grade UI layer, an admin platform, and clean integration points so you can connect it to your own core banking system (or to [Apache Fineract](https://fineract.apache.org/), which is supported out of the box for client sync).

---

## What's included

### Client management (CRM)
- Client file (card) with profile, contacts, addresses, documents, photo
- Document storage: passports, contracts, supplementary agreements
- Document expiry tracking with alerts and reports
- Webcam photo capture for the client dossier
- Address autocomplete from address-element dictionaries
- Audit trail of all client changes

### Apache Fineract integration — out of the box
- Two-way client sync between Front Office CRM and [Apache Fineract](https://fineract.apache.org/), the open-source core banking platform of the Apache Software Foundation
- Search Fineract clients from the Client Search screen and import them into CRM
- Link existing CRM clients to existing Fineract clients
- On CRM save, the matching Fineract client is created or updated automatically
- Configure in `front-office.properties` (`integration.fineract.enabled=true`); details in **[FINERACT_INTEGRATION.md](FINERACT_INTEGRATION.md)**

### Currency exchange
- Full multi-step flow: operation type, currency, amount, rate, commission, VAT, identification
- Local rate orders with PDF and email notification to cashiers
- Order screen with full rate management and approval flow

### Transfers (send / receive)
- Multi-step send and payout flows
- Payment-system selection (Money Transfer, Western Union, MoneyGram, …)
- Fee calculation, validation, recipient data forms, print forms

### WOA payments (counterparty / one-off payments)
- Full flow for legal-entity, public-sector, and tax payments
- TIN/EIN search and counterparty management
- Category, payment type, purpose, sum, commission, VAT

### Administration
- Users, roles, rights, security profiles, groups
- Departments and organisational structure
- Audit logs for user actions and IO events

### Dictionaries and reference data
- Countries, currencies, banks, accounts, payment systems
- Address elements (regions, cities, streets) for autocomplete
- Extended dictionaries with configurable update jobs

### Reports
- Application-rendered PDF receipts and print forms (`ReportService` over iText)
- Operation reports designed to be populated from a connected core

### Hardware
- Webcam capture for client photos directly from the browser
- Printing of receipts and print forms via standard browser dialogs
- Data model and integration points for scanners

See **[FEATURES.md](FEATURES.md)** for a per-screen breakdown with screenshots.

---

## Connecting your core banking system

Currency exchange, transfer, and WOA-payment posting calls are routed through a thin Spring layer in `web.service.back.*`. Out of the box these calls return a friendly *"Core banking is not connected"* message so flows can be demonstrated end-to-end; to actually post operations you implement an adapter for your core.

### Where to put your code

| Service | Operations it covers |
|---------|-----------------------|
| `web/service/back/CurrencyExchangeBackService.java` | Process operation, account rest, business-day check, install rates |
| `web/service/back/TransferBackService.java` | Process transfer (send / receive) |
| `web/service/back/WoaPaymentBackService.java` | Process WOA payment |
| `web/service/back/PersonBackService.java` | Client products (deposits, credits), sanctions and document checks |

Replace the method bodies with calls to your core — REST, SOAP, JDBC, message broker, anything. Keep the existing signatures so the UI continues to work unchanged. After wiring, set `integration.back.enabled=true` in `front-office.properties`.

A few more services use sample data and can be replaced when you're ready: bank registry (`web/service/stubs/bankstub/`), external/market FX rates (`web/service/stubs/ratestub/`), payment-system data (`web/service/pat/payment/PaymentTransferService.java`).

### Three paths to a working back

1. **Write your own adapter** — implement the services above against your core (Temenos T24, Finastra, Mambu, Oracle FLEXCUBE, a home-grown ledger, …). Detailed integration points in **[INTEGRATION_AND_ARCHITECTURE.md](INTEGRATION_AND_ARCHITECTURE.md)**.
2. **Extend the Apache Fineract integration** — Fineract is fully open-source and exposes REST endpoints for accounts, deposits, loans, journal entries, and transactions. The client connector is already in place; extend `web/integration/fineract/` and call it from the back services above to make exchange / transfer / WOA posting hit Fineract.
3. **Enterprise edition** — Plus8Soft sells production-grade core-banking connectors and integration services. Contact [sales@plus8soft.com](mailto:sales@plus8soft.com).

> Some interface-level stubs in `web/repository/back/...` are kept as **examples of the contract shape** (parameter names, types, return structure) each back operation expects. Treat them as a template only; replace them with your own adapter when wiring a real core.

---

## Why this is useful even before you connect a core

Plugging in a banking core is real engineering work — and the UI, the workflows, and the integration boundary are already built for you:

- **Complete multi-step flows** — recipient data, validation, rates, fee and commission calculation, VAT, payment-type selection, print forms — all wired in JSF + PrimeFaces and Spring Web Flow
- **A clean integration seam** — one Spring service per module (`*BackService`); the rest of the codebase doesn't change when you plug in your core
- **Demo data and friendly stub messages** — every flow can be clicked through end-to-end; stubs return a clear message instead of stack traces
- **All the infrastructure around the teller** — users, roles, rights, departments, audit logs, dictionaries, localization, address autocomplete, photo capture, PDF reports — already on the Front Office DB
- **Apache Fineract for clients out of the box** — search, import, link, two-way sync — no additional code required

---

## Documentation

| Document | What's in it |
|----------|--------------|
| **[FEATURES.md](FEATURES.md)** | Per-screen feature description with screenshots and module-level integration notes. |
| **[INTEGRATION_AND_ARCHITECTURE.md](INTEGRATION_AND_ARCHITECTURE.md)** | Integration points, stub inventory, database schemas (Liquibase), the back-adapter contract. |
| **[FINERACT_INTEGRATION.md](FINERACT_INTEGRATION.md)** | Apache Fineract connector — configuration, what it covers, how to extend it. |
| **[project-description.md](project-description.md)** | Business-oriented project description. |
| **[LICENSING.md](LICENSING.md)** | Plain-English licensing guide. |
| **[CONTRIBUTING.md](CONTRIBUTING.md)** | How to contribute. |
| **[SECURITY.md](SECURITY.md)** | Vulnerability disclosure policy. |

---

## Tech stack

| Component | Technologies |
|-----------|--------------|
| Backend | Java 8, Spring MVC, Spring Security, Spring Data JPA, Spring Web Flow |
| Frontend | JSF (Jakarta Faces), PrimeFaces (server-side rendering) |
| Database | Microsoft SQL Server, Liquibase for schema migrations |
| Build | Gradle |

---

## Quick start

### Prerequisites

- **JDK 8** (or 11+ with `--release 8`)
- **Gradle**
- **Microsoft SQL Server** (empty database; Liquibase creates the schema on first run)

### Build and run

1. **Build:**

   ```bash
   ./gradlew :web:war
   ```

2. **Configure:**

   ```bash
   cp front-office.properties.example front-office.properties
   ```

   Set `datasource.url`, `back.datasource.url`, `lob.datasource.url` (and SMTP if needed). Keep `integration.back.enabled=false` for the first run — UI flows work end-to-end and posting steps display a clear message. **Do not commit** `front-office.properties` (it is gitignored).

3. **Deploy:**

   Deploy `web/build/libs/web.war` to a servlet container (e.g. Apache Tomcat). Point the container at `front-office.properties` via a JVM system property or startup script.

4. **Sign in:**

   After the first run (Liquibase seeds initial data):

   - **Login:** `admin`
   - **Password:** `123456789a`

   > ⚠️ **CRITICAL SECURITY WARNING — change immediately.**
   >
   > These are **demo defaults** for a quick start. Never use them in production or on any internet-accessible deployment. Sign in once, change the `admin` password, replace demo seed data, configure SSO via the Enterprise Edition. Operating with default credentials may violate regulatory cybersecurity requirements (GLBA, PCI-DSS, EBA Guidelines on ICT Risk, NIS2). Production hardening is the operator's responsibility.

---

## Screenshots

| Login | User management |
|-------|-----------------|
| ![Login](docs/screenshots/login.png) | ![User Management](docs/screenshots/user-management.png) |

| Currencies | Exchange rates |
|------------|----------------|
| ![Currencies](docs/screenshots/currencies.png) | ![Exchange rates](docs/screenshots/exchange%20rates.png) |

| Audit logs |
|------------|
| ![Audit logs](docs/screenshots/audit-logs.png) |

More: **[FEATURES.md](FEATURES.md)**.

---

## Optional services

### MsReportService (PDF from Word)

**Location:** `web/src/main/java/web/service/report/MsReportService.java`
**Status:** Disabled by default (`@Component` commented out). Depends on Microsoft Word (COM4J) and runs on Windows only. Enable on Windows hosts by uncommenting the annotation; see comments in the class.

The Enterprise Edition provides a cross-platform PDF generation module that does not require Microsoft Word.

---

## Commercial support and Enterprise Edition

The Community Edition is Apache 2.0 and free to use, including for commercial production use. The Enterprise Edition adds, among other things:

- Pre-built **core-banking connectors** (Temenos, Finastra, Finacle, FIS, Fiserv, Mambu, Oracle FLEXCUBE)
- Multi-branch / multi-tenant deployment with strict data isolation
- AD / LDAP / SSO (SAML, OIDC), advanced RBAC + ABAC, segregation-of-duties
- 7-year immutable audit logs
- Sanctions screening (OFAC / EU / UN / HMT)
- Biometric / video-based KYC (face matching, liveness, document OCR)
- E-signature integrations (DocuSign, Adobe Sign, native PKI)
- SWIFT MT / MX, ISO 20022, card-management-system integrations
- Multi-jurisdictional regulatory packs (EU PSD2 / 6AMLD, US BSA, UK MLR, GCC AML)
- HA, clustering, DR, air-gapped deployment, LTS branches, 24/7 support, IP indemnification
- Implementation services (delivery teams in 7 countries)

Contact: **[sales@plus8soft.com](mailto:sales@plus8soft.com)** · Website: **[plus8soft.com](https://plus8soft.com)**

---

## License

Community Edition: **Apache License 2.0** — see [LICENSE](LICENSE), [NOTICE](NOTICE), [LICENSING.md](LICENSING.md).
Enterprise Edition: separate commercial license — contact sales.
"Plus8Soft", "Plus8Soft Front Office", and the Plus8Soft logo are trademarks of AxiomaSoft LLC — see [TRADEMARK.md](TRADEMARK.md).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). All contributors must sign the [CLA](CLA.md).

## Security

**Do not report security vulnerabilities via public GitHub issues.** Email [security@plus8soft.com](mailto:security@plus8soft.com). See [SECURITY.md](SECURITY.md).

> **Important:** never commit secrets (`.env`, `front-office.properties`, API keys, credentials, customer data). Replace demo seed data before any production deployment.
