# Handoff: Day Counter — 1×1 Android home-screen widget + companion app

## Overview
An Android app for day counters, each one tracking the days **until** a future date or **since** a past date. You place a counter on the home screen as a **1×1 widget** that shows only a big number. Counters are created and edited inside the app. You pick a counter when placing the widget.

## About the design files
The files in `design/` are **design references built in HTML**. They show the intended look and behavior and are not production code. Recreate them natively. The repo (`okdaithi/app-counter`) is currently empty, so the recommended stack is:
- **Kotlin + Jetpack Compose** for the app
- **Jetpack Glance** (`GlanceAppWidget`) for the widget
- **DataStore** (Proto or Preferences with JSON) or **Room** for counters
- A **WorkManager** daily job, plus a midnight `AlarmManager` refresh, so that numbers roll over at local midnight

Open `design/Day Counter.dc.html` in a browser to see the widget styles (1a–1c) and a working prototype (1d).

## Fidelity
**High-fidelity.** Colors, type, radii and behavior are final. The one decision still open is which widget style ships (1a Tile, 1b Ring or 1c Signed). Implement all three behind an internal enum so the product owner can choose; default to **Tile**.

## Product rules (source of truth)
1. **Counting.** `today` = start of the local day. `target` = start of the day of the counter date. `diff = round((target − today) / 1 day)`.
   - `diff > 0` → future ("until"). `diff < 0` → past ("since"). `diff == 0` → today (count 0).
   - `days = |diff| + (includeToday ? 1 : 0)`.
