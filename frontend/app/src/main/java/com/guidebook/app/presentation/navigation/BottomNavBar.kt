package com.guidebook.app.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp, top = 4.dp)
            .navigationBarsPadding(),
        shape           = RoundedCornerShape(28.dp),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        tonalElevation  = 2.dp
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                FloatingNavItem(
                    item     = item,
                    selected = selected,
                    onClick  = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    item:     BottomNavItem,
    selected: Boolean,
    onClick:  () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue   = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label         = "nav_icon_color"
    )
    val labelColor by animateColorAsState(
        targetValue   = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label         = "nav_label_color"
    )
    val pillWidth by animateDpAsState(
        targetValue   = if (selected) 52.dp else 40.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "nav_pill_width"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        // Pill indicator behind icon
        Box(
            modifier         = Modifier
                .size(width = pillWidth, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selected) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = if (selected) item.iconSelected else item.iconUnselected,
                contentDescription = item.label,
                tint               = iconColor,
                modifier           = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.height(3.dp))

        Text(
            text       = item.label,
            fontSize   = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = labelColor,
            maxLines   = 1
        )
    }
}
