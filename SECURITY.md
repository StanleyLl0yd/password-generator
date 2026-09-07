# Security Policy

## Supported versions

Password Generator follows a latest-release support model.

| Version | Supported |
| --- | --- |
| Latest published release | Yes |
| Older releases | No |

Security fixes are normally shipped in a new release rather than backported to older versions.

## Reporting a vulnerability

Use GitHub private vulnerability reporting for this repository when the **Report a vulnerability** option is available. Do not disclose a suspected vulnerability, zero-day, credential, signing material, token, private key, generated password, or other sensitive detail in a public issue, discussion, pull request, commit message, or log.

If private vulnerability reporting is unavailable, open a minimal public issue titled `Security contact request` without technical details. A private channel can then be arranged before sensitive information is shared.

Include privately:

- the affected app version or commit;
- Android version and device model when relevant;
- clear reproduction steps;
- expected and actual behavior;
- expected security impact;
- relevant logs or screenshots with secrets and generated passwords removed.

If testing may expose credentials, signing material, or another user's data, stop after collecting the minimum evidence needed to report the issue safely.

## Triage process

- Initial acknowledgement target: within 3 business days.
- Initial severity and scope assessment target: within 7 business days.
- Valid reports are reproduced when practical, assigned a severity, and tracked privately until a fix or mitigation is available.
- Critical and high-impact issues are prioritized over feature work.
- Disclosure timing is coordinated after affected users have a reasonable opportunity to update.

These are response targets, not a guarantee of a specific remediation date.

## Security scope

In scope:

- Android application code and packaged resources;
- password generation, clipboard handling, screen-capture protection, and local preference storage;
- Android manifest, exported components, permissions, and unintended network exposure;
- dependency and build-toolchain risks introduced by this repository;
- GitHub Actions, CI/CD, release signing, checksums, provenance, and release integrity;
- repository-secret exposure or supply-chain weaknesses caused by repository configuration.

Generally out of scope unless this repository directly causes or amplifies the issue:

- vulnerabilities in GitHub, Android, device firmware, RuStore, Google Play, browsers, keyboards, or other third-party infrastructure;
- social engineering and phishing;
- denial-of-service requiring unrealistic local resource exhaustion;
- findings that require a rooted or otherwise compromised device and do not cross an additional Password Generator trust boundary.

## Security model

Password Generator intentionally minimizes attack surface:

- no Android `INTERNET` permission;
- no backend, accounts, analytics, advertising, tracking, or cloud synchronization;
- generated passwords are not persisted by the application;
- Android backup is disabled and generator preferences are excluded from transfer rules;
- only the vibration permission is requested;
- release signing material is supplied only through the protected GitHub release environment and is never stored in the repository;
- release APK/AAB signatures and the expected signing certificate are verified before publication;
- release artifacts receive SHA-256 checksums and OIDC-backed GitHub artifact attestations;
- external GitHub Actions are pinned to immutable commit SHAs and workflow containers are digest-pinned;
- Gradle dependency verification metadata and wrapper validation protect dependency/build-tool integrity;
- CodeQL, Semgrep, Gitleaks, Dependency Review, Qodana, Android Lint, Detekt, Gradle dependency verification, and Dependabot provide layered automated analysis;
- the default branch requires the repository's aggregate verification gate before merge.

Never commit a keystore, `key.properties`, private key, token, `.env` file, service-account credential, generated password, or other credential material.
