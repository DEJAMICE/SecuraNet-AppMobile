package com.example.securanet.presentation.contacts

import com.example.securanet.domain.model.Contact

data class ContactsState(
    val allContacts: List<Contact> = emptyList(),
    val priorityContacts: List<Contact> = emptyList(),
    val otherContacts: List<Contact> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isAddDialogOpen: Boolean = false,
    val nameInput: String = "",
    val relationshipInput: String = "",
    val nameError: Int? = null
)
