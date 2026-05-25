package com.guidebook.service

import com.guidebook.data.dto.CategoryCreateRequest
import com.guidebook.data.dto.CategoryUpdateRequest
import com.guidebook.data.repository.CategoryRepository
import com.guidebook.domain.exception.ConflictException
import com.guidebook.domain.exception.NotFoundException
import com.guidebook.domain.model.Category

class CategoryService(private val categoryRepository: CategoryRepository) {

    suspend fun findAll(): List<Category> = categoryRepository.findAll()

    suspend fun findById(id: Int): Category =
        categoryRepository.findById(id) ?: throw NotFoundException("Category not found")

    suspend fun create(request: CategoryCreateRequest): Category {
        val name = request.name.trim()
        if (categoryRepository.findByName(name) != null)
            throw ConflictException("Category '$name' already exists")
        return categoryRepository.create(name, request.description?.trim())
    }

    suspend fun update(id: Int, request: CategoryUpdateRequest): Category {
        categoryRepository.findById(id) ?: throw NotFoundException("Category not found")
        request.name?.trim()?.let { name ->
            val conflict = categoryRepository.findByName(name)
            if (conflict != null && conflict.id != id)
                throw ConflictException("Category '$name' already exists")
        }
        return categoryRepository.update(id, request.name?.trim(), request.description?.trim())
            ?: throw NotFoundException("Category not found")
    }

    suspend fun delete(id: Int) {
        if (!categoryRepository.delete(id)) throw NotFoundException("Category not found")
    }
}
