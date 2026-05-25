package com.guidebook.config

import com.guidebook.data.repository.CategoryRepository
import com.guidebook.data.repository.CategoryRepositoryImpl
import com.guidebook.data.repository.UserRepository
import com.guidebook.data.repository.UserRepositoryImpl
import com.guidebook.service.AuthService
import com.guidebook.service.CategoryService
import io.ktor.server.application.*
import org.koin.dsl.module

object KoinModule {
    fun appModule(application: Application) = module {
        single { JwtConfig(application) }
        single<UserRepository> { UserRepositoryImpl() }
        single<CategoryRepository> { CategoryRepositoryImpl() }
        single { AuthService(get(), get()) }
        single { CategoryService(get()) }
    }
}
