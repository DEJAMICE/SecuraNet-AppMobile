package com.example.securanet.presentation.devices

import com.example.securanet.domain.model.Device
import com.example.securanet.domain.model.DeviceType
import com.example.securanet.domain.model.DiscoveredDevice

data class DevicesState(
    val devices: List<Device> = emptyList(),
    val reconnectingDeviceId: String? = null,
    val deviceToUnlink: Device? = null,
    val snackbarMessage: String? = null,
    val isLinkSheetOpen: Boolean = false,
    val selectedDeviceType: DeviceType? = null,
    val isScanning: Boolean = false,
    val discoveredDevices: List<DiscoveredDevice> = emptyList(),
    val isLinkingDevice: Boolean = false
)
