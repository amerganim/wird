package com.wird.feature.hifz.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HifzModule {

    @Binds
    @Singleton
    abstract fun bindHifzRepository(impl: HifzRepositoryImpl): HifzRepository
}
