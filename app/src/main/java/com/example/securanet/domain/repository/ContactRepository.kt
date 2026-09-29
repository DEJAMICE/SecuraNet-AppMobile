package com.example.securanet.domain.repository

import com.example.securanet.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>
    suspend fun addContact(name: String, phone: String, isPriority: Boolean)
    suspend fun deleteContact(id: String)
    suspend fun togglePriority(id: String)
}
