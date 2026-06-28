package com.wird.feature.prayer.ui

import androidx.lifecycle.ViewModel
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class PrayerTimeRow(val name: String, val time: String)

data class PrayerUiState(
    val locationName: String,
    val dateLabel: String,
    val method: String,
    val times: List<PrayerTimeRow>,
)

@HiltViewModel
class PrayerViewModel @Inject constructor() : ViewModel() {

    // First slice: a fixed default location. A location/city picker comes next.
    val uiState: PrayerUiState = computeToday()

    private fun computeToday(): PrayerUiState {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date

        val coordinates = Coordinates(DEFAULT_LAT, DEFAULT_LNG)
        val params = CalculationMethod.KARACHI.parameters.copy(madhab = Madhab.HANAFI)
        val date = DateComponents(today.year, today.monthNumber, today.dayOfMonth)
        val pt = PrayerTimes(coordinates, date, params)

        fun fmt(instant: Instant): String {
            val t = instant.toLocalDateTime(tz)
            val hour12 = if (t.hour % 12 == 0) 12 else t.hour % 12
            val amPm = if (t.hour < 12) "AM" else "PM"
            return "%d:%02d %s".format(hour12, t.minute, amPm)
        }

        val month = today.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return PrayerUiState(
            locationName = "Dhaka",
            dateLabel = "${today.dayOfMonth} $month ${today.year}",
            method = "Karachi · Hanafi asr",
            times = listOf(
                PrayerTimeRow("Fajr", fmt(pt.fajr)),
                PrayerTimeRow("Sunrise", fmt(pt.sunrise)),
                PrayerTimeRow("Dhuhr", fmt(pt.dhuhr)),
                PrayerTimeRow("Asr", fmt(pt.asr)),
                PrayerTimeRow("Maghrib", fmt(pt.maghrib)),
                PrayerTimeRow("Isha", fmt(pt.isha)),
            ),
        )
    }

    companion object {
        private const val DEFAULT_LAT = 23.8103
        private const val DEFAULT_LNG = 90.4125
    }
}
