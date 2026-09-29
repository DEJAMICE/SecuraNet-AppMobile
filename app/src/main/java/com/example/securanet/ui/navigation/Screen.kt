package com.example.securanet.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    
    object Home : Screen("home")
    object Devices : Screen("devices")
    object TrustedNetwork : Screen("trusted_network")
    object Profile : Screen("profile")
}
