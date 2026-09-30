package com.example.securanet.common

import com.example.securanet.data.AuthRepositoryFake
import com.example.securanet.data.ContactRepositoryFake
import com.example.securanet.data.DeviceRepositoryFake
import com.example.securanet.domain.repository.AuthRepository
import com.example.securanet.domain.repository.ContactRepository
import com.example.securanet.domain.repository.DeviceRepository

interface AppContainer {
    val authRepository: AuthRepository
    val contactRepository: ContactRepository
    val deviceRepository: DeviceRepository
}

class DefaultAppContainer : AppContainer {
    
    override val authRepository: AuthRepository by lazy {
        AuthRepositoryFake()
    }
    
    override val contactRepository: ContactRepository by lazy {
        ContactRepositoryFake()
    }

    override val deviceRepository: DeviceRepository by lazy {
        DeviceRepositoryFake()
    }
}
