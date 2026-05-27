package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Client
import com.example.data.model.FinancialAccount
import com.example.data.model.Project
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.WarningYellow
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.ui.viewmodel.ProjectDraft
import java.text.SimpleDateFormat
import java.util.*

// Simplified Screen Routing States
enum class AppScreen {
    DASHBOARD,
    CLIENTS,
    COLLECTIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscapeAppUI(
    viewModel: InvoiceViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var showCreateClientDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }

    // Observers
    val clients by viewModel.clients.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val accounts by viewModel.financialAccounts.collectAsState()
    
    val totalCapital by viewModel.totalCapital.collectAsState(initial = 0.0)
    val banksTotalBalance by viewModel.banksTotalBalance.collectAsState()
    val safesTotalBalance by viewModel.safesTotalBalance.collectAsState()
    val totalProfits by viewModel.totalProfits.collectAsState(initial = 0.0)
    val openProjectsCount by viewModel.openProjectsCount.collectAsState(initial = 0)

    // Layout Scaffold with edge to edge safe draw values
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { /* notifications action */ },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .testTag("notification_button")
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "التنبيهات",
                            tint = Color(0xFF1C1B1F)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "نظام فواتير لاندسكيب",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F),
                                lineHeight = 22.sp
                            )
                            Text(
                                text = "لوحة التحكم المالية",
                                fontSize = 12.sp,
                                color = Color(0xFF49454F),
                                lineHeight = 16.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF386A20)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Landscape,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFE1E2E1), thickness = 1.dp)
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .testTag("nav_bar"),
                containerColor = Color(0xFFF3F4F9)
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.COLLECTIONS,
                    onClick = { currentScreen = AppScreen.COLLECTIONS },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "المستحقات") },
                    label = { Text("المستحقات", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_collections")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.CLIENTS,
                    onClick = { currentScreen = AppScreen.CLIENTS },
                    icon = { Icon(Icons.Default.People, contentDescription = "العملاء") },
                    label = { Text("العملاء", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_clients")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.DASHBOARD,
                    onClick = { currentScreen = AppScreen.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_dashboard")
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == AppScreen.CLIENTS) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateClientDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "إضافة عميل") },
                    text = { Text("إنشاء عميل ومشاريع", fontWeight = FontWeight.Bold) },
                    containerColor = Color(0xFF386A20),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .border(width = 2.dp, color = Color(0xFFFDFDFD), shape = RoundedCornerShape(16.dp))
                        .testTag("create_client_fab")
                        .padding(bottom = 16.dp)
                )
            } else if (currentScreen == AppScreen.DASHBOARD) {
                ExtendedFloatingActionButton(
                    onClick = { showAddAccountDialog = true },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "إضافة حساب") },
                    text = { Text("إضافة بنك/خزينة", fontWeight = FontWeight.Bold) },
                    containerColor = Color(0xFF386A20),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .border(width = 2.dp, color = Color(0xFFFDFDFD), shape = RoundedCornerShape(16.dp))
                        .testTag("add_account_fab")
                        .padding(bottom = 16.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                label = "screen_transition",
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }
            ) { screen ->
                when (screen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        totalCapital = totalCapital,
                        banksTotal = banksTotalBalance,
                        safesTotal = safesTotalBalance,
                        totalProfits = totalProfits,
                        openProjectsCount = openProjectsCount,
                        accounts = accounts,
                        onDeleteAccount = { viewModel.deleteFinancialAccount(it) },
                        onUpdateAccount = { viewModel.updateFinancialAccount(it) }
                    )
                    AppScreen.CLIENTS -> ClientsScreen(
                        clients = clients,
                        projects = projects,
                        onDeleteClient = { viewModel.deleteClient(it) },
                        onDeleteProject = { viewModel.deleteProject(it) },
                        onUpdateProject = { viewModel.updateProject(it) }
                    )
                    AppScreen.COLLECTIONS -> CollectionsScreen(
                        projects = projects,
                        clients = clients,
                        onUpdateProject = { viewModel.updateProject(it) }
                    )
                }
            }
        }
    }

    // CREATE CLIENT & UNLIMITED PROJECTS DIALOG
    if (showCreateClientDialog) {
        CreateClientFullscreenDialog(
            onDismiss = { showCreateClientDialog = false },
            onSave = { name, projectsList ->
                viewModel.createClientWithProjects(name, projectsList)
                showCreateClientDialog = false
            }
        )
    }

    // ADD ACCOUNT DIALOG
    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { name, balance, type ->
                viewModel.addFinancialAccount(name, balance, type)
                showAddAccountDialog = false
            }
        )
    }
}

