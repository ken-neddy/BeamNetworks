package com.helper.beamnetworks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LogInstallationViewModel : ViewModel() {
    var clientName by mutableStateOf("")
    var clientPhone by mutableStateOf("")
    var clientLocation by mutableStateOf("")
    var installationDate by mutableStateOf("")
    var moreNotes by mutableStateOf("")
    var submitted by mutableStateOf(false)

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    fun saveInstallation() {
        submitted = true
        if (clientPhone.isBlank() || clientLocation.isBlank() || installationDate.isBlank()) {
            _saveState.value = SaveState.Error("Please fill in all required fields.")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("installations")
            val newInstallationRef = myRef.push()

            val installationData = InstallationData(
                id = newInstallationRef.key ?: "",
                clientName = clientName,
                clientPhone = clientPhone,
                clientLocation = clientLocation,
                installationDate = installationDate,
                moreNotes = moreNotes
            )

            newInstallationRef.setValue(installationData)
                .addOnSuccessListener {
                    _saveState.value = SaveState.Success
                }
                .addOnFailureListener { 
                    _saveState.value = SaveState.Error(it.message ?: "An unknown error occurred")
                }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}