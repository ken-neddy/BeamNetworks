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
import java.util.Locale

class DashboardViewModel : ViewModel() {

    private val _upcomingInstallationsCount = MutableStateFlow(0)
    val upcomingInstallationsCount: StateFlow<Int> = _upcomingInstallationsCount

    private val _completedInstallationsThisMonthCount = MutableStateFlow(0)
    val completedInstallationsThisMonthCount: StateFlow<Int> = _completedInstallationsThisMonthCount

    private val _monthlyExpensesTotal = MutableStateFlow(0.0)
    val monthlyExpensesTotal: StateFlow<Double> = _monthlyExpensesTotal

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    init {
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        viewModelScope.launch {
            val database = FirebaseDatabase.getInstance()
            val installationsRef = database.getReference("installations")
            val expensesRef = database.getReference("expenses")

            val firstDayOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            installationsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var upcomingCount = 0
                    var completedThisMonthCount = 0
                    for (child in snapshot.children) {
                        try {
                            val installation = child.getValue(InstallationData::class.java)
                            if (installation != null) {
                                if (installation.status == "Upcoming") {
                                    upcomingCount++
                                } else if (installation.status == "Completed") {
                                    try {
                                        if (installation.installationDate.isNotBlank()) {
                                            val installationDate = dateFormat.parse(installation.installationDate)
                                            if (installationDate != null && !installationDate.before(firstDayOfMonth)) {
                                                completedThisMonthCount++
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Ignore entries with malformed dates
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore invalid data
                        }
                    }
                    _upcomingInstallationsCount.value = upcomingCount
                    _completedInstallationsThisMonthCount.value = completedThisMonthCount
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
                                    if (expenseDate != null && !expenseDate.before(firstDayOfMonth)) {
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
}