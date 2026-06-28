package com.wird.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wird.core.database.entity.LastPositionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LastPositionDao {

    @Query("SELECT * FROM last_position WHERE id = :id LIMIT 1")
    fun observe(id: Int = LastPositionEntity.SINGLE_ROW_ID): Flow<LastPositionEntity?>

    @Upsert
    suspend fun upsert(position: LastPositionEntity)
}
