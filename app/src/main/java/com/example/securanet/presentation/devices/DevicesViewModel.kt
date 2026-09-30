package com.example.securanet.presentation.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securanet.domain.model.Device
import com.example.securanet.domain.model.DeviceType
import com.example.securanet.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DevicesViewModel(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DevicesState())
    val state: StateFlow<DevicesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.getDevices().collect { devices ->
                _state.update { it.copy(devices = devices) }
            }
        }
    }

    fun linkDevice(type: DeviceType) {
        viewModelScope.launch {
            deviceRepository.linkDevice(type)
        }
    }

    fun requestUnlink(device: Device) {
        _state.update { it.copy(deviceToUnlink = device) }
    }

    fun dismissUnlinkDialog() {
        _state.update { it.copy(deviceToUnlink = null) }
    }

    fun confirmUnlink() {
        val device = _state.value.deviceToUnlink ?: return
        _state.update { it.copy(deviceToUnlink = null) }
        viewModelScope.launch {
            deviceRepository.unlinkDevice(device.id)
        }
    }

    fun reconnectDevice(id: String) {
        if (_state.value.reconnectingDeviceId != null) return
        _state.update { it.copy(reconnectingDeviceId = id) }
        viewModelScope.launch {
            deviceRepository.reconnectDevice(id)
            _state.update { it.copy(reconnectingDeviceId = null) }
        }
    }

    fun testDevice(id: String, message: String) {
        viewModelScope.launch {
            deviceRepository.testDevice(id)
            _state.update { it.copy(snackbarMessage = message) }
        }
    }

    fun clearSnackbarMessage() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}
