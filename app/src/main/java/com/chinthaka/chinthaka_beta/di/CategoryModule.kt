package com.chinthaka.chinthaka_beta.di

import com.chinthaka.chinthaka_beta.repositories.CategoryRepository
import com.chinthaka.chinthaka_beta.repositories.DefaultCategoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object CategoryModule {

    @Provides
    fun providesCategoryRepository() = DefaultCategoryRepository() as CategoryRepository
}