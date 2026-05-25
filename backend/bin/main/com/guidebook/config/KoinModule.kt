package com.guidebook.config

import com.guidebook.data.repository.CategoryRepository
import com.guidebook.data.repository.CategoryRepositoryImpl
import com.guidebook.data.repository.PlaceRepository
import com.guidebook.data.repository.PlaceRepositoryImpl
import com.guidebook.data.repository.UserRepository
import com.guidebook.data.repository.UserRepositoryImpl
import com.guidebook.service.AuthService
import com.guidebook.service.CategoryService
import com.guidebook.service.PlaceService
import io.ktor.server.application.*
import org.koin.dsl.module

object KoinModule {
    fun appModule(application: Application) = module {
        single { JwtConfig(application) }
        single<UserRepository> { UserRepositoryImpl() }
        single<CategoryRepository> { CategoryRepositoryImpl() }
        single<PlaceRepository> { PlaceRepositoryImpl() }
        single { AuthService(get(), get()) }
        single { CategoryService(get()) }
        single {
            val baseUrl = application.environment.config
                .propertyOrNull("server.baseUrl")?.getString() ?: "http://localhost:8080"
            PlaceService(get(), get(), baseUrl)
        }
    }
}
