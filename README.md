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
- **Jalali dates**: every date is displayed in the Solar Hijri calendar with
  Persian digits and relative labels («۱۲ روز مانده»), while storage stays ISO
  Gregorian so databases and backups remain compatible. Dates are chosen with a
  theme-aware Jalali picker.
- **Home screen widget**: the three pens closest to expiry, always one glance
  away.
- **Actionable notifications**: mark a pen as opened straight from the
  notification.

## Tech stack

- Kotlin, Jetpack Compose (Material 3), RTL forced layout
- Static Dark Theme (permanent, non-switchable) with embedded **Vazirmatn** Persian font (`res/font/vazirmatn_*.ttf`)
- Design system with tokens and a reusable component library (`ui/theme`, `ui/components`) — see [docs/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md)
- `navigation-compose` graph with animated transitions, edge-to-edge layout and a splash screen (`androidx.core:core-splashscreen`)
- Jalali (Solar Hijri) calendar conversion and Persian digit formatting implemented in-project (no dependency)
- R8 code shrinking, obfuscation, optimization, and resource shrinking enabled for release builds (`isMinifyEnabled = true`, `isShrinkResources = true`)
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
├── MainActivity.kt          # Edge-to-edge activity, intent/deep-link handling
├── MyPenApp.kt              # Application: notification channel + periodic work
├── data/                    # AppDatabase (SQLite) + PenRepository
├── model/                   # InsulinPen, HistoryEvent, enums, settings
├── notification/            # NotificationHelper, ExpiryCheckWorker, action receiver
├── ui/nav/                  # Route definitions + NavHost and app scaffold
├── ui/components/           # Design-system components (cards, chips, picker, ...)
├── ui/screens/              # Compose screens (dashboard, inventory, ...)
├── ui/theme/                # Palette, typography, shapes, tokens, semantics
├── util/                    # DateUtils, JalaliDate, PersianText, PriorityCalculator
└── widget/                  # Home screen widget provider (RemoteViews)

app/src/test/                # JVM unit tests + v0 backup fixture (classpath)
```

## Documentation

- [ARCHITECTURE.md](docs/ARCHITECTURE.md) — components, data model, backup format
- [REQUIREMENTS.md](docs/REQUIREMENTS.md) — functional/non-functional requirements
- [DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md) — colour, typography, spacing, motion and components
- [CHANGELOG.md](CHANGELOG.md) — release history (Keep a Changelog + SemVer)
- [RELEASES.md](RELEASES.md) — release process and artifact naming
- [SECURITY.md](SECURITY.md) — signing policy, vulnerability reporting
- [AGENTS.md](AGENTS.md) — permanent contributor/agent rules for this repo
- [.github/README.md](.github/README.md) — Persian (RTL) version of this README

## License

Proprietary — all rights reserved. See [LICENSE](LICENSE).
