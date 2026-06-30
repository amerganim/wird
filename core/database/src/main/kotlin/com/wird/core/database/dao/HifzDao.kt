package com.wird.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wird.core.database.entity.AyahEntity
import com.wird.core.database.entity.HifzItemEntity
import kotlinx.coroutines.flow.Flow

/** How many ayat of a surah are under memorization. */
data class SurahHifzCount(val surahNo: Int, val count: Int)

@Dao
interface HifzDao {

    @Query("SELECT COUNT(*) FROM hifz_item")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM hifz_item WHERE dueEpochDay <= :today")
    fun observeDueCount(today: Long): Flow<Int>

    @Query("SELECT * FROM hifz_item WHERE dueEpochDay <= :today ORDER BY dueEpochDay, ayahId")
    suspend fun getDue(today: Long): List<HifzItemEntity>

    @Query("SELECT * FROM hifz_item WHERE ayahId = :ayahId LIMIT 1")
    suspend fun getById(ayahId: Int): HifzItemEntity?

    @Query("SELECT ayahId FROM hifz_item")
    fun observeIds(): Flow<List<Int>>

    @Query(
        "SELECT a.surahNo AS surahNo, COUNT(*) AS count FROM hifz_item h " +
            "JOIN ayah a ON a.id = h.ayahId GROUP BY a.surahNo ORDER BY a.surahNo",
    )
    fun observeSurahCounts(): Flow<List<SurahHifzCount>>

    @Query(
        "SELECT a.* FROM ayah a JOIN hifz_item h ON h.ayahId = a.id " +
            "WHERE a.surahNo = :surahNo ORDER BY a.ayahNo",
    )
    suspend fun getMemorizedAyahsInSurah(surahNo: Int): List<AyahEntity>

    @Upsert
    suspend fun upsert(item: HifzItemEntity)

    @Upsert
    suspend fun upsertAll(items: List<HifzItemEntity>)

    @Query("DELETE FROM hifz_item WHERE ayahId = :ayahId")
    suspend fun delete(ayahId: Int)
}
