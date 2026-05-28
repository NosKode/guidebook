package com.guidebook.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.guidebook.app.presentation.navigation.AppNavGraph
import com.guidebook.app.presentation.theme.GuidebookTheme
import com.guidebook.app.presentation.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            GuidebookTheme(darkTheme = isDarkTheme) {
                AppNavGraph()
            }
        }
    }
}
