package com.example.securanet.presentation.devices

import com.example.securanet.domain.model.Device

data class DevicesState(
    val devices: List<Device> = emptyList(),
    val reconnectingDeviceId: String? = null,
    val deviceToUnlink: Device? = null,
    val snackbarMessage: String? = null
)
