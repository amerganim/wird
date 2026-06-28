package com.wird.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Where the user last left off in the reader. A single-row table (id is fixed)
 * so "resume reading" is a trivial lookup. First real entity of the schema; the
 * shipped read-only Quran tables and the rest of the user-state tables described
 * in DEV_PLAN.md §2 will be added in the reader slice.
 */
@Entity(tableName = "last_position")
data class LastPositionEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val ayahId: Int,
    val scrollOffset: Int,
    val updatedAt: Long,
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
