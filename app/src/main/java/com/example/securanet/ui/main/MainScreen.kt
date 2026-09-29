package com.example.securanet.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.securanet.R
import com.example.securanet.SecuraNetApplication
import com.example.securanet.presentation.ViewModelFactory
import com.example.securanet.presentation.contacts.ContactsViewModel
import com.example.securanet.presentation.sos.SosViewModel
import com.example.securanet.ui.contacts.ContactsScreen
import com.example.securanet.ui.devices.DevicesScreen
import com.example.securanet.ui.home.HomeScreen
import com.example.securanet.ui.navigation.Screen
import com.example.securanet.ui.profile.ProfileScreen

sealed class BottomNavItem(val route: String, val iconResId: Int, val labelResId: Int) {
    object Home : BottomNavItem(Screen.Home.route, R.drawable.home_24px, R.string.nav_home)
    object Devices : BottomNavItem(Screen.Devices.route, R.drawable.devices_24px, R.string.nav_devices)
    object TrustedNetwork : BottomNavItem(Screen.TrustedNetwork.route, R.drawable.group_24px, R.string.nav_trusted_network)
    object Profile : BottomNavItem(Screen.Profile.route, R.drawable.account_circle_24px, R.string.nav_profile)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Devices,
        BottomNavItem.TrustedNetwork,
        BottomNavItem.Profile
    )

    val context = LocalContext.current
    val appContainer = remember { (context.applicationContext as SecuraNetApplication).container }
    val viewModelFactory = remember { ViewModelFactory(appContainer) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = stringResource(id = item.labelResId)
                            )
                        },
                        label = { Text(stringResource(id = item.labelResId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val sosViewModel: SosViewModel = viewModel(factory = viewModelFactory)
                HomeScreen(viewModel = sosViewModel)
            }
            composable(Screen.Devices.route) { DevicesScreen() }
            composable(Screen.TrustedNetwork.route) {
                val contactsViewModel: ContactsViewModel = viewModel(factory = viewModelFactory)
                ContactsScreen(viewModel = contactsViewModel)
            }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}
