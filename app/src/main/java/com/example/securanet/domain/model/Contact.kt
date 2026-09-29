package com.example.securanet.domain.model

data class Contact(
    val id: String,
    val name: String,
    val relationship: String,
    val phone: String = "",
    val isPriority: Boolean = false
)
