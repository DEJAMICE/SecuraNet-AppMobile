package com.example.securanet.data

import com.example.securanet.domain.model.Contact
import com.example.securanet.domain.repository.ContactRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class ContactRepositoryFake : ContactRepository {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())

    override fun getContacts(): Flow<List<Contact>> = _contacts.asStateFlow()

    override suspend fun addContact(name: String, phone: String, isPriority: Boolean) {
        delay(500)
        val newContact = Contact(
            id = UUID.randomUUID().toString(),
            name = name,
            phone = phone,
            isPriority = isPriority
        )
        _contacts.update { currentList ->
            currentList + newContact
        }
    }

    override suspend fun deleteContact(id: String) {
        delay(500)
        _contacts.update { currentList ->
            currentList.filterNot { it.id == id }
        }
    }

    override suspend fun togglePriority(id: String) {
        delay(300)
        _contacts.update { currentList ->
            currentList.map { contact ->
                if (contact.id == id) contact.copy(isPriority = !contact.isPriority)
                else contact
            }
        }
    }
}
