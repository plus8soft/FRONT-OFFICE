# Contributing to Plus8Soft Front Office

Thanks for your interest in contributing! This document explains how to contribute, what to expect, and the legal requirements.

---

## Quick start

1. **Check existing issues and pull requests** before starting work to avoid duplication.
2. **For non-trivial changes, open an issue first** to discuss the approach. PRs that haven't been pre-discussed may be closed without review.
3. **Fork the repository** and create a feature branch.
4. **Sign the CLA** when prompted (one-time, automated via [CLA Assistant](https://cla-assistant.io/)).
5. **Follow the code style** and include tests.
6. **Open a pull request** against `main`.

---

## What we accept

We welcome contributions in the following areas:

- **Bug fixes** in the Community Edition core
- **Performance improvements** with benchmarks
- **UI / accessibility improvements** in the JSF / PrimeFaces front-end
- **New community connectors** for open / generic data sources (open dictionaries, open exchange-rate APIs, generic SMTP, etc.)
- **Liquibase migrations** for schema improvements (with backward-compatibility notes)
- **Documentation improvements**, examples, deployment guides for additional servlet containers
- **Test coverage** improvements (unit tests, integration tests with H2 / SQL Server)
- **Translations** of UI strings and documentation
- **Bug reports** and feature requests via GitHub Issues

We may decline contributions that:

- Duplicate functionality available in the Enterprise Edition (we'll explain and may suggest alternatives)
- Add dependencies with incompatible licenses (anything not Apache 2.0 / MIT / BSD compatible)
- Significantly change the architecture without prior discussion
- Add features outside the project's scope (e.g., back-office accounting modules)
- Are not accompanied by tests
- Don't include a signed CLA

---

## Special note on banking / regulatory contributions

Because this project is used in regulated financial-services contexts:

- **Do not include real customer data, real account numbers, real transaction data, real document scans, or real personal data** in tests, examples, screenshots, or pull requests. Use synthetic data, fully anonymized data, or clearly fake test fixtures.
- **Do not include any production credentials**, API keys, database connection strings, or tokens — even in documentation examples. Use placeholders like `<YOUR_DATABASE_URL>` and `<API_KEY_HERE>`.
- **Do not include sanctions lists** (OFAC SDN, EU consolidated, UN, HMT) in the repository. The Enterprise Edition handles sanctions integration through licensed list providers.
- **Do not modify the demo seed data** to use credentials that could appear "real" or "production-like" — the existing demo password is intentionally obvious as a default.
- **Document feature provenance.** If a feature is based on a banking standard (e.g., SWIFT MT103 message format, ISO 20022 XML schemas, EBA technical standards), cite the standard and version in the PR description and code comments.
- **Don't claim regulatory compliance.** The Community Edition is a tool. Final regulatory compliance is the operator's responsibility. PRs that claim "this PR makes the project PSD2-compliant" or "GDPR-compliant" will be reframed.

### Biometric and KYC features

The Community Edition includes basic webcam capture for client identification. Any contributions to this area must:

- Not store biometric templates beyond what is reasonably necessary for the documented feature
- Not introduce facial-recognition matching against external databases
- Document any biometric-data handling clearly so operators can make informed GDPR Article 9 / equivalent assessments
- Not include any pre-trained ML models that were trained on data of unclear provenance

Production-grade AI-assisted video identification with proper liveness detection, anti-spoofing, and audit trail is part of the Enterprise Edition.

---

## Contributor License Agreement (CLA)

**All contributors must sign the Plus8Soft CLA** before their contributions can be merged. This is a one-time action handled automatically when you open your first pull request.

**Why we require a CLA:**

Plus8Soft Front Office is dual-licensed:
- The Community Edition is Apache License 2.0 (open source)
- The Enterprise Edition is under the Plus8Soft Commercial License (proprietary)

Both editions share core code. Without a CLA, we couldn't include external contributions in the Enterprise Edition without re-licensing each contribution individually — which would be impractical and would put the project's commercial sustainability at risk.

The CLA grants Plus8Soft (specifically, AxiomaSoft LLC) the necessary rights to use your contribution under both licenses. **You retain copyright to your contribution.**

**The CLA does not:**
- Transfer copyright to Plus8Soft
- Prevent you from using your contribution in your own projects
- Prevent you from contributing to other projects

**Two CLA forms exist:**
- **Individual CLA** — sign if you are contributing as an individual
- **Corporate CLA** — sign if you are contributing on behalf of an employer (your employer must sign)

Read the full CLA: [CLA.md](CLA.md)

---

## Development setup

> Detailed setup instructions are in the project README. Brief overview:

```bash
# Clone the repo
git clone https://github.com/plus8soft/front-office.git
cd front-office

# Configure (do NOT commit your front-office.properties)
cp front-office.properties.example front-office.properties
# edit front-office.properties with your local DB

# Build
./gradlew :web:war

# Run tests
./gradlew test

# Deploy the WAR to your local Tomcat
```

For a quick setup without SQL Server, we provide an H2 in-memory profile for development. See [INTEGRATION_AND_ARCHITECTURE.md](INTEGRATION_AND_ARCHITECTURE.md).

---

## Code standards

- **Language style:** follow the conventions in the existing codebase. We use standard Java / Spring conventions; checkstyle configuration is in the repo.
- **Java version:** code targets JDK 8 for compatibility (some banks still standardize on JDK 8). If you use 11+ features, gate them behind compatibility checks.
- **JSF / PrimeFaces:** follow JSF 2.x patterns (or Jakarta Faces if migrating). Don't introduce client-side framework dependencies (React, Vue) into the Community Edition — the Enterprise Edition has separate front-end roadmap considerations.
- **Database:** schema changes go through Liquibase. Always provide a forward migration; backward migrations are encouraged where feasible. Test migrations against an empty database and an existing populated database.
- **Tests:** every PR must include tests. Unit tests for service-layer logic, integration tests for controllers and JSF managed beans where appropriate.
- **Commits:** use clear, descriptive commit messages. Conventional Commits format preferred (`feat:`, `fix:`, `docs:`, `refactor:`, etc.).
- **PR title:** describe the change concisely. Reference issue numbers (`Fixes #123`).
- **PR description:** explain what changed and why. Include before/after for behavior changes. Note any breaking changes. Note any database migration impact.

---

## Pull request review

- A maintainer will review your PR within 5 business days.
- Expect feedback. Most non-trivial PRs go through 1–3 rounds of review.
- Reviews may ask for changes to scope, design, tests, documentation, or security implications.
- Banking-context PRs (anything touching authentication, audit logging, financial calculations, document handling) will go through a more careful security and correctness review.
- We aim to be responsive but reserve the right to decline contributions that don't fit the project's direction.

---

## Reporting security issues

**Do not report security vulnerabilities via public GitHub issues.**

Email **security@plus8soft.com** with details. See [SECURITY.md](SECURITY.md) for our disclosure process and response timelines.

Security issues we particularly care about:
- Authentication / authorization bypass
- SQL injection, XSS, CSRF, deserialization vulnerabilities
- Privilege escalation
- Audit-log tampering or bypass
- Issues in financial-calculation code (rounding errors, currency conversion mistakes, fee calculation errors that could be exploited)
- Dependency vulnerabilities, particularly in Spring, JSF / PrimeFaces, and JDBC drivers

---

## Code of conduct

Be respectful. Be constructive. Don't be a jerk.

We follow the spirit of the [Contributor Covenant](https://www.contributor-covenant.org/). Harassment, discrimination, personal attacks, and bad-faith behavior will result in removal from project spaces.

Report concerns to **conduct@plus8soft.com**.

---

## Recognition

Significant contributors are recognized in:

- The repository's `CONTRIBUTORS.md` file
- Release notes when their contributions ship
- Annual contributor highlights in our blog

We do not currently offer paid bounties, but we do offer:

- Free Pro tier subscriptions for active contributors
- Discounted Enterprise tier for contributors' employers (case by case)
- Conference travel sponsorship for major contributors (especially banking-tech and FinDevOps conferences)

---

## Trademarks

Contributing code does not grant rights to use Plus8Soft trademarks. See [TRADEMARK.md](TRADEMARK.md).

---

## Questions?

- **General questions:** open a GitHub Discussion
- **Real-time chat:** join our Discord (link in README)
- **Email:** community@plus8soft.com

Thanks for contributing.

— The Plus8Soft team
