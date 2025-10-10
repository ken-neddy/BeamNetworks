package com.helper.beamnetworks

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

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
    MISSED
}

val sampleCallLogs = listOf(
    CallLogEntry("1", "John Doe", "555-1234", "2024-07-29", "2m 30s", CallType.INCOMING),
    CallLogEntry("2", "Jane Smith", "555-5678", "2024-07-29", "1m 15s", CallType.OUTGOING),
    CallLogEntry("3", "Unknown", "555-9012", "2024-07-28", "0s", CallType.MISSED),
    CallLogEntry("4", "Peter Jones", "555-3456", "2024-07-28", "5m 0s", CallType.INCOMING),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogScreen(navController: NavController) {
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
        LazyColumn(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            items(sampleCallLogs) { callLog ->
                CallLogItem(callLog = callLog)
            }
        }
    }
}

@Composable
fun CallLogItem(callLog: CallLogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
    }
}

@Preview(showBackground = true)
@Composable
fun CallLogScreenPreview() {
    CallLogScreen(rememberNavController())
}
