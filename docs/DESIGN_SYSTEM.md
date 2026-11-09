# Design System — My Pen

Status: describes the UI foundation introduced in the "professional UI"
overhaul. Keep this file synchronized with `ui/theme` and `ui/components`.

## Principles

1. **One neutral surface scale, one accent.** Saturated colour is reserved for
   meaning (status, insulin type, destructive actions). Cards never carry a
   decorative coloured border.
2. **Persian first.** Vazirmatn, generous line height, zero letter tracking,
   Persian digits everywhere, Jalali dates for display.
3. **Calm motion.** Short, purposeful transitions (150–400 ms); numbers count up
   once on entry; nothing loops in the background.
4. **Static dark.** The theme is permanent; there is no light theme and no
   dynamic colour, so screenshots and support conversations are reproducible.
5. **No new dependency without a reason.** Charts, rings, the segmented control
   and the date picker are drawn with Compose primitives.

## Tokens

### Colour (`ui/theme/Color.kt`)

| Token | Value | Usage |
| ----- | ----- | ----- |
| `BrandNavy` | `#305892` | Brand identity (matches launcher icon) |
| `BackgroundBase` | `#0A111C` | Page background |
| `SurfaceBase` | `#111A27` | Cards, top bar, sheets |
| `SurfaceContainerColor` | `#141F2E` | Segmented control track, snackbars |
| `SurfaceHigh` | `#1A2739` | Raised elements |
| `AccentBlue` | `#7CB6F2` | Primary interactive colour |
| `AccentTeal` / `AccentViolet` | `#57D9C6` / `#B5A8FF` | Secondary accents |
| `OnSurfaceColor` | `#E9EFF7` | Primary text |
| `OnSurfaceMuted` | `#A6B6C9` | Secondary text |
| `OutlineVariantColor` | `#243244` | Hairlines and card borders |

Semantic status colours (`StatusActive`, `StatusOpened`, `StatusDefective`,
`StatusExpired`, `StatusExpiringSoon`) are mapped to domain enums in
`ui/theme/Semantics.kt`; screens must not hard-code status colours.

### Typography (`ui/theme/Type.kt`)

Vazirmatn in three weights (Regular / Medium / Bold). Every Material 3 slot is
overridden; `letterSpacing = 0.sp` and line height ≈ 1.5–1.7 × size, which is what
Persian script needs.

### Spacing, radius, motion (`ui/theme/Dimens.kt`)

- `Spacing`: 2, 4, 8, 12, 16, 24, 32, 48 dp
- `Radius`: 10, 14, 18, 24 dp, plus `pill`
- `Motion`: 150 / 250 / 400 / 700 ms, `standard` and `emphasized` curves

## Components (`ui/components`)

| Component | File | Notes |
| --------- | ---- | ----- |
| `AppCard` | `AppCard.kt` | The only card container: radius `lg`, 1 dp outline, surface tone |
| `SectionHeader` | `AppCard.kt` | Section label above card groups |
| `AppPill`, `StatusChip`, `TypeChip`, `EventChip` | `Chips.kt` | Status/type/event badges with a coloured dot |
| `MetricCard`, `AnimatedCounter` | `Metrics.kt` | Dashboard tiles; the value counts up on first composition |
| `CountdownRing`, `ShelfLifeBar`, `PriorityIndicator` | `Metrics.kt` | Canvas/Box based indicators, no chart dependency |
| `EmptyState` | `EmptyState.kt` | Tinted icon + title + description + optional CTA |
| `MyPenTopBar`, `MyPenLargeTopBar` | `AppTopBar.kt` | Surface-coloured bars with a hairline separator |
| `MyPenBottomBar` | `AppBottomBar.kt` | Four tabs, badge for pens expiring soon |
| `SettingsRow`, `RowDivider` | `SettingsRow.kt` | Grouped settings rows |
| `AppMessages` | `Feedback.kt` | Snackbar facade (`info` / `success` / `error` / `undo`) exposed via `LocalAppMessages` |
| `TypeSelector` | `TypeSelector.kt` | Segmented control for insulin type |
| `DateField`, `JalaliDatePickerDialog` | `DateField.kt`, `JalaliDatePicker.kt` | Jalali/Solar Hijri picker, theme-consistent |
| `LoadingSkeleton` | `LoadingSkeleton.kt` | Pulsing placeholders for the first IO pass |

## Date policy

- **Storage**: ISO Gregorian `yyyy-MM-dd` (unchanged, keeps the database and the
  `schemaVersion: 1` backup format valid).
- **Display**: Jalali (Solar Hijri) through `util/JalaliDate.kt`, formatted in
  `DateUtils.displayDate` / `displayDateLong` / `displayDateTime`.
- **Relative labels**: `DateUtils.remainingLabel` yields «۱۲ روز مانده»،
  «امروز منقضی می‌شود»، «۳ روز گذشته».
- **Input**: `JalaliDatePickerDialog`; the platform picker is not used because it
  renders a Gregorian calendar.

## Number policy

All user-visible numbers use Persian digits (`util/PersianText.kt`). Storage,
parsing and tests keep Latin digits.

## Accessibility checklist

- Every icon that conveys meaning has a `contentDescription` from `strings.xml`.
- No fixed-height button depends on text size except primary CTAs, which use
  `height(50.dp)` with a label that never wraps.
- Touch targets ≥ 48 dp for icon buttons.
- Text contrast: `OnSurfaceColor` on `SurfaceBase` ≈ 14:1, `OnSurfaceMuted` ≈ 7:1.
- Snackbars announce errors through `LiveRegion` (Material 3 default) instead of
  toasts.
