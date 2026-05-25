package com.guidebook.data.repository

import com.guidebook.db.tables.CategoriesTable
import com.guidebook.domain.model.Category
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

interface CategoryRepository {
    suspend fun findAll(): List<Category>
    suspend fun findById(id: Int): Category?
    suspend fun findByName(name: String): Category?
    suspend fun create(name: String, description: String?): Category
    suspend fun update(id: Int, name: String?, description: String?): Category?
    suspend fun delete(id: Int): Boolean
}

class CategoryRepositoryImpl : CategoryRepository {

    override suspend fun findAll(): List<Category> = newSuspendedTransaction {
        CategoriesTable.selectAll().map { it.toCategory() }
    }

    override suspend fun findById(id: Int): Category? = newSuspendedTransaction {
        CategoriesTable.select { CategoriesTable.id eq id }
            .singleOrNull()?.toCategory()
    }

    override suspend fun findByName(name: String): Category? = newSuspendedTransaction {
        CategoriesTable.select { CategoriesTable.name eq name }
            .singleOrNull()?.toCategory()
    }

    override suspend fun create(name: String, description: String?): Category = newSuspendedTransaction {
        val newId = CategoriesTable.insertAndGetId {
            it[CategoriesTable.name] = name
            it[CategoriesTable.description] = description
        }
        CategoriesTable.select { CategoriesTable.id eq newId }
            .single().toCategory()
    }

    override suspend fun update(id: Int, name: String?, description: String?): Category? =
        newSuspendedTransaction {
            val rowsUpdated = CategoriesTable.update({ CategoriesTable.id eq id }) {
                name?.let { n -> it[CategoriesTable.name] = n }
                description?.let { d -> it[CategoriesTable.description] = d }
            }
            if (rowsUpdated == 0) return@newSuspendedTransaction null
            CategoriesTable.select { CategoriesTable.id eq id }
                .single().toCategory()
        }

    override suspend fun delete(id: Int): Boolean = newSuspendedTransaction {
        CategoriesTable.deleteWhere { CategoriesTable.id eq id } > 0
    }

    private fun ResultRow.toCategory() = Category(
        id = this[CategoriesTable.id].value,
        name = this[CategoriesTable.name],
        description = this[CategoriesTable.description]
    )
}
