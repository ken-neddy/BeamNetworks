package com.helper.beamnetworks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RaiseTicketViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference()

    private val _clientName = MutableStateFlow("")
    val clientName: StateFlow<String> = _clientName

    private val _issue = MutableStateFlow("")
    val issue: StateFlow<String> = _issue

    private val _issues = MutableStateFlow<List<String>>(emptyList())
    val issues: StateFlow<List<String>> = _issues

    private val _customers = MutableStateFlow<List<String>>(emptyList())
    private val _filteredCustomers = MutableStateFlow<List<String>>(emptyList())
    val filteredCustomers: StateFlow<List<String>> = _filteredCustomers.asStateFlow()

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus

    init {
        fetchIssues()
        fetchCustomers()
    }

    private fun fetchIssues() {
        database.child("issues").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val issueList = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                _issues.value = issueList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun fetchCustomers() {
        database.child("customers").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customerList = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                _customers.value = customerList
                _filteredCustomers.value = customerList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun onClientNameChange(name: String) {
        _clientName.value = name
        _filteredCustomers.value = if (name.isBlank()) {
            _customers.value
        } else {
            _customers.value.filter { it.contains(name, ignoreCase = true) }
        }
    }

    fun onIssueChange(newIssue: String) {
        _issue.value = newIssue
    }

    fun raiseTicket() {
        viewModelScope.launch {
            val client = _clientName.value
            val issueText = _issue.value

            if (client.isBlank() || issueText.isBlank()) {
                _saveStatus.value = "Client name and issue cannot be empty"
                return@launch
            }

            if (!_customers.value.contains(client)) {
                _saveStatus.value = "Please select a valid client from the list"
                return@launch
            }

            // Save new issue to the list of issues if it's not already there
            if (!_issues.value.contains(issueText)) {
                database.child("issues").push().setValue(issueText)
            }

            val timestamp = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDateTime = sdf.format(Date(timestamp))

            val ticket = mapOf(
                "clientName" to client,
                "issue" to issueText,
                "status" to "open",
                "timestamp" to timestamp,
                "dateTime" to currentDateTime
            )

            database.child("tickets").push().setValue(ticket)
                .addOnSuccessListener {
                    _saveStatus.value = "Ticket raised successfully"
                    _clientName.value = ""
                    _issue.value = ""
                }
                .addOnFailureListener {
                    _saveStatus.value = "Failed to raise ticket"
                }
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }
}