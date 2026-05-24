package com.guidebook

import com.guidebook.config.DatabaseConfig
import com.guidebook.config.KoinModule
import com.guidebook.plugins.*
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    val hoconConfig = HoconApplicationConfig(ConfigFactory.load())
    val port = hoconConfig.propertyOrNull("ktor.deployment.port")
        ?.getString()?.toIntOrNull() ?: 8080

    embeddedServer(
        factory = Netty,
        environment = applicationEngineEnvironment {
            config = hoconConfig
            connector { this.port = port }
            module(Application::module)
        }
    ).start(wait = true)
}

fun Application.module() {
    DatabaseConfig.init(this)
    install(Koin) {
        slf4jLogger()
        modules(KoinModule.appModule(this@module))
    }
    configureSerialization()
    configureAuthentication()
    configureStatusPages()
    configureCallLogging()
    configureCors()
    configureRouting()
}
