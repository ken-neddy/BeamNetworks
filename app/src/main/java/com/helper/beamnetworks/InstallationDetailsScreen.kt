package com.helper.beamnetworks

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun InstallationDetailsScreen(
    navController: NavController,
    viewModel: LogInstallationViewModel = viewModel()
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Installation Details") },
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DetailItem("Client Name:", viewModel.clientName)
            ClickableDetailItemWithIcon("Client Phone:", viewModel.clientPhone) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${viewModel.clientPhone}"))
                context.startActivity(intent)
            }
            ClickableDetailItem("Client Location:", "See Location") {
                val gmmIntentUri = Uri.parse("google.navigation:q=${viewModel.clientLocation}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                context.startActivity(mapIntent)
            }
            DetailItem("Installation Date:", viewModel.installationDate)
            DetailItem("Has Router:", if (viewModel.hasRouter) "Yes" else "No")
            if (viewModel.moreNotes.isNotBlank()) {
                DetailItem("More Notes:", viewModel.moreNotes)
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        Text(value, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ClickableDetailItem(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append(value)
                }
            },
            modifier = Modifier.clickable(onClick = onClick),
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ClickableDetailItemWithIcon(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(value, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Call, contentDescription = "Call")
            }
        }
    }
}