package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val targetDateMillis: Long,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isCompleted: Boolean
        get() = targetAmount > 0 && savedAmount >= targetAmount

    val remainingAmount: Double
        get() = (targetAmount - savedAmount).coerceAtLeast(0.0)

    val progressFraction: Float
        get() = if (targetAmount > 0.0) (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = if (targetAmount > 0.0) ((savedAmount / targetAmount) * 100).toInt().coerceIn(0, 100) else 0
}
