package com.helper.beamnetworks

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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
    val incomeViewModel: IncomeViewModel = viewModel()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController = navController, incomeViewModel = incomeViewModel)
        }
        composable("schedule_installation") {
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
        composable(
            route = "complete_installation/{installationId}",
            arguments = listOf(navArgument("installationId") { type = NavType.StringType })
        ) {
            CompleteInstallationScreen(navController = navController)
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
        composable("available_stock") {
            AvailableStockScreen(navController = navController)
        }
        composable("stock_running_low") {
            StockRunningLowScreen(navController = navController)
        }
        composable(
            route = "edit_product/{productName}",
            arguments = listOf(navArgument("productName") { type = NavType.StringType })
        ) {
            EditProductScreen(navController = navController)
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
            IncomeScreen(navController = navController, incomeViewModel = incomeViewModel)
        }
        composable("customers") {
            CustomersScreen(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel = viewModel(),
    incomeViewModel: IncomeViewModel = viewModel()
) {
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

    LaunchedEffect(Unit) {
        multiplePermissionsState.launchMultiplePermissionRequest()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 40.dp, horizontal = 16.dp)
                ) {
                    Text(
                        "Beam Networks",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    label = { Text("Completed Installations", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("completed_installations")
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(id = R.drawable.currency_usd), contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                    label = { Text("Lipwa Link", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        val url = "https://app.payhero.co.ke/lipwa/1787"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(id = R.drawable.account_group_outline), contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    label = { Text("PPPoE List", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        val url = "http://router.klizyentp.com/pppoe/list"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Beam Networks", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                DashboardCard(dashboardViewModel, navController)
                
//                Text(
//                    "Quick Access",
//                    modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 12.dp),
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.onBackground
//                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ManagerCard(
                        title = "Installation",
                        subtitle = "Manager",
                        iconRes = R.drawable.installation_hammer_screwdriver,
                        iconTint = Color(0xFFF9AE1A),
                        onClick = { navController.navigate("installation_manager") }
                    )
                    ManagerCard(
                        title = "Accounts",
                        subtitle = "Manager",
                        iconRes = R.drawable.currency_usd,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        onClick = { navController.navigate("accounts_manager") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ManagerCard(
                        title = "Stock",
                        subtitle = "Manager",
                        iconRes = R.drawable.stock,
                        iconTint = Color(0xFFF26222),
                        onClick = { navController.navigate("stock_manager") }
                    )
                    ManagerCard(
                        title = "Tickets",
                        subtitle = "Manager",
                        iconRes = R.drawable.ticket_account,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        onClick = { navController.navigate("ticket_manager") }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(viewModel: DashboardViewModel, navController: NavController) {
    val upcomingInstallations by viewModel.upcomingInstallationsCount.collectAsState()
    val incomeThisMonth by viewModel.incomeThisMonth.collectAsState()
    val monthlyExpenses by viewModel.monthlyExpensesTotal.collectAsState()
    val stockRunningLow by viewModel.stockRunningLow.collectAsState()
    val openTickets by viewModel.openTicketsCount.collectAsState()
    val totalCustomers by viewModel.totalCustomersCount.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DashboardItem(
                label = "Income This Month",
                value = NumberFormat.getCurrencyInstance(Locale("en", "KE")).format(incomeThisMonth),
                valueColor = MaterialTheme.colorScheme.primary,
                valueFontSize = 22.sp,
                valueFontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable { navController.navigate("income") }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                DashboardItem(
                    label = "Customers",
                    value = totalCustomers.toString(),
                    valueColor = MaterialTheme.colorScheme.primary,
                    valueFontSize = 16.sp,
                    modifier = Modifier.weight(1f).clickable { navController.navigate("customers") }
                )
                DashboardItem(
                    label = "Installations",
                    value = upcomingInstallations.toString(),
                    valueColor = MaterialTheme.colorScheme.secondary,
                    valueFontSize = 16.sp,
                    modifier = Modifier.weight(1f).clickable { navController.navigate("upcoming_installations") }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                DashboardItem(
                    label = "Open Tickets",
                    value = openTickets.toString(),
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    valueFontSize = 16.sp,
                    modifier = Modifier.weight(1f).clickable { navController.navigate("open_tickets") }
                )
                DashboardItem(
                    label = "Expenses",
                    value = NumberFormat.getCurrencyInstance(Locale("en", "KE")).format(monthlyExpenses),
                    valueColor = Color(0xFF475467),
                    valueFontSize = 16.sp,
                    modifier = Modifier.weight(1f).clickable { navController.navigate("monthly_expenses") }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            DashboardItem(
                label = "Low Stock",
                value = stockRunningLow.toString(),
                valueColor = MaterialTheme.colorScheme.error,
                valueFontSize = 16.sp,
                modifier = Modifier.clickable { navController.navigate("stock_running_low") }
            )
        }
    }
}

@Composable
fun DashboardItem(
    label: String, 
    value: String, 
    valueColor: Color, 
    modifier: Modifier = Modifier,
    valueFontSize: TextUnit = 18.sp,
    valueFontWeight: FontWeight = FontWeight.Bold
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = valueFontWeight,
            color = valueColor,
            fontSize = valueFontSize,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF667085),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ManagerCard(title: String, subtitle: String, iconRes: Int, iconTint: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 160.dp, height = 150.dp).padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallationManagerScreen(navController: NavController) {
    val view = LocalView.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val managerColor = Color(0xFFF9AE1A)

    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as Activity).window
            window.statusBarColor = managerColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            onDispose {
                window.statusBarColor = primaryColor.toArgb()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Installations Manager", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = managerColor)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            ManagerActionCard("Schedule an Installation", painterResource(id = R.drawable.installation_hammer_screwdriver), { navController.navigate("schedule_installation") })
            Spacer(modifier = Modifier.height(12.dp))
            ManagerActionCard("Upcoming Installations", painterResource(id = R.drawable.upcoming_installation), { navController.navigate("upcoming_installations") })
            Spacer(modifier = Modifier.height(12.dp))
            ManagerActionCard("Completed Installations", painterResource(id = R.drawable.installation_hammer_screwdriver), { navController.navigate("completed_installations") })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerActionCard(title: String, icon: androidx.compose.ui.graphics.painter.Painter, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsManagerScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts Manager", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            ManagerActionCard("Income", painterResource(R.drawable.cash_100), { navController.navigate("income") })
            Spacer(modifier = Modifier.height(12.dp))
            ManagerActionCard("Log an expense", painterResource(id = R.drawable.currency_usd), { navController.navigate("log_expense") })
            Spacer(modifier = Modifier.height(12.dp))
            ManagerActionCard("Expenses History", painterResource(R.drawable.history), { navController.navigate("monthly_expenses") })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(navController: NavController, incomeViewModel: IncomeViewModel = viewModel()) {
    val payments by incomeViewModel.payments.collectAsState()
    val searchQuery by incomeViewModel.searchQuery.collectAsState()
    val durationFilter by incomeViewModel.durationFilter.collectAsState()
    val totalAmount by incomeViewModel.totalAmount.collectAsState()
    var durationExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Income", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("en", "KE")).format(totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(end = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { incomeViewModel.onSearchQueryChanged(it) },
                label = { Text("Search payments") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = durationExpanded, onExpandedChange = { durationExpanded = !durationExpanded }) {
                OutlinedTextField(
                    value = durationFilter.displayName,
                    onValueChange = { },
                    label = { Text("Filter by date") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = durationExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = durationExpanded, onDismissRequest = { durationExpanded = false }) {
                    IncomeDurationFilter.values().forEach { filter ->
                        DropdownMenuItem(text = { Text(filter.displayName) }, onClick = { incomeViewModel.onDurationFilterChanged(filter); durationExpanded = false }) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(payments) { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = payment.date, style = MaterialTheme.typography.labelMedium, color = Color(0xFF667085))
                                Text(text = payment.customerName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale("en", "KE")).format(payment.amount.replace(",", "").toDoubleOrNull() ?: 0.0),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
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
                title = { Text("Stock Manager", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            ManagerActionCard("Add a product", painterResource(id = R.drawable.shape_plus_outline), { navController.navigate("add_product") })
            Spacer(modifier = Modifier.height(12.dp))
            ManagerActionCard("Available Stock", painterResource(id = R.drawable.stock), { navController.navigate("available_stock") })
            Spacer(modifier = Modifier.height(12.dp))
            ManagerActionCard("Stock Running Low", painterResource(id = R.drawable.alert), { navController.navigate("stock_running_low") })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketManagerScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tickets Manager", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            ManagerActionCard("Raise a ticket", painterResource(id = R.drawable.raiseticket), { navController.navigate("raise_a_ticket") })
            Spacer(modifier = Modifier.height(12.dp))
            ManagerActionCard("See open tickets", painterResource(id = R.drawable.ticket_open), { navController.navigate("open_tickets") })
            Spacer(modifier = Modifier.height(12.dp))
            ManagerActionCard("See completed tickets", painterResource(id = R.drawable.completedtickets), { navController.navigate("completed_tickets") })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseTicketScreen(navController: NavController, viewModel: OpenTicketsViewModel = viewModel(), raiseTicketViewModel: RaiseTicketViewModel = viewModel()) {
    val clientName by raiseTicketViewModel.clientName.collectAsState()
    val clientPhone by raiseTicketViewModel.clientPhone.collectAsState()
    val issue by raiseTicketViewModel.issue.collectAsState()
    val filteredCustomers by raiseTicketViewModel.filteredCustomers.collectAsState()
    val issues by raiseTicketViewModel.issues.collectAsState()
    val saveStatus by raiseTicketViewModel.saveStatus.collectAsState()
    val context = LocalContext.current
    var clientNameExpanded by remember { mutableStateOf(false) }
    var issueExpanded by remember { mutableStateOf(false) }

    // Logic to retrieve the selected phone number from Call Log
    val selectedPhone = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_phone_number")
    val selectedName = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_client_name")

    LaunchedEffect(selectedPhone, selectedName) {
        selectedPhone?.let {
            raiseTicketViewModel.onClientPhoneChange(it)
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_phone_number")
        }
        selectedName?.let {
            raiseTicketViewModel.onClientNameChange(it)
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_client_name")
        }
    }

    LaunchedEffect(saveStatus) { saveStatus?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); raiseTicketViewModel.clearSaveStatus() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Ticket", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            ExposedDropdownMenuBox(expanded = clientNameExpanded, onExpandedChange = { clientNameExpanded = !clientNameExpanded }) {
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { raiseTicketViewModel.onClientNameChange(it); clientNameExpanded = true },
                    label = { Text("Client Name") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientNameExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = clientNameExpanded, onDismissRequest = { clientNameExpanded = false }) {
                    filteredCustomers.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { raiseTicketViewModel.onClientNameChange(it); clientNameExpanded = false }) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = clientPhone,
                onValueChange = { raiseTicketViewModel.onClientPhoneChange(it) },
                label = { Text("Client Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { navController.navigate("call_log") }) {
                        Icon(painter = painterResource(id = R.drawable.call_log_24px), contentDescription = "Call Log", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ExposedDropdownMenuBox(expanded = issueExpanded, onExpandedChange = { issueExpanded = !issueExpanded }) {
                OutlinedTextField(
                    value = issue,
                    onValueChange = { raiseTicketViewModel.onIssueChange(it) },
                    label = { Text("Select Issue") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = issueExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = issueExpanded, onDismissRequest = { issueExpanded = false }) {
                    issues.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { raiseTicketViewModel.onIssueChange(it); issueExpanded = false }) }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { raiseTicketViewModel.raiseTicket() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Submit Ticket", fontWeight = FontWeight.Bold) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenTicketsScreen(navController: NavController, viewModel: OpenTicketsViewModel = viewModel()) {
    val openTickets by viewModel.openTickets.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Tickets", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (openTickets.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active tickets", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)
            ) {
                items(openTickets) { ticket ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ticket.clientName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = ticket.dateTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Issue: ${ticket.issue}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (ticket.clientPhone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Phone: ${ticket.clientPhone}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (ticket.clientPhone.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ticket.clientPhone}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                                Button(
                                    onClick = { viewModel.markAsResolved(ticket.id) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Mark Resolved")
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
fun CompletedTicketsScreen(navController: NavController, viewModel: CompletedTicketsViewModel = viewModel()) {
    val completedTickets by viewModel.completedTickets.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resolved Tickets", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (completedTickets.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No resolved tickets found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)
            ) {
                items(completedTickets) { ticket ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ticket.clientName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Text(
                                    text = ticket.dateTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Issue: ${ticket.issue}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.DarkGray
                            )
                            if (ticket.clientPhone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Phone: ${ticket.clientPhone}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ticket.clientPhone}"))
                                            context.startActivity(intent)
                                        }
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .align(Alignment.End)
                            ) {
                                Text(
                                    text = "RESOLVED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold
                                )
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
fun CustomersScreen(navController: NavController, viewModel: CustomersViewModel = viewModel()) {
    val customers by viewModel.filteredCustomers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers List", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search by name or number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (customers.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No customers found", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(customers) { customer ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = customer.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text(text = "Package: ${customer.packageName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text(text = customer.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
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
fun AddProductScreen(navController: NavController, viewModel: AddProductViewModel = viewModel()) {
    val productNames by viewModel.productNames.collectAsState()
    val productName by viewModel.productName.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(saveStatus) { saveStatus?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.clearSaveStatus() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Product", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(value = productName, onValueChange = { viewModel.onProductNameChange(it) }, label = { Text("Product Name") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    productNames.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { viewModel.onProductNameChange(it); expanded = false }) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = quantity, onValueChange = { viewModel.onQuantityChange(it) }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { viewModel.saveProduct() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Save Product", fontWeight = FontWeight.Bold) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableStockScreen(navController: NavController, viewModel: AvailableStockViewModel = viewModel()) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Stock", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(value = searchQuery, onValueChange = { viewModel.onSearchQueryChanged(it) }, label = { Text("Search products") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(products) { product ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = product.quantity.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { navController.navigate("edit_product/${product.name}") }) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.outline) }
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
fun EditProductScreen(navController: NavController, viewModel: EditProductViewModel = viewModel()) {
    val product by viewModel.product.collectAsState()
    val lowStockThreshold by viewModel.lowStockThreshold.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val context = LocalContext.current
    val productName = navController.currentBackStackEntry?.arguments?.getString("productName") ?: ""

    LaunchedEffect(productName) { viewModel.getProduct(productName) }
    LaunchedEffect(saveStatus) { saveStatus?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.clearSaveStatus() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Product", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            product?.let {
                Text("Product: ${it.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(value = lowStockThreshold, onValueChange = { viewModel.onLowStockThresholdChange(it) }, label = { Text("Low Stock Threshold") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { viewModel.saveProduct(productName) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Save Changes", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockRunningLowScreen(navController: NavController, viewModel: AvailableStockViewModel = viewModel()) {
    val products by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Low Stock Alert", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            items(products.filter { it.quantity <= it.lowStockThreshold }) { product ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = product.name, fontWeight = FontWeight.Bold)
                        Text(text = product.quantity.toString(), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteInstallationScreen(navController: NavController, viewModel: CompleteInstallationViewModel = viewModel()) {
    val products by viewModel.products.collectAsState()
    val usedProducts by viewModel.usedProducts.collectAsState()
    val completionStatus by viewModel.completionStatus.collectAsState()
    val context = LocalContext.current
    val installationId = navController.currentBackStackEntry?.arguments?.getString("installationId") ?: ""

    LaunchedEffect(completionStatus) { completionStatus?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.clearCompletionStatus(); navController.popBackStack() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalize Installation", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            Text("Select Products Used", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(products) { product ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = product.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        OutlinedTextField(value = usedProducts.find { it.name == product.name }?.quantity?.toString() ?: "0", onValueChange = { viewModel.onProductQuantityChanged(product.name, it) }, label = { Text("Qty") }, modifier = Modifier.width(80.dp), shape = RoundedCornerShape(8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.completeInstallation(installationId) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Submit Completion", fontWeight = FontWeight.Bold) }
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
