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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.helper.beamnetworks.ui.theme.BeamNetworksTheme
import com.helper.beamnetworks.R
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

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
        composable(
            route = "log_expense?expenseId={expenseId}",
            arguments = listOf(navArgument("expenseId") {
                type = NavType.StringType
                nullable = true
            })
        ) {
            LogExpenseScreen(navController)
        }
        composable("upcoming_installations") {
            UpcomingInstallationsScreen(navController)
        }
        composable("completed_installations") {
            CompletedInstallationsScreen(navController)
        }
        composable("monthly_expenses") {
            MonthlyExpensesScreen(navController)
        }
        composable(
            route = "installation_details/{installationId}",
            arguments = listOf(navArgument("installationId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "beamnetworks://app/installation_details/{installationId}" })
        ) {
            InstallationDetailsScreen(navController)
        }
        composable("call_log") {
            CallLogScreen(navController)
        }
        composable("map") {
            MapScreen(navController)
        }
        composable("installation_manager") {
            InstallationManagerScreen(navController = navController)
        }
        composable("accounts_manager") {
            AccountsManagerScreen(navController = navController)
        }
        composable("stock_manager") {
            StockManagerScreen(navController = navController)
        }
        composable("add_product") {
            AddProductScreen(navController = navController)
        }
        composable("ticket_manager") {
            TicketManagerScreen(navController = navController)
        }
        composable("raise_a_ticket") {
            RaiseTicketScreen(navController = navController)
        }
        composable("open_tickets") {
            OpenTicketsScreen(navController = navController)
        }
        composable("completed_tickets") {
            CompletedTicketsScreen(navController = navController)
        }
        composable("message_picker") {
            MessagePickerScreen(navController = navController)
        }
        composable("income") {
            IncomeScreen(navController = navController)
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
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.GET_ACCOUNTS
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
                        scope.launch { drawerState.close() }
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
                    InstallationManagerCard(navController = navController)
                    AccountsManagerCard(navController = navController)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    StockManagerCard(navController = navController)
                    TicketManagerCard(navController = navController)
                }
            }
        }
    }
}

@Composable
fun DashboardCard(viewModel: DashboardViewModel, navController: NavController) {
    val upcomingInstallations by viewModel.upcomingInstallationsCount.collectAsState()
    val completedInstallations by viewModel.completedInstallationsThisMonthCount.collectAsState()
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { navController.navigate("upcoming_installations") }
                ) {
                    Text("Upcoming Installations")
                    Text(upcomingInstallations.toString())
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { navController.navigate("completed_installations") }
                ) {
                    Text("Completed This Month")
                    Text(completedInstallations.toString())
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { navController.navigate("monthly_expenses") }
            ) {
                Text("This Month's Expenses")
                Text(String.format("%.2f", monthlyExpenses))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallationManagerCard(navController: NavController) {
    Card(
        onClick = { navController.navigate("installation_manager") },
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
                contentDescription = "Installations Manager",
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center)
                    .offset(y = (-10).dp),
                tint = Color.Unspecified
            )
            Text(
                text = "Installations Manager",
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsManagerCard(navController: NavController) {
    Card(
        onClick = { navController.navigate("accounts_manager") },
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
                painter = painterResource(id = R.drawable.currency_usd),
                contentDescription = "Accounts Manager",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center)
                    .offset(y = (-10).dp),
                tint = Color.Unspecified
            )
            Text(
                text = "Accounts Manager",
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockManagerCard(navController: NavController) {
    Card(
        onClick = { navController.navigate("stock_manager") },
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
                painter = painterResource(id = R.drawable.stock),
                contentDescription = "Stock Manager",
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center)
                    .offset(y = (-10).dp),
                tint = Color.Unspecified
            )
            Text(
                text = "Stock Manager",
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketManagerCard(navController: NavController) {
    Card(
        onClick = { navController.navigate("ticket_manager") },
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
                painter = painterResource(id = R.drawable.ticket_account),
                contentDescription = "Ticket Manager",
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center)
                    .offset(y = (-10).dp),
                tint = Color.Unspecified
            )
            Text(
                text = "Tickets Manager",
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallationManagerScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Installations Manager") },
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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                onClick = { navController.navigate("log_installation") },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.installation_hammer_screwdriver),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Log an Installation", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                onClick = { navController.navigate("upcoming_installations") },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.upcoming_installation),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Upcoming Installations", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                onClick = { navController.navigate("completed_installations") },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Completed Installations", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsManagerScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts Manager") },
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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                onClick = { navController.navigate("income") },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.currency_usd),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Income", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                onClick = { navController.navigate("log_expense") },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.currency_usd),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Log an expense", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                onClick = { navController.navigate("monthly_expenses") },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.currency_usd),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Expenses", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(navController: NavController, incomeViewModel: IncomeViewModel = viewModel()) {
    val googleSignInState by incomeViewModel.googleSignInState.collectAsState()
    val sheetData by incomeViewModel.sheetData.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val searchQuery by incomeViewModel.searchQuery.collectAsState()
    val durationFilter by incomeViewModel.durationFilter.collectAsState()
    val totalAmount by incomeViewModel.totalAmount.collectAsState()
    var durationExpanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        incomeViewModel.handleGoogleSignInResult(it)
    }

    LaunchedEffect(googleSignInState.error) {
        googleSignInState.error?.let {
            snackbarHostState.showSnackbar(it)
            incomeViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Income") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("en", "KE")).format(totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (googleSignInState.account == null) {
                Button(onClick = { launcher.launch(googleSignInState.signInIntent) }) {
                    Text("Sign in with Google")
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { incomeViewModel.onSearchQueryChanged(it) },
                        label = { Text("Search Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = durationExpanded,
                        onExpandedChange = { durationExpanded = !durationExpanded }
                    ) {
                        OutlinedTextField(
                            value = durationFilter.displayName,
                            onValueChange = { },
                            label = { Text("Duration") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = durationExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = durationExpanded,
                            onDismissRequest = { durationExpanded = false }
                        ) {
                            IncomeDurationFilter.values().forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter.displayName) },
                                    onClick = {
                                        incomeViewModel.onDurationFilterChanged(filter)
                                        durationExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(sheetData) { row ->
                            if (row.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = row.getOrNull(0)?.toString() ?: "", fontWeight = FontWeight.Bold)
                                            Text(text = row.getOrNull(1)?.toString() ?: "")
                                        }
                                        Text(text = row.getOrNull(2)?.toString() ?: "", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockManagerScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Manager") },
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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                onClick = { navController.navigate("add_product") },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.upcoming_installation),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Add a product", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketManagerScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tickets Manager") },
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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                onClick = { navController.navigate("raise_a_ticket") },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.raiseticket),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Raise a ticket", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                onClick = { navController.navigate("open_tickets") },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ticket_open),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("See open tickets", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                onClick = { navController.navigate("completed_tickets") },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.completedtickets),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("See completed tickets", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Product") },
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Add Product Screen")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseTicketScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raise a Ticket") },
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Raise a Ticket Screen")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenTicketsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Tickets") },
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Open Tickets Screen")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedTicketsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Completed Tickets") },
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Completed Tickets Screen")
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
