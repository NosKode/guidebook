package com.guidebook.config

import io.ktor.server.application.*
import org.koin.dsl.module

object KoinModule {
    fun appModule(application: Application) = module {
        single { JwtConfig(application) }
    }
}
