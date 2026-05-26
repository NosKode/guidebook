package com.guidebook.app.presentation.navigation

object Routes {
    const val SPLASH       = "splash"
    const val LOGIN        = "login"
    const val REGISTER     = "register"
    const val CATALOG      = "catalog"
    const val PLACE_DETAIL = "place/{placeId}"
    const val PHOTO_VIEWER = "photo_viewer?url={photoUrl}"
    const val FAVORITES    = "favorites"
    const val MY_PLACES    = "my_places"
    const val ADD_PLACE    = "add_place"
    const val ADD_PHOTO    = "add_photo/{placeId}"
    const val PROFILE      = "profile"
    const val ADMIN        = "admin"

    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"

    fun placeDetail(placeId: String) = "place/$placeId"
    fun addPhoto(placeId: String)    = "add_photo/$placeId"
    fun photoViewer(url: String)     = "photo_viewer?url=${java.net.URLEncoder.encode(url, "UTF-8")}"
}
