package com.wird.feature.hifz.data

import com.wird.core.database.dao.AyahDao
import com.wird.core.database.dao.HifzDao
import com.wird.core.database.dao.SurahDao
import com.wird.core.database.entity.AyahEntity
import com.wird.core.database.entity.HifzItemEntity
import com.wird.core.database.entity.SurahEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/** An ayah due for review, with its memorization state. */
data class ReviewCard(
    val ayah: AyahEntity,
    val item: HifzItemEntity,
)

/** A surah the user is memorizing, for the dashboard list. */
data class MemorizingSurah(
    val surahNo: Int,
    val nameTranslit: String,
    val count: Int,
)

interface HifzRepository {
    fun observeTotalCount(): Flow<Int>
    fun observeDueCount(): Flow<Int>
    fun observeHifzAyahIds(): Flow<Set<Int>>
    fun observeSurahs(): Flow<List<SurahEntity>>
    fun observeMemorizingSurahs(): Flow<List<MemorizingSurah>>
    suspend fun addSurah(surahNo: Int): Int
    suspend fun getDueCards(): List<ReviewCard>
    suspend fun getMemorizedAyat(surahNo: Int): List<AyahEntity>
    suspend fun getSurahName(surahNo: Int): String
    suspend fun grade(ayahId: Int, grade: Sm2.Grade)
    suspend fun logMistake(ayahId: Int)
    /** ayahId -> mistake count for a surah (only ayat with at least one mistake). */
    fun observeSurahMistakes(surahNo: Int): Flow<Map<Int, Int>>
}

class HifzRepositoryImpl @Inject constructor(
    private val hifzDao: HifzDao,
    private val ayahDao: AyahDao,
    private val surahDao: SurahDao,
) : HifzRepository {

    private fun today(): Long =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).toEpochDays().toLong()

    override fun observeTotalCount(): Flow<Int> = hifzDao.observeCount()

    override fun observeDueCount(): Flow<Int> = hifzDao.observeDueCount(today())

    override fun observeHifzAyahIds(): Flow<Set<Int>> = hifzDao.observeIds().map { it.toSet() }

    override fun observeSurahs(): Flow<List<SurahEntity>> = surahDao.observeAll()

    override fun observeMemorizingSurahs(): Flow<List<MemorizingSurah>> =
        hifzDao.observeSurahCounts().map { counts ->
            val names = surahDao.getAll().associate { it.number to it.nameTranslit }
            counts.map { MemorizingSurah(it.surahNo, names[it.surahNo].orEmpty(), it.count) }
        }

    override suspend fun getMemorizedAyat(surahNo: Int): List<AyahEntity> =
        hifzDao.getMemorizedAyahsInSurah(surahNo)

    override suspend fun getSurahName(surahNo: Int): String =
        surahDao.getByNumber(surahNo)?.nameTranslit ?: "Surah $surahNo"

    override suspend fun addSurah(surahNo: Int): Int {
        val today = today()
        val ayahs = ayahDao.getBySurah(surahNo)
        val newItems = ayahs
            .filter { hifzDao.getById(it.id) == null }
            .map { ayah ->
                HifzItemEntity(
                    ayahId = ayah.id,
                    easeFactor = Sm2.NEW.easeFactor,
                    intervalDays = Sm2.NEW.intervalDays,
                    repetitions = Sm2.NEW.repetitions,
                    dueEpochDay = today,
                    lastReviewedEpochDay = 0,
                    lapses = Sm2.NEW.lapses,
                )
            }
        hifzDao.upsertAll(newItems)
        return newItems.size
    }

    override suspend fun getDueCards(): List<ReviewCard> {
        val today = today()
        return hifzDao.getDue(today).mapNotNull { item ->
            ayahDao.getById(item.ayahId)?.let { ReviewCard(it, item) }
        }
    }

    override suspend fun grade(ayahId: Int, grade: Sm2.Grade) {
        val item = hifzDao.getById(ayahId) ?: return
        val newState = Sm2.review(
            Sm2.State(item.easeFactor, item.intervalDays, item.repetitions, item.lapses),
            grade,
        )
        val today = today()
        hifzDao.upsert(
            item.copy(
                easeFactor = newState.easeFactor,
                intervalDays = newState.intervalDays,
                repetitions = newState.repetitions,
                lapses = newState.lapses,
                dueEpochDay = today + newState.intervalDays,
                lastReviewedEpochDay = today,
            ),
        )
        // A failed recall is a trouble spot — record it for the heatmap.
        if (grade == Sm2.Grade.AGAIN) hifzDao.logMistake(ayahId, today)
    }

    override suspend fun logMistake(ayahId: Int) = hifzDao.logMistake(ayahId, today())

    override fun observeSurahMistakes(surahNo: Int): Flow<Map<Int, Int>> =
        hifzDao.observeSurahMistakes(surahNo).map { list ->
            list.associate { it.ayahId to it.mistakes }
        }
}
