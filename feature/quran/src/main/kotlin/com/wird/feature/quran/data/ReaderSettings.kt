package com.wird.feature.quran.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.readerDataStore by preferencesDataStore(name = "reader_settings")

/** Persistent reader preferences (currently just Arabic font size). */
@Singleton
class ReaderSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val ARABIC_FONT_SP = intPreferencesKey("arabic_font_sp")
    }

    val arabicFontSp: Flow<Int> = context.readerDataStore.data
        .map { it[Keys.ARABIC_FONT_SP] ?: DEFAULT_SP }

    suspend fun setArabicFontSp(sp: Int) {
        context.readerDataStore.edit { it[Keys.ARABIC_FONT_SP] = sp.coerceIn(MIN_SP, MAX_SP) }
    }

    companion object {
        const val DEFAULT_SP = 28
        const val MIN_SP = 18
        const val MAX_SP = 48
    }
}
