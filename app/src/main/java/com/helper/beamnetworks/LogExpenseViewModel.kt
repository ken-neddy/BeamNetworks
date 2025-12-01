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

class LogExpenseViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val expenseId: String? = savedStateHandle.get("expenseId")

    var item by mutableStateOf("")
    var amount by mutableStateOf("")
    var date by mutableStateOf("")
    var moreNotes by mutableStateOf("")
    var submitted by mutableStateOf(false)

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    val isEditing = expenseId != null

    private val database = FirebaseDatabase.getInstance()
    private val expensesRef = database.getReference("expenses")

    init {
        if (isEditing) {
            fetchExpenseDetails()
        } else {
            // Set today's date for new expenses
            if (date.isBlank()) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                date = sdf.format(Date())
            }
        }
    }

    private fun fetchExpenseDetails() {
        expenseId?.let {
            expensesRef.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expense = snapshot.getValue(ExpenseData::class.java)
                    expense?.let {
                        item = it.item
                        amount = it.amount
                        date = it.date
                        moreNotes = it.moreNotes
                    }
                }

                override fun onCancelled(error: DatabaseError) { /* Handle error */ }
            })
        }
    }

    fun saveExpense() {
        submitted = true
        if (item.isBlank() || amount.isBlank() || date.isBlank()) {
            _saveState.value = SaveState.Error("Please fill in all required fields.")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving

            if (isEditing) {
                val updatedExpense = ExpenseData(expenseId!!, item, amount, date, moreNotes)
                expensesRef.child(expenseId).setValue(updatedExpense)
                    .addOnSuccessListener { _saveState.value = SaveState.Success }
                    .addOnFailureListener { _saveState.value = SaveState.Error(it.message ?: "Unknown error") }
            } else {
                val newExpenseRef = expensesRef.push()
                val expenseData = ExpenseData(newExpenseRef.key!!, item, amount, date, moreNotes)
                newExpenseRef.setValue(expenseData)
                    .addOnSuccessListener { _saveState.value = SaveState.Success }
                    .addOnFailureListener { _saveState.value = SaveState.Error(it.message ?: "Unknown error") }
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}