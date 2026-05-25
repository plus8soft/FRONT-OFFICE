# Apache Fineract integration

Front Office includes an optional **Fineract connector** — REST integration with [Apache Fineract](https://fineract.apache.org/), the open-source core banking platform of the Apache Software Foundation. It runs alongside the Front Office CRM database and does not replace the back datasource used by the legacy stored-procedure adapter (see [INTEGRATION_AND_ARCHITECTURE.md](INTEGRATION_AND_ARCHITECTURE.md)).

Turn it on in `front-office.properties` (`integration.fineract.enabled=true`; see `front-office.properties.example`).

---

## What works now

**Two-way client (person) sync between Front Office CRM and Fineract.** This is the only Fineract integration shipped out of the box.

### CRM → Fineract

- After saving a client in CRM, Front Office creates or updates the matching Fineract client.
- On the client card: **Sync to Fineract** and **Refresh Fineract status**.
- Link in Fineract: `externalId` = `FO-{PERSONS_ID}`.

Uses: `GET /clients?externalId=…`, `POST /clients`, `PUT /clients/{id}`.

### Fineract → CRM

- **Fineract search** on Client Search: find clients in Fineract that are not linked to CRM.
- **Create in CRM** — import person from Fineract into CRM and set the link.
- **Link** — attach an open CRM client to an existing Fineract client.
- Same Fineract hits appear in the “client not found” dialog when CRM search has no match.

Uses: `GET /clients?search=…`, `GET /clients/{id}`, then CRM save and `PUT /clients/{id}` for linking.

### Data synced

Name, birth date, mobile (on import). Office and legal form for new Fineract clients come from configuration.

`Person.externalId` in CRM stays the legacy back-office id when used; the Fineract link is only via Fineract `externalId` (`FO-{id}`).

---

## What is NOT covered (yet)

The shipped connector handles **clients only**. The following teller operations still go to the back-service stubs and need either your own core-banking adapter or an extension of this Fineract integration:

- Currency exchange — posting, cash balance, business-day check, install rates
- Transfers (send / receive) — posting to core
- WOA payments — posting to core
- Client products on the card — deposits, credits (read from the back layer)
- Person checks — sanctions, passport-registry

Fineract has REST endpoints for accounts, deposits, loans, journal entries and savings/loan transactions. Extending this connector to cover those operations and calling it from the relevant `*BackService` (see [INTEGRATION_AND_ARCHITECTURE.md → Replacing the back layer](INTEGRATION_AND_ARCHITECTURE.md#replacing-the-back-layer-your-own-adapter)) is a fully open-source path to a working production setup. See **Extending the integration** below.

---

## Configuration (summary)

```properties
integration.fineract.enabled=true
integration.fineract.base-url=https://localhost:8443/fineract-provider/api/v1
integration.fineract.tenant-id=default
integration.fineract.username=your-user
integration.fineract.password=your-password
integration.fineract.office-id=1
integration.fineract.legal-form-id=1
integration.fineract.ssl-trust-all=true   # local HTTPS with self-signed cert only
```

Use a local `front-office.properties` (gitignored); do not commit credentials.

---

## Extending the integration

To add more Fineract features (accounts, loans, deposits, transfers, journal entries, reports):

| Layer | Where to start |
|-------|----------------|
| HTTP / auth | `web.integration.fineract.FineractRestClient` — `get`, `post`, `put` |
| Business logic | `web.integration.fineract.FineractIntegrationService` — follow patterns for client sync/search |
| After CRM save | `web.service.crm.PersonService` — `save()` already calls sync |
| UI (Client Search) | `web.view.ce.clientsearch.ClientSearchView`, `client-search.xhtml` |
| Settings | `web.configuration.Settings` + `front-office.properties.example` |

New API calls belong in `web.integration.fineract.*`; call them from services or views, not directly from XHTML.

Health check: `FineractIntegrationService.isAvailable()` (`GET /offices`).

### Making teller operations post to Fineract

To replace the "Core banking is not connected" stubs in the back layer with Fineract calls:

1. Add the Fineract endpoints you need (e.g. savings transactions, journal entries) to `FineractIntegrationService`.
2. Inject `FineractIntegrationService` into the relevant `*BackService` (`CurrencyExchangeBackService`, `TransferBackService`, `WoaPaymentBackService`) and call it instead of the legacy stored-procedure path.
3. Keep the existing method signatures so no UI code has to change.
4. Set `integration.back.enabled=true` (or remove that guard) so the application stops short-circuiting back calls.

This is the recommended fully-open-source path to a working posting setup.

---

## Quick check

1. Start Fineract, enable properties, deploy WAR.
2. Save a client in CRM → verify `GET .../clients?externalId=FO-{personId}`.
3. Use **Fineract search** → **Create in CRM** or **Link** for the reverse direction.
