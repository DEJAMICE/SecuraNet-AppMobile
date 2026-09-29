package com.example.securanet.domain.repository

import com.example.securanet.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>
    suspend fun addContact(name: String, relationship: String, phone: String = "", isPriority: Boolean = false)
    suspend fun deleteContact(id: String)
    suspend fun togglePriority(id: String)
}
