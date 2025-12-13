package com.helper.beamnetworks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
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

    private val _stockRunningLowCount = MutableStateFlow(0)
    val stockRunningLowCount: StateFlow<Int> = _stockRunningLowCount

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    init {
        fetchDashboardData()
    }

    private fun parseDate(dateString: String): Date? {
        val datePart = dateString.split(" ")[0]
        val supportedFormats = listOf(
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "M/d/yyyy",
            "MM/dd/yyyy",
            "yyyy/MM/dd"
        )
        for (format in supportedFormats) {
            try {
                return SimpleDateFormat(format, Locale.getDefault()).parse(datePart)
            } catch (e: ParseException) {
                // Continue to next format
            }
        }
        return null
    }

    private fun isThisMonth(date: Date): Boolean {
        val itemCalendar = Calendar.getInstance().apply { time = date }
        val now = Calendar.getInstance()
        return itemCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                itemCalendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }

    private fun fetchDashboardData() {
        viewModelScope.launch {
            val database = FirebaseDatabase.getInstance()
            val installationsRef = database.getReference("installations")
            val expensesRef = database.getReference("expenses")
            val productsRef = database.getReference("products")
            val stockRef = database.getReference("stock")

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

            val stockListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val stockItems = snapshot.children.mapNotNull { it.getValue(StockItem::class.java) }
                    productsRef.get().addOnSuccessListener { productSnapshot ->
                        val products = productSnapshot.children.mapNotNull { it.getValue(Product::class.java) }
                        val runningLowCount = stockItems.count { stockItem ->
                            val product = products.find { it.name == stockItem.name }
                            product != null && stockItem.quantity < product.minStockLevel
                        }
                        _stockRunningLowCount.value = runningLowCount
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            }
            stockRef.addValueEventListener(stockListener)

            // Fetch Income Data from Google Sheet
            val account = GoogleSignIn.getLastSignedInAccount(getApplication())
            if (account != null && GoogleSignIn.hasPermissions(account, Scope(SheetsScopes.SPREADSHEETS_READONLY))) {
                withContext(Dispatchers.IO) {
                    try {
                        val credential = GoogleAccountCredential.usingOAuth2(
                            getApplication(),
                            listOf(SheetsScopes.SPREADSHEETS_READONLY)
                        ).setSelectedAccount(account.account)

                        val sheets = Sheets.Builder(
                            NetHttpTransport(),
                            GsonFactory.getDefaultInstance(),
                            credential
                        )
                            .setApplicationName("BeamNetworks")
                            .build()

                        val spreadsheetId = "1-1oKNTxuDza_Q8QXMKDwwydCVc_V5r-keKqPbuR_t3A"
                        val range = "balancesheet!A1:C2000"

                        val response = sheets.spreadsheets().values().get(spreadsheetId, range).execute()
                        val values = response.getValues()

                        if (values != null) {
                            val totalIncome = values.sumOf { row ->
                                val dateString = row.getOrNull(0)?.toString()
                                val amountString = row.getOrNull(2)?.toString()
                                if (dateString != null && amountString != null) {
                                    val date = parseDate(dateString)
                                    if (date != null && isThisMonth(date)) {
                                        amountString.replace(",", "").toDoubleOrNull() ?: 0.0
                                    } else {
                                        0.0
                                    }
                                } else {
                                    0.0
                                }
                            }
                            _incomeThisMonth.value = totalIncome
                        }
                    } catch (e: Exception) {
                        // Handle error, e.g., token expiration or network issues
                    }
                }
            }
        }
    }
}