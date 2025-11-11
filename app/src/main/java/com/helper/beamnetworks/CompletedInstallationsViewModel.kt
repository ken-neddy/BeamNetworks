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

class CompletedInstallationsViewModel : ViewModel() {

    private val _completedInstallations = MutableStateFlow<List<InstallationData>>(emptyList())
    val completedInstallations: StateFlow<List<InstallationData>> = _completedInstallations

    init {
        fetchCompletedInstallations()
    }

    private fun fetchCompletedInstallations() {
        viewModelScope.launch {
            val database = FirebaseDatabase.getInstance()
            val installationsRef = database.getReference("installations")

            installationsRef.orderByChild("status").equalTo("Completed")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val installations = mutableListOf<InstallationData>()
                        for (child in snapshot.children) {
                            try {
                                val installation = child.getValue(InstallationData::class.java)
                                if (installation != null) {
                                    installations.add(installation)
                                }
                            } catch (e: Exception) {
                                // Ignore invalid data
                            }
                        }
                        _completedInstallations.value = installations
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
        }
    }
}