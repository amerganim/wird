package com.wird.core.database.di

import android.content.Context
import androidx.room.Room
import com.wird.core.database.WirdDatabase
import com.wird.core.database.dao.AyahDao
import com.wird.core.database.dao.LastPositionDao
import com.wird.core.database.dao.SurahDao
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
        // Quran content is seeded from bundled assets (QuranSeeder), so the DB
        // can always be rebuilt — destructive migration is safe pre-release.
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideSurahDao(database: WirdDatabase): SurahDao = database.surahDao()

    @Provides
    fun provideAyahDao(database: WirdDatabase): AyahDao = database.ayahDao()

    @Provides
    fun provideLastPositionDao(database: WirdDatabase): LastPositionDao =
        database.lastPositionDao()
}
