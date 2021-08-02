package com.chinthaka.chinthaka_beta.di

import com.chinthaka.chinthaka_beta.repositories.AuthRepository
import com.chinthaka.chinthaka_beta.repositories.DefaultAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object AuthModule {

    @Provides
    fun providesAuthRepository() = DefaultAuthRepository() as AuthRepository
}