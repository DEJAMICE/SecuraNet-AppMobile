package com.example.securanet.ui.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.securanet.R
import com.example.securanet.presentation.contacts.ContactsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.nav_contacts)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onOpenAddDialog,
                modifier = Modifier
                    .size(56.dp)
                    .semantics { contentDescription = "Add Contact" }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add_24px),
                    contentDescription = stringResource(id = R.string.add_contact)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text(stringResource(id = R.string.search_contacts)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.search_24px),
                        contentDescription = stringResource(id = R.string.search_contacts)
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = viewModel::onClearSearch) {
                            Icon(
                                painter = painterResource(id = R.drawable.close_24px),
                                contentDescription = stringResource(id = R.string.clear_search)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val hasContacts = state.allContacts.isNotEmpty()
            val hasSearchResults = state.priorityContacts.isNotEmpty() || state.otherContacts.isNotEmpty()

            when {
                !hasContacts -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_contacts),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                !hasSearchResults -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_results),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        if (state.priorityContacts.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = stringResource(
                                        id = R.string.priority_contacts,
                                        state.priorityContacts.size
                                    )
                                )
                            }
                            items(state.priorityContacts, key = { it.id }) { contact ->
                                ContactItem(
                                    contact = contact,
                                    onTogglePriority = { viewModel.togglePriority(contact.id) },
                                    onDelete = { viewModel.deleteContact(contact.id) }
                                )
                            }
                        }

                        if (state.otherContacts.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = stringResource(
                                        id = R.string.other_contacts,
                                        state.otherContacts.size
                                    )
                                )
                            }
                            items(state.otherContacts, key = { it.id }) { contact ->
                                ContactItem(
                                    contact = contact,
                                    onTogglePriority = { viewModel.togglePriority(contact.id) },
                                    onDelete = { viewModel.deleteContact(contact.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.isAddDialogOpen) {
            AddContactDialog(
                name = state.nameInput,
                relationship = state.relationshipInput,
                nameError = state.nameError,
                onNameChange = viewModel::onNameChange,
                onRelationshipChange = viewModel::onRelationshipChange,
                onDismiss = viewModel::onCloseAddDialog,
                onConfirm = viewModel::addContact
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun AddContactDialog(
    name: String,
    relationship: String,
    nameError: Int?,
    onNameChange: (String) -> Unit,
    onRelationshipChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.add_contact)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(id = R.string.contact_name)) },
                    isError = nameError != null,
                    supportingText = {
                        if (nameError != null) Text(stringResource(id = nameError))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = relationship,
                    onValueChange = onRelationshipChange,
                    label = { Text(stringResource(id = R.string.contact_relationship)) },
                    placeholder = { Text(stringResource(id = R.string.relationship_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.height(48.dp)
            ) {
                Text(stringResource(id = R.string.add))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}
