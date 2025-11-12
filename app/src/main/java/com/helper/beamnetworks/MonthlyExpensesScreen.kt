package com.helper.beamnetworks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyExpensesScreen(
    navController: NavController,
    viewModel: MonthlyExpensesViewModel = viewModel()
) {
    val expenses by viewModel.expenses.collectAsState()

    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedYear by remember { mutableStateOf(currentYear) }
    var selectedDurationIndex by remember { mutableStateOf<Int?>(0) } // Default to "This Month"

    val months = (0..11).map { SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(2000, it, 1)) }
    val years = (2020..Calendar.getInstance().get(Calendar.YEAR) + 5).toList()
    val durations = listOf("This Month", "Last Month", "Last 3 Months", "Last 6 Months", "Last Year")
    val durationValues = listOf(0, 1, 3, 6, 12)

    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }
    var durationExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMonth, selectedYear, selectedDurationIndex) {
        val cal = Calendar.getInstance()
        selectedDurationIndex?.let {
            if (durationValues[it] == 0) { // This Month
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                viewModel.fetchExpenses(cal.time, Date())
            } else {
                cal.add(Calendar.MONTH, -durationValues[it])
                viewModel.fetchExpenses(cal.time, Date())
            }
        } ?: run {
            cal.clear()
            cal.set(Calendar.YEAR, selectedYear)
            cal.set(Calendar.MONTH, selectedMonth)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val startDate = cal.time

            cal.add(Calendar.MONTH, 1)
            cal.add(Calendar.MILLISECOND, -1)
            val endDate = cal.time

            viewModel.fetchExpenses(startDate, endDate)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Expenses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(expanded = monthExpanded, onExpandedChange = { monthExpanded = !monthExpanded }) {
                        OutlinedTextField(
                            value = months[selectedMonth],
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Month") },
                            enabled = selectedDurationIndex == null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                            months.forEachIndexed { index, month ->
                                DropdownMenuItem(
                                    text = { Text(month) },
                                    onClick = {
                                        selectedMonth = index
                                        selectedDurationIndex = null
                                        monthExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(expanded = yearExpanded, onExpandedChange = { yearExpanded = !yearExpanded }) {
                        OutlinedTextField(
                            value = selectedYear.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Year") },
                            enabled = selectedDurationIndex == null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year.toString()) },
                                    onClick = {
                                        selectedYear = year
                                        selectedDurationIndex = null
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(expanded = durationExpanded, onExpandedChange = { durationExpanded = !durationExpanded }) {
                OutlinedTextField(
                    value = selectedDurationIndex?.let { durations[it] } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Or Select a Duration") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = durationExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = durationExpanded, onDismissRequest = { durationExpanded = false }) {
                    durations.forEachIndexed { index, duration ->
                        DropdownMenuItem(
                            text = { Text(duration) },
                            onClick = {
                                selectedDurationIndex = index
                                durationExpanded = false
                            }
                        )
                    }
                }
            }

            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(expenses) { expense ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Item: ${expense.item}")
                            Text("Amount: ${expense.amount}")
                            Text("Date: ${expense.date}")
                            if (expense.moreNotes.isNotBlank()) {
                                Text("Notes: ${expense.moreNotes}")
                            }
                        }
                    }
                }
            }
        }
    }
}
