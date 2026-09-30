package com.example.securanet.presentation.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securanet.domain.model.Device
import com.example.securanet.domain.model.DeviceType
import com.example.securanet.domain.model.DiscoveredDevice
import com.example.securanet.domain.repository.DeviceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var scanJob: Job? = null
    private var testSignalJob: Job? = null

    init {
        viewModelScope.launch {
            deviceRepository.getDevices().collect { devices ->
                _state.update { it.copy(devices = devices) }
            }
        }
    }

    // ── Link Sheet Flow ──────────────────────────────────────────────────────────

    fun openLinkSheet(preselectedType: DeviceType) {
        _state.update { it.copy(isLinkSheetOpen = true) }
        selectDeviceType(preselectedType)
    }

    fun closeLinkSheet() {
        scanJob?.cancel()
        scanJob = null
        _state.update {
            it.copy(
                isLinkSheetOpen = false,
                selectedDeviceType = null,
                isScanning = false,
                discoveredDevices = emptyList(),
                isLinkingDevice = false
            )
        }
    }

    fun selectDeviceType(type: DeviceType) {
        scanJob?.cancel()
        scanJob = null

        val isAlreadyLinked = _state.value.devices.any { it.type == type && it.isLinked }

        if (isAlreadyLinked) {
            _state.update {
                it.copy(
                    selectedDeviceType = type,
                    isScanning = false,
                    discoveredDevices = emptyList()
                )
            }
        } else {
            _state.update {
                it.copy(
                    selectedDeviceType = type,
                    isScanning = true,
                    discoveredDevices = emptyList()
                )
            }

            scanJob = viewModelScope.launch {
                delay(3000)
                val discovered = generateFakeDiscoveredDevices(type)
                _state.update {
                    it.copy(
                        isScanning = false,
                        discoveredDevices = discovered
                    )
                }
            }
        }
    }

    fun linkDiscoveredDevice(discoveredDevice: DiscoveredDevice) {
        scanJob?.cancel()
        scanJob = null
        _state.update { it.copy(isLinkingDevice = true) }

        viewModelScope.launch {
            deviceRepository.linkDevice(discoveredDevice.type)
            val updatedDevice = _state.value.devices.firstOrNull { it.type == discoveredDevice.type }
                ?: Device(
                    id = discoveredDevice.id,
                    type = discoveredDevice.type,
                    name = discoveredDevice.name,
                    isLinked = true,
                    isConnected = true,
                    batteryPercent = 100
                )

            _state.update {
                it.copy(
                    isLinkingDevice = false,
                    isLinkSheetOpen = false,
                    selectedDeviceType = null,
                    isScanning = false,
                    discoveredDevices = emptyList(),
                    linkedSuccessDevice = updatedDevice
                )
            }
        }
    }

    private fun generateFakeDiscoveredDevices(type: DeviceType): List<DiscoveredDevice> {
        return when (type) {
            DeviceType.PANIC_BUTTON -> listOf(
                DiscoveredDevice(
                    id = "panic_button_1",
                    name = "SecuraNet Panic Button #042",
                    type = DeviceType.PANIC_BUTTON
                )
            )
            DeviceType.SMART_SENSOR -> listOf(
                DiscoveredDevice(
                    id = "smart_sensor_1",
                    name = "SecuraNet Sensor #108",
                    type = DeviceType.SMART_SENSOR
                )
            )
        }
    }

    // ── Test Signal Sheet Flow ───────────────────────────────────────────────────

    fun openTestSheet(device: Device) {
        testSignalJob?.cancel()
        _state.update {
            it.copy(
                isTestSheetOpen = true,
                testingDevice = device,
                isTestWaiting = true,
                isTestSuccess = false
            )
        }

        testSignalJob = viewModelScope.launch {
            deviceRepository.testDevice(device.id)
            delay(3000)
            _state.update {
                it.copy(
                    isTestWaiting = false,
                    isTestSuccess = true
                )
            }
        }
    }

    fun closeTestSheet() {
        testSignalJob?.cancel()
        testSignalJob = null
        _state.update {
            it.copy(
                isTestSheetOpen = false,
                testingDevice = null,
                isTestWaiting = false,
                isTestSuccess = false
            )
        }
    }

    // ── Link Success Sheet Flow ─────────────────────────────────────────────────

    fun closeLinkSuccessSheet() {
        _state.update { it.copy(linkedSuccessDevice = null) }
    }

    fun onTestFromLinkSuccess(device: Device) {
        _state.update { it.copy(linkedSuccessDevice = null) }
        openTestSheet(device)
    }

    // ── How To Link Sheet Flow ──────────────────────────────────────────────────

    fun openHowToLinkSheet() {
        _state.update { it.copy(isHowToLinkSheetOpen = true) }
    }

    fun closeHowToLinkSheet() {
        _state.update { it.copy(isHowToLinkSheetOpen = false) }
    }

    // ── Existing Device Actions ──────────────────────────────────────────────────

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

    fun clearSnackbarMessage() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}
