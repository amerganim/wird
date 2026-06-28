package com.wird.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wird.core.database.dao.AyahDao
import com.wird.core.database.dao.LastPositionDao
import com.wird.core.database.dao.SurahDao
import com.wird.core.database.entity.AyahEntity
import com.wird.core.database.entity.LastPositionEntity
import com.wird.core.database.entity.SurahEntity

@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        LastPositionEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class WirdDatabase : RoomDatabase() {
    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao
    abstract fun lastPositionDao(): LastPositionDao

    companion object {
        const val DATABASE_NAME = "wird.db"
    }
}
