# My Pen

A Persian (RTL) Android app for tracking insulin pen inventories: expiry dates,
consumption priority, notifications, and backup/restore.

## Overview

My Pen (`com.mypen.app`) helps people on insulin therapy manage their pen
stock:

- **Inventory** of long-acting and short-acting insulin pens, with per-pen
  status (`ACTIVE`, `OPENED`, `DEFECTIVE`, `EXPIRED`).
- **Consumption priority**: pens are ranked by expiry date (earliest first);
  pens that share an expiry date share the same rank.
- **Group entry**: register several identical pens in one atomic transaction;
  each pen gets its own independent record.
- **Expiry notifications**: configurable thresholds at 30, 14 and 7 days
  before expiry, deduplicated via a local log table. A periodic WorkManager
  job and an on-resume check deliver reminders once per threshold per pen.
- **Automatic expiry transition**: pens whose expiry date has passed are
  moved to `EXPIRED` automatically (with one history entry each).
- **History**: an audit log of all events (entry, opened, defective, expired,
  edited).
- **Backup & restore**: full export/import of the database as a versioned
  JSON document (`schemaVersion: 1`). Restore is atomic and fully replaces
  current data after confirmation.

## Tech stack

- Kotlin, Jetpack Compose (Material 3), RTL forced layout
- Raw SQLite via `SQLiteOpenHelper` (singleton), no Room
- WorkManager for periodic expiry checks
- Manual `org.json` (de)serialization for backups
- Local-timezone aware date handling (`java.util.Calendar`/`SimpleDateFormat`)

## Requirements

- JDK 17
- Android SDK Platform 34 (compileSdk/targetSdk = 34), minSdk = 24
- Gradle 8.6+ (CI installs Gradle 8.7; no wrapper is committed yet — see
  RELEASES.md and the audit notes)

## Build and test

```bash
# Unit tests (hermetic; no device required)
gradle testDebugUnitTest

# Debug APK (no signing keystore needed)
gradle assembleDebug

# Release APK (unsigned unless signing secrets are provided, see SECURITY.md)
gradle assembleRelease
```

CI runs the same steps on every pull request targeting `main`
(`.github/workflows/ci.yml`). Documentation-only changes do not trigger CI.

## Project structure

```
app/src/main/java/com/mypen/app/
├── MainActivity.kt          # App shell, navigation, background data loading
├── MyPenApp.kt              # Application: notification channel + periodic work
├── data/                    # AppDatabase (SQLite) + PenRepository
├── model/                   # InsulinPen, HistoryEvent, enums, settings
├── notification/            # NotificationHelper, ExpiryCheckWorker
├── ui/screens/              # Compose screens (dashboard, inventory, ...)
├── ui/theme/                # Color scheme (single source of color truth)
└── util/                    # DateUtils (local TZ), PriorityCalculator

app/src/test/                # JVM unit tests + v0 backup fixture (classpath)
```

## Documentation

- [ARCHITECTURE.md](docs/ARCHITECTURE.md) — components, data model, backup format
- [REQUIREMENTS.md](docs/REQUIREMENTS.md) — functional/non-functional requirements
- [CHANGELOG.md](CHANGELOG.md) — release history (Keep a Changelog + SemVer)
- [RELEASES.md](RELEASES.md) — release process and artifact naming
- [SECURITY.md](SECURITY.md) — signing policy, vulnerability reporting
- [AGENTS.md](AGENTS.md) — permanent contributor/agent rules for this repo
- [.github/README.md](.github/README.md) — Persian (RTL) version of this README

## License

Proprietary — all rights reserved. See [LICENSE](LICENSE).
