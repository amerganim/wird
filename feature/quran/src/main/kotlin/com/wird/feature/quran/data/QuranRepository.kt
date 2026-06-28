package com.wird.feature.quran.data

import com.wird.core.database.dao.AyahDao
import com.wird.core.database.dao.BookmarkDao
import com.wird.core.database.dao.LastPositionDao
import com.wird.core.database.dao.SurahDao
import com.wird.core.database.entity.AyahEntity
import com.wird.core.database.entity.BookmarkEntity
import com.wird.core.database.entity.LastPositionEntity
import com.wird.core.database.entity.SurahEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface QuranRepository {
    suspend fun ensureSeeded()
    fun observeSurahs(): Flow<List<SurahEntity>>
    suspend fun getSurah(number: Int): SurahEntity?
    suspend fun getSurahMap(): Map<Int, SurahEntity>
    fun observeAyahs(surahNo: Int): Flow<List<AyahEntity>>
    fun observeAyahsByJuz(juz: Int): Flow<List<AyahEntity>>
    suspend fun getJuzStarts(): List<JuzStart>
    suspend fun getAyah(id: Int): AyahEntity?
    fun observeLastPosition(): Flow<LastPositionEntity?>
    suspend fun saveLastPosition(ayahId: Int, scrollOffset: Int = 0)
    fun observeBookmarkedAyahIds(): Flow<Set<Int>>
    fun observeBookmarks(): Flow<List<BookmarkEntity>>
    suspend fun toggleBookmark(ayahId: Int)
}

class QuranRepositoryImpl @Inject constructor(
    private val seeder: QuranSeeder,
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao,
    private val lastPositionDao: LastPositionDao,
    private val bookmarkDao: BookmarkDao,
) : QuranRepository {

    override suspend fun ensureSeeded() = seeder.seedIfNeeded()

    override fun observeSurahs(): Flow<List<SurahEntity>> = surahDao.observeAll()

    override suspend fun getSurah(number: Int): SurahEntity? = surahDao.getByNumber(number)

    override suspend fun getSurahMap(): Map<Int, SurahEntity> =
        surahDao.getAll().associateBy { it.number }

    override fun observeAyahs(surahNo: Int): Flow<List<AyahEntity>> =
        ayahDao.observeBySurah(surahNo)

    override fun observeAyahsByJuz(juz: Int): Flow<List<AyahEntity>> =
        ayahDao.observeByJuz(juz)

    override suspend fun getJuzStarts(): List<JuzStart> {
        val surahNames = surahDao.getAll().associate { it.number to it.nameTranslit }
        return ayahDao.getJuzStartAyahs().map { ayah ->
            JuzStart(
                juz = ayah.juz,
                surahNo = ayah.surahNo,
                surahNameTranslit = surahNames[ayah.surahNo].orEmpty(),
                ayahNo = ayah.ayahNo,
            )
        }
    }

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

    override fun observeBookmarkedAyahIds(): Flow<Set<Int>> =
        bookmarkDao.observeIds().map { it.toSet() }

    override fun observeBookmarks(): Flow<List<BookmarkEntity>> = bookmarkDao.observeAll()

    override suspend fun toggleBookmark(ayahId: Int) {
        if (bookmarkDao.exists(ayahId)) {
            bookmarkDao.delete(ayahId)
        } else {
            bookmarkDao.insert(BookmarkEntity(ayahId = ayahId, createdAt = System.currentTimeMillis()))
        }
    }
}
