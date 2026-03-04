package com.helper.beamnetworks

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CustomersViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("Customers")

    private val _customers = MutableStateFlow<List<CustomerData>>(emptyList())
    val customers: StateFlow<List<CustomerData>> = _customers.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filteredCustomers = MutableStateFlow<List<CustomerData>>(emptyList())
    val filteredCustomers: StateFlow<List<CustomerData>> = _filteredCustomers.asStateFlow()

    init {
        fetchCustomers()
    }

    private fun fetchCustomers() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customerList = mutableListOf<CustomerData>()
                for (child in snapshot.children) {
                    try {
                        val name = child.child("Name").getValue(String::class.java) ?: ""
                        val phone = child.child("Phone").getValue()?.toString() ?: ""
                        val pkg = child.child("Package").getValue(String::class.java) ?: ""
                        
                        if (name.isNotEmpty()) {
                            customerList.add(CustomerData(name, phone, pkg))
                        }
                    } catch (e: Exception) {
                        Log.e("CustomersViewModel", "Error parsing customer", e)
                    }
                }
                _customers.value = customerList
                applyFilter()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CustomersViewModel", "Database error: ${error.message}")
            }
        })
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    private fun applyFilter() {
        val query = _searchQuery.value
        if (query.isBlank()) {
            _filteredCustomers.value = _customers.value
        } else {
            _filteredCustomers.value = _customers.value.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
            }
        }
    }
}
