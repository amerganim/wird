package com.wird.feature.quran.data

import com.wird.core.database.dao.AyahDao
import com.wird.core.database.dao.LastPositionDao
import com.wird.core.database.dao.SurahDao
import com.wird.core.database.entity.AyahEntity
import com.wird.core.database.entity.LastPositionEntity
import com.wird.core.database.entity.SurahEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface QuranRepository {
    suspend fun ensureSeeded()
    fun observeSurahs(): Flow<List<SurahEntity>>
    suspend fun getSurah(number: Int): SurahEntity?
    fun observeAyahs(surahNo: Int): Flow<List<AyahEntity>>
    suspend fun getAyah(id: Int): AyahEntity?
    fun observeLastPosition(): Flow<LastPositionEntity?>
    suspend fun saveLastPosition(ayahId: Int, scrollOffset: Int = 0)
}

class QuranRepositoryImpl @Inject constructor(
    private val seeder: QuranSeeder,
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
    private val lastPositionDao: LastPositionDao,
) : QuranRepository {

    override suspend fun ensureSeeded() = seeder.seedIfNeeded()

    override fun observeSurahs(): Flow<List<SurahEntity>> = surahDao.observeAll()

    override suspend fun getSurah(number: Int): SurahEntity? = surahDao.getByNumber(number)

    override fun observeAyahs(surahNo: Int): Flow<List<AyahEntity>> =
        ayahDao.observeBySurah(surahNo)

    override suspend fun getAyah(id: Int): AyahEntity? = ayahDao.getById(id)

    override fun observeLastPosition(): Flow<LastPositionEntity?> = lastPositionDao.observe()

    override suspend fun saveLastPosition(ayahId: Int, scrollOffset: Int) {
        lastPositionDao.upsert(
            LastPositionEntity(
                ayahId = ayahId,
                scrollOffset = scrollOffset,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }
}
