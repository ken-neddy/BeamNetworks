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

class LogExpenseViewModel : ViewModel() {
    var item by mutableStateOf("")
    var amount by mutableStateOf("")
    var date by mutableStateOf("")
    var moreNotes by mutableStateOf("")
    var submitted by mutableStateOf(false)

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    fun saveExpense() {
        submitted = true
        if (item.isBlank() || amount.isBlank() || date.isBlank()) {
            _saveState.value = SaveState.Error("Please fill in all required fields.")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("expenses")

            val expenseData = ExpenseData(
                item = item,
                amount = amount,
                date = date,
                moreNotes = moreNotes
            )

            myRef.push().setValue(expenseData)
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