package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppNavDestination
import com.example.ui.SalesSaaSViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: SalesSaaSViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentDest by viewModel.currentDestination.collectAsStateWithLifecycle()
                val uiNotice by viewModel.uiNotice.collectAsStateWithLifecycle()
                val organization by viewModel.organization.collectAsStateWithLifecycle()
                val conversations by viewModel.conversations.collectAsStateWithLifecycle()

                val unreadTotal = conversations.sumOf { it.unreadCount }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiNotice) {
                    uiNotice?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearNotice()
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(300.dp),
                            drawerContainerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(WhatsAppGreenDark)
                                    .padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SmartToy,
                                            contentDescription = null,
                                            tint = WhatsAppGreenDark,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = organization?.name ?: "NexStore Commercial",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Plan : ${organization?.plan ?: "PRO"} · WhatsApp IA",
                                            color = WhatsAppGreenLight,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val drawerItems = listOf(
                                Triple(AppNavDestination.DASHBOARD, "Tableau de Bord", Icons.Default.Dashboard),
                                Triple(AppNavDestination.INBOX, "Boîte de Réception", Icons.Default.Chat),
                                Triple(AppNavDestination.CRM_PIPELINE, "CRM & Pipeline Leads", Icons.Default.ViewKanban),
                                Triple(AppNavDestination.CATALOG, "Catalogue Produits", Icons.Default.Inventory2),
                                Triple(AppNavDestination.ORDERS, "Commandes & RDV", Icons.Default.ShoppingCart),
                                Triple(AppNavDestination.AUTOMATIONS, "Automatisations & Relances", Icons.Default.Repeat),
                                Triple(AppNavDestination.AI_SETTINGS, "Assistant IA & Base RAG", Icons.Default.AutoAwesome),
                                Triple(AppNavDestination.SIMULATOR, "Simulateur WhatsApp Client", Icons.Default.PhoneAndroid),
                                Triple(AppNavDestination.ANALYTICS, "Analytics & ROI", Icons.Default.BarChart),
                                Triple(AppNavDestination.ADMIN_SETTINGS, "Paramètres & Équipe", Icons.Default.Settings),
                                Triple(AppNavDestination.LANDING_PAGE, "Landing Page Commerciale", Icons.Default.Web),
                                Triple(AppNavDestination.ONBOARDING, "Assistant Onboarding", Icons.Default.Checklist)
                            )

                            drawerItems.forEach { (dest, label, icon) ->
                                NavigationDrawerItem(
                                    icon = { Icon(icon, contentDescription = null) },
                                    label = { Text(label, fontSize = 13.sp) },
                                    selected = currentDest == dest,
                                    onClick = {
                                        viewModel.navigateTo(dest)
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = WhatsAppGreenDark.copy(alpha = 0.15f),
                                        selectedIconColor = WhatsAppGreenDark,
                                        selectedTextColor = WhatsAppGreenDark
                                    )
                                )
                            }
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            TopAppBar(
                                title = {
                                    Column {
                                        Text(
                                            text = when (currentDest) {
                                                AppNavDestination.DASHBOARD -> "Tableau de Bord"
                                                AppNavDestination.INBOX -> "Messagerie WhatsApp"
                                                AppNavDestination.CRM_PIPELINE -> "Pipeline CRM"
                                                AppNavDestination.CATALOG -> "Catalogue Produits"
                                                AppNavDestination.ORDERS -> "Commandes WhatsApp"
                                                AppNavDestination.APPOINTMENTS -> "Rendez-vous"
                                                AppNavDestination.AUTOMATIONS -> "Automatisations"
                                                AppNavDestination.AI_SETTINGS -> "Configuration IA & RAG"
                                                AppNavDestination.SIMULATOR -> "Simulateur Client"
                                                AppNavDestination.ANALYTICS -> "Analytics IA"
                                                AppNavDestination.ADMIN_SETTINGS -> "Paramètres & SaaS"
                                                AppNavDestination.LANDING_PAGE -> "Landing Page"
                                                AppNavDestination.ONBOARDING -> "Onboarding Guide"
                                            },
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = organization?.name ?: "NexStore",
                                            fontSize = 11.sp,
                                            color = WhatsAppGreenLight
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        modifier = Modifier.testTag("btn_open_drawer")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Menu Navigation",
                                            tint = Color.White
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { viewModel.navigateTo(AppNavDestination.SIMULATOR) }) {
                                        Icon(
                                            imageVector = Icons.Default.PhoneAndroid,
                                            contentDescription = "Simulateur WhatsApp",
                                            tint = Color.White
                                        )
                                    }
                                    IconButton(onClick = { viewModel.navigateTo(AppNavDestination.ADMIN_SETTINGS) }) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "Paramètres",
                                            tint = Color.White
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = WhatsAppGreenDark,
                                    titleContentColor = Color.White
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.navigationBarsPadding()
                            ) {
                                NavigationBarItem(
                                    selected = currentDest == AppNavDestination.DASHBOARD,
                                    onClick = { viewModel.navigateTo(AppNavDestination.DASHBOARD) },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                    label = { Text("Dashboard", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WhatsAppGreenDark,
                                        indicatorColor = WhatsAppGreenLight.copy(alpha = 0.2f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentDest == AppNavDestination.INBOX,
                                    onClick = { viewModel.navigateTo(AppNavDestination.INBOX) },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (unreadTotal > 0) {
                                                    Badge(containerColor = WhatsAppGreenDark) {
                                                        Text("$unreadTotal")
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "Inbox")
                                        }
                                    },
                                    label = { Text("Inbox", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WhatsAppGreenDark,
                                        indicatorColor = WhatsAppGreenLight.copy(alpha = 0.2f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentDest == AppNavDestination.CRM_PIPELINE,
                                    onClick = { viewModel.navigateTo(AppNavDestination.CRM_PIPELINE) },
                                    icon = { Icon(Icons.Default.ViewKanban, contentDescription = "CRM") },
                                    label = { Text("CRM", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WhatsAppGreenDark,
                                        indicatorColor = WhatsAppGreenLight.copy(alpha = 0.2f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentDest == AppNavDestination.CATALOG,
                                    onClick = { viewModel.navigateTo(AppNavDestination.CATALOG) },
                                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Catalogue") },
                                    label = { Text("Catalogue", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WhatsAppGreenDark,
                                        indicatorColor = WhatsAppGreenLight.copy(alpha = 0.2f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentDest == AppNavDestination.SIMULATOR,
                                    onClick = { viewModel.navigateTo(AppNavDestination.SIMULATOR) },
                                    icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = "Simulateur") },
                                    label = { Text("Simulateur", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WhatsAppGreenDark,
                                        indicatorColor = WhatsAppGreenLight.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentDest) {
                                AppNavDestination.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                                AppNavDestination.INBOX -> InboxScreen(viewModel = viewModel)
                                AppNavDestination.CRM_PIPELINE -> CrmPipelineScreen(viewModel = viewModel)
                                AppNavDestination.CATALOG -> CatalogScreen(viewModel = viewModel)
                                AppNavDestination.ORDERS -> OrdersAndAppointmentsScreen(viewModel = viewModel)
                                AppNavDestination.APPOINTMENTS -> OrdersAndAppointmentsScreen(viewModel = viewModel)
                                AppNavDestination.AUTOMATIONS -> AutomationsScreen(viewModel = viewModel)
                                AppNavDestination.AI_SETTINGS -> AiSettingsScreen(viewModel = viewModel)
                                AppNavDestination.SIMULATOR -> SimulatorScreen(viewModel = viewModel)
                                AppNavDestination.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                                AppNavDestination.ADMIN_SETTINGS -> AdminSettingsScreen(viewModel = viewModel)
                                AppNavDestination.LANDING_PAGE -> LandingPageScreen(viewModel = viewModel)
                                AppNavDestination.ONBOARDING -> OnboardingScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
