package com.example.securanet.domain.repository

import com.example.securanet.domain.model.Device
import com.example.securanet.domain.model.DeviceType
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getDevices(): Flow<List<Device>>
    suspend fun linkDevice(type: DeviceType)
    suspend fun unlinkDevice(id: String)
    suspend fun reconnectDevice(id: String)
    suspend fun testDevice(id: String)
}
