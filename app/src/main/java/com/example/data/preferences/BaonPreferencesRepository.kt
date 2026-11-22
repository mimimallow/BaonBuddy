package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.baonDataStore: DataStore<Preferences> by preferencesDataStore(name = "baon_preferences")

data class StudentPreferences(
    val studentName: String = "Student",
    val schoolLevel: String = "College Student",
    val dailyAllowance: Double = 0.0,
    val allowanceDaysPerWeek: Int = 5,
    val warningThresholdPercent: Int = 80,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val tipOffset: Int = 0,
    val bookmarkedTipIds: Set<String> = emptySet()
)

class BaonPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.applicationContext.baonDataStore)

    private object Keys {
        val STUDENT_NAME = stringPreferencesKey("student_name")
        val SCHOOL_LEVEL = stringPreferencesKey("school_level")
        val DAILY_ALLOWANCE = doublePreferencesKey("daily_allowance")
        val ALLOWANCE_DAYS_PER_WEEK = intPreferencesKey("allowance_days_per_week")
        val WARNING_THRESHOLD_PERCENT = intPreferencesKey("warning_threshold_percent")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val TIP_OFFSET = intPreferencesKey("tip_offset")
        val BOOKMARKED_TIPS = stringSetPreferencesKey("bookmarked_tips")
    }

    val preferencesFlow: Flow<StudentPreferences> = dataStore.data.map { prefs ->
        val themeName = prefs[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val themeMode = AppThemeMode.entries.firstOrNull { it.name == themeName } ?: AppThemeMode.SYSTEM
        StudentPreferences(
            studentName = prefs[Keys.STUDENT_NAME]?.takeIf { it.isNotBlank() } ?: "Student",
            schoolLevel = prefs[Keys.SCHOOL_LEVEL]?.takeIf { it.isNotBlank() } ?: "College Student",
            dailyAllowance = (prefs[Keys.DAILY_ALLOWANCE] ?: 0.0).coerceAtLeast(0.0),
            allowanceDaysPerWeek = (prefs[Keys.ALLOWANCE_DAYS_PER_WEEK] ?: 5).let {
                if (it == 7) 7 else 5
            },
            warningThresholdPercent = (prefs[Keys.WARNING_THRESHOLD_PERCENT] ?: 80).coerceIn(50, 95),
            themeMode = themeMode,
            tipOffset = prefs[Keys.TIP_OFFSET] ?: 0,
            bookmarkedTipIds = prefs[Keys.BOOKMARKED_TIPS] ?: emptySet()
        )
    }

    suspend fun updateProfile(name: String, schoolLevel: String) {
        dataStore.edit { prefs ->
            prefs[Keys.STUDENT_NAME] = name.trim().ifBlank { "Student" }
            prefs[Keys.SCHOOL_LEVEL] = schoolLevel.trim().ifBlank { "Student" }
        }
    }

    suspend fun updateAllowanceSettings(
        dailyAllowance: Double,
        allowanceDaysPerWeek: Int,
        warningThresholdPercent: Int
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.DAILY_ALLOWANCE] = dailyAllowance.coerceAtLeast(0.0)
            prefs[Keys.ALLOWANCE_DAYS_PER_WEEK] = if (allowanceDaysPerWeek == 7) 7 else 5
            prefs[Keys.WARNING_THRESHOLD_PERCENT] = warningThresholdPercent.coerceIn(50, 95)
        }
    }

    suspend fun updateThemeMode(themeMode: AppThemeMode) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun advanceTipOffset() {
        dataStore.edit { prefs ->
            val current = prefs[Keys.TIP_OFFSET] ?: 0
            prefs[Keys.TIP_OFFSET] = current + 1
        }
    }

    suspend fun toggleTipBookmark(tipId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.BOOKMARKED_TIPS] ?: emptySet()
            prefs[Keys.BOOKMARKED_TIPS] = if (tipId in current) {
                current - tipId
            } else {
                current + tipId
            }
        }
    }

    suspend fun resetAllPreferences() {
        dataStore.edit { it.clear() }
    }
}
