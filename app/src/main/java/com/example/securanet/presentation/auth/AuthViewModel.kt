package com.example.securanet.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securanet.R
import com.example.securanet.common.Validators
import com.example.securanet.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun login() {
        if (!validateFields()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(_state.value.email, _state.value.password)
            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun register() {
        if (!validateFields()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(_state.value.email, _state.value.password)
            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun resetSuccess() {
        _state.update { it.copy(isSuccess = false) }
    }

    private fun validateFields(): Boolean {
        val emailValid = Validators.isValidEmail(_state.value.email)
        val passwordValid = Validators.isValidPassword(_state.value.password)

        _state.update {
            it.copy(
                emailError = if (emailValid) null else R.string.invalid_email,
                passwordError = if (passwordValid) null else R.string.invalid_password
            )
        }

        return emailValid && passwordValid
    }
}