// FORMATUTILS
object FormatUtils {
    fun formatCurrency(amount: Double): String {
        return String.format("%,.0f ج.م", amount)
    }

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return "غير محدد"
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun calculateRemainingDays(deliveryReceiptDateMs: Long?, paymentTermDays: Int): Int? {
        if (deliveryReceiptDateMs == null) return null
        
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val deliveryCalendar = Calendar.getInstance().apply {
            timeInMillis = deliveryReceiptDateMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMs = todayCalendar.timeInMillis - deliveryCalendar.timeInMillis
        val elapsedDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
        val remaining = paymentTermDays - elapsedDays
        return remaining
    }
}

// ==========================================
// SC 1: DASHBOARD
// ==========================================
@Composable
fun DashboardScreen(
    totalCapital: Double,
    banksTotal: Double,
    safesTotal: Double,
    totalProfits: Double,
    openProjectsCount: Int,
    accounts: List<FinancialAccount>,
    onDeleteAccount: (FinancialAccount) -> Unit,
    onUpdateAccount: (FinancialAccount) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Header Arabic text
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "نظرة مالية عامة",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(end = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Metrics Grid (Capital, Profits, Active Projects)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Capital Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD7E8CD)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "إجمالي رأس المال",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF111F0B).copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = "Capital",
                                tint = Color(0xFF386A20)
                            )
                        }

                        Text(
                            text = FormatUtils.formatCurrency(totalCapital),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF111F0B),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Branching breakdown
                        HorizontalDivider(
                            color = Color(0xFF386A20).copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Safes
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    "إجمالي الخزينة",
                                    fontSize = 11.sp,
                                    color = Color(0xFF111F0B).copy(alpha = 0.7f)
                               )
                                Text(
                                    FormatUtils.formatCurrency(safesTotal),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF386A20)
                                )
                            }

                            // Banks
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "إجمالي البنوك",
                                    fontSize = 11.sp,
                                    color = Color(0xFF111F0B).copy(alpha = 0.7f)
                                )
                                Text(
                                    FormatUtils.formatCurrency(banksTotal),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF386A20)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profits card (الأرباح)
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF386A20)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = "Profits",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "إجمالي الأرباح",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = FormatUtils.formatCurrency(totalProfits),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Open projects card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF3F4F9)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Icon(
                                Icons.Default.Handyman,
                                contentDescription = "Active Projects",
                                tint = Color(0xFF386A20)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "المشاريع المفتوحة",
                                fontSize = 13.sp,
                                color = Color(0xFF49454F)
                            )
                            Text(
                                text = "$openProjectsCount مشروع",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF386A20)
                            )
                        }
                    }
                }
            }
        }

        // Section: Detailed Branching Capital Accounts
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مواقع الميزانية (بنك / خزينة)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (accounts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = "Empty Accounts",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد حسابات مضافة حاليًا.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "اضغط على زر الإضافة بالأسفل لإضافة بنوك أو خزينة يدويًا.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(accounts) { account ->
                AccountRowItem(
                    account = account,
                    onDelete = { onDeleteAccount(account) },
                    onUpdate = { onUpdateAccount(it) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun AccountRowItem(
    account: FinancialAccount,
    onDelete: () -> Unit,
    onUpdate: (FinancialAccount) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(16.dp))
            .clickable { showEditDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_account_${account.id}")
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "مسح الحساب",
                    tint = Color(0xFFBA1A1A).copy(alpha = 0.7f)
                )
            }

            // Price Details
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FormatUtils.formatCurrency(account.balance),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = if (account.type == "BANK") "حساب بنكي" else "خزينة نقدية",
                    fontSize = 11.sp,
                    color = Color(0xFF49454F)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Bank details name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = account.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (account.type == "BANK") Icons.Default.AccountBalance else Icons.Default.Payments,
                        contentDescription = "Type",
                        tint = if (account.type == "BANK") Color(0xFF386A20) else Color(0xFFD89D00),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AddAccountDialog(
            titleText = "تعديل الحساب المالي",
            initialName = account.name,
            initialBalance = account.balance,
            initialType = account.type,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, balance, type ->
                onUpdate(account.copy(name = name, balance = balance, type = type))
                showEditDialog = false
            }
        )
    }
}

