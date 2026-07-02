package com.wird.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wird.core.database.entity.MistakeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MistakeLogDao {

    @Insert
    suspend fun insertAll(rows: List<MistakeLogEntity>)

    @Query("SELECT * FROM mistake_log WHERE ayahId = :ayahId ORDER BY createdAt DESC")
    fun observeForAyah(ayahId: Int): Flow<List<MistakeLogEntity>>

    @Query("SELECT COUNT(*) FROM mistake_log")
    fun observeCount(): Flow<Int>
}
