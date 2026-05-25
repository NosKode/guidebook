package com.guidebook.config

import com.guidebook.data.repository.UserRepository
import com.guidebook.data.repository.UserRepositoryImpl
import com.guidebook.service.AuthService
import io.ktor.server.application.*
import org.koin.dsl.module

object KoinModule {
    fun appModule(application: Application) = module {
        single { JwtConfig(application) }
        single<UserRepository> { UserRepositoryImpl() }
        single { AuthService(get(), get()) }
    }
}
