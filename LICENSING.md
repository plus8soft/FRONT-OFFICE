# Licensing — Plus8Soft Front Office

> **Plain-English summary.** The legal source of truth is the LICENSE file in this repository for the Community Edition, and the separate Plus8Soft Commercial License Agreement for the Enterprise Edition.
>
> This document is a practical guide, not legal advice — and it is not regulatory, banking-supervisory, or compliance advice.

---

## TL;DR

| | Community Edition | Enterprise Edition |
|---|---|---|
| **License** | Apache License 2.0 | Plus8Soft Commercial License |
| **Source code** | Public, on GitHub | Provided to paying customers |
| **Cost** | Free | Annual subscription |
| **Production use** | Allowed | Allowed (with subscription) |
| **Internal company use** | Allowed | Allowed (with subscription) |
| **Resell / SaaS / managed service** | Allowed (Apache 2.0 permits) | Not allowed |
| **Build a competing product** | Allowed (Apache 2.0 permits) | Not allowed |
| **Multi-branch / multi-tenant** | Not included | Included |
| **SSO (AD / LDAP / SAML / OIDC)** | Not included | Included |
| **Sanctions screening** | Not included | Included |
| **Core-banking connectors** | Not included | Included |
| **Native AML / RAG integration** | Not included | Included |
| **Use of "Plus8Soft" trademark** | See TRADEMARK.md | See TRADEMARK.md |
| **Support** | Community (Discord, GitHub) | SLA-backed support |
| **Indemnification** | None | Up to $25M (per tier) |

---

## What you can do with the Community Edition (Apache 2.0)

You can:

- **Use it for any purpose**, including commercial production use, internal company use, and SaaS hosting.
- **Modify it.** Fork it. Build derivative works.
- **Distribute it.** Share it with colleagues, customers, partners.
- **Embed it** in your own products, including proprietary products.
- **Use it without paying us anything.** Forever.

Apache 2.0 is one of the most permissive open source licenses in existence. We chose it deliberately.

What you must do under Apache 2.0:

- **Keep the copyright and license notices.** Don't strip out the LICENSE and NOTICE files.
- **Mark modified files** as having been modified.
- **Don't sue us for patent infringement** over the work — if you do, your patent rights under this license terminate.
- **Don't use the "Plus8Soft" trademarks** to promote your own product or imply endorsement. See TRADEMARK.md.

That's it.

---

## Why some features are NOT in this repository

The Community Edition deliberately ships only the core front-office workstation functionality. It does not include:

- Multi-tenant / multi-branch deployment with strict data isolation
- Active Directory / LDAP / SSO (SAML, OIDC) integration
- Advanced RBAC + ABAC, segregation-of-duties policies
- 7-year regulator-grade immutable audit logging
- E-signature integrations (DocuSign, Adobe Sign, native eIDAS-compliant PKI)
- Production-grade AI-assisted video identification (KYC)
- Sanctions screening (OFAC, EU, UN, HMT)
- Premium core-banking connectors (Temenos T24, Finastra, Finacle, FIS Profile, Fiserv DNA, Mambu, Oracle FLEXCUBE)
- SWIFT MT / MX, ISO 20022 integration
- Card-management-system integrations
- Multi-jurisdictional regulatory packs
- Advanced workflow engine, HA clustering, disaster recovery
- Mobile companion app for managers
- Cross-platform PDF generation
- Native integration with Plus8Soft AML and Plus8Soft RAG
- Long-term support (LTS) branches with extended security backports
- Air-gapped deployment support
- IP indemnification

These are part of the **Enterprise Edition**, which is a separate product distributed under a commercial license to paying subscribers.

This is the standard "open core" model used by companies like GitLab, Confluent, Elastic, Neo4j, and Strapi.

---

## The Plus8Soft BankOps Suite

Plus8Soft Front Office is part of an integrated trio of open-source products:

- **Plus8Soft Front Office** (this product) — the staff-facing UI for in-person banking and service operations
- **Plus8Soft AML** — transaction fraud and AML detection
- **Plus8Soft RAG** — enterprise RAG for staff knowledge assistance

Each product is independently useful and independently licensed (Apache 2.0 Community Edition + commercial Enterprise Edition).

The **Enterprise Editions** are designed to interoperate. When deployed together, the Front Office Enterprise tier includes:

