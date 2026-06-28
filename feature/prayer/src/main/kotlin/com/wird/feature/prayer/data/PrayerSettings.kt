package com.wird.feature.prayer.data

import android.content.Context
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
)

/** Persistent prayer-calculation preferences. */
@Singleton
class PrayerSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val METHOD = stringPreferencesKey("calculation_method")
        val MADHAB = stringPreferencesKey("madhab")
    }

    val prefs: Flow<PrayerPrefs> = context.prayerDataStore.data.map { p ->
        PrayerPrefs(
            method = p[Keys.METHOD]?.toMethod() ?: CalculationMethod.KARACHI,
            madhab = p[Keys.MADHAB]?.toMadhab() ?: Madhab.HANAFI,
        )
    }

    suspend fun setMethod(method: CalculationMethod) {
        context.prayerDataStore.edit { it[Keys.METHOD] = method.name }
    }

    suspend fun setMadhab(madhab: Madhab) {
        context.prayerDataStore.edit { it[Keys.MADHAB] = madhab.name }
    }

    private fun String.toMethod(): CalculationMethod =
        runCatching { CalculationMethod.valueOf(this) }.getOrDefault(CalculationMethod.KARACHI)

    private fun String.toMadhab(): Madhab =
        runCatching { Madhab.valueOf(this) }.getOrDefault(Madhab.HANAFI)
}
