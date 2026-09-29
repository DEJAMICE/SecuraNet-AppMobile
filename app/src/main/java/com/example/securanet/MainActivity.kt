package com.example.securanet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.securanet.presentation.ViewModelFactory
import com.example.securanet.presentation.auth.AuthViewModel
import com.example.securanet.ui.auth.LoginScreen
import com.example.securanet.ui.auth.RegisterScreen
import com.example.securanet.ui.navigation.Screen
import com.example.securanet.ui.theme.SecuraNetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SecuraNetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val appContainer = (application as SecuraNetApplication).container
                    val viewModelFactory = ViewModelFactory(appContainer)
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route
                    ) {
                        composable(Screen.Login.route) {
                            val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                            LoginScreen(
                                viewModel = authViewModel,
                                onNavigateToRegister = {
                                    navController.navigate(Screen.Register.route)
                                },
                                onLoginSuccess = {
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Register.route) {
                            val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                            RegisterScreen(
                                viewModel = authViewModel,
                                onNavigateToLogin = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                },
                                onRegisterSuccess = {
                                    navController.navigate(Screen.Main.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Main.route) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Main Flow Placeholder")
                            }
                        }
                    }
                }
            }
        }
    }
}