- **Real-time transaction screening** — every operation entered by a teller is scored by Plus8Soft AML before posting; high-risk operations route to investigators with full case context.
- **In-app knowledge assistance** — staff can query the institution's policies, KYC workflows, and compliance procedures through Plus8Soft RAG without leaving the operation screen.
- **Unified audit trail** across the three products.

This integrated package is offered as the **Plus8Soft BankOps Suite** under a single commercial agreement. Discounts apply for multi-product subscriptions.

For BankOps Suite pricing and architecture: **sales@plus8soft.com**

---

## Why we chose Apache 2.0 (not BSL, ELv2, or SSPL)

We considered the various "source-available" licenses popular in the database / infrastructure space — Business Source License (BSL), Elastic License 2.0, Server Side Public License (SSPL), Functional Source License (FSL).

We chose Apache 2.0 instead because:

1. **It removes friction.** Bank, MFO, and insurance company legal and procurement teams approve Apache 2.0 immediately. Source-available licenses trigger lengthy reviews that can kill or delay deals — often by 6+ months in regulated procurement.
2. **It builds genuine community.** Source-available licenses have caused community fractures in projects like Elasticsearch (→ OpenSearch), Terraform (→ OpenTofu), and Redis (→ Valkey). We don't want that.
3. **The moat isn't the license — it's the Enterprise modules and connectors.** A competitor can fork our Apache code, but they can't ship our SSO, sanctions screening, core-banking connectors, AML / RAG integration, or LTS support. Those are what regulated financial-services customers pay for.
4. **The trademark is reserved.** No one but Plus8Soft can ship a product called "Plus8Soft Front Office."

---

## How to use the Enterprise Edition

If your organization needs:

- Multi-branch / multi-tenant production deployment
- SSO via Active Directory, LDAP, SAML, or OIDC
- E-signature workflows (DocuSign, Adobe Sign, eIDAS-qualified)
- Production AI-assisted video identification for KYC
- Sanctions screening against OFAC, EU, UN, and HMT lists
- Connectors to core banking systems (Temenos, Finastra, Finacle, FIS, Fiserv, Mambu, FLEXCUBE)
- SWIFT and ISO 20022 message integration
- Card-management-system integration
- Jurisdictional regulatory packs (PSD2, 6AMLD, BSA, MLR, GCC AML)
- High-availability clustering and disaster recovery
- 24/7 production support with SLAs and IP indemnification

Contact us: **sales@plus8soft.com**

We offer four tiers (Community, Pro, Enterprise, Sovereign). Pricing for Pro is published on our website; Enterprise and Sovereign pricing is provided on request. Multi-product BankOps Suite pricing is available on request.

---

## Important regulatory disclaimer

The Community Edition is **not** a turnkey banking system. It is a front-office workstation application that helps staff perform in-person operations and manage client documents.

**Final compliance with banking, payments, AML / CTF, sanctions, data-protection, and cybersecurity regulations remains the responsibility of the operator and the operator's compliance, risk, and security officers.** This includes (but is not limited to):

- Banking licenses and supervisory requirements in your jurisdiction
- Payment Services Directive (PSD2) requirements where applicable
- Strong Customer Authentication (SCA)
- Customer Due Diligence (CDD) and Enhanced Due Diligence (EDD)
- AML / CTF screening and reporting (handled by Plus8Soft AML when integrated)
- Sanctions screening (OFAC, EU, UN, HMT — included in Enterprise)
- Bank Secrecy Act (BSA) compliance (US)
- 6th EU Anti-Money Laundering Directive (6AMLD)
- UK Money Laundering Regulations (MLR)
- GDPR, including biometric-data handling under Article 9 (relevant for video identification)
- CCPA / CPRA and related US state privacy laws
- Banking-secrecy laws in your jurisdiction
- Cybersecurity regulations: NIS2 (EU), NYDFS Part 500 (NY), EBA Guidelines on ICT Risk, FFIEC IT Handbook (US), and equivalents
- Outsourcing and third-party-risk-management requirements (EBA Guidelines on Outsourcing, OCC, PRA SS2/21, MAS, HKMA equivalents)

The Enterprise Edition includes modules that materially help with several of the above (sanctions screening, audit logging, regulatory packs, integrations) — but final regulatory responsibility always rests with the operator.

