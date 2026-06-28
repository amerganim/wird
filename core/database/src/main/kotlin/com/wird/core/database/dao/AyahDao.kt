package com.wird.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wird.core.database.entity.AyahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AyahDao {

    @Query("SELECT * FROM ayah WHERE surahNo = :surahNo ORDER BY ayahNo")
    fun observeBySurah(surahNo: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayah WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): AyahEntity?

    @Query("SELECT COUNT(*) FROM ayah")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ayahs: List<AyahEntity>)
}
