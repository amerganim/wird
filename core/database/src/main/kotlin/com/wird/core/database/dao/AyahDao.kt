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

    @Query("SELECT * FROM ayah WHERE surahNo = :surahNo ORDER BY ayahNo")
    suspend fun getBySurah(surahNo: Int): List<AyahEntity>

    @Query("SELECT * FROM ayah WHERE juz = :juz ORDER BY id")
    fun observeByJuz(juz: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayah WHERE id IN (SELECT MIN(id) FROM ayah GROUP BY juz) ORDER BY juz")
    suspend fun getJuzStartAyahs(): List<AyahEntity>

    @Query("SELECT * FROM ayah WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): AyahEntity?

    @Query("SELECT COUNT(*) FROM ayah")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ayahs: List<AyahEntity>)
}
