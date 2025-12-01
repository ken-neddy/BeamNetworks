package com.helper.beamnetworks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _upcomingInstallationsCount = MutableStateFlow(0)
    val upcomingInstallationsCount: StateFlow<Int> = _upcomingInstallationsCount

    private val _incomeThisMonth = MutableStateFlow(0.0)
    val incomeThisMonth: StateFlow<Double> = _incomeThisMonth

    private val _monthlyExpensesTotal = MutableStateFlow(0.0)
    val monthlyExpensesTotal: StateFlow<Double> = _monthlyExpensesTotal

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val incomeViewModel = IncomeViewModel(application)

    init {
        fetchDashboardData()
        incomeViewModel.totalAmount.onEach { _incomeThisMonth.value = it }.launchIn(viewModelScope)
    }

    private fun isThisMonth(date: Date): Boolean {
        val itemCalendar = Calendar.getInstance().apply { time = date }
        val now = Calendar.getInstance()
        return itemCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                itemCalendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }

    private fun fetchDashboardData() {
        val database = FirebaseDatabase.getInstance()
        val installationsRef = database.getReference("installations")
        val expensesRef = database.getReference("expenses")

        installationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var upcomingCount = 0
                for (child in snapshot.children) {
                    try {
                        val installation = child.getValue(InstallationData::class.java)
                        if (installation != null && installation.status == "Upcoming") {
                            upcomingCount++
                        }
                    } catch (e: Exception) {
                        // Ignore invalid data
                    }
                }
                _upcomingInstallationsCount.value = upcomingCount
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        expensesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0.0
                for (child in snapshot.children) {
                    try {
                        val expense = child.getValue(ExpenseData::class.java)
                        expense?.let {
                            if (it.date.isNotBlank()) {
                                val expenseDate = dateFormat.parse(it.date)
                                if (expenseDate != null && isThisMonth(expenseDate)) {
                                    total += it.amount.toDoubleOrNull() ?: 0.0
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore invalid data
                    }
                }
                _monthlyExpensesTotal.value = total
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}