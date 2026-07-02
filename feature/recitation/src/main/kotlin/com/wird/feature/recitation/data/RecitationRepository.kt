package com.wird.feature.recitation.data

import com.wird.core.database.dao.AyahDao
import com.wird.core.database.dao.HifzDao
import com.wird.core.database.dao.MistakeLogDao
import com.wird.core.database.dao.SurahDao
import com.wird.core.database.entity.AyahEntity
import com.wird.core.database.entity.MistakeLogEntity
import com.wird.feature.recitation.align.AlignmentResult
import com.wird.feature.recitation.align.MistakeType
import com.wird.feature.recitation.align.WordStatus
import java.time.LocalDate
import javax.inject.Inject

interface RecitationRepository {
    suspend fun getAyat(surahNo: Int): List<AyahEntity>
    suspend fun getSurahName(surahNo: Int): String
    /** Persist a checked recitation: granular per-word rows + an aggregate stumble for the heatmap. */
    suspend fun logResult(ayahId: Int, result: AlignmentResult)
}

class RecitationRepositoryImpl @Inject constructor(
    private val ayahDao: AyahDao,
    private val surahDao: SurahDao,
    private val mistakeLogDao: MistakeLogDao,
    private val hifzDao: HifzDao,
) : RecitationRepository {

    override suspend fun getAyat(surahNo: Int): List<AyahEntity> = ayahDao.getBySurah(surahNo)

    override suspend fun getSurahName(surahNo: Int): String =
        surahDao.getByNumber(surahNo)?.nameTranslit ?: "Surah $surahNo"

    override suspend fun logResult(ayahId: Int, result: AlignmentResult) {
        if (result.isPerfect) return
        val now = System.currentTimeMillis()
        val rows = buildList {
            result.words.forEach { word ->
                val type = when (word.status) {
                    WordStatus.MISSED -> MistakeType.MISSED
                    WordStatus.SUBSTITUTED -> MistakeType.SUBSTITUTED
                    WordStatus.CORRECT -> return@forEach
                }
                add(MistakeLogEntity(ayahId = ayahId, wordPosition = word.position, mistakeType = type.name, createdAt = now))
            }
            result.extraWords.forEach {
                add(MistakeLogEntity(ayahId = ayahId, wordPosition = -1, mistakeType = MistakeType.EXTRA.name, createdAt = now))
            }
        }
        mistakeLogDao.insertAll(rows)
        // One stumble on this ayah — lights it up in the existing Hifz mistake heatmap.
        hifzDao.logMistake(ayahId, LocalDate.now().toEpochDay())
    }
}
