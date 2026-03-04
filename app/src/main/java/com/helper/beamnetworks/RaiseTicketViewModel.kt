package com.helper.beamnetworks

import android.util.Log
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

    private val _clientPhone = MutableStateFlow("")
    val clientPhone: StateFlow<String> = _clientPhone

    private val _issue = MutableStateFlow("")
    val issue: StateFlow<String> = _issue

    private val _issues = MutableStateFlow<List<String>>(emptyList())
    val issues: StateFlow<List<String>> = _issues

    private val _customers = MutableStateFlow<List<CustomerData>>(emptyList())
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
                Log.e("RaiseTicketViewModel", "Error fetching issues: ${error.message}")
            }
        })
    }

    private fun fetchCustomers() {
        database.child("Customers").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customerList = mutableListOf<CustomerData>()
                for (child in snapshot.children) {
                    try {
                        // Manually parse to handle case-sensitivity and type differences (String vs Long)
                        val name = child.child("Name").getValue(String::class.java) ?: ""
                        val phone = child.child("Phone").getValue()?.toString() ?: ""
                        val pkg = child.child("Package").getValue(String::class.java) ?: ""
                        
                        if (name.isNotEmpty()) {
                            customerList.add(CustomerData(name, phone, pkg))
                        }
                    } catch (e: Exception) {
                        Log.e("RaiseTicketViewModel", "Error parsing customer at ${child.key}", e)
                    }
                }
                _customers.value = customerList
                _filteredCustomers.value = customerList.map { it.name }.distinct()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RaiseTicketViewModel", "Error fetching customers: ${error.message}")
            }
        })
    }

    private fun normalizePhoneNumber(phone: String): String {
        return phone.filter { it.isDigit() }.takeLast(9)
    }

    fun onClientNameChange(name: String) {
        _clientName.value = name
        
        // Auto-fill phone if exact name match found
        val customer = _customers.value.find { it.name.equals(name, ignoreCase = true) }
        if (customer != null) {
            _clientPhone.value = customer.phone
        }

        val allNames = _customers.value.map { it.name }.distinct()
        _filteredCustomers.value = if (name.isBlank()) {
            allNames
        } else {
            allNames.filter { it.contains(name, ignoreCase = true) }
        }
    }

    fun onClientPhoneChange(phone: String) {
        _clientPhone.value = phone
        
        // Auto-fill name if matching phone number found
        if (phone.length >= 9) {
            val normalizedInput = normalizePhoneNumber(phone)
            val customer = _customers.value.find { 
                normalizePhoneNumber(it.phone) == normalizedInput 
            }
            if (customer != null) {
                _clientName.value = customer.name
            }
        }
    }

    fun onIssueChange(newIssue: String) {
        _issue.value = newIssue
    }

    fun raiseTicket() {
        viewModelScope.launch {
            val client = _clientName.value
            val phone = _clientPhone.value
            val issueText = _issue.value

            if (client.isBlank() || issueText.isBlank() || phone.isBlank()) {
                _saveStatus.value = "All fields are required"
                return@launch
            }

            if (!_issues.value.contains(issueText)) {
                database.child("issues").push().setValue(issueText)
            }

            val timestamp = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentDateTime = sdf.format(Date(timestamp))

            val ticket = mapOf(
                "clientName" to client,
                "clientPhone" to phone,
                "issue" to issueText,
                "status" to "open",
                "timestamp" to timestamp,
                "dateTime" to currentDateTime
            )

            database.child("tickets").push().setValue(ticket)
                .addOnSuccessListener {
                    _saveStatus.value = "Ticket raised successfully"
                    _clientName.value = ""
                    _clientPhone.value = ""
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