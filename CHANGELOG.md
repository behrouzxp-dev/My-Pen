# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Design system** foundation: design tokens (`ui/theme/Dimens.kt` for spacing,
  radius and motion), semantic colour mapping (`ui/theme/Semantics.kt`) and a
  reusable component library (`ui/components`: `AppCard`, `SectionHeader`,
  `StatusChip`/`TypeChip`/`EventChip`, `MetricCard`, `CountdownRing`,
  `ShelfLifeBar`, `PriorityIndicator`, `EmptyState`, `SettingsRow`,
  `TypeSelector`, `DateField`, `JalaliDatePickerDialog`, `LoadingSkeleton`).
  Documented in `docs/DESIGN_SYSTEM.md`.
- **Jalali (Solar Hijri) calendar** support without any new dependency
  (`util/JalaliDate.kt` + `util/PersianText.kt`): Jalali display dates, Persian
  digits, relative shelf-life labels, and a theme-aware Jalali date picker that
  replaces the platform Gregorian `DatePickerDialog`. Covered by
  `JalaliDateTest` including an exhaustive 1900–2100 round-trip.
- **Navigation graph** (`ui/nav/AppNavigation.kt`) built on `navigation-compose`
  with animated transitions, single-top tab navigation, state restoration and
  deep links from notifications (`OPEN_TAB`, `PEN_TYPE`, `PEN_ID`).
- **Home screen widget** (`widget/MyPenWidgetProvider` + `res/layout/widget_mypen.xml`)
  listing the three pens closest to expiry, updated on every data change.
- **Actionable notifications**: an inline "opened" action handled by
  `NotificationActionReceiver` marks a pen as opened without opening the app.
- **Edge-to-edge** layout and a branded **splash screen**
  (`androidx.core:core-splashscreen`).
- **Dashboard insights**: a dependency-free expiry-distribution chart, an
  urgent-pen hero card with a countdown ring, and metrics that count up on entry.
- **Inventory upgrades**: segmented type switcher, search, three sort orders,
  per-pen shelf-life bar and quick-action menu.
- **History timeline** grouped by day with per-type event counts.
- **Settings redesign** with grouped rows, an About dialog (version, features,
  disclaimer) and snackbar-based feedback.
- **Loading skeleton** instead of a blocking spinner on first launch.
- Embedded **Vazirmatn** (v33.003) Persian font family (`vazirmatn_regular.ttf`,
  `vazirmatn_medium.ttf`, `vazirmatn_bold.ttf`) and global Material 3
  `AppTypography` (`ui/theme/Type.kt`) applied across all UI screens and
  components.

### Changed

- Replaced the light theme with a permanent, non-switchable **Static Dark
  Theme** (`ui/theme/Theme.kt`, `ui/theme/Color.kt`, and
  `Theme.Material.NoActionBar` in `AndroidManifest.xml`) with dark-mode
  optimized card, badge, table-header, and status colors.
- Enabled **R8** code shrinking, obfuscation, optimization, and resource
  shrinking for release builds (`isMinifyEnabled = true`,
  `isShrinkResources = true` in `app/build.gradle.kts`) with verified
  `proguard-rules.pro` keeps.
- Replaced every ad-hoc card/colour combination with the neutral surface scale:
  coloured card borders are gone, and colour is now reserved for status and
  interactive elements (also applied to the top bar, which is no longer a
  saturated blue band).
- Replaced all `Toast` feedback with themable Material 3 snackbars that support
  actions, and moved hard-coded Persian strings into `strings.xml`.
- Persian typography is now tuned for the script: explicit line heights and
  `letterSpacing = 0.sp` on every Material 3 slot.
- Replaced the application launcher icon with the new multi-density adaptive
  and legacy launcher icon set (`mipmap-*/ic_launcher*.png`,
  `mipmap-anydpi-v26/ic_launcher*.xml`) and removed the old
  `drawable/ic_insulin_pen.xml` vector icon.

