package com.helper.beamnetworks

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class IncomeDurationFilter(val displayName: String) {
    ALL_TIME("All time"),
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    LAST_WEEK("Last Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months")
}

class IncomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FirebaseDatabase.getInstance().getReference()

    private var originalPaymentData = listOf<PaymentData>()
    private val _payments = MutableStateFlow<List<PaymentData>>(emptyList())
    val payments = _payments.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _durationFilter = MutableStateFlow(IncomeDurationFilter.ALL_TIME)
    val durationFilter = _durationFilter.asStateFlow()

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount = _totalAmount.asStateFlow()

    init {
        fetchPaymentsFromFirebase()
    }

    private fun fetchPaymentsFromFirebase() {
        database.child("Payments").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val paymentList = mutableListOf<PaymentData>()
                for (child in snapshot.children) {
                    try {
                        val amount = child.child("Amount").getValue()?.toString() ?: ""
                        val date = child.child("Created at").getValue(String::class.java) ?: ""
                        val customerName = child.child("Customer name").getValue(String::class.java) ?: ""
                        val notes = child.child("Notes").getValue(String::class.java) ?: ""
                        
                        // If Customer name is empty, use Notes as the name (common for service payments)
                        val displayName = if (customerName.isBlank()) notes else customerName

                        if (displayName.isNotEmpty() || amount.isNotEmpty()) {
                            paymentList.add(PaymentData(
                                id = child.key ?: "",
                                date = date,
                                customerName = displayName,
                                amount = amount,
                                notes = notes
                            ))
                        }
                    } catch (e: Exception) {
                        Log.e("IncomeViewModel", "Error parsing payment at ${child.key}", e)
                    }
                }
                
                originalPaymentData = paymentList.reversed()
                applyFilters()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("IncomeViewModel", "Database error: ${error.message}")
            }
        })
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun onDurationFilterChanged(filter: IncomeDurationFilter) {
        _durationFilter.value = filter
        applyFilters()
    }

    private fun applyFilters() {
        if (originalPaymentData.isEmpty()) {
            _payments.value = emptyList()
            _totalAmount.value = 0.0
            return
        }

        val filteredList = originalPaymentData.filter { payment ->
            val nameMatches = payment.customerName.contains(_searchQuery.value, ignoreCase = true)
            val durationMatches = checkDate(payment.date, _durationFilter.value)
            nameMatches && durationMatches
        }
        _payments.value = filteredList

        val total = filteredList.sumOf {
            it.amount.replace(",", "").toDoubleOrNull() ?: 0.0
        }
        _totalAmount.value = total
    }

    private fun parseDate(dateString: String): Date? {
        if (dateString.isBlank()) return null
        
        // Handle ISO 8601 format: 2024-09-30T14:58:24.000Z
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        try {
            return isoFormat.parse(dateString)
        } catch (e: ParseException) {
            // Fallback to other formats
            val datePart = dateString.split(" ")[0].split("T")[0]
            val supportedFormats = listOf(
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "M/d/yyyy",
                "MM/dd/yyyy",
                "yyyy/MM/dd",
                "dd-MM-yyyy"
            )
            for (format in supportedFormats) {
                try {
                    return SimpleDateFormat(format, Locale.getDefault()).parse(datePart)
                } catch (e: ParseException) {
                }
            }
        }
        return null
    }

    private fun getStartAndEndOf(filter: IncomeDurationFilter): Pair<Date, Date>? {
        if (filter == IncomeDurationFilter.ALL_TIME) return null

        fun Calendar.startOfDay(): Calendar = this.apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }

        fun Calendar.endOfDay(): Calendar = this.apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }

        val startCal: Calendar = Calendar.getInstance()
        val endCal: Calendar = Calendar.getInstance()

        when (filter) {
            IncomeDurationFilter.TODAY -> {}
            IncomeDurationFilter.YESTERDAY -> {
                startCal.add(Calendar.DAY_OF_YEAR, -1)
                endCal.add(Calendar.DAY_OF_YEAR, -1)
            }
            IncomeDurationFilter.THIS_WEEK -> {
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
            }
            IncomeDurationFilter.LAST_WEEK -> {
                startCal.add(Calendar.WEEK_OF_YEAR, -1)
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                endCal.time = startCal.time
                endCal.add(Calendar.DAY_OF_YEAR, 6)
            }
            IncomeDurationFilter.THIS_MONTH -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            IncomeDurationFilter.LAST_MONTH -> {
                startCal.add(Calendar.MONTH, -1)
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                endCal.time = startCal.time
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            IncomeDurationFilter.LAST_3_MONTHS -> {
                startCal.add(Calendar.MONTH, -3)
            }
            IncomeDurationFilter.LAST_6_MONTHS -> {
                startCal.add(Calendar.MONTH, -6)
            }
            IncomeDurationFilter.ALL_TIME -> return null
        }

        return Pair(startCal.startOfDay().time, endCal.endOfDay().time)
    }

    private fun checkDate(dateString: String, filter: IncomeDurationFilter): Boolean {
        if (filter == IncomeDurationFilter.ALL_TIME) return true
        val itemDate = parseDate(dateString) ?: return false
        val (start, end) = getStartAndEndOf(filter) ?: return true
        return !itemDate.before(start) && !itemDate.after(end)
    }
}
