package com.example.data.repository

import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.SavingsGoalDao
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.SavingsContributionEntity
import com.example.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

class BaonRepository(
    private val expenseDao: ExpenseDao,
    private val savingsGoalDao: SavingsGoalDao
) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()
    val allContributions: Flow<List<SavingsContributionEntity>> = savingsGoalDao.getAllContributions()

    suspend fun addExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun addSavingsGoal(goal: SavingsGoalEntity): Long {
        return savingsGoalDao.insertGoal(goal)
    }

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.updateGoal(goal)
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.deleteGoalWithContributions(goal)
    }

    suspend fun addMoneyToGoal(goalId: Long, amount: Double, note: String = "") {
        savingsGoalDao.addContributionToGoal(
            goalId = goalId,
            amount = amount,
            note = note,
            dateMillis = System.currentTimeMillis()
        )
    }

    suspend fun clearAllLocalRecords() {
        expenseDao.deleteAllExpenses()
        savingsGoalDao.clearAllSavings()
    }
}
