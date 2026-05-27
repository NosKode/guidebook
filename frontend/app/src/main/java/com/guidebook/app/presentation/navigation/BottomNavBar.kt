package com.guidebook.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

private data class BottomNavItem(
    val route:          String,
    val label:          String,
    val iconSelected:   ImageVector,
    val iconUnselected: ImageVector
)

// Короткие подписи — влезают в 1 строку при 5 вкладках
private val bottomNavItems = listOf(
    BottomNavItem(Routes.CATALOG,   "Каталог",   Icons.Filled.Explore,      Icons.Outlined.Explore),
    BottomNavItem(Routes.MAP,       "Карта",     Icons.Filled.Map,          Icons.Outlined.Map),
    BottomNavItem(Routes.FAVORITES, "Избранное", Icons.Filled.Favorite,     Icons.Outlined.FavoriteBorder),
    BottomNavItem(Routes.MY_PLACES, "Места",     Icons.Filled.Place,        Icons.Outlined.Place),
    BottomNavItem(Routes.PROFILE,   "Профиль",   Icons.Filled.Person,       Icons.Outlined.Person),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon = {
                    Icon(
                        imageVector        = if (selected) item.iconSelected else item.iconUnselected,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text     = item.label,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        textAlign = TextAlign.Center
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}
