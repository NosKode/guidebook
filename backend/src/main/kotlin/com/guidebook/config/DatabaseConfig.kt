package com.guidebook.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.initDatabase() {
    val config = environment.config
    val jdbcUrl   = config.property("database.url").getString()
    val user      = config.property("database.user").getString()
    val password  = config.property("database.password").getString()

    val hikariConfig = HikariConfig().apply {
        driverClassName     = "org.postgresql.Driver"
        this.jdbcUrl        = jdbcUrl
        username            = user
        this.password       = password
        maximumPoolSize     = 10
        isAutoCommit        = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(hikariConfig)
    environment.monitor.subscribe(ApplicationStopped) { dataSource.close() }

    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate()

    Database.connect(dataSource)
}
