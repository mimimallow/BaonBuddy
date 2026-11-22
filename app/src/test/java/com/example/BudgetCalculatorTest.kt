package com.example

import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.model.ExpenseCategory
import com.example.data.preferences.StudentPreferences
import com.example.domain.BudgetAlertLevel
import com.example.domain.BudgetCalculator
import com.example.domain.HistoryPeriod
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetCalculatorTest {

    @Test
    fun `calculateSummary computes balances and warning thresholds accurately`() {
        val now = 1_763_800_000_000L
        val prefs = StudentPreferences(
            dailyAllowance = 200.0,
            allowanceDaysPerWeek = 5,
            warningThresholdPercent = 80
        )
        val expenses = listOf(
            ExpenseEntity(id = 1, title = "Jeepney", amount = 26.0, category = ExpenseCategory.TRANSPORT, dateMillis = now),
            ExpenseEntity(id = 2, title = "Lunch", amount = 140.0, category = ExpenseCategory.FOOD, dateMillis = now)
        )
        val goals = listOf(
            SavingsGoalEntity(id = 1, name = "Books", targetAmount = 400.0, savedAmount = 150.0, targetDateMillis = now + 86_400_000L)
        )

        val summary = BudgetCalculator.calculateSummary(prefs, expenses, goals, nowMillis = now)
        assertEquals(200.0, summary.dailyAllowance, 0.001)
        assertEquals(1000.0, summary.weeklyAllowance, 0.001)
        assertEquals(4400.0, summary.monthlyAllowance, 0.001)
        assertEquals(166.0, summary.todaySpent, 0.001)
        assertEquals(34.0, summary.remainingToday, 0.001)
        assertEquals(83, summary.dailyUsagePercent)
        assertEquals(BudgetAlertLevel.APPROACHING, summary.alertLevel)
        assertEquals(150.0, summary.totalSaved, 0.001)
        assertEquals(1, summary.ongoingGoalsCount)
        assertEquals(0, summary.completedGoalsCount)
    }

    @Test
    fun `calculateSummary marks EXCEEDED when spending surpasses daily allowance`() {
        val now = 1_763_800_000_000L
        val prefs = StudentPreferences(dailyAllowance = 150.0, warningThresholdPercent = 80)
        val expenses = listOf(
            ExpenseEntity(id = 1, title = "Supplies", amount = 180.0, category = ExpenseCategory.SUPPLIES, dateMillis = now)
        )

        val summary = BudgetCalculator.calculateSummary(prefs, expenses, emptyList(), nowMillis = now)
        assertEquals(-30.0, summary.remainingToday, 0.001)
        assertEquals(BudgetAlertLevel.EXCEEDED, summary.alertLevel)
    }

    @Test
    fun `calculateCategoryBreakdown sorts descending by amount`() {
        val now = 1_763_800_000_000L
        val expenses = listOf(
            ExpenseEntity(id = 1, title = "Snacks", amount = 50.0, category = ExpenseCategory.FOOD, dateMillis = now),
            ExpenseEntity(id = 2, title = "Print", amount = 150.0, category = ExpenseCategory.SUPPLIES, dateMillis = now)
        )
        val breakdown = BudgetCalculator.calculateCategoryBreakdown(
            BudgetCalculator.filterExpensesByPeriod(expenses, HistoryPeriod.DAILY, now)
        )
        assertEquals(2, breakdown.size)
        assertEquals(ExpenseCategory.SUPPLIES, breakdown[0].category)
        assertEquals(0.75f, breakdown[0].percentage, 0.001f)
        assertEquals(ExpenseCategory.FOOD, breakdown[1].category)
        assertEquals(0.25f, breakdown[1].percentage, 0.001f)
    }
}
