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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MonthlyExpensesViewModel : ViewModel() {

    private val _expenses = MutableStateFlow<List<ExpenseData>>(emptyList())
    val expenses: StateFlow<List<ExpenseData>> = _expenses

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun fetchExpenses(startDate: Date, endDate: Date) {
        viewModelScope.launch {
            val database = FirebaseDatabase.getInstance()
            val expensesRef = database.getReference("expenses")

            expensesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenses = mutableListOf<ExpenseData>()
                    for (child in snapshot.children) {
                        try {
                            val expense = child.getValue(ExpenseData::class.java)
                            expense?.let {
                                if (it.date.isNotBlank()) {
                                    val expenseDate = dateFormat.parse(it.date)
                                    if (expenseDate != null && !expenseDate.before(startDate) && !expenseDate.after(endDate)) {
                                        expenses.add(it)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore invalid data
                        }
                    }
                    _expenses.value = expenses
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}