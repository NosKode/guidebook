package com.guidebook.util

import com.guidebook.domain.exception.ForbiddenException
import com.guidebook.domain.model.User
import com.guidebook.domain.model.UserRole

fun requireAdmin(user: User) {
    if (user.role != UserRole.ADMIN) throw ForbiddenException("Admin access required")
}
