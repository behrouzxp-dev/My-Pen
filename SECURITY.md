# Security Policy

## Reporting a vulnerability

Please do **not** open a public issue for security reports. Report
vulnerabilities privately to the repository owner (GitHub private
vulnerability reporting is currently disabled; the owner must enable it or
share a private contact channel).

Include: affected version (tag or commit), description, reproduction steps,
and impact. You can expect an acknowledgement and a fix plan; do not test
against production builds you do not own.

## Signing policy (release keystore)

- The release keystore lives **outside** this repository (never commit
  `*.jks`, `*.keystore`, `key.properties`, or `secrets.properties`;
  `.gitignore` enforces this).
- Release signing is read from environment variables or an untracked
  `gradle.properties` in `GRADLE_USER_HOME`:
  `MYPEN_KEYSTORE_PATH`, `MYPEN_STORE_PASSWORD`, `MYPEN_KEY_ALIAS`,
  `MYPEN_KEY_PASSWORD`.
- CI/release workflows decode the keystore from the GitHub secret
  `MYPEN_KEYSTORE_BASE64` at build time only.
- Debug builds use the default Android debug keystore.
- **Known historical exposure:** a release keystore password was committed
  in plaintext in `app/build.gradle.kts` up to 2026-11-09. The password must
  be rotated by the project owner (the keystore itself is not in the repo).
  Removing the value from the latest commit does not purge history; history
  remediation requires explicit owner approval.
- Never generate a replacement signing key for the published application
  without explicit owner authorization: it would break updates for existing
  installs.

## Handling of user data

- All insulin inventory data stays on-device (SQLite, `mypen_database.db`).
  The app declares **no INTERNET permission** and never transmits data.
- `android:allowBackup="false"` prevents `adb`/cloud backup extraction of
  the inventory.
- Backups are JSON files exported manually by the user via the share sheet;
  treat them as sensitive health information. The app never uploads them.
- The internal SQLite database is **not encrypted** currently; encryption
  (e.g. SQLCipher) is a documented future enhancement.
- Restore is destructive by design: it wipes and replaces all local data in
  a single atomic transaction after explicit user confirmation.

## Dependency and build security

- Dependencies are pinned in `app/build.gradle.kts`; the Compose BOM pins
  the Compose set. Review Dependabot/Renovate alerts when enabled.
- CI runs with least-privilege permissions (`contents: read`); the release
  workflow is the only job with `contents: write`.

## Supported versions

| Version | Supported |
| ------- | --------- |
| 1.0.x   | Yes       |

Only the latest 1.0.x release line receives fixes.
