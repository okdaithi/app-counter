# Day Counter

A 1×1 Android home-screen widget and companion app for counting days until or since a date.
Built from the design handoff in [`docs/design-handoff/`](docs/design-handoff/README.md), which is the extracted copy of `Countdown widget app design.zip`.

## Stack
- Kotlin, Jetpack Compose (app), Jetpack Glance (widget)
- Preferences DataStore with JSON for counters and widget bindings
- An inexact midnight alarm, time/date/time-zone/boot receivers and a daily WorkManager backstop keep widgets current

## Modules
- `core/`: pure Kotlin counting and formatting rules (`CounterMath`, `CounterFormat`), unit-tested on the JVM
- `app/`: Android app, widget and configuration activity

## Widget style
All three handoff styles are implemented. Choose one in
`app/src/main/java/com/okdaithi/daycounter/widget/WidgetStyle.kt` (`WidgetConfig.style`, default `TILE`).
The widget face is drawn to a bitmap by `WidgetRenderer`, so Inter, letter-spacing, tabular figures and the ring glow match the design.

## Build
```
./gradlew :core:test :app:testDebugUnitTest :app:assembleDebug
```
Requires JDK 17+ and the Android SDK (compileSdk 35). CI runs the same commands on every push.

## Licences
Inter font: SIL Open Font License (`docs/INTER-LICENSE.txt`). Icons: Phosphor Icons, MIT.
