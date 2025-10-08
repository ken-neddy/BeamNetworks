package com.helper.beamnetworks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.helper.beamnetworks.ui.theme.BeamNetworksTheme
import com.helper.beamnetworks.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeamNetworksTheme {
                BeamNetworksApp()
            }
        }
    }
}

@Composable
fun BeamNetworksApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController = navController)
        }
        composable("log_installation") {
            LogInstallationScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beam Networks") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier.padding(innerPadding).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LogInstallationCard(navController = navController)
            LogExpenseCard()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogInstallationCard(navController: NavController) {
    Card(
        onClick = { navController.navigate("log_installation") },
        modifier = Modifier.padding(16.dp).width(150.dp).height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 7.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.installation_hammer_screwdriver),
                contentDescription = "Log an installation",
                modifier = Modifier.size(30.dp).align(Alignment.Center).offset(y = (-10).dp),
                tint = Color.Unspecified
            )
            Text(
                text = "Log an installation",
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogExpenseCard() {
    Card(
        onClick = {},
        modifier = Modifier.padding(16.dp).width(150.dp).height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.currency_usd),
                contentDescription = "Log an Expense",
                modifier = Modifier.size(35.dp).align(Alignment.Center).offset(y = (-10).dp),
                tint = Color.Unspecified
            )
            Text(
                text = "Log an Expense",
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BeamNetworksTheme {
        val navController = rememberNavController()
        MainScreen(navController)
    }
}
