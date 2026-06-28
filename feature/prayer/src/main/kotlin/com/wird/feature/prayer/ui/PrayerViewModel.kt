package com.wird.feature.prayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.wird.feature.prayer.data.City
import com.wird.feature.prayer.data.PrayerPrefs
import com.wird.feature.prayer.data.PrayerSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class PrayerTimeRow(val name: String, val time: String)

data class PrayerUiState(
    val locationName: String,
    val dateLabel: String,
    val method: CalculationMethod,
    val madhab: Madhab,
    val times: List<PrayerTimeRow>,
)

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val settings: PrayerSettings,
) : ViewModel() {

    val uiState: StateFlow<PrayerUiState> = settings.prefs
        .map { computeToday(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = computeToday(
                PrayerPrefs(
                    method = CalculationMethod.KARACHI,
                    madhab = Madhab.HANAFI,
                    cityName = "Dhaka",
                    latitude = DEFAULT_LAT,
                    longitude = DEFAULT_LNG,
                    timeZone = DEFAULT_TZ,
                ),
            ),
        )

    fun setMethod(method: CalculationMethod) {
        viewModelScope.launch { settings.setMethod(method) }
    }

    fun setMadhab(madhab: Madhab) {
        viewModelScope.launch { settings.setMadhab(madhab) }
    }

    fun setCity(city: City) {
        viewModelScope.launch { settings.setCity(city) }
    }

    private fun computeToday(prefs: PrayerPrefs): PrayerUiState {
        val tz = runCatching { TimeZone.of(prefs.timeZone) }
            .getOrDefault(TimeZone.currentSystemDefault())
        val today = Clock.System.now().toLocalDateTime(tz).date

        val coordinates = Coordinates(prefs.latitude, prefs.longitude)
        val params = prefs.method.parameters.copy(madhab = prefs.madhab)
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
            locationName = prefs.cityName,
            dateLabel = "${today.dayOfMonth} $month ${today.year}",
            method = prefs.method,
            madhab = prefs.madhab,
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
        private const val DEFAULT_TZ = "Asia/Dhaka"
    }
}
