package com.example.securanet.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.securanet.R
import com.example.securanet.ui.contacts.ContactsScreen
import com.example.securanet.ui.home.HomeScreen
import com.example.securanet.ui.navigation.Screen
import com.example.securanet.ui.sos.SosScreen
import androidx.compose.ui.res.painterResource

sealed class BottomNavItem(val route: String, val iconResId: Int, val labelResId: Int) {
    object Home : BottomNavItem(Screen.Home.route, R.drawable.home_24px, R.string.nav_home)
    object Contacts : BottomNavItem(Screen.Contacts.route, R.drawable.person_24px, R.string.nav_contacts)
    object Sos : BottomNavItem(Screen.Sos.route, R.drawable.warning_24px, R.string.nav_sos)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(BottomNavItem.Home, BottomNavItem.Contacts, BottomNavItem.Sos)

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
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
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
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Contacts.route) { ContactsScreen() }
            composable(Screen.Sos.route) { SosScreen() }
        }
    }
}
