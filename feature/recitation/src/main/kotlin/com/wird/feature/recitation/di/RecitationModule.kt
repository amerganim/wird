package com.wird.feature.recitation.di

import com.wird.feature.recitation.asr.AsrEngine
import com.wird.feature.recitation.asr.SimulatedAsrEngine
import com.wird.feature.recitation.data.RecitationRepository
import com.wird.feature.recitation.data.RecitationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecitationModule {

    @Binds
    @Singleton
    abstract fun bindRepository(impl: RecitationRepositoryImpl): RecitationRepository

    // Swap this binding for the real on-device engine after the accuracy spike.
    @Binds
    @Singleton
    abstract fun bindAsrEngine(impl: SimulatedAsrEngine): AsrEngine
}
