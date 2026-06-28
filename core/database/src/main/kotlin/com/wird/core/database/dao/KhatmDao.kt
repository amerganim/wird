package com.wird.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wird.core.database.entity.KhatmPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KhatmDao {

    @Query("SELECT * FROM khatm_plan WHERE id = :id LIMIT 1")
    fun observe(id: Int = KhatmPlanEntity.SINGLE_ROW_ID): Flow<KhatmPlanEntity?>

    @Upsert
    suspend fun upsert(plan: KhatmPlanEntity)

    @Query("DELETE FROM khatm_plan")
    suspend fun clear()
}