2. **Date reached.** A future counter automatically flips to counting "since" once its date passes. No user action is needed.
3. **Repeat yearly** (birthdays, anniversaries). The target becomes the **next occurrence** of that month and day (this year if it's still ahead or is today, otherwise next year), so it always counts "until". If Feb 29 falls in a non-leap year, use Feb 28.
4. **Units** (set per counter): days | weeks | months | years.
   - weeks = `floor(days / 7)`
   - months = the number of whole calendar months between the earlier and later date. Subtract 1 if the later date's day-of-month is less than the earlier one's. Minimum 0.
   - years = `floor(months / 12)`
5. **Widget number formatting.** Values under 1000 are shown as-is. Values from 1000 to 9999 are shown as `floor(n/100)/10` + "k", so 3916 becomes "3.9k" and 1000 becomes "1k". Values of 10000 and above are shown as `floor(n/1000)` + "k", so 12345 becomes "12k". Inside the app, numbers are always shown in full with grouping, e.g. "3,916".
6. **Unit suffix on the widget.** days → none. weeks → "w". months → "mo". years → "y". The suffix is small, at 11sp and 70% opacity, set right after the number.
7. **Widget size:** 1×1 only, not resizable. **Widget color:** fixed, not theme- or wallpaper-driven.
8. **Tap on the widget** opens the app directly on that counter's edit screen.
9. **Multiple widgets.** Each widget instance is bound to one counter ID. Several widgets can exist, and several may point at the same counter. If a counter is deleted, remove or invalidate every widget bound to it (show an empty state that opens the app).

## Screens / views

### A. Home screen widget (1×1)
The base size in the prototype is **68×68dp**; let Glance fill the launcher cell. The number is centered in Inter Medium (500) with letter-spacing −0.04em, line-height 1 and tabular figures.

Number size scales with the character count, where `len = chars + suffixChars × 0.6`:
- len ≤ 2 → 30sp
- ≤ 3 → 27sp
- ≤ 4 → 22sp
- ≤ 5 → 18sp
- otherwise 15sp

Each style marks "until" vs "since" differently, because the widget has no label.

**1a Tile (default)**
- Rounded square, radius 22dp, background `#232532`
- Edge: 1dp `#3f424d` ring, plus shadow `0 4 12 rgba(0,0,0,.35)`
- Number color: until → `#d2cefd` (accent-300); since/today → `#e9e9ed`

**1b Ring**
- Circle, background `#292b31`
- Until: 2dp border `#9184d9`, plus a glow of 16dp blur in `#9184d9` at 40% alpha
- Since: 2dp border `#3f424d`, no glow
- Number is always `#e9e9ed`

**1c Signed**
- Rounded square, radius 22dp, background `#232532` at 70% alpha (frosted; blur where supported), edge 1dp `#3f424d`
- A sign is set before the number, top-aligned: "−" for until, "+" for since. It is 13sp in `#b5abfc` (accent-400).
- Number is `#e9e9ed`

### B. Launcher placement flow
The HTML simulates this with a long press; on a real device it's the system widget picker plus a configuration activity.
1. The user adds the "Counter" widget (preview: 1×1, subtitle "1×1 · Days since or until a date").
2. **Configuration activity** (`android.appwidget.action.APPWIDGET_CONFIGURE`) titled **"Choose a counter"**, on a surface sheet with background `#232532` and top radius 22dp.
   - Each row: widget preview at 70% scale, then the title (14sp), then a subtitle (12sp, `#b2b6ca`).
   - Tapping a row binds the counter and finishes with RESULT_OK.
   - Empty state: "No counters yet. Create one in the Counter app first."
3. After placement the widget renders immediately. The prototype shows a "Widget added" toast.
4. Long-pressing the widget should offer **Reconfigure** ("Edit counter"). Use `widgetFeatures="reconfigurable"` on Android 12+. "Remove from home screen" is handled by the launcher.

### C. Counter list (app home)
- Background `#161826`. The status bar area is transparent.
- Title **"Counters"**: Inter 500, 26sp, letter-spacing −0.015em, padding 22/20/16.
- List: horizontal padding 14dp, gap 8dp between rows, bottom padding 110dp to clear the FAB.
- Row: background `#232532`, radius 8dp, padding 14×16dp. Two columns: text on the left (flexible) and the number on the right.
  - Left:
    - Title: 15sp, weight 500, single line with ellipsis
    - Subtitle: 12sp, `#b2b6ca`
    - Optional tag "On home screen" (tag-neutral: bg `#3f424d`, text `#f3f5fe`, 10.5sp, radius 6dp, padding 1×8) when any widget is bound to the counter
  - Subtitle copy:
    - Future: "Until 18 Nov 2026"
    - Past: "Since 12 Mar 2024"
    - Today: "Today, 24 Sep 2026"
    - Repeating: "Every 7 February"
  - Right, aligned to the end:
    - Number: Inter 500, 28sp, letter-spacing −0.03em, tabular figures, full with grouping. Color: until → `#d2cefd`, since → `#e9e9ed`.
    - Below it, 11sp in `#b2b6ca`: "days until" / "days since" (singular for 1: "day since"). Other units read "weeks since", "months until" and so on. It reads "today" when the date is today and Include today is off.
  - Hover/pressed: background mixes toward the accent (about 15% `#9184d9` over `#232532`).
- Empty state: "No counters yet. Tap + to add one." (14sp, `#b2b6ca`)
- **FAB:** 56×56dp, radius 16dp, bottom 40 / right 18. It is outlined, per Nocturne: 1dp `#9184d9` border, fill of `#9184d9` at 12% over the background, and a "plus" icon in `#9184d9` at 24dp. Pressed: fill at 22%. Tapping opens a new counter.
- Tapping a row opens its editor.

### D. Counter editor (new / edit)
- App bar: back arrow (icon button 36dp) · title "New counter" or "Edit counter" (Inter 500, 18sp) · **Save** button on the right (outlined primary: 1dp `#9184d9` border, text `#9184d9`, radius 8dp, 14sp/500).
- Scroll content: padding 4/18/40, vertical gap 18dp.
- **Live preview card:**
  - Radius 14dp. Background: a gradient from `#1d2036` to `#232532`, with a soft radial bloom of `#353b80` from the top left.
  - Contains the widget in the current style at 1.5×, centered.
  - Below it, a line in 12sp `#cfd3e5`, e.g. "55 days until 18 Nov 2026". It reads "Today" or "Pick a date" when those apply.
  - It updates on every field change.
- Fields, in order. Labels are 12sp at 70% text opacity; inputs have a min height of 36dp, background `#232532`, a 1dp border in divider color (text at 16%) and radius 8dp. Focused inputs take a `#9184d9` border.
  1. **Title**: text, placeholder "e.g. Trip to Japan". Required. Error: "Add a title."
  2. **Date**: date picker (Material 3 DatePicker, dark). Required. Error: "Pick a date." The default for a new counter is today.
  3. **Count in**: a segmented control with 4 equal options, Days / Weeks / Months / Years. The container has a 1dp divider border and radius 8dp. Selected: text `#9184d9` with an inset 1dp `#9184d9` outline.
  4. **Include today**: switch. Help text: "Adds one day to the count".
  5. **Repeat yearly**: switch. Help text: "Counts to the next anniversary".
  6. **Notes**: multiline, min height 90dp, placeholder "Optional".
  7. **Delete counter**: edit mode only. Secondary button with a trash icon, left-aligned. It deletes the counter, unbinds its widgets and returns to the list with a "Counter deleted" toast.
- Switch spec:
  - 38×22dp track, radius 11dp
  - Off: divider border, transparent track, 14dp knob in `#9397ab`
  - On: `#9184d9` border, track `#423a6a`, knob `#9184d9`
  - 150ms transition
- Validation happens on Save. Errors are 12sp in `#d2cefd`, 5dp below the field, and clear on the next edit.
- Save: create or update, return to the list, show the "Saved" toast, and refresh every widget bound to that counter.

### Copy tone
Plain and functional. No exclamation marks and no emoji.

## Interactions & motion
- The app opens over the launcher with opacity 0→1 and scale 0.94→1, 220–260ms, easing cubic-bezier(.2,.8,.2,1). Standard Android activity transitions are acceptable.
- The editor slides in from the right (translateX 100%→0, 280ms, same easing). Back reverses it.
- Bottom sheets translate up from 100%, 260ms. The scrim is `#05060c` at 55%.
- Toast: bg `#3f424d`, 12.5sp, radius 18dp, auto-hides after 1.8s. Use a Snackbar or Toast natively.
- Tap feedback on widgets and icons: scale to 0.94 while pressed.

## Data model
```kotlin
data class Counter(
  val id: String,
  val title: String,
  val date: LocalDate,       // stored ISO yyyy-MM-dd
  val unit: Unit,            // DAYS, WEEKS, MONTHS, YEARS
  val includeToday: Boolean,
  val repeatYearly: Boolean,
  val notes: String
)
// widget binding: appWidgetId -> counterId (Glance state or DataStore)
```
- `calc(counter, today) -> { n: Int, future: Boolean, diff: Int, target: LocalDate }` implements the rules above. Unit-test it for leap years, today, Feb 29 repeats and month-end dates.
- Widget refresh triggers:
  - local midnight
  - counter saved or deleted
  - time zone or date change (`ACTION_TIME_CHANGED`, `ACTION_TIMEZONE_CHANGED`, `ACTION_DATE_CHANGED`)
  - boot

## Design tokens (Nocturne)
Colors:
- bg `#161826` · surface `#232532` · text `#e9e9ed` · accent `#9184d9` · divider = text at 16%
- Neutral ramp 100–900: `#f3f5fe #e4e7f5 #cfd3e5 #b2b6ca #9397ab #75798c #595d6c #3f424d #292b31`
- Accent ramp 100–900: `#f5f4ff #e7e5fe #d2cefd #b5abfc #968ae0 #796cbf #5d5294 #423a6a #2b2741`
- Section glow (wallpaper/preview bloom): `#353b80` · section `#262a60`

Other tokens:
- **Type:** Inter only. Headings at weight 500 max; body 400. Scale in use: 26 / 22 / 18 / 15 / 14 / 13 / 12 / 11 / 10.5.
- **Spacing** (0.7× density): 2.8, 5.6, 8.4, 11.2, 16.8, 22.4. Round to the nearest dp.
- **Radii:** sm 4, md 8, lg 14. Widget 22, sheet 22, FAB 16.
- **Shadows:**
  - sm = 1dp ring `#3f424d`
  - md = 1dp ring `#595d6c` + `0 6 18 rgba(0,0,0,.55)`
  - lg = 1dp ring `#9397ab` + `0 16 40 rgba(0,0,0,.65)`
- **Rules:** primary buttons are outlined and never filled; don't flood areas with accent; no pure black or white; focus ring is 2dp `#9184d9`.

## Assets
- **Icons:** Phosphor (regular) — plus, arrow-left, trash, pencil-simple, x, caret-right, hourglass-medium (app icon glyph). Use the Phosphor Android vector drawables or export the SVGs.
- **Font:** Inter, from Google Fonts (downloadable font or bundled).
- The launcher chrome in the prototype (clock, other app icons, wallpaper) is illustrative only and should not be built.

## Files
- `design/Day Counter.dc.html`: all widget styles (1a–1c) and the interactive prototype (1d). Open it in a browser.
- `design/Widget.dc.html`: the 1×1 widget in all three styles.
- `design/styles.css`: the Nocturne token sheet.
- The counting and formatting logic is in the `<script>` of `Day Counter.dc.html`: `calc`, `short`, `wv` and `sub`. It is the reference implementation for the rules above.
