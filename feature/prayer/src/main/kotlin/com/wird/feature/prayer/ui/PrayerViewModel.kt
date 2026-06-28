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
import com.wird.feature.prayer.notification.PrayerAlarmScheduler
import com.wird.feature.prayer.notification.PrayerNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class PrayerTimeRow(
    val name: String,
    val time: String,
    val instant: Instant,
    val isPrayer: Boolean,
)

data class PrayerUiState(
    val locationName: String,
    val dateLabel: String,
    val method: CalculationMethod,
    val madhab: Madhab,
    val times: List<PrayerTimeRow>,
    val tomorrowFajr: Instant,
    val notificationsEnabled: Boolean,
)

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val settings: PrayerSettings,
    private val scheduler: PrayerAlarmScheduler,
    private val notifier: PrayerNotifier,
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
                    notificationsEnabled = false,
                ),
            ),
        )

    fun setMethod(method: CalculationMethod) {
        viewModelScope.launch {
            settings.setMethod(method)
            rescheduleIfEnabled()
        }
    }

    fun setMadhab(madhab: Madhab) {
        viewModelScope.launch {
            settings.setMadhab(madhab)
            rescheduleIfEnabled()
        }
    }

    fun setCity(city: City) {
        viewModelScope.launch {
            settings.setCity(city)
            rescheduleIfEnabled()
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.setNotificationsEnabled(enabled)
            if (enabled) {
                notifier.ensureChannel()
                scheduler.scheduleNext()
            } else {
                scheduler.cancel()
            }
        }
    }

    private suspend fun rescheduleIfEnabled() {
        if (settings.prefs.first().notificationsEnabled) scheduler.scheduleNext()
    }

    private fun computeToday(prefs: PrayerPrefs): PrayerUiState {
        val tz = runCatching { TimeZone.of(prefs.timeZone) }
            .getOrDefault(TimeZone.currentSystemDefault())
        val today = Clock.System.now().toLocalDateTime(tz).date

        val coordinates = Coordinates(prefs.latitude, prefs.longitude)
        val params = prefs.method.parameters.copy(madhab = prefs.madhab)
        val date = DateComponents(today.year, today.monthNumber, today.dayOfMonth)
        val pt = PrayerTimes(coordinates, date, params)

        val tomorrow = today.plus(DatePeriod(days = 1))
        val ptTomorrow = PrayerTimes(
            coordinates,
            DateComponents(tomorrow.year, tomorrow.monthNumber, tomorrow.dayOfMonth),
            params,
        )

        fun fmt(instant: Instant): String {
            val t = instant.toLocalDateTime(tz)
            val hour12 = if (t.hour % 12 == 0) 12 else t.hour % 12
            val amPm = if (t.hour < 12) "AM" else "PM"
            return "%d:%02d %s".format(hour12, t.minute, amPm)
        }

        fun row(name: String, instant: Instant, isPrayer: Boolean) =
            PrayerTimeRow(name, fmt(instant), instant, isPrayer)

        val month = today.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return PrayerUiState(
            locationName = prefs.cityName,
            dateLabel = "${today.dayOfMonth} $month ${today.year}",
            method = prefs.method,
            madhab = prefs.madhab,
            times = listOf(
                row("Fajr", pt.fajr, isPrayer = true),
                row("Sunrise", pt.sunrise, isPrayer = false),
                row("Dhuhr", pt.dhuhr, isPrayer = true),
                row("Asr", pt.asr, isPrayer = true),
                row("Maghrib", pt.maghrib, isPrayer = true),
                row("Isha", pt.isha, isPrayer = true),
            ),
            tomorrowFajr = ptTomorrow.fajr,
            notificationsEnabled = prefs.notificationsEnabled,
        )
    }

    companion object {
        private const val DEFAULT_LAT = 23.8103
        private const val DEFAULT_LNG = 90.4125
        private const val DEFAULT_TZ = "Asia/Dhaka"
    }
}
