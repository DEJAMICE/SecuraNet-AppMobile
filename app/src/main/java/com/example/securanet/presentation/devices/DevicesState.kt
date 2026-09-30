package com.example.securanet.presentation.devices

import com.example.securanet.domain.model.Device
import com.example.securanet.domain.model.DeviceType
import com.example.securanet.domain.model.DiscoveredDevice

data class DevicesState(
    val devices: List<Device> = emptyList(),
    val reconnectingDeviceId: String? = null,
    val deviceToUnlink: Device? = null,
    val snackbarMessage: String? = null,

    // Link sheet flow
    val isLinkSheetOpen: Boolean = false,
    val selectedDeviceType: DeviceType? = null,
    val isScanning: Boolean = false,
    val discoveredDevices: List<DiscoveredDevice> = emptyList(),
    val isLinkingDevice: Boolean = false,

    // Test signal sheet flow
    val isTestSheetOpen: Boolean = false,
    val testingDevice: Device? = null,
    val isTestWaiting: Boolean = false,
    val isTestSuccess: Boolean = false,

    // Link success confirmation sheet flow
    val linkedSuccessDevice: Device? = null,

    // How to link info sheet flow
    val isHowToLinkSheetOpen: Boolean = false
)
