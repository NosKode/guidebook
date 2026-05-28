package com.guidebook.app.data.mapper

import com.guidebook.app.data.remote.dto.CategoryDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryMappersTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = CategoryDto(id = 1, name = "Park", description = "Parks and gardens")
        val domain = dto.toDomain()
        assertEquals(1, domain.id)
        assertEquals("Park", domain.name)
        assertEquals("Parks and gardens", domain.description)
    }

    @Test
    fun `toDomain handles null description`() {
        assertNull(CategoryDto(id = 2, name = "Museum", description = null).toDomain().description)
    }

    @Test
    fun `toDomain preserves numeric id`() {
        assertEquals(42, CategoryDto(id = 42, name = "Cafe", description = null).toDomain().id)
    }
}
