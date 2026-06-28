# Wird

Offline-first, ad-free, zero-tracking Muslim companion app for Android.

Four deep pillars — dismiss-proof Fajr alarm, Hifz (memorization) system, reading
habit/khatm planner, and recitation mistake-detection — on a clean, fully-offline
Quran reader + prayer-times core. See [DEV_PLAN.md](DEV_PLAN.md) for the full plan.

## Tech stack

Kotlin · Jetpack Compose · Room · Hilt · Media3/ExoPlayer · adhan2 (prayer times)
· AlarmManager (exact) · WorkManager. Multi-module, MVVM + unidirectional data flow,
Room as the single source of truth.

## Modules

```
:app                 nav host, DI wiring, app entry
:core:common         result types, coroutine dispatcher qualifiers
:core:ui             Material3 theme, Arabic typography, shared composables
:core:database       Room db, DAOs, entities (prepackaged .db loader to come)
:feature:quran       reader (surah/juz nav, translation toggle, word-by-word)
:feature:prayer      times, qada ledger, adhkar, auto-DND
:feature:qibla       sensor-fusion compass + AR mode
:feature:alarm       dismiss-proof Fajr alarm engine
:feature:hifz        SRS, tikrar loop, progressive blanking, heatmap
:feature:recitation  mic capture + forced-alignment mistake detection
:data:audio          per-ayah audio download/cache, reciter management
```

Shared build configuration lives in `build-logic/` as Gradle convention plugins
(`wird.android.application`, `wird.android.library`, `wird.android.compose`,
`wird.android.feature`, `wird.android.hilt`), so each module's build file stays minimal.

## Build

Requires the Android SDK and a JDK 17+ (Android Studio's bundled JBR works).

```bash
./gradlew assembleDebug        # build all modules' debug variants
./gradlew :app:assembleDebug   # build just the app APK
```

Create a `local.properties` with `sdk.dir` pointing at your Android SDK (Android
Studio generates this on first open).

## Toolchain versions

AGP 8.7.3 · Gradle 8.10.2 · Kotlin 2.1.0 · KSP 2.1.0-1.0.29 · Hilt 2.54 · Room 2.6.1
· Compose BOM 2024.12.01 · compileSdk/targetSdk 35 · minSdk 26.