If you are deploying this software in a regulated financial-services context, engage qualified counsel, your internal compliance and risk functions, and follow your supervisor's expectations for technology risk management, change management, and operational resilience.

---

## Demo credentials warning

The Community Edition first-run seed data includes default credentials (`admin` / `123456789a`). These are for **development and demonstration only**.

**Operating the application with default credentials in any production, internet-accessible, or shared environment is a serious security and regulatory issue.** It may constitute a violation of:

- Cybersecurity regulations applicable to financial institutions
- The PCI-DSS standard if cardholder data is processed
- Internal information-security policies most banks and MFOs are obligated to maintain

Before any deployment beyond a developer's local machine: **change the default password, disable demo seed data, and configure proper authentication** (Active Directory / SAML / OIDC via the Enterprise Edition for production). The user is solely responsible for production hardening; Plus8Soft accepts no liability for breaches arising from operation with default credentials.

---

## Frequently asked questions

**Q: Can my bank, MFO, or insurance company use the Community Edition in production?**
Yes. Apache 2.0 places no restrictions on use. Note the regulatory and security disclaimers above. Most regulated institutions will need at least the Enterprise Edition for SSO, audit retention, and sanctions screening to satisfy supervisor expectations.

**Q: Can we modify it?**
Yes. Apache 2.0 permits modification and creation of derivative works.

**Q: Can we redistribute it (e.g., bundle it with our own product)?**
Yes, subject to the Apache 2.0 conditions: keep the LICENSE and NOTICE files, mark modified files, retain attributions.

**Q: Can we offer the Community Edition as a hosted/managed service to our customers (e.g., a regional bank-tech vendor offering teller workstations)?**
Yes. Apache 2.0 permits this. You may not use the "Plus8Soft" name in your service name (see TRADEMARK.md).

**Q: Can we call our hosted service "Plus8Soft Front Office Cloud" or similar?**
**No.** Apache 2.0 does not grant trademark rights. The names "Plus8Soft," "Plus8Soft Front Office," "Plus8Soft BankOps Suite," and the logo are trademarks of AxiomaSoft LLC. See TRADEMARK.md for permitted nominative use.

**Q: Can we contribute back to the Community Edition?**
Yes — and we'd love that. See CONTRIBUTING.md. Contributors must sign our Contributor License Agreement (CLA.md).

**Q: Does the Community Edition support multiple branches?**
The Community Edition supports a single-tenant / single-branch deployment. The Enterprise Edition adds multi-tenant / multi-branch support with strict data isolation.

**Q: What happens if my Enterprise subscription expires?**
The Enterprise binaries you have continue to function but stop receiving updates, regulatory-pack updates, and support. To continue receiving them, renew the subscription. The Community Edition you've deployed continues to work indefinitely under Apache 2.0.

**Q: Do you offer educational / non-profit pricing?**
Yes — particularly for academic institutions teaching banking IT, and for non-profit MFOs in developing markets. Contact sales@plus8soft.com.

**Q: Do you offer source-code escrow?**
Yes, for Sovereign tier customers — relevant for tier-1 banks, central banks, and government-related institutions where escrow is a procurement requirement.

**Q: Is there a managed cloud version?**
Not currently. The Enterprise Edition is deployed in the customer's own environment (on-premises, customer-managed cloud, or air-gapped). Contact sales@plus8soft.com to discuss managed deployment options.

**Q: Where can I see the full Enterprise feature list and pricing?**
See: https://plus8soft.com/front-office (or contact sales@plus8soft.com)

---

## Important notes

- **Do not commit secrets** (`.env`, `front-office.properties`, API keys, database credentials, customer data) to public repositories — including this one.
- **Do not commit real customer data, real account numbers, or real personal data** to public repositories. Even sample data should be synthetic.
- **Replace demo seed data and default credentials** before any production or internet-accessible deployment.
- **This document is informational, not legal, regulatory, or compliance advice.** Consult qualified counsel and your internal compliance / risk / security functions for legal and regulatory questions.
- **The LICENSE file is the legally binding source of truth** for the Community Edition. The Plus8Soft Commercial License Agreement is the legally binding source of truth for the Enterprise Edition.

---

*Questions? legal@plus8soft.com*
