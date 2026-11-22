package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.BaonDatabase
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.SavingsContributionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.SavingTip
import com.example.data.model.SavingTipsCatalog
import com.example.data.preferences.BaonPreferencesRepository
import com.example.data.preferences.StudentPreferences
import com.example.data.repository.BaonRepository
import com.example.domain.BudgetCalculator
import com.example.domain.BudgetSummary
import com.example.domain.CategoryBreakdownItem
import com.example.domain.DailySpendPoint
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class BaonUiState(
    val isLoading: Boolean = true,
    val preferences: StudentPreferences = StudentPreferences(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val goals: List<SavingsGoalEntity> = emptyList(),
    val contributions: List<SavingsContributionEntity> = emptyList(),
    val budgetSummary: BudgetSummary = BudgetSummary(),
    val todayBreakdown: List<CategoryBreakdownItem> = emptyList(),
    val last7DaysTrend: List<DailySpendPoint> = emptyList(),
    val featuredTip: SavingTip = SavingTipsCatalog.tips.first()
)

class BaonViewModel(
    private val repository: BaonRepository,
    private val preferencesRepository: BaonPreferencesRepository
) : ViewModel() {

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val uiState: StateFlow<BaonUiState> = combine(
        preferencesRepository.preferencesFlow,
        repository.allExpenses,
        repository.allGoals,
        repository.allContributions
    ) { prefs, expenses, goals, contributions ->
        val now = System.currentTimeMillis()
        val summary = BudgetCalculator.calculateSummary(
            preferences = prefs,
            expenses = expenses,
            goals = goals,
            nowMillis = now
        )
        val todayExpenses = BudgetCalculator.filterExpensesByPeriod(
            expenses = expenses,
            period = com.example.domain.HistoryPeriod.DAILY,
            nowMillis = now
        )
        val breakdownSource = if (todayExpenses.isNotEmpty()) {
            todayExpenses
        } else {
            BudgetCalculator.filterExpensesByPeriod(
                expenses = expenses,
                period = com.example.domain.HistoryPeriod.WEEKLY,
                nowMillis = now
            )
        }
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val tipIndex = (dayOfYear + prefs.tipOffset) % SavingTipsCatalog.tips.size

        BaonUiState(
            isLoading = false,
            preferences = prefs,
            expenses = expenses,
            goals = goals,
            contributions = contributions,
            budgetSummary = summary,
            todayBreakdown = BudgetCalculator.calculateCategoryBreakdown(breakdownSource),
            last7DaysTrend = BudgetCalculator.calculateLast7DaysTrend(expenses, now),
            featuredTip = SavingTipsCatalog.tips[tipIndex]
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BaonUiState()
    )

    fun saveAllowanceSettings(
        dailyAllowance: Double,
        daysPerWeek: Int,
        warningThresholdPercent: Int
    ) {
        if (dailyAllowance < 0.0) {
            _messages.tryEmit("Allowance cannot be negative.")
            return
        }
        viewModelScope.launch {
            preferencesRepository.updateAllowanceSettings(
                dailyAllowance = dailyAllowance,
                allowanceDaysPerWeek = daysPerWeek,
                warningThresholdPercent = warningThresholdPercent
            )
            _messages.tryEmit("Daily allowance updated")
        }
    }

    fun saveExpense(
        existingId: Long? = null,
        title: String,
        amount: Double,
        category: ExpenseCategory,
        dateMillis: Long,
        notes: String
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty() || amount <= 0.0) {
            _messages.tryEmit("Please enter a valid expense name and amount.")
            return
        }
        viewModelScope.launch {
            if (existingId != null && existingId > 0L) {
                val existing = uiState.value.expenses.firstOrNull { it.id == existingId }
                repository.updateExpense(
                    ExpenseEntity(
                        id = existingId,
                        title = cleanTitle,
                        amount = amount,
                        category = category,
                        dateMillis = dateMillis,
                        notes = notes.trim(),
                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                    )
                )
                _messages.tryEmit("Expense updated")
            } else {
                repository.addExpense(
                    ExpenseEntity(
                        title = cleanTitle,
                        amount = amount,
                        category = category,
                        dateMillis = dateMillis,
                        notes = notes.trim()
                    )
                )
                _messages.tryEmit("Expense added")
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _messages.tryEmit("Expense deleted")
        }
    }

    fun saveSavingsGoal(
        existingGoal: SavingsGoalEntity? = null,
        name: String,
        targetAmount: Double,
        targetDateMillis: Long
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty() || targetAmount <= 0.0) {
            _messages.tryEmit("Please enter a valid goal name and target amount.")
            return
        }
        viewModelScope.launch {
            if (existingGoal != null) {
                repository.updateSavingsGoal(
                    existingGoal.copy(
                        name = cleanName,
                        targetAmount = targetAmount,
                        targetDateMillis = targetDateMillis
                    )
                )
                _messages.tryEmit("Savings goal updated")
            } else {
                repository.addSavingsGoal(
                    SavingsGoalEntity(
                        name = cleanName,
                        targetAmount = targetAmount,
                        savedAmount = 0.0,
                        targetDateMillis = targetDateMillis
                    )
                )
                _messages.tryEmit("Savings goal created")
            }
        }
    }

    fun addMoneyToGoal(goal: SavingsGoalEntity, amount: Double, note: String = "") {
        if (amount <= 0.0) {
            _messages.tryEmit("Deposit amount must be greater than zero.")
            return
        }
        viewModelScope.launch {
            repository.addMoneyToGoal(goalId = goal.id, amount = amount, note = note.trim())
            val newTotal = goal.savedAmount + amount
            if (newTotal >= goal.targetAmount && !goal.isCompleted) {
                _messages.tryEmit("Goal reached! Congratulations on completing ${goal.name}!")
            } else {
                _messages.tryEmit("Added to ${goal.name}")
            }
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
            _messages.tryEmit("Savings goal deleted")
        }
    }

    fun updateProfile(name: String, schoolLevel: String) {
        viewModelScope.launch {
            preferencesRepository.updateProfile(name, schoolLevel)
            _messages.tryEmit("Profile updated")
        }
    }

    fun updateThemeMode(themeMode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(themeMode)
        }
    }

    fun nextTip() {
        viewModelScope.launch {
            preferencesRepository.advanceTipOffset()
        }
    }

    fun toggleTipBookmark(tipId: String) {
        viewModelScope.launch {
            preferencesRepository.toggleTipBookmark(tipId)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllLocalRecords()
            preferencesRepository.resetAllPreferences()
            _messages.tryEmit("All local data has been reset")
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = BaonDatabase.getInstance(application)
                    val repo = BaonRepository(db.expenseDao(), db.savingsGoalDao())
                    val prefs = BaonPreferencesRepository(application)
                    return BaonViewModel(repo, prefs) as T
                }
            }
        }
    }
}
