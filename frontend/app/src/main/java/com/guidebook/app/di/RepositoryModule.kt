package com.guidebook.app.di

import com.guidebook.app.data.repository.AuthRepositoryImpl
import com.guidebook.app.data.repository.CategoryRepositoryImpl
import com.guidebook.app.data.repository.PlaceRepositoryImpl
import com.guidebook.app.domain.repository.AuthRepository
import com.guidebook.app.domain.repository.CategoryRepository
import com.guidebook.app.domain.repository.PlaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPlaceRepository(impl: PlaceRepositoryImpl): PlaceRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
}
