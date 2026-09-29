package com.example.securanet.common

import com.example.securanet.data.AuthRepositoryFake
import com.example.securanet.data.ContactRepositoryFake
import com.example.securanet.domain.repository.AuthRepository
import com.example.securanet.domain.repository.ContactRepository

interface AppContainer {
    val authRepository: AuthRepository
    val contactRepository: ContactRepository
}

class DefaultAppContainer : AppContainer {
    
    override val authRepository: AuthRepository by lazy {
        AuthRepositoryFake()
    }
    
    override val contactRepository: ContactRepository by lazy {
        ContactRepositoryFake()
    }
}
