package com.wird.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wird.core.database.dao.LastPositionDao
import com.wird.core.database.entity.LastPositionEntity

@Database(
    entities = [
        LastPositionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class WirdDatabase : RoomDatabase() {
    abstract fun lastPositionDao(): LastPositionDao

    companion object {
        const val DATABASE_NAME = "wird.db"
    }
}
