package com.chinthaka.chinthaka_beta.di

import com.chinthaka.chinthaka_beta.repositories.DefaultMainRepository
import com.chinthaka.chinthaka_beta.repositories.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object MainModule {

    @Provides
    fun providesMainRepository() = DefaultMainRepository() as MainRepository
}