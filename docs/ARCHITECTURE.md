# Architecture — My Pen

Status: reverse-documented from the v1.0.0 implementation (no earlier spec
exists). Keep this file synchronized with the code.

## Overview

My Pen is a single-activity Android app (Compose UI) that manages an
insulin-pen inventory locally on the device. There is **no backend, no
INTERNET permission, and no network access**. All state lives in a local
SQLite database; backups are explicit, user-triggered JSON files.

```
┌────────────────────────────────────────────────┐
│ UI layer (Jetpack Compose, Material 3, RTL)    │
│   Dashboard · Inventory · History · Settings   │
│   AddPen · PenDetails · ExpiredPens            │
└───────────────┬────────────────────────────────┘
                │ calls on background coroutines (Dispatchers.IO)
┌───────────────▼────────────────────────────────┐
│ Domain / data access                           │
│   PenRepository (singleton, @Synchronized)     │
│   PriorityCalculator · DateUtils               │
└───────────────┬────────────────────────────────┘
┌───────────────▼────────────────────────────────┐
│ Persistence                                    │
│   AppDatabase (SQLiteOpenHelper singleton)     │
│   mypen_database.db                            │
└────────────────────────────────────────────────┘

Notifications: MyPenApp → WorkManager (24h periodic ExpiryCheckWorker)
             + onResume check → NotificationHelper.checkAndSendNotifications
             → PenRepository → NotificationManagerCompat
```

## Key components

### AppDatabase (`data/AppDatabase.kt`)
`SQLiteOpenHelper`, version 1, database file `mypen_database.db`.
Singleton via `getInstance()` using double-checked locking on the
application context. Foreign-key constraints are enabled (`onConfigure`),
but no explicit foreign keys are declared yet (see Known limitations).

Tables:

| Table | Purpose | Primary key |
| ----- | ------- | ----------- |
| `pens` | Insulin pen stock | `id` (TEXT, UUID) |
| `history_events` | Audit log of all events | `id` (INTEGER AUTOINCREMENT) |
| `notification_logs` | Per-(pen, threshold) notification dedup | `(pen_id, threshold_days)` |
| `settings` | App settings key/value (`notify_30`, `notify_14`, `notify_7`) | `key` |

Indexes: `pens(status, type)`, `pens(expiry_date)`,
`history_events(pen_id)`, `history_events(event_type)`.

`onUpgrade` currently does nothing — there is no migration framework yet;
bump `DATABASE_VERSION` and add a migration when the schema changes.

### PenRepository (`data/PenRepository.kt`)
Single source of truth for reads and writes. Multi-step writes run in
transactions (`beginTransaction`/`setTransactionSuccessful`). Public
mutation methods are `@Synchronized`. Notable behaviors:

- `addPens` — validates name/expiry/count (1..`MAX_PENS_PER_ENTRY` = 100),
  inserts N pens + N `ENTRY` history rows atomically.
- `checkAndTransitionExpiredPens` — `ACTIVE` and `OPENED` pens with
  `expiry_date <= today` become `EXPIRED`; exactly one `EXPIRED` history
  entry per pen (deduplicated); notification logs for those pens are
  cleared. Called before reads/writes that depend on current state.
- `getActivePens(type, today)` / `getAllActivePens(today)` — status
  `ACTIVE`, ordered by `expiry_date ASC, name ASC`, then assigned
  consumption priorities (ties on expiry share the same rank).
- `getExpiredPens`, `getPenById`, `getPenHistory(id)`,
  `getAllHistory(filterType)` — `getAllHistory` LEFT JOINs pens to display
  the pen name; deleted pens show as «قلم حذف‌شده» (this also covers orphan
  events inside imported backups).
- Backup: `exportBackupJson()` / `restoreFromJson(json)` — see below.

### Backup format (`schemaVersion: 1`)

```json
{
  "schemaVersion": 1,
  "exportedAt": "2026-10-18 10:00:00",
  "pens": [ { "id": "…", "name": "…", "type": "LONG_ACTING",
              "expiryDate": "2026-10-25", "status": "ACTIVE",
              "entryDate": "2026-09-01", "openedDate": null,
              "defectiveDate": null, "defectiveReason": null } ],
  "events": [ { "id": 1, "penId": "…", "eventType": "ENTRY",
                "eventDate": "2026-09-01", "eventTime": "2026-09-01 09:00:00",
                "description": "…" } ],
  "notificationSettings": { "notify30Days": true, "notify14Days": true, "notify7Days": true }
}
```

Nullable fields are serialized as JSON `null` and must be read via
`isNull()`/`optX()`. Restore is **atomic and destructive**: it wipes
`notification_logs`, `history_events` and `pens` inside one transaction and
re-inserts everything from the backup (explicit ids are preserved for
events). Any error aborts the transaction and preserves prior data.
Notification logs are not restored; after a restore, expiry reminders for
the restored pens may be re-sent once.

A real v0 backup (`exportedAt: 2026-10-18 10:00:00`, 8 pens, 11 events)
is kept at `app/src/test/resources/sample_insulin_backup.json` and asserted
on in `MyPenCompatibilityTest`.

