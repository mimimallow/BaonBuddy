package com.example.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DepositToGoalDialog
import com.example.ui.components.ExpenseEditorSheet
import com.example.ui.components.SavingsGoalEditorSheet
import com.example.ui.screens.AllowanceSetupScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SavingsScreen
import com.example.ui.screens.TipsScreen
import com.example.ui.theme.BaonBuddyTheme

object BaonRoutes {
    const val HOME = "home"
    const val EXPENSES = "expenses"
    const val SAVINGS = "savings"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val ALLOWANCE_SETUP = "allowance_setup"
    const val TIPS = "tips"
}

private enum class BottomNavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(BaonRoutes.HOME, "Home", Icons.Rounded.Home, Icons.Outlined.Home),
    EXPENSES(BaonRoutes.EXPENSES, "Expenses", Icons.Rounded.ReceiptLong, Icons.Outlined.ReceiptLong),
    SAVINGS(BaonRoutes.SAVINGS, "Savings", Icons.Rounded.Savings, Icons.Outlined.Savings),
    HISTORY(BaonRoutes.HISTORY, "History", Icons.Rounded.History, Icons.Outlined.History),
    PROFILE(BaonRoutes.PROFILE, "Profile", Icons.Rounded.Person, Icons.Outlined.Person)
}

