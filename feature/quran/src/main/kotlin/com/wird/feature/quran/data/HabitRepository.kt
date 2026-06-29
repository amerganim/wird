package com.wird.feature.quran.data

import com.wird.core.database.dao.AyahDao
import com.wird.core.database.dao.SurahDao
import com.wird.core.database.entity.AyahEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

private const val TOTAL_AYAT = 6236
private const val DAILY_COUNT = 3

data class DailyAyahItem(
    val ayah: AyahEntity,
    val surahNameTranslit: String,
)

interface HabitRepository {
    fun observeState(): Flow<HabitState>
    suspend fun getDailyAyat(): List<DailyAyahItem>
    suspend fun markReadToday()
}

class HabitRepositoryImpl @Inject constructor(
    private val ayahDao: AyahDao,
    private val surahDao: SurahDao,
    private val habitSettings: HabitSettings,
) : HabitRepository {

    override fun observeState(): Flow<HabitState> = habitSettings.state

    override suspend fun getDailyAyat(): List<DailyAyahItem> {
        // Deterministic per-day selection of consecutive ayat.
        val day = LocalDate.now().toEpochDay()
        val start = (Math.floorMod(day, (TOTAL_AYAT - DAILY_COUNT).toLong()) + 1).toInt()
        return (start until start + DAILY_COUNT).mapNotNull { id ->
            val ayah = ayahDao.getById(id) ?: return@mapNotNull null
            val name = surahDao.getByNumber(ayah.surahNo)?.nameTranslit.orEmpty()
            DailyAyahItem(ayah, name)
        }
    }

    override suspend fun markReadToday() = habitSettings.markReadToday()
}