// ==========================================
// SC 2: CLIENTS & THEIR INFINITE PROJECTS
// ==========================================
@Composable
fun ClientsScreen(
    clients: List<Client>,
    projects: List<Project>,
    onDeleteClient: (Client) -> Unit,
    onDeleteProject: (Project) -> Unit,
    onUpdateProject: (Project) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "قائمة عملاء ومشاريع اللاندسكيب",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(end = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (clients.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "No Clients",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد سجلات عملاء حالية",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ابدأ بإنشاء عميلك الأول مع عدد لا نهائي من المشاريع المرفقة به عبر الضغط على الزر بالأسفل.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        } else {
            items(clients) { client ->
                val clientProjects = projects.filter { it.clientId == client.id }
                ClientCardItem(
                    client = client,
                    projects = clientProjects,
                    onDeleteClient = { onDeleteClient(client) },
                    onDeleteProject = onDeleteProject,
                    onUpdateProject = onUpdateProject
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ClientCardItem(
    client: Client,
    projects: List<Project>,
    onDeleteClient: () -> Unit,
    onDeleteProject: (Project) -> Unit,
    onUpdateProject: (Project) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(16.dp))
            .testTag("client_card_${client.id}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Client Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = Color(0xFF386A20)
                )

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(4f)
                ) {
                    Text(
                        text = client.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "عدد المشاريع: ${projects.size}",
                        fontSize = 12.sp,
                        color = Color(0xFF49454F),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Client",
                        tint = Color(0xFF386A20)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F9))
                        .padding(12.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFE1E2E1))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Danger Button to delete customer
                        TextButton(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFBA1A1A))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "مسح")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف العميل نهائيًا", fontWeight = FontWeight.Bold)
                        }

                        Text(
                            "مشاريع العميل المعتمدة:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF386A20)
                        )
                    }

                    if (projects.isEmpty()) {
                        Text(
                            "لا توجد مشاريع مخصصة لهذا العميل حاليًا.",
                            color = Color(0xFF49454F),
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    } else {
                        projects.forEach { project ->
                            ProjectListItem(
                                project = project,
                                onDelete = { onDeleteProject(project) },
                                onUpdate = onUpdateProject
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    "حذف العميل وماريعه؟",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            text = {
                Text(
                    "هل أنت متأكد من حذف العميل [${client.name}] وكل المشاريع التابعة له؟ هذه العملية غير قابلة للتراجع نهائيًا.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClient()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("نعم، احذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// Sub item inside detail cards
@Composable
fun ProjectListItem(
    project: Project,
    onDelete: () -> Unit,
    onUpdate: (Project) -> Unit
) {
    var showProjectDetails by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(12.dp))
            .clickable { showProjectDetails = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "حذف المشروع",
                    tint = Color(0xFFBA1A1A).copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    project.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1C1B1F)
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = FormatUtils.formatCurrency(project.totalValue),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF386A20)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "قيمة العمل:",
                        fontSize = 11.sp,
                        color = Color(0xFF49454F)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // State indicators
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (project.isCompleted) Icons.Default.CheckCircle else Icons.Default.Pending,
                    contentDescription = "Status",
                    tint = if (project.isCompleted) Color(0xFF386A20) else Color(0xFFD89D00),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showProjectDetails) {
        ProjectDetailsDialog(
            project = project,
            onDismiss = { showProjectDetails = false },
            onUpdate = onUpdate
        )
    }
}

// Project detail dialog (allows updating status, dates, actual costs)
@Composable
fun ProjectDetailsDialog(
    project: Project,
    onDismiss: () -> Unit,
    onUpdate: (Project) -> Unit
) {
    val context = LocalContext.current
    var isCompleted by remember { mutableStateOf(project.isCompleted) }
    var completionDate by remember { mutableStateOf(project.completionDate) }
    var actualCostInput by remember { mutableStateOf(project.actualCost.toString()) }
    var totalValueInput by remember { mutableStateOf(project.totalValue.toString()) }

    var hasDeliveryReceipt by remember { mutableStateOf(project.hasDeliveryReceipt) }
    var deliveryReceiptDate by remember { mutableStateOf(project.deliveryReceiptDate) }
    var paymentTermInput by remember { mutableStateOf(project.paymentTermDays.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "تفاصيل وتحديث المشروع",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    OutlinedTextField(
                        value = project.name,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("اسم المشروع") },
                        modifier = Modifier.fillMaxWidth().testTag("detail_project_name")
                    )
                }

                item {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                }

                // Value Inputs
                item {
                    OutlinedTextField(
                        value = totalValueInput,
                        onValueChange = { totalValueInput = it },
                        label = { Text("القيمة الإجمالية للعملية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("detail_project_total")
                    )
                }

                item {
                    OutlinedTextField(
                        value = actualCostInput,
                        onValueChange = { actualCostInput = it },
                        label = { Text("التكلفة الفعلية الحالية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("detail_project_cost")
                    )
                }

                // Dynamic calculations
                item {
                    val tot = totalValueInput.toDoubleOrNull() ?: 0.0
                    val act = actualCostInput.toDoubleOrNull() ?: 0.0
                    val prof = tot - act
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (prof >= 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            )
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (prof >= 0) "الربح: ${FormatUtils.formatCurrency(prof)}" 
                                   else "خسارة: ${FormatUtils.formatCurrency(-prof)}",
                            fontWeight = FontWeight.Bold,
                            color = if (prof >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Text("الربحية والمسار")
                    }
                }

                item {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                }

                // Delivery receipt items & payment term days
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Switch(
                            checked = hasDeliveryReceipt,
                            onCheckedChange = { 
                                hasDeliveryReceipt = it 
                                if (it && deliveryReceiptDate == null) {
                                    deliveryReceiptDate = System.currentTimeMillis()
                                }
                            }
                        )
                        Text("بيان استلام؟ (نعم / لا)")
                    }
                }

                if (hasDeliveryReceipt) {
                    item {
                        Button(
                            onClick = {
                                val c = Calendar.getInstance()
                                deliveryReceiptDate?.let { c.timeInMillis = it }
                                DatePickerDialog(
                                    context,
                                    { _: DatePicker, year: Int, month: Int, day: Int ->
                                        val cal = Calendar.getInstance()
                                        cal.set(year, month, day)
                                        deliveryReceiptDate = cal.timeInMillis
                                    },
                                    c.get(Calendar.YEAR),
                                    c.get(Calendar.MONTH),
                                    c.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تاريخ بيان الاستلام: ${FormatUtils.formatDate(deliveryReceiptDate)}")
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = paymentTermInput,
                            onValueChange = { paymentTermInput = it },
                            label = { Text("أيام صرف مستحقاتنا (مثلا ٣٠ يوم)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Remaining days ticker
                    item {
                        val days = paymentTermInput.toIntOrNull() ?: 30
                        val remaining = FormatUtils.calculateRemainingDays(deliveryReceiptDate, days)
                        if (remaining != null) {
                            val lowerLimit = remaining <= 10
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (lowerLimit) ErrorRed.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$remaining يومًا تتبقي للصرف",
                                    color = if (lowerLimit) ErrorRed else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("العد التنازلي التلقائي")
                            }
                        }
                    }
                }

                item {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                }

                // Completion item
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Switch(
                            checked = isCompleted,
                            onCheckedChange = { 
                                isCompleted = it 
                                if (it && completionDate == null) {
                                    completionDate = System.currentTimeMillis()
                                }
                            }
                        )
                        Text("الانتهاء؟ (نعم / لا)")
                    }
                }

                if (isCompleted) {
                    item {
                        Button(
                            onClick = {
                                val c = Calendar.getInstance()
                                completionDate?.let { c.timeInMillis = it }
                                DatePickerDialog(
                                    context,
                                    { _: DatePicker, year: Int, month: Int, day: Int ->
                                        val cal = Calendar.getInstance()
                                        cal.set(year, month, day)
                                        completionDate = cal.timeInMillis
                                    },
                                    c.get(Calendar.YEAR),
                                    c.get(Calendar.MONTH),
                                    c.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تاريخ الانتهاء: ${FormatUtils.formatDate(completionDate)}")
                        }
                    }
                }

                // Action Confirm/Dismiss Buttons
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                val totVal = totalValueInput.toDoubleOrNull() ?: 0.0
                                val actCst = actualCostInput.toDoubleOrNull() ?: 0.0
                                val termDays = paymentTermInput.toIntOrNull() ?: 30
                                
                                val updated = project.copy(
                                    totalValue = totVal,
                                    actualCost = actCst,
                                    isCompleted = isCompleted,
                                    completionDate = if (isCompleted) completionDate else null,
                                    hasDeliveryReceipt = hasDeliveryReceipt,
                                    deliveryReceiptDate = if (hasDeliveryReceipt) deliveryReceiptDate else null,
                                    paymentTermDays = termDays
                                )
                                onUpdate(updated)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("حفظ التحديثات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SC 3: COLLECTIONS REMINDERS & HIGHLIGHTS
// ==========================================
@Composable
fun CollectionsScreen(
    projects: List<Project>,
    clients: List<Client>,
    onUpdateProject: (Project) -> Unit
) {
    val collectionProjects = projects.filter { it.hasDeliveryReceipt }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "متابعة صرف المستحقات والعد التنازلي",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(end = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "يحتوي هذا القسم على المشاريع التي تم تسليم بيان استلامها ولها فترة استحقاق تنازلية.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(end = 4.dp, top = 2.dp)
            )
        }

        if (collectionProjects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.HourglassDisabled,
                            contentDescription = "No Collections",
                            tint = Color(0xFF386A20),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لا توجد مستحقات في مرحلة الانتظار",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1F)
                        )
                        Text(
                            text = "بمجرد إضافة 'بيان استلام' لأي مشروع وتحديد أيام الاستحقاق سيبدأ العد التنازلي للتنبيهات.",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(collectionProjects) { project ->
                val client = clients.find { it.id == project.clientId }
                val remaining = FormatUtils.calculateRemainingDays(project.deliveryReceiptDate, project.paymentTermDays)
                val isRedAlert = remaining != null && remaining <= 10

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isRedAlert) 2.dp else 1.dp,
                            color = if (isRedAlert) Color(0xFFBA1A1A) else Color(0xFFE1E2E1),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("collection_item_${project.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Countdown Banner (lit error red if <= 10 days)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        if (isRedAlert) Color(0xFFF9DEDC)
                                        else Color(0xFFD7E8CD)
                                    )
                                    .padding(vertical = 4.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = if (remaining != null && remaining > 0) "يتبقي: $remaining يوم"
                                           else if (remaining != null && remaining == 0) "اليوم موعد الصرف"
                                           else "منتهي الصرف",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRedAlert) Color(0xFF410002) else Color(0xFF111F0B)
                                )
                            }

                            // Project Context details
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = project.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (isRedAlert) Color(0xFFBA1A1A) else Color(0xFF1C1B1F)
                                )
                                Text(
                                    text = "العميل: ${client?.name ?: "غير معروف"}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF49454F)
                                )
                            }
                        }

                        HorizontalDivider(
                            color = Color(0xFFE1E2E1),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        // Key Timeline Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Completion status click toggle
                            Row(
                                modifier = Modifier.clickable {
                                    onUpdateProject(project.copy(isCompleted = !project.isCompleted, completionDate = if (!project.isCompleted) System.currentTimeMillis() else null))
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (project.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    tint = if (project.isCompleted) Color(0xFF386A20) else Color(0xFF8E918F),
                                    contentDescription = "Complete toggle",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تم اتمام الصرف",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (project.isCompleted) Color(0xFF386A20) else Color(0xFF49454F)
                                )
                            }

                            // Dates info
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "تاريخ الاستلام: ${FormatUtils.formatDate(project.deliveryReceiptDate)}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF49454F)
                                )
                                Text(
                                    text = "مدة الصرف المستهدفة: ${project.paymentTermDays} يوم",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF386A20)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ==========================================
// FORM DIALOGS & SHEET UTILITIES
// ==========================================

// Create account bank dialog
@Composable
fun AddAccountDialog(
    titleText: String = "إضافة حساب مالي جديد",
    initialName: String = "",
    initialBalance: Double = 0.0,
    initialType: String = "BANK",
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var balance by remember { mutableStateOf(if (initialBalance == 0.0) "" else initialBalance.toString()) }
    var type by remember { mutableStateOf(initialType) } // "BANK" or "SAFE"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = titleText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Account Type Switch (Segmented selection)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (type == "SAFE") MaterialTheme.colorScheme.secondary else Color.Transparent)
                            .clickable { type = "SAFE" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "خزينة يدوية",
                            color = if (type == "SAFE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (type == "BANK") MaterialTheme.colorScheme.secondary else Color.Transparent)
                            .clickable { type = "BANK" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "بنك مالي",
                            color = if (type == "BANK") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم البنك أو الخزينة") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_name_input"),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                )

                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("الرصيد الافتتاحي بالجنيه") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_balance_input"),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name, balance.toDoubleOrNull() ?: 0.0, type)
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("تأكيد وحفظ")
                    }
                }
            }
        }
    }
}

// FULLSCREEN MULTI PROJECT CLIENT CREATOR DIALOG
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClientFullscreenDialog(
    onDismiss: () -> Unit,
    onSave: (String, List<ProjectDraft>) -> Unit
) {
    val context = LocalContext.current
    var clientName by remember { mutableStateOf("") }
    val projectDrafts = remember { mutableStateListOf(ProjectDraft()) } // Initial starter project

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Header Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }

                    Text(
                        "إنشاء عميل ومشاريع متعددة",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Client Info Cards
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    "بيانات العميل الأساسية",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = clientName,
                                    onValueChange = { clientName = it },
                                    label = { Text("اسم العميل بالكامل") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("client_name_field"),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { projectDrafts.add(ProjectDraft()) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add project icon")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة مشروع آخر +", fontWeight = FontWeight.Bold)
                            }

                            Text(
                                "تعيين مشاريع العميل (مفتوحة/مغلقة)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Countless Dynamic project draft forms
                    items(projectDrafts.size) { index ->
                        val draft = projectDrafts[index]

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Form Index details header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (projectDrafts.size > 1) {
                                        IconButton(
                                            onClick = { projectDrafts.removeAt(index) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.RemoveCircleOutline,
                                                contentDescription = "Remove Project",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(32.dp))
                                    }

                                    Text(
                                        "بيانات مشروع رقم ${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                OutlinedTextField(
                                    value = draft.name,
                                    onValueChange = { projectDrafts[index] = draft.copy(name = it) },
                                    label = { Text("اسم المشروع") },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                                )

                                // Flags Segmented switches
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Switch(
                                        checked = draft.hasElectronicInvoice,
                                        onCheckedChange = { projectDrafts[index] = draft.copy(hasElectronicInvoice = it) }
                                    )
                                    Text("فاتورة إلكترونية؟")
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Switch(
                                        checked = draft.hasAssignmentLetter,
                                        onCheckedChange = { projectDrafts[index] = draft.copy(hasAssignmentLetter = it) }
                                    )
                                    Text("امر اسناد؟")
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Switch(
                                        checked = draft.hasDeliveryReceipt,
                                        onCheckedChange = { 
                                            val tDate = if (it) System.currentTimeMillis() else null
                                            projectDrafts[index] = draft.copy(hasDeliveryReceipt = it, deliveryReceiptDate = tDate) 
                                        }
                                    )
                                    Text("بيان استلام؟")
                                }

                                // Date inputs & Days if delivery receipt active
                                if (draft.hasDeliveryReceipt) {
                                    // Delivery receipt Date chooser
                                    Button(
                                        onClick = {
                                            val c = Calendar.getInstance()
                                            draft.deliveryReceiptDate?.let { c.timeInMillis = it }
                                            DatePickerDialog(
                                                context,
                                                { _: DatePicker, year: Int, month: Int, day: Int ->
                                                    val cal = Calendar.getInstance()
                                                    cal.set(year, month, day)
                                                    projectDrafts[index] = draft.copy(deliveryReceiptDate = cal.timeInMillis)
                                                },
                                                c.get(Calendar.YEAR),
                                                c.get(Calendar.MONTH),
                                                c.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("تاريخ الاستلام: ${FormatUtils.formatDate(draft.deliveryReceiptDate)}")
                                    }

                                    OutlinedTextField(
                                        value = draft.paymentTermDays.toString(),
                                        onValueChange = { projectDrafts[index] = draft.copy(paymentTermDays = it.toIntOrNull() ?: 30) },
                                        label = { Text("أيام صرف مستحقاتنا التنازلية") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                                    )
                                }

                                // Invoicing details
                                OutlinedTextField(
                                    value = if (draft.totalValue == 0.0) "" else draft.totalValue.toString(),
                                    onValueChange = { projectDrafts[index] = draft.copy(totalValue = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text("القيمة الإجمالية للعامية") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                                )

                                OutlinedTextField(
                                    value = if (draft.actualCost == 0.0) "" else draft.actualCost.toString(),
                                    onValueChange = { projectDrafts[index] = draft.copy(actualCost = it.toDoubleOrNull() ?: 0.0) },
                                    label = { Text("قيمة التكلفة الفعلية") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End)
                                )

                                // Calculated automatic state details
                                val profitVal = draft.totalValue - draft.actualCost
                                if (profitVal > 0) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${FormatUtils.formatCurrency(profitVal)} (+)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text("صافي الربح المتوقع:")
                                    }
                                } else if (profitVal < 0) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ErrorRed.copy(alpha = 0.12f))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${FormatUtils.formatCurrency(-profitVal)} (-)", color = ErrorRed, fontWeight = FontWeight.Bold)
                                        Text("خسارة مالية متوقعة:")
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Switch(
                                        checked = draft.isCompleted,
                                        onCheckedChange = { 
                                            val tDate = if (it) System.currentTimeMillis() else null
                                            projectDrafts[index] = draft.copy(isCompleted = it, completionDate = tDate) 
                                        }
                                    )
                                    Text("المشروع منتهي وصرف بالكامل؟")
                                }

                                if (draft.isCompleted) {
                                    Button(
                                        onClick = {
                                            val c = Calendar.getInstance()
                                            draft.completionDate?.let { c.timeInMillis = it }
                                            DatePickerDialog(
                                                context,
                                                { _: DatePicker, year: Int, month: Int, day: Int ->
                                                    val cal = Calendar.getInstance()
                                                    cal.set(year, month, day)
                                                    projectDrafts[index] = draft.copy(completionDate = cal.timeInMillis)
                                                },
                                                c.get(Calendar.YEAR),
                                                c.get(Calendar.MONTH),
                                                c.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("تاريخ الانتهاء: ${FormatUtils.formatDate(draft.completionDate)}")
                                    }
                                }
                            }
                        }
                    }
                }

                // Action panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (clientName.isNotBlank() && projectDrafts.any { it.name.isNotBlank() }) {
                                onSave(clientName, projectDrafts.toList())
                            }
                        },
                        enabled = clientName.isNotBlank() && projectDrafts.any { it.name.isNotBlank() },
                        modifier = Modifier
                            .weight(2f)
                            .testTag("save_client_button")
                    ) {
                        Text("حفظ وبناء السجلات", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
