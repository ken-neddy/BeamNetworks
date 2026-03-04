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

    private val _stockRunningLow = MutableStateFlow(0)
    val stockRunningLow: StateFlow<Int> = _stockRunningLow

    private val _openTicketsCount = MutableStateFlow(0)
    val openTicketsCount: StateFlow<Int> = _openTicketsCount

    private val _totalCustomersCount = MutableStateFlow(0)
    val totalCustomersCount: StateFlow<Int> = _totalCustomersCount

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val incomeViewModel = IncomeViewModel(application)

    init {
        // Set the filter specifically to THIS_MONTH for dashboard display
        incomeViewModel.onDurationFilterChanged(IncomeDurationFilter.THIS_MONTH)
        
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
        val productsRef = database.getReference("Products")
        val ticketsRef = database.getReference("tickets")
        val customersRef = database.getReference("Customers")

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
        
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var lowStockCount = 0
                for (child in snapshot.children) {
                    try {
                        val product = child.getValue(Product::class.java)
                        if (product != null && product.quantity <= product.lowStockThreshold) {
                            lowStockCount++
                        }
                    } catch (e: Exception) {
                        // Ignore invalid data
                    }
                }
                _stockRunningLow.value = lowStockCount
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        ticketsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (child in snapshot.children) {
                    if (child.child("status").getValue(String::class.java) == "open") {
                        count++
                    }
                }
                _openTicketsCount.value = count
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        customersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _totalCustomersCount.value = snapshot.childrenCount.toInt()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}