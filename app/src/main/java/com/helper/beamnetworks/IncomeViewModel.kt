package com.helper.beamnetworks

import android.app.Application
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
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

data class GoogleSignInState(
    val account: GoogleSignInAccount? = null,
    val signInIntent: Intent,
    val error: String? = null
)

class IncomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _googleSignInState = MutableStateFlow(GoogleSignInState(signInIntent = createSignInIntent()))
    val googleSignInState = _googleSignInState.asStateFlow()

    private var originalSheetData = listOf<List<Any>>()
    private val _sheetData = MutableStateFlow<List<List<Any>>>(emptyList())
    val sheetData = _sheetData.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _durationFilter = MutableStateFlow(IncomeDurationFilter.THIS_MONTH)
    val durationFilter = _durationFilter.asStateFlow()

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount = _totalAmount.asStateFlow()

    init {
        checkForExistingSignIn()
    }

    private fun checkForExistingSignIn() {
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        if (account != null && GoogleSignIn.hasPermissions(account, Scope(SheetsScopes.SPREADSHEETS_READONLY))) {
            _googleSignInState.value = _googleSignInState.value.copy(account = account)
            fetchSheetData(account)
        }
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
        if (originalSheetData.isEmpty()) {
            _sheetData.value = emptyList()
            _totalAmount.value = 0.0
            return
        }

        val filteredList = originalSheetData.filter { row ->
            val name = row.getOrNull(1)?.toString() ?: ""
            val nameMatches = name.contains(_searchQuery.value, ignoreCase = true)

            val dateString = row.getOrNull(0)?.toString() ?: ""
            val durationMatches = checkDate(dateString, _durationFilter.value)

            nameMatches && durationMatches
        }
        _sheetData.value = filteredList

        val total = filteredList.sumOf {
            it.getOrNull(2)?.toString()?.replace(",", "")?.toDoubleOrNull() ?: 0.0
        }
        _totalAmount.value = total
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
        Log.w("IncomeViewModel", "Unparseable date: '$dateString'")
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
            IncomeDurationFilter.TODAY -> {
                // Calendars are already today
            }
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

    private fun createSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("264762838650-4omo9h19m9s27i3pg48a7iuf8jq0ntmu.apps.googleusercontent.com")
            .requestEmail()
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS_READONLY))
            .build()
        return GoogleSignIn.getClient(getApplication(), gso).signInIntent
    }

    fun handleGoogleSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                _googleSignInState.value = _googleSignInState.value.copy(account = account, error = null)
                fetchSheetData(account)
            } catch (e: ApiException) {
                Log.e("IncomeViewModel", "Sign-in failed with code: ${e.statusCode}", e)
                _googleSignInState.value = _googleSignInState.value.copy(error = "Sign-in failed. Check SHA-1 and Web Client ID config in Google Cloud. Error code: ${e.statusCode}")
            }
        } else {
            Log.e("IncomeViewModel", "Sign-in cancelled or failed with result code: ${result.resultCode}")
            _googleSignInState.value = _googleSignInState.value.copy(error = "Sign-in cancelled or failed. Check your Web Client ID.")
        }
    }

    fun clearError() {
        _googleSignInState.value = _googleSignInState.value.copy(error = null)
    }

    private fun fetchSheetData(account: GoogleSignInAccount) {
        viewModelScope.launch(Dispatchers.IO) {
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

                val response = sheets.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute()

                originalSheetData = response.getValues()?.reversed() ?: emptyList()
                applyFilters()
            } catch (e: Exception) {
                Log.e("IncomeViewModel", "Failed to fetch sheet data", e)
                _googleSignInState.value = _googleSignInState.value.copy(error = "Failed to fetch sheet data: ${e.message}")
            }
        }
    }
}