## [1.0.0] - 2026-11-09

First stable release. Originally imported as the Kotlin/Android baseline on
2026-10-29 (applicationId `com.arena.mypen`, versionCode 1000); this release
adds the audit remediation, the test suite, CI/release automation, full
documentation, and the application ID rename to `com.mypen.app`.

### Baseline features

- Inventory of long-acting/short-acting insulin pens with per-pen status
  (ACTIVE, OPENED, DEFECTIVE, EXPIRED).
- Consumption priority ranking by expiry date (equal expiry = equal rank).
- Atomic group entry of identical pens.
- Automatic transition of expired ACTIVE pens with history events.
- Dashboard metrics, inventory list, history log with type filters.
- Notification settings (30/14/7 days) with a periodic WorkManager check.
- JSON backup export/restore (`schemaVersion: 1`), atomic restore.
- Persian (RTL) UI built with Jetpack Compose.

### Security

- Removed the hardcoded release keystore credentials (`storePassword` /
  `keyPassword`) from `app/build.gradle.kts`. Release signing is now read
  from environment variables or an untracked `gradle.properties`.
  **The previously committed password must be rotated by the project owner.**
  See SECURITY.md.
- Debug builds now use the default debug keystore instead of the release
  signing configuration, so building never requires the release keystore.
- `android:allowBackup` is now `false`; the app's medical inventory data
  cannot be extracted via `adb backup` or cloud device backups.
- Release builds without configured signing secrets now build as intentionally
  UNSIGNED with a build-time warning instead of failing or silently signing.

### Fixed

- "Today" and all timestamps use the device's local timezone instead of UTC,
  so expiry transitions, remaining-day counts and the Persian calendar day
  boundaries are correct for the user.
- Expiry notifications fire as soon as a pen is within its configured window
  (days remaining ≤ 30/14/7) instead of only on the exact day; the existing
  `notification_logs` table still guarantees one notification per threshold
  per pen. On-resume notification checks also run off the main thread.
- `OPENED` pens whose expiry date has passed are now transitioned to
  `EXPIRED` automatically (with exactly one `EXPIRED` history entry per pen).
  `DEFECTIVE` pens keep their status.
- Group entry is capped at 100 pens per batch and rejects larger values.
- A failure while saving new pens now surfaces an error toast in the UI
  (previously the Save button stayed in a perpetual loading state).
- Deterministic, always-non-negative notification request codes.
- Database reads/writes for the main tabs and sub-screens now run on
  background coroutines (`Dispatchers.IO`) instead of synchronous calls in
  Compose composition on the main thread.

### Changed

- Application ID (and `namespace`) changed from `com.arena.mypen` to
  `com.mypen.app`, before the first stable release. The database engine,
  file names and backup format are unchanged; existing local data is
  unaffected because the database file is keyed by app sandbox, and this
  change precedes any published build.
- The notification small icon is a branded monochrome vector
  (`ic_notification`) instead of a platform framework icon.
- Add/Edit validation messages use string resources.

### Added

- Hermetic unit test suite: the v0 backup compatibility test loads
  `app/src/test/resources/sample_insulin_backup.json` from the classpath;
  new `DataModelsTest` covers JSON round-trips, enum parsing and calendar math.
- `ci.yml`: on pull requests to `main` (skipping documentation-only changes),
  runs unit tests and a debug build. `release.yml`: on approved `vX.Y.Z` tags,
  builds, signs, checksums and publishes `My Pen Vx.y.z.apk` directly.
- Project documentation: English README, Persian README (`.github/README.md`),
  SECURITY.md, RELEASES.md, docs/ARCHITECTURE.md, docs/REQUIREMENTS.md,
  LICENSE (proprietary), .gitattributes, .editorconfig, proguard-rules.pro.

### Removed

- 21 unused vector drawables and 17 unused string resources.
- `colors.xml` (duplicate color source); `Color.kt` is now authoritative.
