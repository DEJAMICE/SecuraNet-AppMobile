package com.example.securanet.data

import com.example.securanet.domain.model.Device
import com.example.securanet.domain.model.DeviceType
import com.example.securanet.domain.repository.DeviceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DeviceRepositoryFake : DeviceRepository {

    private val _devices = MutableStateFlow<List<Device>>(
        listOf(
            Device(
                id = "panic_button_1",
                type = DeviceType.PANIC_BUTTON,
                name = "Panic Button",
                isLinked = true,
                isConnected = true,
                batteryPercent = 80,
                lastSeenMinutes = 0
            ),
            Device(
                id = "smart_sensor_1",
                type = DeviceType.SMART_SENSOR,
                name = "Smart Sensor",
                isLinked = false,
                isConnected = false,
                batteryPercent = 100,
                lastSeenMinutes = 0
            )
        )
    )

    override fun getDevices(): Flow<List<Device>> = _devices.asStateFlow()

    override suspend fun linkDevice(type: DeviceType) {
        delay(300)
        _devices.update { current ->
            current.map { device ->
                if (device.type == type) {
                    device.copy(isLinked = true, isConnected = true, lastSeenMinutes = 0)
                } else {
                    device
                }
            }
        }
    }

    override suspend fun unlinkDevice(id: String) {
        delay(300)
        _devices.update { current ->
            current.map { device ->
                if (device.id == id) {
                    device.copy(isLinked = false, isConnected = false)
                } else {
                    device
                }
            }
        }
    }

    override suspend fun reconnectDevice(id: String) {
        delay(2000)
        _devices.update { current ->
            current.map { device ->
                if (device.id == id) {
                    device.copy(isConnected = true, lastSeenMinutes = 0)
                } else {
                    device
                }
            }
        }
    }

    override suspend fun testDevice(id: String) {
        delay(300)
    }

    // Exposed strictly for @Preview and ViewModel unit tests
    fun setBattery(id: String, percent: Int) {
        _devices.update { current ->
            current.map { device ->
                if (device.id == id) device.copy(batteryPercent = percent) else device
            }
        }
    }

    fun setConnected(id: String, connected: Boolean, lastSeenMinutes: Int = 5) {
        _devices.update { current ->
            current.map { device ->
                if (device.id == id) device.copy(isConnected = connected, lastSeenMinutes = lastSeenMinutes) else device
            }
        }
    }
}
