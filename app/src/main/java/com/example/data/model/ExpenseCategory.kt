package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CategoryFoodColor
import com.example.ui.theme.CategoryOtherColor
import com.example.ui.theme.CategorySuppliesColor
import com.example.ui.theme.CategoryTransportColor

enum class ExpenseCategory(
    val displayName: String,
    val shortName: String,
    val icon: ImageVector,
    val accentColor: Color,
    val quickExamples: List<String>
) {
    FOOD(
        displayName = "Food",
        shortName = "Food",
        icon = Icons.Rounded.Restaurant,
        accentColor = CategoryFoodColor,
        quickExamples = listOf("Lunch", "Snacks", "Water / Drinks", "Breakfast")
    ),
    TRANSPORT(
        displayName = "Fare / Transportation",
        shortName = "Fare",
        icon = Icons.Rounded.DirectionsBus,
        accentColor = CategoryTransportColor,
        quickExamples = listOf("Jeepney Fare", "Tricycle", "Train / MRT", "Bus Fare")
    ),
    SUPPLIES(
        displayName = "School Supplies",
        shortName = "Supplies",
        icon = Icons.Rounded.MenuBook,
        accentColor = CategorySuppliesColor,
        quickExamples = listOf("Printing / Photocopy", "Notebooks", "Ballpen", "Project Materials")
    ),
    OTHER(
        displayName = "Other",
        shortName = "Other",
        icon = Icons.Rounded.Category,
        accentColor = CategoryOtherColor,
        quickExamples = listOf("Mobile Load", "Club Fee", "Personal Care", "Emergency")
    )
}
