package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BaonDatabase
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.model.ExpenseCategory
import com.example.data.repository.BaonRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: BaonDatabase
    private lateinit var repository: BaonRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BaonDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BaonRepository(database.expenseDao(), database.savingsGoalDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `app name matches BaonBuddy`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BaonBuddy", appName)
    }

    @Test
    fun `expense CRUD persists and updates reactively`() = runTest {
        val id = repository.addExpense(
            ExpenseEntity(
                title = "Canteen Lunch",
                amount = 85.0,
                category = ExpenseCategory.FOOD,
                dateMillis = System.currentTimeMillis(),
                notes = "Rice and ulam"
            )
        )
        assertTrue(id > 0)

        var list = repository.allExpenses.first()
        assertEquals(1, list.size)
        assertEquals("Canteen Lunch", list.first().title)

        repository.updateExpense(list.first().copy(amount = 95.0))
        list = repository.allExpenses.first()
        assertEquals(95.0, list.first().amount, 0.001)

        repository.deleteExpense(list.first())
        list = repository.allExpenses.first()
        assertTrue(list.isEmpty())
    }

    @Test
    fun `savings goal deposit increments savedAmount and completes goal`() = runTest {
        val goalId = repository.addSavingsGoal(
            SavingsGoalEntity(
                name = "Scientific Calculator",
                targetAmount = 500.0,
                savedAmount = 0.0,
                targetDateMillis = System.currentTimeMillis() + 86_400_000L * 14
            )
        )

        repository.addMoneyToGoal(goalId, 200.0, "Leftover baon")
        var goal = repository.allGoals.first().first()
        assertEquals(200.0, goal.savedAmount, 0.001)
        assertEquals(40, goal.progressPercent)
        assertTrue(!goal.isCompleted)

        repository.addMoneyToGoal(goalId, 300.0, "Weekend savings")
        goal = repository.allGoals.first().first()
        assertEquals(500.0, goal.savedAmount, 0.001)
        assertEquals(100, goal.progressPercent)
        assertTrue(goal.isCompleted)
    }
}
