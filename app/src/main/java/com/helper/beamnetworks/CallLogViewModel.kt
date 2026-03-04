package com.helper.beamnetworks

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CallLogViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().reference

    private val _installations = MutableStateFlow<List<InstallationData>>(emptyList())
    val installations = _installations.asStateFlow()

    private val _customers = MutableStateFlow<List<CustomerData>>(emptyList())
    val customers = _customers.asStateFlow()

    init {
        fetchInstallations()
        fetchCustomers()
    }

    private fun fetchCustomers() {
        database.child("Customers").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customerList = snapshot.children.mapNotNull {
                    try {
                        it.getValue(CustomerData::class.java)
                    } catch (e: Exception) {
                        Log.e("CallLogViewModel", "Error parsing customer", e)
                        null
                    }
                }
                _customers.value = customerList
                Log.d("CallLogViewModel", "Loaded ${customerList.size} customers")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CallLogViewModel", "Failed to fetch customers", error.toException())
            }
        })
    }

    private fun fetchInstallations() {
        database.child("installations").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val installationList = snapshot.children.mapNotNull {
                    it.getValue(InstallationData::class.java)
                }
                _installations.value = installationList
                Log.d("CallLogViewModel", "Loaded ${installationList.size} installations")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CallLogViewModel", "Failed to fetch installations", error.toException())
            }
        })
    }
}
