package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.BudgetAlertLevel
import com.example.ui.BaonUiState
import com.example.ui.theme.DangerRose
import com.example.ui.theme.DangerRoseContainer
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.util.Formatters
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AllowanceSetupScreen(
    uiState: BaonUiState,
    onBack: () -> Unit,
    onSaveSettings: (dailyAllowance: Double, daysPerWeek: Int, warningPercent: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val prefs = uiState.preferences
    val summary = uiState.budgetSummary

    var allowanceText by remember(prefs.dailyAllowance) {
        mutableStateOf(
            if (prefs.dailyAllowance > 0.0) {
                if (prefs.dailyAllowance % 1.0 == 0.0) prefs.dailyAllowance.toLong().toString()
                else prefs.dailyAllowance.toString()
            } else ""
        )
    }
    var daysPerWeek by remember(prefs.allowanceDaysPerWeek) {
        mutableIntStateOf(prefs.allowanceDaysPerWeek)
    }
    var warningThreshold by remember(prefs.warningThresholdPercent) {
        mutableFloatStateOf(prefs.warningThresholdPercent.toFloat())
    }
    var amountError by remember { mutableStateOf<String?>(null) }

    val previewDaily = allowanceText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: prefs.dailyAllowance
    val previewWeekly = previewDaily * daysPerWeek
    val previewMonthly = previewDaily * (if (daysPerWeek == 5) 22 else 30)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Daily Allowance Setup") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("allowance_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Budget Status Alert
            if (summary.alertLevel == BudgetAlertLevel.APPROACHING || summary.alertLevel == BudgetAlertLevel.EXCEEDED) {
                item {
                    val isExceeded = summary.alertLevel == BudgetAlertLevel.EXCEEDED
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExceeded) DangerRoseContainer else WarningAmberContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = if (isExceeded) DangerRose else WarningAmber
                            )
                            Text(
                                text = if (isExceeded) {
                                    "Heads up: Today's spending (${Formatters.formatPeso(summary.todaySpent)}) has exceeded your ${Formatters.formatPeso(summary.dailyAllowance)} daily allowance."
                                } else {
                                    "Heads up: You've spent ${summary.dailyUsagePercent}% of today's allowance."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1F2937)
                            )
                        }
                    }
                }
            }

            // Input & Configuration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Set Your Daily Baon",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Update your allowance anytime. Remaining balances adjust automatically against your recorded expenses.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = allowanceText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() || it == '.' }
                                if (filtered.count { it == '.' } <= 1) {
                                    allowanceText = filtered
                                    amountError = null
                                }
                            },
                            label = { Text("Daily Allowance (₱)") },
                            prefix = { Text("₱ ") },
                            placeholder = { Text("e.g., 200") },
                            singleLine = true,
                            isError = amountError != null,
                            supportingText = amountError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("allowance_amount_input")
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(100, 150, 200, 300, 500).forEach { preset ->
                                SuggestionChip(
                                    onClick = {
                                        allowanceText = preset.toString()
                                        amountError = null
                                    },
                                    label = { Text("₱$preset") }
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Allowance Schedule",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilterChip(
                                    selected = daysPerWeek == 5,
                                    onClick = { daysPerWeek = 5 },
                                    label = { Text("5 School Days/wk") },
                                    modifier = Modifier.testTag("schedule_5_days")
                                )
                                FilterChip(
                                    selected = daysPerWeek == 7,
                                    onClick = { daysPerWeek = 7 },
                                    label = { Text("7 Days/wk") },
                                    modifier = Modifier.testTag("schedule_7_days")
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Budget Warning Alert",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "At ${warningThreshold.roundToInt()}% spent",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = warningThreshold,
                                onValueChange = { warningThreshold = it },
                                valueRange = 50f..95f,
                                steps = 8
                            )
                        }

                        Button(
                            onClick = {
                                val parsed = allowanceText.toDoubleOrNull()
                                if (parsed == null || parsed <= 0.0) {
                                    amountError = "Enter a valid daily allowance > 0"
                                } else {
                                    onSaveSettings(
                                        parsed,
                                        daysPerWeek,
                                        warningThreshold.roundToInt()
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_allowance_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Allowance Settings")
                        }
                    }
                }
            }

            // Live Daily, Weekly & Monthly Budget Overview
            item {
                Text(
                    text = "Budget Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                BudgetPeriodOverviewCard(
                    title = "Daily Budget",
                    subtitle = "Today's limit & spending",
                    budget = previewDaily,
                    spent = summary.todaySpent
                )
            }

            item {
                BudgetPeriodOverviewCard(
                    title = "Weekly Budget",
                    subtitle = "$daysPerWeek days × ${Formatters.formatPesoCompact(previewDaily)}",
                    budget = previewWeekly,
                    spent = summary.weekSpent
                )
            }

            item {
                val monthDays = if (daysPerWeek == 5) 22 else 30
                BudgetPeriodOverviewCard(
                    title = "Monthly Budget",
                    subtitle = "$monthDays days × ${Formatters.formatPesoCompact(previewDaily)}",
                    budget = previewMonthly,
                    spent = summary.monthSpent
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun BudgetPeriodOverviewCard(
    title: String,
    subtitle: String,
    budget: Double,
    spent: Double
) {
    val remaining = budget - spent
    val fraction = if (budget > 0.0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val isOver = budget > 0.0 && spent > budget

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = Formatters.formatPeso(budget),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${Formatters.formatPeso(spent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (remaining >= 0) {
                        "Remaining: ${Formatters.formatPeso(remaining)}"
                    } else {
                        "Over by: ${Formatters.formatPeso(kotlin.math.abs(remaining))}"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
