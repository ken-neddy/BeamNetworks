package com.helper.beamnetworks

import android.Manifest
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CallLogEntry(
    val id: String,
    val name: String,
    val number: String,
    val date: String,
    val duration: String,
    val type: CallType
)

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    UNKNOWN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogScreen(navController: NavController) {
    val context = LocalContext.current
    var callLogs by remember { mutableStateOf<List<CallLogEntry>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }

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
                title = { Text("Call Log") },
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
        if (hasPermission) {
            LaunchedEffect(Unit) {
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
                                date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(date)),
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
                    callLogs = logs
                }
            }
            LazyColumn(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                items(callLogs) { callLog ->
                    CallLogItem(callLog = callLog) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_phone_number", callLog.number)
                        navController.popBackStack()
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(innerPadding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Permission to read call logs is required to use this feature.")
                Button(onClick = { launcher.launch(Manifest.permission.READ_CALL_LOG) }) {
                    Text("Request Permission")
                }
            }
        }
    }
}

@Composable
fun CallLogItem(callLog: CallLogEntry, onCallLogClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onCallLogClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = callLog.type.toIcon(),
                contentDescription = "Call type",
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(text = callLog.name)
                Text(text = callLog.number)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = callLog.date)
            Text(text = callLog.duration)
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
