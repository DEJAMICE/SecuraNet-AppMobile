package com.example.securanet.domain.model

data class DiscoveredDevice(
    val id: String,
    val name: String,
    val type: DeviceType
)
