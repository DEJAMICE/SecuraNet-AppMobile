package com.example.securanet.presentation.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securanet.R
import com.example.securanet.domain.model.Contact
import com.example.securanet.domain.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsViewModel(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ContactsState())
    val state: StateFlow<ContactsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            contactRepository.getContacts().collect { contacts ->
                _state.update { currentState ->
                    val filtered = filterContacts(contacts, currentState.searchQuery)
                    val priority = filtered.filter { it.isPriority }
                    val others = filtered.filter { !it.isPriority }
                    currentState.copy(
                        allContacts = contacts,
                        priorityContacts = priority,
                        otherContacts = others
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { currentState ->
            val filtered = filterContacts(currentState.allContacts, query)
            val priority = filtered.filter { it.isPriority }
            val others = filtered.filter { !it.isPriority }
            currentState.copy(
                searchQuery = query,
                priorityContacts = priority,
                otherContacts = others
            )
        }
    }

    fun onClearSearch() {
        onSearchQueryChange("")
    }

    private fun filterContacts(contacts: List<Contact>, query: String): List<Contact> {
        if (query.isBlank()) return contacts
        val trimmedQuery = query.trim()
        return contacts.filter { contact ->
            contact.name.contains(trimmedQuery, ignoreCase = true) ||
                    contact.relationship.contains(trimmedQuery, ignoreCase = true)
        }
    }

    fun onOpenAddDialog() {
        _state.update {
            it.copy(
                isAddDialogOpen = true,
                nameInput = "",
                relationshipInput = "",
                nameError = null
            )
        }
    }

    fun onCloseAddDialog() {
        _state.update { it.copy(isAddDialogOpen = false) }
    }

    fun onNameChange(name: String) {
        _state.update { it.copy(nameInput = name, nameError = null) }
    }

    fun onRelationshipChange(relationship: String) {
        _state.update { it.copy(relationshipInput = relationship) }
    }

    fun addContact() {
        val name = _state.value.nameInput.trim()
        if (name.isEmpty()) {
            _state.update { it.copy(nameError = R.string.name_required) }
            return
        }

        viewModelScope.launch {
            contactRepository.addContact(
                name = name,
                relationship = _state.value.relationshipInput.trim(),
                isPriority = false
            )
            onCloseAddDialog()
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch {
            contactRepository.deleteContact(id)
        }
    }

    fun togglePriority(id: String) {
        viewModelScope.launch {
            contactRepository.togglePriority(id)
        }
    }
}
