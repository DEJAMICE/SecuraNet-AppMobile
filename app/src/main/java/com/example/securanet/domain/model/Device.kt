package com.example.securanet.domain.model

data class Device(
    val id: String,
    val type: DeviceType,
    val name: String,
    val isLinked: Boolean,
    val isConnected: Boolean,
    val batteryPercent: Int,
    val lastSeenMinutes: Int = 0
)
