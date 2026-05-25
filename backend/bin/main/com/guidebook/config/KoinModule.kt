package com.guidebook.config

import com.guidebook.data.repository.CategoryRepository
import com.guidebook.data.repository.CategoryRepositoryImpl
import com.guidebook.data.repository.FavoriteRepository
import com.guidebook.data.repository.FavoriteRepositoryImpl
import com.guidebook.data.repository.PhotoRepository
import com.guidebook.data.repository.PhotoRepositoryImpl
import com.guidebook.data.repository.PlaceRepository
import com.guidebook.data.repository.PlaceRepositoryImpl
import com.guidebook.data.repository.ReviewRepository
import com.guidebook.data.repository.ReviewRepositoryImpl
import com.guidebook.data.repository.UserRepository
import com.guidebook.data.repository.UserRepositoryImpl
import com.guidebook.service.AuthService
import com.guidebook.service.CategoryService
import com.guidebook.service.FavoriteService
import com.guidebook.service.FileStorageService
import com.guidebook.service.PhotoService
import com.guidebook.service.PlaceService
import com.guidebook.service.ReviewService
import io.ktor.server.application.*
import org.koin.dsl.module

object KoinModule {
    fun appModule(application: Application) = module {
        single { JwtConfig(application) }
        single<UserRepository> { UserRepositoryImpl() }
        single<CategoryRepository> { CategoryRepositoryImpl() }
        single<PlaceRepository> { PlaceRepositoryImpl() }
        single<PhotoRepository> { PhotoRepositoryImpl() }
        single<ReviewRepository> { ReviewRepositoryImpl() }
        single<FavoriteRepository> { FavoriteRepositoryImpl() }
        single { AuthService(get(), get()) }
        single { CategoryService(get()) }
        single {
            val storagePath = application.environment.config
                .propertyOrNull("storage.path")?.getString() ?: "./storage"
            FileStorageService(storagePath)
        }
        single {
            val baseUrl = application.environment.config
                .propertyOrNull("server.baseUrl")?.getString() ?: "http://localhost:8080"
            PlaceService(get(), get(), get(), baseUrl)
        }
        single {
            val baseUrl = application.environment.config
                .propertyOrNull("server.baseUrl")?.getString() ?: "http://localhost:8080"
            PhotoService(get(), get(), get(), baseUrl)
        }
        single { ReviewService(get(), get()) }
        single {
            val baseUrl = application.environment.config
                .propertyOrNull("server.baseUrl")?.getString() ?: "http://localhost:8080"
            FavoriteService(get(), get(), get(), baseUrl)
        }
    }
}
