package com.wird.feature.prayer.data

import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** Shared prayer-time math used by both the UI and the notification scheduler. */
object PrayerCalculator {

    data class Timing(val prayer: String, val instant: Instant)

    /** The next obligatory prayer strictly after [from] (rolls into tomorrow). */
    fun nextPrayerAfter(prefs: PrayerPrefs, from: Instant): Timing {
        val tz = runCatching { TimeZone.of(prefs.timeZone) }
            .getOrDefault(TimeZone.currentSystemDefault())
        val today = from.toLocalDateTime(tz).date
        val candidates = dailyPrayers(prefs, today) + dailyPrayers(prefs, today.plus(DatePeriod(days = 1)))
        return candidates.first { it.instant > from }
    }

    /** The next Fajr strictly after [from] (today's if still ahead, else tomorrow's). */
    fun nextFajrAfter(prefs: PrayerPrefs, from: Instant): Instant {
        val tz = runCatching { TimeZone.of(prefs.timeZone) }
            .getOrDefault(TimeZone.currentSystemDefault())
        val today = from.toLocalDateTime(tz).date
        val todayFajr = dailyPrayers(prefs, today).first { it.prayer == "Fajr" }.instant
        return if (todayFajr > from) {
            todayFajr
        } else {
            dailyPrayers(prefs, today.plus(DatePeriod(days = 1))).first { it.prayer == "Fajr" }.instant
        }
    }

    private fun dailyPrayers(prefs: PrayerPrefs, date: LocalDate): List<Timing> {
        val coordinates = Coordinates(prefs.latitude, prefs.longitude)
        val params = prefs.method.parameters.copy(madhab = prefs.madhab)
        val pt = PrayerTimes(
            coordinates,
            DateComponents(date.year, date.monthNumber, date.dayOfMonth),
            params,
        )
        return listOf(
            Timing("Fajr", pt.fajr),
            Timing("Dhuhr", pt.dhuhr),
            Timing("Asr", pt.asr),
            Timing("Maghrib", pt.maghrib),
            Timing("Isha", pt.isha),
        )
    }
}
