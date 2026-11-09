# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

## [1.0.0] - 2026-10-29

Initial import of the My Pen v1.0.0 Kotlin/Android baseline
(applicationId `com.arena.mypen`, versionCode 1000):

- Inventory of long-acting/short-acting insulin pens with per-pen status
  (ACTIVE, OPENED, DEFECTIVE, EXPIRED).
- Consumption priority ranking by expiry date (equal expiry = equal rank).
- Atomic group entry of identical pens.
- Automatic transition of expired ACTIVE pens with history events.
- Dashboard metrics, inventory list, history log with type filters.
- Notification settings (30/14/7 days) with a periodic WorkManager check.
- JSON backup export/restore (`schemaVersion: 1`), atomic restore.
- Persian (RTL) UI built with Jetpack Compose.
