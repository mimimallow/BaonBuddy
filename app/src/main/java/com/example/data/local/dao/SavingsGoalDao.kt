package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.SavingsContributionEntity
import com.example.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_contributions ORDER BY dateMillis DESC")
    fun getAllContributions(): Flow<List<SavingsContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_contributions WHERE goalId = :goalId")
    suspend fun deleteContributionsForGoal(goalId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: SavingsContributionEntity): Long

    @Query("UPDATE savings_goals SET savedAmount = savedAmount + :amount WHERE id = :goalId")
    suspend fun incrementGoalSavedAmount(goalId: Long, amount: Double)

    @Transaction
    suspend fun addContributionToGoal(goalId: Long, amount: Double, note: String, dateMillis: Long) {
        insertContribution(
            SavingsContributionEntity(
                goalId = goalId,
                amount = amount,
                note = note,
                dateMillis = dateMillis
            )
        )
        incrementGoalSavedAmount(goalId, amount)
    }

    @Transaction
    suspend fun deleteGoalWithContributions(goal: SavingsGoalEntity) {
        deleteContributionsForGoal(goal.id)
        deleteGoal(goal)
    }

    @Query("DELETE FROM savings_contributions")
    suspend fun deleteAllContributions()

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAllGoals()

    @Transaction
    suspend fun clearAllSavings() {
        deleteAllContributions()
        deleteAllGoals()
    }
}
