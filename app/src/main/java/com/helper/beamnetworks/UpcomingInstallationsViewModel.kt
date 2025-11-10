package com.helper.beamnetworks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UpcomingInstallationsViewModel : ViewModel() {

    private val _upcomingInstallations = MutableStateFlow<List<InstallationData>>(emptyList())
    val upcomingInstallations: StateFlow<List<InstallationData>> = _upcomingInstallations

    init {
        fetchUpcomingInstallations()
    }

    private fun fetchUpcomingInstallations() {
        viewModelScope.launch {
            val database = FirebaseDatabase.getInstance()
            val installationsRef = database.getReference("installations")

            installationsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val installations = mutableListOf<InstallationData>()
                    for (child in snapshot.children) {
                        try {
                            val installation = child.getValue(InstallationData::class.java)
                            if (installation != null && installation.status == "Upcoming") {
                                installation.id = child.key ?: ""
                                installations.add(installation)
                            }
                        } catch (e: Exception) {
                            // Ignore invalid data
                        }
                    }
                    _upcomingInstallations.value = installations
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    fun completeInstallation(installationId: String) {
        viewModelScope.launch {
            if (installationId.isNotBlank()) {
                val database = FirebaseDatabase.getInstance()
                val installationRef = database.getReference("installations").child(installationId)
                installationRef.child("status").setValue("Completed")
            }
        }
    }
}