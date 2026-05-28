package com.guidebook.app.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guidebook.app.data.local.prefs.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = themePreferences.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}
