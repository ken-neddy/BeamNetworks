package com.helper.beamnetworks

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingInstallationsScreen(
    navController: NavController,
    viewModel: UpcomingInstallationsViewModel = viewModel()
) {
    val upcomingInstallations by viewModel.upcomingInstallations.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Upcoming Installations") },
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
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {            items(upcomingInstallations) { installation ->
                InstallationListItem(
                    installation = installation,
                    onComplete = { navController.navigate("product_selection/${installation.id}") },
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun InstallationListItem(installation: InstallationData, onComplete: () -> Unit, navController: NavController) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)) {
                Text("Client: ${installation.clientName}")
                Text("Phone: ${installation.clientPhone}")
                Text(
                    buildAnnotatedString {
                        append("Location: ")
                        withStyle(style = SpanStyle(color = Color.Blue)) {
                            append("See Location")
                        }
                    },
                    modifier = Modifier.clickable { 
                        val gmmIntentUri = Uri.parse("google.navigation:q=${installation.clientLocation}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    }
                )
                Text("Date: ${installation.installationDate}")
                Text("Has Router: ${if (installation.hasRouter) "Yes" else "No"}")
                if (installation.moreNotes.isNotBlank()) {
                    Text("Notes: ${installation.moreNotes}")
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${installation.clientPhone}"))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Call, contentDescription = "Call Client")
                    }
                    IconButton(onClick = { navController.navigate("log_installation?installationId=${installation.id}") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Installation")
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onComplete) {
                    Text("Complete")
                }
            }
        }
    }
}
