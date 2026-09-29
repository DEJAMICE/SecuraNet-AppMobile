package com.example.securanet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.securanet.common.AppContainer
import com.example.securanet.presentation.auth.AuthViewModel
import com.example.securanet.presentation.contacts.ContactsViewModel

class ViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(appContainer.authRepository) as T
        }
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(appContainer.contactRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
