# Wird — Muslim Companion App — Development Plan

**Positioning:** Offline-first, ad-free, zero-tracking. Go deep on four pillars (Fajr alarm, Hifz, reading habit, recitation mistake-detection); keep everything else clean and correct. Trust is the moat — no ad SDKs, no analytics that leave the device.

**Stack:** Kotlin · Jetpack Compose · Room · Hilt · Media3/ExoPlayer · Adhan (prayer times) · AlarmManager (exact) · WorkManager (daily reschedule only).

---

## 1. Architecture

Multi-module, offline-first, MVVM + unidirectional data flow. Single source of truth = Room. Repositories expose `Flow`s; ViewModels hold `StateFlow` UI state.

```
:app                      // nav host, DI wiring, app entry
:core:common              // result types, dispatchers, date/hijri utils
:core:database            // Room db, DAOs, entities, prepackaged .db loader
:core:ui                  // design system, theme, reusable composables, Arabic typography
:feature:quran            // reader (surah/juz nav, translations toggle, word-by-word)
:feature:prayer           // times, qada ledger, adhkar, auto-DND
:feature:qibla            // sensor-fusion compass + AR mode
:feature:alarm            // dismiss-proof alarm engine (Fajr hook)
:feature:hifz             // SRS, tikrar loop, progressive blanking, heatmap
:feature:recitation       // mic capture + forced-alignment mistake detection
:data:audio               // per-ayah audio download/cache, reciter management
```

Rationale for prepackaged Room DB: ship the Quran + translations as a built `.db` in `assets/` and open with `createFromAsset()`. Faster first launch than parsing JSON, and keeps the app fully usable with zero network.

---

## 2. Data model (Room)

**Quran content (read-only, shipped):**
- `surah` — number, name_ar, name_en, name_bn, name_translit, revelation_place, ayah_count
- `ayah` — id, surah_no, ayah_no, text_uthmani, juz, hizb, page, sajda_flag
- `translation` — ayah_id, edition (`sahih_intl` | `muhiuddin_khan` | …), text  *(one row per ayah per edition)*
- `word` — ayah_id, position, text_ar, translit, translation_en, root  *(powers tap-to-define + root lookup)*

**User state (read-write):**
- `bookmark` / `last_position` — ayah_id, scroll offset, updated_at
- `hifz_item` — ayah_id, status, **ease_factor, interval_days, due_date, last_reviewed, lapses** (SM-2 fields)
- `mistake_log` — ayah_id, word_position, mistake_type, created_at  *(feeds the heatmap)*
- `prayer_log` — date, prayer, status (`on_time` | `late` | `missed` | `qada_done`)
- `qada_ledger` — prayer, owed_count  *(decrement as made up)*
- `alarm_config` — prayer, enabled, dismissal_task, sound_uri, pre_offset_min
- `khatm_plan` — start_date, target_date, daily_portion, last_read_ayah_id

Index `ayah(surah_no, ayah_no)`, `translation(ayah_id, edition)`, `hifz_item(due_date)`.

**⚠ Data licensing — resolve before shipping content.** Arabic Uthmani text from Tanzil must be reproduced unmodified with attribution per their terms. Translations are often copyrighted: confirm redistribution rights for Sahih International (English) and Muhiuddin Khan (Bangla) — pull from a clearly-licensed source (e.g. quranenc.com / alquran.cloud editions) and keep the license notes in-repo. Per-ayah audio (e.g. everyayah.com style: one mp3 per ayah) simplifies tikrar timing — no need for word-level timestamp files.

---

## 3. Phase plan

### Phase 0 — Foundation
- Module skeleton, Hilt, Compose nav, theme + Arabic font (e.g. a verified Uthmani/Hafs font, RTL handling).
- Build the prepackaged `.db`: write an ingestion script (off-device) that loads Tanzil text + chosen translations + word-by-word into the Room schema, ship as asset.
- Lock the no-ads / local-first decision: no GMS-mandatory deps where avoidable, no analytics SDK.

### Phase 1 — Core, done clean
- **Reader:** surah list + juz list navigation, ayah view with independent EN/BN toggles, resume-last-position, bookmarks, continuous scroll within surah/juz.
- **Prayer times:** Adhan library — 5 times + sunrise. User-configurable calculation method (default *Karachi / Hanafi asr* for BD) and high-latitude rule. Today + monthly view.
- **Location:** Fused Location (or `LocationManager` for a GMS-free build) + manual city search fallback so it works without GPS.
- **Qibla v1 (2D):** see §4.1.
- **Basic notifications** for prayer times (non-dismiss-proof) to have something shippable.

Milestone: a clean, ad-free, fully-offline reader + prayer-times app. Already beats much of the market on cleanliness.

