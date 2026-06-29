package com.wird.feature.qibla

import android.hardware.GeomagneticField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wird.core.prayertimes.PrayerSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class QiblaUiState(
    val qiblaBearing: Float = 0f,
    val declination: Float = 0f,
    val locationName: String = "",
)

@HiltViewModel
class QiblaViewModel @Inject constructor(
    settings: PrayerSettings,
) : ViewModel() {

    val uiState: StateFlow<QiblaUiState> = settings.prefs.map { prefs ->
        val declination = GeomagneticField(
            prefs.latitude.toFloat(),
            prefs.longitude.toFloat(),
            0f,
            System.currentTimeMillis(),
        ).declination
        QiblaUiState(
            qiblaBearing = QiblaMath.bearingToKaaba(prefs.latitude, prefs.longitude),
            declination = declination,
            locationName = prefs.cityName,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QiblaUiState(),
    )
}
