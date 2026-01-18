package com.jamal.desktopclock.navigation

sealed class Screen(val route: String) {
    object Clock : Screen("clock")
    object Tasks : Screen("tasks")
}
