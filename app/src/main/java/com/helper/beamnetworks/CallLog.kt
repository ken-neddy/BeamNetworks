package com.helper.beamnetworks

import android.Manifest
import android.content.pm.PackageManager
import android.provider.CallLog
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CallLogEntry(
    val id: String,
    val name: String,
    val number: String,
    val date: String,
    val dateMillis: Long,
    val duration: String,
    val type: CallType
)

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    UNKNOWN
}

private fun getDayGroup(dateMillis: Long): String {
    val calendar = Calendar.getInstance()
    val today = calendar.clone() as Calendar

    calendar.timeInMillis = dateMillis

    val yesterday = today.clone() as Calendar
    yesterday.add(Calendar.DAY_OF_YEAR, -1)

    return when {
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
        else -> {
            SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(Date(dateMillis))
        }
    }
}

private fun phoneNumbersMatch(num1: String, num2: String): Boolean {
    val normalizedNum1 = num1.filter { it.isDigit() }.takeLast(9)
    val normalizedNum2 = num2.filter { it.isDigit() }.takeLast(9)
    return normalizedNum1.isNotEmpty() && normalizedNum1 == normalizedNum2
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CallLogScreen(navController: NavController, callLogViewModel: CallLogViewModel = viewModel()) {
    val context = LocalContext.current
    var groupedCallLogs by remember { mutableStateOf<Map<String, List<CallLogEntry>>>(emptyMap()) }
    var hasPermission by remember { mutableStateOf(false) }
    val installations by callLogViewModel.installations.collectAsState()
    val customers by callLogViewModel.customers.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Call Log", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        if (hasPermission) {
            LaunchedEffect(installations, customers) {
                val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    null,
                    null,
                    null,
                    "${CallLog.Calls.DATE} DESC"
                )

                cursor?.use {
                    val idColumn = it.getColumnIndex(CallLog.Calls._ID)
                    val nameColumn = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                    val numberColumn = it.getColumnIndex(CallLog.Calls.NUMBER)
                    val dateColumn = it.getColumnIndex(CallLog.Calls.DATE)
                    val durationColumn = it.getColumnIndex(CallLog.Calls.DURATION)
                    val typeColumn = it.getColumnIndex(CallLog.Calls.TYPE)

                    val logs = mutableListOf<CallLogEntry>()
                    while (it.moveToNext()) {
                        val id = it.getString(idColumn)
                        val name = it.getString(nameColumn) ?: "Unknown"
                        val number = it.getString(numberColumn)
                        val date = it.getLong(dateColumn)
                        val duration = it.getLong(durationColumn)
                        val type = it.getInt(typeColumn)

                        logs.add(
                            CallLogEntry(
                                id = id,
                                name = name,
                                number = number,
                                date = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(date)),
                                dateMillis = date,
                                duration = "${duration / 60}m ${duration % 60}s",
                                type = when (type) {
                                    CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                                    CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                                    CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                                    else -> CallType.UNKNOWN
                                }
                            )
                        )
                    }
                    groupedCallLogs = logs.groupBy { getDayGroup(it.dateMillis) }
                }
            }
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                groupedCallLogs.forEach { (day, logs) ->
                    stickyHeader {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                    items(logs) { callLog ->
                        val installation = installations.find { phoneNumbersMatch(it.clientPhone, callLog.number) }
                        val customer = customers.find { phoneNumbersMatch(it.phone, callLog.number) }
                        CallLogItem(
                            callLog = callLog,
                            installation = installation,
                            customer = customer
                        ) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_phone_number", callLog.number)
                            
                            // Modified logic: Use "Unknown Number" if not found in Firebase
                            val finalClientName = customer?.name ?: installation?.clientName ?: "Unknown Number"
                            
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_client_name", finalClientName)
                            navController.popBackStack()
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(innerPadding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Permission to read call logs is required.", style = MaterialTheme.typography.bodyLarge)
                Button(onClick = { launcher.launch(Manifest.permission.READ_CALL_LOG) }, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Request Permission")
                }
            }
        }
    }
}

@Composable
fun CallLogItem(
    callLog: CallLogEntry,
    installation: InstallationData?,
    customer: CustomerData?,
    modifier: Modifier = Modifier, 
    onCallLogClick: () -> Unit
) {
    val isIncomingCustomer = customer != null && callLog.type == CallType.INCOMING
    
    // Using subtle background tints instead of solid shouting colors
    val backgroundColor = when {
        isIncomingCustomer -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        installation != null -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else -> Color.Transparent
    }
    
    val contentColor = when {
        isIncomingCustomer -> MaterialTheme.colorScheme.error
        installation != null -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onCallLogClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = callLog.type.toIcon(),
                contentDescription = null,
                tint = if (callLog.type == CallType.MISSED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(
                    text = customer?.name ?: installation?.clientName ?: callLog.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
                Text(
                    text = callLog.number,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = callLog.date,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = callLog.duration,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

fun CallType.toIcon(): ImageVector {
    return when (this) {
        CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
        CallType.OUTGOING -> Icons.Default.Call
        CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed
        CallType.UNKNOWN -> Icons.Default.Call
    }
}

@Preview(showBackground = true)
@Composable
fun CallLogScreenPreview() {
    CallLogScreen(rememberNavController())
}
