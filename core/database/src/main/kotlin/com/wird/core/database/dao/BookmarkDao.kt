package com.wird.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wird.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT ayahId FROM bookmark")
    fun observeIds(): Flow<List<Int>>

    @Query("SELECT * FROM bookmark ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmark WHERE ayahId = :ayahId)")
    suspend fun exists(ayahId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmark WHERE ayahId = :ayahId")
    suspend fun delete(ayahId: Int)
}
