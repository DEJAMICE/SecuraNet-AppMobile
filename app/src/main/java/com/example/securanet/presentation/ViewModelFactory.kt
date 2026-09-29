package com.example.securanet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.securanet.common.AppContainer
import com.example.securanet.presentation.auth.AuthViewModel

class ViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(appContainer.authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