### Date handling (`util/DateUtils.kt`)
All "today" computations use the **device's local timezone**
(`TimeZone.getDefault()`). Dates are `yyyy-MM-dd`; datetimes are
`yyyy-MM-dd HH:mm:ss`. Day arithmetic uses a Howard Hinnant-style
days-from-civil algorithm (verified against leap years in unit tests).

### Priority ranking (`util/PriorityCalculator.kt`)
Pens are sorted by `expiryDate ASC`, then name (case-insensitive), then id.
Rank starts at 1 and increments only when the expiry date changes, so
groups sharing a date share a rank. `daysRemaining = expiryDate − today`.

### Notifications (`notification/NotificationHelper.kt`)
- Channel `mypen_expiry_channel` (high importance) created on app start.
- `MyPenApp.onCreate` enqueues a unique periodic `ExpiryCheckWorker`
  (24h, `ExistingPeriodicWorkPolicy.KEEP`).
- `MainActivity.onResume` also triggers a check **on `Dispatchers.IO`**.
- For each ACTIVE pen, for each enabled threshold (30/14/7 days), a
  notification fires once the pen is within the window
  (`daysRemaining ∈ 0..threshold`) and the `(pen_id, threshold_days)` pair
  is absent from `notification_logs`. On success the row is inserted.
- Notification tap intent opens the `INVENTORY` tab with the pen's type.

### UI layer (`ui/`)

Single `Activity` (`MainActivity`, edge-to-edge with a branded splash screen)
hosting a `navigation-compose` graph (`ui/nav/AppNavigation.kt`):

```
NavHost(startDestination = "dashboard")
├── dashboard            # tab
├── inventory            # tab
├── history              # tab
├── settings             # tab
├── add_pen
├── expired
└── pen/{penId}
```

The four tabs share one `Scaffold` (top bar, bottom navigation with an
"expiring soon" badge, extended FAB, snackbar host); sub-screens render their
own `Scaffold` with a back arrow. Navigation uses animated transitions and
`popUpTo(start) { saveState = true }` + `restoreState` for tab switching.
Intents from notifications carry `OPEN_TAB`, `PEN_TYPE` and `PEN_ID`; the
activity mirrors them into Compose state so a notification tapped while the
activity already exists still opens the requested pen.

Screen data is collected via `produceState` keyed on a `dataVersion` counter;
bumping `dataVersion` reloads everything and refreshes the home screen widget.
Repository calls run on `Dispatchers.IO`.

The layout direction is forced RTL in `MyPenTheme`, which applies a permanent
static dark color scheme (`Color.kt`), Vazirmatn typography (`Type.kt`) and the
shape scale (`Theme.kt`). Spacing/radius/motion tokens live in `Dimens.kt`,
domain-to-colour mapping in `Semantics.kt`, and reusable UI pieces in
`ui/components` (see `docs/DESIGN_SYSTEM.md`).

Dates are stored as ISO Gregorian strings and rendered as Jalali (Solar Hijri)
through `util/JalaliDate.kt`; user-visible numbers use Persian digits from
`util/PersianText.kt`. Feedback is delivered through snackbars
(`AppMessages` / `LocalAppMessages`) rather than toasts.

### Widget and notification actions
`widget/MyPenWidgetProvider` (platform `RemoteViews`, no extra dependency)
renders the three pens closest to expiry and is refreshed via `updateAll()`
after every mutation. `notification/NotificationActionReceiver` handles the
inline "opened" action of expiry notifications.

## Known limitations / deliberate decisions

- Raw SQLite, no Room migrations; schema changes require manual
  `onUpgrade` handling and a `DATABASE_VERSION` bump.
- No foreign keys between `history_events` and `pens` (deletion is handled
  in code by design); `setForeignKeyConstraintsEnabled(true)` is kept for
  future use.
- The internal database is not encrypted (see SECURITY.md).
- `OPENED` pens past expiry transition to `EXPIRED` (as of the audit fixes);
  `DEFECTIVE` pens never auto-transition.
- Backup notification logs are not preserved across restore.
- `versionCode = 1000`, `versionName = "1.0.0"`; versioning is documented in
  RELEASES.md.
- Persian is the only supported UI locale; strings live in
  `res/values/strings.xml`, static dark theme colors in `ui/theme/Color.kt`,
  design tokens in `ui/theme/Dimens.kt`, and Vazirmatn font typography in
  `ui/theme/Type.kt`.
- Jalali conversion is a pure integer algorithm valid for 1264–1500 Jalali
  (well beyond the app's usable range); ISO Gregorian remains the storage format.
- The home screen widget reads the database on the main thread of the widget
  update callback (`AppWidgetProvider.onUpdate`); it is bounded to three rows.
- R8 code shrinking, obfuscation, optimization, and resource shrinking are
  enabled for release builds (`isMinifyEnabled = true`,
  `isShrinkResources = true`); `proguard-rules.pro` preserves backup models,
  `ExpiryCheckWorker`, and Compose metadata.