@Composable
fun BaonBuddyApp(
    viewModel: BaonViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: BaonRoutes.HOME

    // Shared modal states
    var showExpenseEditor by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var deletingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    var showGoalEditor by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var deletingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var depositingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val isTopLevelTab = BottomNavDestination.entries.any { it.route == currentRoute }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BaonBuddyTheme(themeMode = uiState.preferences.themeMode) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val useNavRail = maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (isTopLevelTab && !useNavRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ) {
                            BottomNavDestination.entries.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    modifier = Modifier.testTag("nav_${dest.route}")
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    when (currentRoute) {
                        BaonRoutes.EXPENSES -> {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    editingExpense = null
                                    showExpenseEditor = true
                                },
                                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                                text = { Text("Add Expense") },
                                modifier = Modifier.testTag("fab_add_expense")
                            )
                        }
                        BaonRoutes.SAVINGS -> {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    editingGoal = null
                                    showGoalEditor = true
                                },
                                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                                text = { Text("New Goal") },
                                modifier = Modifier.testTag("fab_add_goal")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isTopLevelTab && useNavRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ) {
                            BottomNavDestination.entries.forEach { dest ->
                                val selected = currentRoute == dest.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    modifier = Modifier.testTag("rail_${dest.route}")
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = BaonRoutes.HOME,
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() },
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 840.dp)
                        ) {
                            composable(BaonRoutes.HOME) {
                                DashboardScreen(
                                    uiState = uiState,
                                    onAddExpenseClick = {
                                        editingExpense = null
                                        showExpenseEditor = true
                                    },
                                    onEditExpenseClick = { expense ->
                                        editingExpense = expense
                                        showExpenseEditor = true
                                    },
                                    onDeleteExpenseClick = { expense ->
                                        deletingExpense = expense
                                    },
                                    onNavigateToAllowanceSetup = {
                                        navController.navigate(BaonRoutes.ALLOWANCE_SETUP)
                                    },
                                    onNavigateToSavings = { navigateToTab(BaonRoutes.SAVINGS) },
                                    onNavigateToHistory = { navigateToTab(BaonRoutes.HISTORY) },
                                    onNavigateToTips = {
                                        navController.navigate(BaonRoutes.TIPS)
                                    },
                                    onNavigateToProfile = { navigateToTab(BaonRoutes.PROFILE) },
                                    onNextTip = { viewModel.nextTip() }
                                )
                            }

                            composable(BaonRoutes.EXPENSES) {
                                ExpensesScreen(
                                    uiState = uiState,
                                    onAddExpenseClick = {
                                        editingExpense = null
                                        showExpenseEditor = true
                                    },
                                    onEditExpenseClick = { expense ->
                                        editingExpense = expense
                                        showExpenseEditor = true
                                    },
                                    onDeleteExpenseClick = { expense ->
                                        deletingExpense = expense
                                    }
                                )
                            }

                            composable(BaonRoutes.SAVINGS) {
                                SavingsScreen(
                                    uiState = uiState,
                                    onCreateGoalClick = {
                                        editingGoal = null
                                        showGoalEditor = true
                                    },
                                    onEditGoalClick = { goal ->
                                        editingGoal = goal
                                        showGoalEditor = true
                                    },
                                    onDeleteGoalClick = { goal ->
                                        deletingGoal = goal
                                    },
                                    onAddMoneyClick = { goal ->
                                        depositingGoal = goal
                                    }
                                )
                            }

                            composable(BaonRoutes.HISTORY) {
                                HistoryScreen(
                                    uiState = uiState,
                                    onEditExpenseClick = { expense ->
                                        editingExpense = expense
                                        showExpenseEditor = true
                                    },
                                    onDeleteExpenseClick = { expense ->
                                        deletingExpense = expense
                                    }
                                )
                            }

                            composable(BaonRoutes.PROFILE) {
                                ProfileScreen(
                                    uiState = uiState,
                                    onUpdateProfile = { name, level ->
                                        viewModel.updateProfile(name, level)
                                    },
                                    onUpdateThemeMode = { mode ->
                                        viewModel.updateThemeMode(mode)
                                    },
                                    onNavigateToAllowanceSetup = {
                                        navController.navigate(BaonRoutes.ALLOWANCE_SETUP)
                                    },
                                    onNavigateToTips = {
                                        navController.navigate(BaonRoutes.TIPS)
                                    },
                                    onClearAllData = {
                                        viewModel.clearAllData()
                                    }
                                )
                            }

                            composable(BaonRoutes.ALLOWANCE_SETUP) {
                                AllowanceSetupScreen(
                                    uiState = uiState,
                                    onBack = { navController.popBackStack() },
                                    onSaveSettings = { daily, days, warnPct ->
                                        viewModel.saveAllowanceSettings(daily, days, warnPct)
                                    }
                                )
                            }

                            composable(BaonRoutes.TIPS) {
                                TipsScreen(
                                    uiState = uiState,
                                    onBack = { navController.popBackStack() },
                                    onNextTip = { viewModel.nextTip() },
                                    onToggleBookmark = { tipId -> viewModel.toggleTipBookmark(tipId) }
                                )
                            }
                        }
                    }
                }
            }

            if (showExpenseEditor) {
                ExpenseEditorSheet(
                    existingExpense = editingExpense,
                    onDismiss = {
                        showExpenseEditor = false
                        editingExpense = null
                    },
                    onSave = { title, amount, category, dateMillis, notes ->
                        viewModel.saveExpense(
                            existingId = editingExpense?.id,
                            title = title,
                            amount = amount,
                            category = category,
                            dateMillis = dateMillis,
                            notes = notes
                        )
                        showExpenseEditor = false
                        editingExpense = null
                    }
                )
            }

            deletingExpense?.let { target ->
                ConfirmDeleteDialog(
                    title = "Delete Expense?",
                    message = "Remove \"${target.title}\" (${com.example.util.Formatters.formatPeso(target.amount)})? Your remaining balance will update automatically.",
                    onDismiss = { deletingExpense = null },
                    onConfirm = {
                        viewModel.deleteExpense(target)
                        deletingExpense = null
                    }
                )
            }

            if (showGoalEditor) {
                SavingsGoalEditorSheet(
                    existingGoal = editingGoal,
                    onDismiss = {
                        showGoalEditor = false
                        editingGoal = null
                    },
                    onSave = { name, targetAmount, targetDateMillis ->
                        viewModel.saveSavingsGoal(
                            existingGoal = editingGoal,
                            name = name,
                            targetAmount = targetAmount,
                            targetDateMillis = targetDateMillis
                        )
                        showGoalEditor = false
                        editingGoal = null
                    }
                )
            }

            deletingGoal?.let { target ->
                ConfirmDeleteDialog(
                    title = "Delete Savings Goal?",
                    message = "Remove \"${target.name}\" and its saved history?",
                    onDismiss = { deletingGoal = null },
                    onConfirm = {
                        viewModel.deleteSavingsGoal(target)
                        deletingGoal = null
                    }
                )
            }

            depositingGoal?.let { target ->
                DepositToGoalDialog(
                    goal = target,
                    onDismiss = { depositingGoal = null },
                    onDeposit = { amount, note ->
                        viewModel.addMoneyToGoal(target, amount, note)
                        depositingGoal = null
                    }
                )
            }
        }
    }
}
