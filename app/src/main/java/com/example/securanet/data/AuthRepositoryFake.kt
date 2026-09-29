package com.example.securanet.data

import com.example.securanet.domain.model.User
import com.example.securanet.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepositoryFake : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): Result<Unit> {
        delay(1000) // Simulate network delay
        return if (email.isNotEmpty() && password.length >= 8) {
            _currentUser.value = User(id = UUID.randomUUID().toString(), email = email)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        delay(1000) // Simulate network delay
        return if (email.isNotEmpty() && password.length >= 8) {
            _currentUser.value = User(id = UUID.randomUUID().toString(), email = email)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Registration failed"))
        }
    }

    override suspend fun logout() {
        delay(500)
        _currentUser.value = null
    }
}
