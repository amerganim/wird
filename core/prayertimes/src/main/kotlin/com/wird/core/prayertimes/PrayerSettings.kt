package com.wird.core.prayertimes

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Madhab
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.prayerDataStore by preferencesDataStore(name = "prayer_settings")

data class PrayerPrefs(
    val method: CalculationMethod,
    val madhab: Madhab,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
    val notificationsEnabled: Boolean,
)

/** Persistent prayer-calculation preferences. */
@Singleton
class PrayerSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val METHOD = stringPreferencesKey("calculation_method")
        val MADHAB = stringPreferencesKey("madhab")
        val CITY = stringPreferencesKey("city_name")
        val LAT = doublePreferencesKey("latitude")
        val LNG = doublePreferencesKey("longitude")
        val TZ = stringPreferencesKey("time_zone")
        val NOTIFS = booleanPreferencesKey("notifications_enabled")
    }

    val prefs: Flow<PrayerPrefs> = context.prayerDataStore.data.map { p ->
        PrayerPrefs(
            method = p[Keys.METHOD]?.toMethod() ?: CalculationMethod.KARACHI,
            madhab = p[Keys.MADHAB]?.toMadhab() ?: Madhab.HANAFI,
            cityName = p[Keys.CITY] ?: DEFAULT_CITY.name,
            latitude = p[Keys.LAT] ?: DEFAULT_CITY.latitude,
            longitude = p[Keys.LNG] ?: DEFAULT_CITY.longitude,
            timeZone = p[Keys.TZ] ?: DEFAULT_CITY.timeZone,
            notificationsEnabled = p[Keys.NOTIFS] ?: false,
        )
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.prayerDataStore.edit { it[Keys.NOTIFS] = enabled }
    }

    suspend fun setMethod(method: CalculationMethod) {
        context.prayerDataStore.edit { it[Keys.METHOD] = method.name }
    }

    suspend fun setMadhab(madhab: Madhab) {
        context.prayerDataStore.edit { it[Keys.MADHAB] = madhab.name }
    }

    suspend fun setCity(city: City) {
        context.prayerDataStore.edit {
            it[Keys.CITY] = city.name
            it[Keys.LAT] = city.latitude
            it[Keys.LNG] = city.longitude
            it[Keys.TZ] = city.timeZone
        }
    }

    private fun String.toMethod(): CalculationMethod =
        runCatching { CalculationMethod.valueOf(this) }.getOrDefault(CalculationMethod.KARACHI)

    private fun String.toMadhab(): Madhab =
        runCatching { Madhab.valueOf(this) }.getOrDefault(Madhab.HANAFI)

    private companion object {
        val DEFAULT_CITY = City("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka")
    }
}
