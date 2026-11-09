# Requirements — My Pen

Reverse-engineered from the v1.0.0 implementation. These describe the
behavior the code actually implements (plus the audit fixes applied in this
branch). They are not a product spec by the owner; where behavior was a
judgment call, it is marked *(decision)*.

## Functional requirements

1. **Inventory**
   - Register one or many identical insulin pens (`name`, `type`
     LONG_ACTING/SHORT_ACTING, `expiryDate`) in a single atomic operation.
   - Identical pens are stored as fully independent records (own UUID).
   - List ACTIVE pens per type (inventory tab), ordered by expiry date then
     name; assign each pen a consumption priority rank (equal expiry = equal
     rank) and a `daysRemaining` value.
   - Edit a pen's name/type/expiry; log an `EDITED` history event; clear the
     pen's notification dedup rows when the expiry date changes.

2. **Status lifecycle**
   - `ACTIVE` → `OPENED` (manual, with `openedDate`), → `DEFECTIVE` (manual,
     with `defectiveDate` and reason), → `EXPIRED` (automatic when
     `expiryDate <= today`).
   - *(decision, audit fix)* `OPENED` pens also transition to `EXPIRED`
     automatically. `DEFECTIVE` pens keep their status (defect supersedes
     expiry).
   - Expiry transition clears the pen's notification dedup rows.

3. **Dashboard**
   - Show counts: total ACTIVE, long-acting ACTIVE, short-acting ACTIVE,
     expiring soon (ACTIVE with `today <= expiryDate <= today + 30d`),
     EXPIRED.
   - Quick navigation to add-pen, inventory and the expired list.

4. **User interface (design system)**
   - Single design system: neutral surface scale with colour reserved for status
     and interactive elements; no decorative coloured card borders.
   - Vazirmatn typography for every Material 3 slot, with Persian-tuned line
     height and zero letter tracking.
   - Persian digits for all user-visible numbers.
   - Jalali (Solar Hijri) dates for display and input; ISO Gregorian storage.
   - Jalali date picker in place of the platform (Gregorian) picker.
   - Snackbar feedback (with actions) instead of toasts.
   - Static, non-switchable dark theme; no light theme and no dynamic colour.
   - Themed empty states, loading skeleton, countdown ring, shelf-life bar and
     expiry-distribution chart on the dashboard.
   - Inventory supports search, three sort orders, segmented type switching and
     per-pen quick actions; history groups events by day with type counts.
   - Edge-to-edge layout, animated navigation transitions and a splash screen.
   - Home screen widget listing the three pens closest to expiry.
   - Actionable expiry notifications ("opened" inline action).
   - Every meaningful icon exposes a `contentDescription`; touch targets are at
     least 48 dp.

5. **History**
   - Every state change appends a history event (`ENTRY`, `OPENED`,
     `DEFECTIVE`, `EXPIRED`, `EDITED`) with date, timestamp and Persian
     description.
   - Filter events by type; deleted pens show as «قلم حذف‌شده».
   - Permanent delete removes the pen **and** all of its events and
     notification rows.

6. **Notifications**
   - User-configurable thresholds at 30, 14 and 7 days before expiry
     (toggles, persisted in `settings` table).
   - Each enabled threshold notifies **once per pen** when the pen comes
     within the window (daysRemaining ≤ threshold); dedup via
     `notification_logs`. *(audit fix: no longer exact-day matching)*
   - A periodic WorkManager job (24h) and an app-resume check deliver the
     notifications; opening a notification deep-links to the inventory tab
     for that insulin type.
   - Requires `POST_NOTIFICATIONS` on Android 13+; denied permission
     silently suppresses notifications.

7. **Backup & restore**
   - Export full database to a versioned JSON document
     (`schemaVersion: 1`, see ARCHITECTURE.md) and share it via the system
     share sheet (JSON file named `mypen_backup_yyyyMMdd.json`).
   - Import validates the schema version and required sections, shows a
     confirmation dialog (counts, export date), then atomically replaces
     **all** local data. Failure aborts the transaction; prior data is
     preserved.
   - v0 backups (same format, exported 2026-10-18) are supported
     (compatibility test fixture in `app/src/test/resources/`).

8. **UI language and accessibility**
   - Persian (RTL) interface, Jetpack Compose (Material 3). Language is Persian only.
   - Permanent, non-switchable **Static Dark Theme** (`darkColorScheme`) across all screens and system dialogs.
   - Embedded **Vazirmatn** Persian font family applied globally across all text styles.
   - Android 7.0+ (minSdk 24), target 34.

## Non-functional requirements

- **Privacy**: no INTERNET permission; no data leaves the device except via
  the user-initiated backup share sheet; `allowBackup=false`; no telemetry.
- **Reliability**: DB mutations are transactional; backup restore is atomic;
  grouped entry is atomic and validated.
- **Performance**: main tabs load data on background coroutines; the
  Inventory query is indexed (`pens(status, type)`).
- **Security**: release signing via environment/secrets only (see
  SECURITY.md); the internal DB is currently unencrypted (documented debt).
- **Testing**: unit tests are hermetic; CI (`.github/workflows/ci.yml`)
  runs them plus a debug build on PRs to `main`.
- **Release**: SemVer, annotated Persian tag descriptions,
  `My Pen Vx.y.z.apk` artifact naming, published directly (never draft)
  by `.github/workflows/release.yml` after owner approval (see RELEASES.md).

## Out of scope (v1.0.0)

- Multiple user profiles, cloud sync, remote reminders, encryption at rest,
  Room migration, English localization, widgets/wear support.
