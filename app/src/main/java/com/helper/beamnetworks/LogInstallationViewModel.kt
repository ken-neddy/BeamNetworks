package com.helper.beamnetworks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogInstallationViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val installationId: String? = savedStateHandle.get("installationId")

    var clientName by mutableStateOf("")
    var clientPhone by mutableStateOf("")
    var clientLocation by mutableStateOf("")
    var installationDate by mutableStateOf("")
    var moreNotes by mutableStateOf("")
    var hasRouter by mutableStateOf(false)
    var submitted by mutableStateOf(false)

    val isEditing = installationId != null

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    private val database = FirebaseDatabase.getInstance()
    private val installationsRef = database.getReference("installations")

    init {
        if (isEditing) {
            fetchInstallationDetails()
        } else {
            if (installationDate.isBlank()) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                installationDate = sdf.format(Date())
            }
        }
    }

    private fun fetchInstallationDetails() {
        installationId?.let {
            installationsRef.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val installation = snapshot.getValue(InstallationData::class.java)
                    installation?.let {
                        clientName = it.clientName
                        clientPhone = it.clientPhone
                        clientLocation = it.clientLocation
                        installationDate = it.installationDate
                        moreNotes = it.moreNotes
                        hasRouter = it.hasRouter
                    }
                }

                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })
        }
    }

    fun saveInstallation() {
        submitted = true
        if (clientPhone.isBlank() || clientLocation.isBlank() || installationDate.isBlank()) {
            _saveState.value = SaveState.Error("Please fill in all required fields.")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            
            if (isEditing) {
                val updatedInstallation = InstallationData(installationId!!, clientName, clientPhone, clientLocation, installationDate, moreNotes, "Upcoming", hasRouter)
                installationsRef.child(installationId).setValue(updatedInstallation)
                    .addOnSuccessListener { _saveState.value = SaveState.Success }
                    .addOnFailureListener { _saveState.value = SaveState.Error(it.message ?: "Unknown error") }
            } else {
                val newInstallationRef = installationsRef.push()
                val installationData = InstallationData(newInstallationRef.key!!, clientName, clientPhone, clientLocation, installationDate, moreNotes, "Upcoming", hasRouter)
                newInstallationRef.setValue(installationData)
                    .addOnSuccessListener { _saveState.value = SaveState.Success }
                    .addOnFailureListener { _saveState.value = SaveState.Error(it.message ?: "Unknown error") }
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}