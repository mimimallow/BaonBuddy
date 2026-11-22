package com.example.domain

import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.model.ExpenseCategory
import com.example.data.preferences.StudentPreferences
import com.example.util.Formatters
import java.util.Calendar

enum class BudgetAlertLevel {
    NOT_SET,
    HEALTHY,
    APPROACHING,
    EXCEEDED
}

enum class HistoryPeriod(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    ALL("All Time")
}

data class CategoryBreakdownItem(
    val category: ExpenseCategory,
    val amount: Double,
    val percentage: Float
)

data class DailySpendPoint(
    val dayMillis: Long,
    val label: String,
    val amount: Double
)

data class BudgetSummary(
    val dailyAllowance: Double = 0.0,
    val weeklyAllowance: Double = 0.0,
    val monthlyAllowance: Double = 0.0,
    val todaySpent: Double = 0.0,
    val weekSpent: Double = 0.0,
    val monthSpent: Double = 0.0,
    val remainingToday: Double = 0.0,
    val remainingWeek: Double = 0.0,
    val remainingMonth: Double = 0.0,
    val dailyUsageFraction: Float = 0f,
    val dailyUsagePercent: Int = 0,
    val alertLevel: BudgetAlertLevel = BudgetAlertLevel.NOT_SET,
    val totalSaved: Double = 0.0,
    val totalGoalTarget: Double = 0.0,
    val ongoingGoalsCount: Int = 0,
    val completedGoalsCount: Int = 0
)

object BudgetCalculator {

    fun calculateSummary(
        preferences: StudentPreferences,
        expenses: List<ExpenseEntity>,
        goals: List<SavingsGoalEntity>,
        nowMillis: Long = System.currentTimeMillis()
    ): BudgetSummary {
        val startToday = Formatters.startOfDayMillis(nowMillis)
        val endToday = Formatters.endOfDayMillis(nowMillis)
        val startWeek = Formatters.startOfWeekMillis(nowMillis)
        val startMonth = Formatters.startOfMonthMillis(nowMillis)

        val todaySpent = expenses
            .filter { it.dateMillis in startToday..endToday }
            .sumOf { it.amount }

        val weekSpent = expenses
            .filter { it.dateMillis in startWeek..endToday }
            .sumOf { it.amount }

        val monthSpent = expenses
            .filter { it.dateMillis in startMonth..endToday }
            .sumOf { it.amount }

        val dailyAllowance = preferences.dailyAllowance
        val weeklyAllowance = dailyAllowance * preferences.allowanceDaysPerWeek
        val monthlyMultiplier = if (preferences.allowanceDaysPerWeek == 5) 22 else 30
        val monthlyAllowance = dailyAllowance * monthlyMultiplier

        val remainingToday = dailyAllowance - todaySpent
        val remainingWeek = weeklyAllowance - weekSpent
        val remainingMonth = monthlyAllowance - monthSpent

        val usageFraction = if (dailyAllowance > 0.0) {
            (todaySpent / dailyAllowance).toFloat()
        } else {
            0f
        }
        val usagePercent = if (dailyAllowance > 0.0) {
            ((todaySpent / dailyAllowance) * 100.0).toInt()
        } else {
            0
        }

        val alertLevel = when {
            dailyAllowance <= 0.0 -> BudgetAlertLevel.NOT_SET
            todaySpent > dailyAllowance -> BudgetAlertLevel.EXCEEDED
            usagePercent >= preferences.warningThresholdPercent -> BudgetAlertLevel.APPROACHING
            else -> BudgetAlertLevel.HEALTHY
        }

        val totalSaved = goals.sumOf { it.savedAmount }
        val totalTarget = goals.sumOf { it.targetAmount }
        val completedCount = goals.count { it.isCompleted }
        val ongoingCount = goals.size - completedCount

        return BudgetSummary(
            dailyAllowance = dailyAllowance,
            weeklyAllowance = weeklyAllowance,
            monthlyAllowance = monthlyAllowance,
            todaySpent = todaySpent,
            weekSpent = weekSpent,
            monthSpent = monthSpent,
            remainingToday = remainingToday,
            remainingWeek = remainingWeek,
            remainingMonth = remainingMonth,
            dailyUsageFraction = usageFraction.coerceIn(0f, 1f),
            dailyUsagePercent = usagePercent,
            alertLevel = alertLevel,
            totalSaved = totalSaved,
            totalGoalTarget = totalTarget,
            ongoingGoalsCount = ongoingCount,
            completedGoalsCount = completedCount
        )
    }

    fun filterExpensesByPeriod(
        expenses: List<ExpenseEntity>,
        period: HistoryPeriod,
        nowMillis: Long = System.currentTimeMillis()
    ): List<ExpenseEntity> {
        val endToday = Formatters.endOfDayMillis(nowMillis)
        return when (period) {
            HistoryPeriod.DAILY -> {
                val startToday = Formatters.startOfDayMillis(nowMillis)
                expenses.filter { it.dateMillis in startToday..endToday }
            }
            HistoryPeriod.WEEKLY -> {
                val startWeek = Formatters.startOfWeekMillis(nowMillis)
                expenses.filter { it.dateMillis in startWeek..endToday }
            }
            HistoryPeriod.MONTHLY -> {
                val startMonth = Formatters.startOfMonthMillis(nowMillis)
                expenses.filter { it.dateMillis in startMonth..endToday }
            }
            HistoryPeriod.ALL -> expenses
        }
    }

    fun calculateCategoryBreakdown(expenses: List<ExpenseEntity>): List<CategoryBreakdownItem> {
        val total = expenses.sumOf { it.amount }
        if (total <= 0.0) return emptyList()
        return ExpenseCategory.entries
            .map { category ->
                val catSum = expenses.filter { it.category == category }.sumOf { it.amount }
                CategoryBreakdownItem(
                    category = category,
                    amount = catSum,
                    percentage = (catSum / total).toFloat()
                )
            }
            .filter { it.amount > 0.0 }
            .sortedByDescending { it.amount }
    }

    fun calculateLast7DaysTrend(
        expenses: List<ExpenseEntity>,
        nowMillis: Long = System.currentTimeMillis()
    ): List<DailySpendPoint> {
        val points = mutableListOf<DailySpendPoint>()
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        for (daysAgo in 6 downTo 0) {
            val dayCal = (cal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }
            val start = Formatters.startOfDayMillis(dayCal.timeInMillis)
            val end = Formatters.endOfDayMillis(dayCal.timeInMillis)
            val sum = expenses.filter { it.dateMillis in start..end }.sumOf { it.amount }
            points.add(
                DailySpendPoint(
                    dayMillis = start,
                    label = Formatters.formatDayLabel(start),
                    amount = sum
                )
            )
        }
        return points
    }
}
