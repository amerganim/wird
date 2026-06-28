package com.wird.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wird.core.database.entity.HifzItemEntity
import kotlinx.coroutines.flow.Flow

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

    @Upsert
    suspend fun upsert(item: HifzItemEntity)

    @Upsert
    suspend fun upsertAll(items: List<HifzItemEntity>)

    @Query("DELETE FROM hifz_item WHERE ayahId = :ayahId")
    suspend fun delete(ayahId: Int)
}