### Phase 2 — Pillar 1: dismiss-proof Fajr alarm (marketing wedge)
- **Exact scheduling:** `AlarmManager.setAlarmClock()`. Declare `USE_EXACT_ALARM` (Android 13+, alarm-clock category, auto-granted — avoid `SCHEDULE_EXACT_ALARM` which is revocable). Reschedule on `BOOT_COMPLETED`. Daily recompute via a WorkManager job that re-arms the next day's alarms (times shift daily).
- **Full-screen alarm:** full-screen-intent Activity, `USE_FULL_SCREEN_INTENT` (auto-granted for alarm apps on Android 14), keep-screen-on, audio focus + override ringer for the alarm, persistent foreground service so it can't be swiped away.
- **Dismissal tasks (user picks):** math, scan a pre-registered QR/barcode, step count (~20 steps via sensor), shake. Snooze adds a task or logs a missed on-time Fajr.
- Rising volume, escalates after adhan, per-prayer sound (adhan audio vs silent).
- Doze handling via `setExactAndAllowWhileIdle` for any non-alarm-clock reminders.

### Phase 3 — Pillar 3: reading habit
- **Daily 3 ayat** notification with a one-line reflection/tafsir snippet (teaches, not just displays).
- **Khatm planner:** target date → auto daily portion, pace tracking, re-plan on falling behind (Ramadan is the headline use case).
- **Word-by-word tap:** meaning + root + "this root appears in N places" → builds Quranic Arabic over time.
- Streaks (gentle, not slot-machine), Jummah / Surah Kahf reminders.

### Phase 4 — Pillar 2: Hifz system (the depth moat)
- **SRS review (SM-2):** schedule due ayat; grade recall easy/good/hard/again → update ease/interval/due. *Build this first — pure logic, no ML.*
- **Tikrar loop engine:** pick ayah range, repeat count, A–B loop per ayah, adjustable speed, reciter choice (Media3 + per-ayah audio, gapless looping).
- **Progressive blanking:** reveal → hide words one-by-one → first-letter-only mode.
- **Mistake heatmap:** aggregate `mistake_log` to surface weakest ayat and feed them into SRS more often.

### Phase 5 — Recitation mistake-detection (R&D spike, parallel track)
Scope = **mistake detection only**, not tajweed grading. The key insight that makes this tractable: you already know the expected text, so this is **forced alignment against a known transcript**, not open ASR.
- **Spike:** prototype with an Arabic-capable ASR (Whisper-small Arabic, or Vosk Arabic for fully on-device) on real Quranic recitation; measure WER on Quranic-Arabic recitation specifically (it differs from MSA).
- **Approach:** align the hypothesis to the known ayah token sequence; flag skipped / substituted / out-of-order words; highlight in the reader.
- **Decision gate:** on-device (private, offline, lower accuracy) vs cloud (better, but breaks the no-tracking promise — would need explicit opt-in and a clear privacy note). Pick based on spike WER.
- **Unlocks recite-to-dismiss Fajr** once word-recognition is reliable enough.

### Phase 6 — Community (later, network effects)
- Mosque / jamaat-time finder (community-sourced).
- Halaqah mode: teacher tracks students' hifz progress — a strong madrasah B2B2C angle.

---

## 4. Hard-problem notes

### 4.1 Qibla compass
- `TYPE_ROTATION_VECTOR` (fuses mag + accel + gyro) → `getRotationMatrix` → `getOrientation` for azimuth.
- Correct magnetic→true north with `GeomagneticField` declination.
- Great-circle bearing from user lat/lng to Kaaba (21.4225, 39.8262).
- Low-pass filter the azimuth (needle jitter), show sensor-accuracy indicator + figure-8 calibration prompt on low accuracy.
- **AR mode (differentiator):** CameraX preview + arrow overlay anchored to the computed bearing.

### 4.2 Exact alarms — the usual failure modes
Cleared on reboot (re-arm via `BOOT_COMPLETED`), killed by OEM battery managers (guide users to whitelist), Doze deferral, and Android 12+ exact-alarm restrictions. The dismiss-proof UI needs full-screen intent + foreground service to survive aggressive OEMs.

### 4.3 Recitation
Don't ship verdicts about *the Quran itself* until validated — a wrong "you made a mistake" on a correct recitation is reputationally costly. Keep it framed as a practice aid ("we couldn't follow along here"), not an authority.

---

## 5. Key dependencies
- Compose BOM, Navigation-Compose, Material3
- Room (+ `createFromAsset`)
- Hilt
- Adhan (Kotlin port `adhan2`) — prayer times
- Media3 / ExoPlayer — reciter audio, tikrar A–B looping
- CameraX — AR qibla, QR-scan dismissal
- ML Kit barcode (or ZXing) — QR/barcode dismissal
- WorkManager — daily alarm re-arm
- ASR for Phase 5 (Whisper / Vosk) — decided after the spike

---

## 6. Suggested first sprint
1. Phase 0 module skeleton + theme + Arabic typography proof.
2. Ingestion script → prepackaged Room `.db` (Arabic + EN + BN) with licensing notes captured.
3. Reader vertical slice: juz/surah nav → ayah list → EN/BN toggle → resume position.

That gets a real, navigable Quran reader on a device fast, and everything else layers onto the same data model.
