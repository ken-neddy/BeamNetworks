package com.helper.beamnetworks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.helper.beamnetworks.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogExpenseScreen(navController: NavController, viewModel: LogExpenseViewModel = viewModel()) {
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val saveState by viewModel.saveState.collectAsState()
    val expenseOptions = listOf("link", "ODU", "power", "system")
    var expanded by remember { mutableStateOf(false) }

    val pickedAmount = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("picked_amount")

    LaunchedEffect(pickedAmount) {
        if (pickedAmount != null) {
            viewModel.amount = pickedAmount
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("picked_amount")
        }
    }

    // Set the initial date to today
    LaunchedEffect(Unit) {
        if (viewModel.date.isBlank()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            viewModel.date = sdf.format(Date())
        }
    }

    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is SaveState.Success -> {
                snackbarHostState.showSnackbar("Expense recorded!")
                viewModel.resetSaveState()
            }
            is SaveState.Error -> {
                snackbarHostState.showSnackbar("Error: ${state.message}")
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            viewModel.date = sdf.format(Date(it))
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Log an expense") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveExpense() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Submit"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.item,
                        onValueChange = { viewModel.item = it },
                        label = { Text("Item*") },
                        isError = viewModel.item.isBlank() && viewModel.submitted,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        expenseOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    viewModel.item = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.amount,
                    onValueChange = { viewModel.amount = it },
                    label = { Text("Amount*") },
                    isError = viewModel.amount.isBlank() && viewModel.submitted,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        IconButton(onClick = { navController.navigate("message_picker") }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_message),
                                contentDescription = "Pick from message"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.date,
                    onValueChange = {},
                    label = { Text("Date*") },
                    isError = viewModel.date.isBlank() && viewModel.submitted,
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.calendar_add_on_24px),
                                contentDescription = "Select Date",
                                tint = Color.Unspecified
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.moreNotes,
                    onValueChange = { viewModel.moreNotes = it },
                    label = { Text("More notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.saveExpense() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit")
                }
            }
            if (saveState == SaveState.Saving) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogExpenseScreenPreview() {
    LogExpenseScreen(rememberNavController())
}
