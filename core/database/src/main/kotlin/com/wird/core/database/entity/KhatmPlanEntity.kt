package com.wird.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A single active khatm (complete-the-Quran) plan. Single-row table. */
@Entity(tableName = "khatm_plan")
data class KhatmPlanEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val startEpochDay: Long,
    val targetEpochDay: Long,
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
