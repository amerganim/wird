package com.wird.core.database.di

import android.content.Context
import androidx.room.Room
import com.wird.core.database.WirdDatabase
import com.wird.core.database.dao.LastPositionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWirdDatabase(
        @ApplicationContext context: Context,
    ): WirdDatabase = Room.databaseBuilder(
        context,
        WirdDatabase::class.java,
        WirdDatabase.DATABASE_NAME,
    )
        // The reader slice will switch this to .createFromAsset("database/wird.db")
        // once the prepackaged Quran content database is built (DEV_PLAN.md §1).
        .build()

    @Provides
    fun provideLastPositionDao(database: WirdDatabase): LastPositionDao =
        database.lastPositionDao()
}
