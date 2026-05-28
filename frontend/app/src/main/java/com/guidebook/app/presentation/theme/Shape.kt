package com.guidebook.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // inline badges, snackbar
    small      = RoundedCornerShape(12.dp),  // search bar, text fields
    medium     = RoundedCornerShape(20.dp),  // chips, small buttons
    large      = RoundedCornerShape(24.dp),  // cards — основная единица
    extraLarge = RoundedCornerShape(32.dp)   // bottom sheets, modals, dialogs
)
