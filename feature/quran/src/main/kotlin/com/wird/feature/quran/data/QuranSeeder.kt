package com.wird.feature.quran.data

import android.content.Context
import com.wird.core.database.dao.AyahDao
import com.wird.core.database.dao.SurahDao
import com.wird.core.database.entity.AyahEntity
import com.wird.core.database.entity.SurahEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Populates the read-only Quran tables from bundled TSV assets on first launch.
 * Assets live in `core/database/src/main/assets/quran/` (Tanzil Uthmani text).
 */
@Singleton
class QuranSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
) {
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        if (surahDao.count() > 0) return@withContext

        val surahs = readLines("quran/surahs.tsv") { cols ->
            SurahEntity(
                number = cols[0].toInt(),
                nameAr = cols[1],
                nameTranslit = cols[2],
                nameEn = cols[3],
                revelationPlace = cols[4],
                ayahCount = cols[5].toInt(),
            )
        }

        val ayahs = readLines("quran/ayahs.tsv") { cols ->
            AyahEntity(
                id = cols[0].toInt(),
                surahNo = cols[1].toInt(),
                ayahNo = cols[2].toInt(),
                juz = cols[3].toInt(),
                hizb = cols[4].toInt(),
                page = cols[5].toInt(),
                sajda = cols[6] == "1",
                textUthmani = cols[7],
            )
        }

        surahDao.insertAll(surahs)
        ayahDao.insertAll(ayahs)
    }

    private fun <T> readLines(asset: String, map: (List<String>) -> T): List<T> =
        context.assets.open(asset).bufferedReader().useLines { lines ->
            lines.filter { it.isNotBlank() }
                .map { map(it.split('\t', limit = 8)) }
                .toList()
        }
}
