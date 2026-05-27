package com.guidebook.app.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.guidebook.app.presentation.admin.AdminPanelScreen
import com.guidebook.app.presentation.auth.LoginScreen
import com.guidebook.app.presentation.auth.RegisterScreen
import com.guidebook.app.presentation.catalog.CatalogScreen
import com.guidebook.app.presentation.detail.PhotoViewerScreen
import com.guidebook.app.presentation.detail.PlaceDetailScreen
import com.guidebook.app.presentation.favorites.FavoritesScreen
import com.guidebook.app.presentation.myplaces.AddPhotoScreen
import com.guidebook.app.presentation.myplaces.AddPlaceScreen
import com.guidebook.app.presentation.myplaces.MyPlacesScreen
import com.guidebook.app.presentation.profile.ProfileScreen
import com.guidebook.app.presentation.splash.SplashScreen

private val bottomBarRoutes = setOf(
    Routes.CATALOG, Routes.FAVORITES, Routes.MY_PLACES, Routes.PROFILE
)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                BottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController    = navController,
            startDestination = Routes.AUTH_GRAPH,
            modifier         = Modifier.padding(paddingValues)
        ) {

            // ── Auth Graph ──────────────────────────────────────────────
            navigation(
                route            = Routes.AUTH_GRAPH,
                startDestination = Routes.SPLASH
            ) {
                composable(Routes.SPLASH) {
                    SplashScreen(
                        onNavigateToMain = {
                            navController.navigate(Routes.MAIN_GRAPH) {
                                popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.LOGIN) {
                    LoginScreen(
                        onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                        onLoginSuccess = {
                            navController.navigate(Routes.MAIN_GRAPH) {
                                popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreen(
                        onNavigateToLogin  = { navController.popBackStack() },
                        onRegisterSuccess  = {
                            navController.navigate(Routes.MAIN_GRAPH) {
                                popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // ── Main Graph ──────────────────────────────────────────────
            navigation(
                route            = Routes.MAIN_GRAPH,
                startDestination = Routes.CATALOG
            ) {
                composable(Routes.CATALOG) {
                    CatalogScreen(
                        onPlaceClick = { id ->
                            navController.navigate(Routes.placeDetail(id))
                        }
                    )
                }

                composable(Routes.FAVORITES) {
                    FavoritesScreen()
                }

                composable(Routes.MY_PLACES) {
                    MyPlacesScreen(
                        onAddPlace = { navController.navigate(Routes.ADD_PLACE) }
                    )
                }

                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onAdminPanel = { navController.navigate(Routes.ADMIN) },
                        onLogout = {
                            navController.navigate(Routes.AUTH_GRAPH) {
                                popUpTo(Routes.MAIN_GRAPH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route     = Routes.PLACE_DETAIL,
                    arguments = listOf(navArgument("placeId") { type = NavType.StringType })
                ) { entry ->
                    val placeId = entry.arguments?.getString("placeId") ?: ""
                    PlaceDetailScreen(
                        placeId      = placeId,
                        onBack       = { navController.popBackStack() },
                        onPhotoClick = { url -> navController.navigate(Routes.photoViewer(url)) },
                        onAddPhoto   = { id  -> navController.navigate(Routes.addPhoto(id)) }
                    )
                }

                composable(
                    route     = Routes.PHOTO_VIEWER,
                    arguments = listOf(
                        navArgument("photoUrl") {
                            type         = NavType.StringType
                            nullable     = true
                            defaultValue = null
                        }
                    )
                ) { entry ->
                    val url = entry.arguments?.getString("photoUrl") ?: ""
                    PhotoViewerScreen(
                        photoUrl = url,
                        onBack   = { navController.popBackStack() }
                    )
                }

                composable(Routes.ADD_PLACE) {
                    AddPlaceScreen(onBack = { navController.popBackStack() })
                }

                composable(
                    route     = Routes.ADD_PHOTO,
                    arguments = listOf(navArgument("placeId") { type = NavType.StringType })
                ) { entry ->
                    val placeId = entry.arguments?.getString("placeId") ?: ""
                    AddPhotoScreen(
                        placeId = placeId,
                        onBack  = { navController.popBackStack() }
                    )
                }

                composable(Routes.ADMIN) {
                    AdminPanelScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
