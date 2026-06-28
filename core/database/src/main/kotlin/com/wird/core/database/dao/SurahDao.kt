package com.wird.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wird.core.database.entity.SurahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {

    @Query("SELECT * FROM surah ORDER BY number")
    fun observeAll(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surah WHERE number = :number LIMIT 1")
    suspend fun getByNumber(number: Int): SurahEntity?

    @Query("SELECT COUNT(*) FROM surah")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(surahs: List<SurahEntity>)
}
