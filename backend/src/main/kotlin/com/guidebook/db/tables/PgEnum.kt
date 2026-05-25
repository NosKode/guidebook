package com.guidebook.db.tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject

inline fun <reified T : Enum<T>> Table.pgEnum(columnName: String, pgTypeName: String): Column<T> =
    customEnumeration(
        name = columnName,
        sql = pgTypeName,
        fromDb = { value -> enumValueOf<T>(value as String) },
        toDb = { PGobject().apply { type = pgTypeName; this.value = it.name } }
    )
