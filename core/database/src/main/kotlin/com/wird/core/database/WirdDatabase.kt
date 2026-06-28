package com.wird.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wird.core.database.dao.AyahDao
import com.wird.core.database.dao.BookmarkDao
import com.wird.core.database.dao.HifzDao
import com.wird.core.database.dao.LastPositionDao
import com.wird.core.database.dao.SurahDao
import com.wird.core.database.entity.AyahEntity
import com.wird.core.database.entity.BookmarkEntity
import com.wird.core.database.entity.HifzItemEntity
import com.wird.core.database.entity.LastPositionEntity
import com.wird.core.database.entity.SurahEntity

@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        LastPositionEntity::class,
        BookmarkEntity::class,
        HifzItemEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class WirdDatabase : RoomDatabase() {
    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao
    abstract fun lastPositionDao(): LastPositionDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun hifzDao(): HifzDao

    companion object {
        const val DATABASE_NAME = "wird.db"
    }
}
