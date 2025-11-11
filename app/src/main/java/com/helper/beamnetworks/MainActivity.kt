package com.helper.beamnetworks

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.helper.beamnetworks.ui.theme.BeamNetworksTheme
import com.helper.beamnetworks.R
import kotlinx.coroutines.launch

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
        composable("log_expense") {
            LogExpenseScreen(navController)
        }
        composable("upcoming_installations") {
            UpcomingInstallationsScreen(navController)
        }
        composable("completed_installations") {
            CompletedInstallationsScreen(navController)
        }
        composable("call_log") {
            CallLogScreen(navController)
        }
        composable("map") {
            MapScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(navController: NavController, dashboardViewModel: DashboardViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissionsToRequest = mutableListOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}

    LaunchedEffect(Unit) {
        multiplePermissionsState.launchMultiplePermissionRequest()

        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(250.dp)) {
                Spacer(modifier = Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Check, contentDescription = "Completed Installations") },
                    label = { Text("Completed Installations") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        navController.navigate("completed_installations")
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(id = R.drawable.currency_usd), contentDescription = "Lipwa Link") },
                    label = { Text("Lipwa Link") },
                    selected = false,
                    onClick = { 
                        val url = "https://app.payhero.co.ke/lipwa/1787"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(id = R.drawable.account_group_outline), contentDescription = "PPPoE List") },
                    label = { Text("PPPoE List") },
                    selected = false,
                    onClick = { 
                        val url = "http://router.klizyentp.com/pppoe/list"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Beam Networks") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
            ) {
                DashboardCard(dashboardViewModel, navController)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column {
                        LogInstallationCard(navController = navController)
                    }
                    Column {
                        LogExpenseCard(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(viewModel: DashboardViewModel, navController: NavController) {
    val upcomingInstallations by viewModel.upcomingInstallationsCount.collectAsState()
    val monthlyExpenses by viewModel.monthlyExpensesTotal.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 7.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { navController.navigate("upcoming_installations") }
            ) {
                Text("Upcoming Installations")
                Text(upcomingInstallations.toString())
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("This Month's Expenses")
                Text(String.format("%.2f", monthlyExpenses))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogInstallationCard(navController: NavController) {
    Card(
        onClick = { navController.navigate("log_installation") },
        modifier = Modifier
            .padding(16.dp)
            .width(150.dp)
            .height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 7.dp
        )
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.installation_hammer_screwdriver),
                contentDescription = "Log an installation",
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center)
                    .offset(y = (-10).dp),
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
fun LogExpenseCard(navController: NavController) {
    Card(
        onClick = { navController.navigate("log_expense") },
        modifier = Modifier
            .padding(16.dp)
            .width(150.dp)
            .height(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.currency_usd),
                contentDescription = "Log an Expense",
                modifier = Modifier
                    .size(35.dp)
                    .align(Alignment.Center)
                    .offset(y = (-10).dp),
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
