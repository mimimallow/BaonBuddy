package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ExpenseCategory

class Converters {
    @TypeConverter
    fun fromCategory(category: ExpenseCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): ExpenseCategory {
        return ExpenseCategory.entries.firstOrNull { it.name == value } ?: ExpenseCategory.OTHER
    }
}
