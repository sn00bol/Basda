package com.sn00bol.basda.ui.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

object SettingsManager {
    private const val PREFS_NAME = "basda_settings"
    
    private const val KEY_TRASH_ENABLED = "trash_enabled"
    private const val KEY_TRASH_DELETE_DAYS = "trash_delete_days"
    private const val KEY_SHOW_HIDDEN_FILES = "show_hidden_files"
    private const val KEY_USE_GLOBAL_GRID = "use_global_grid"
    private const val KEY_GRID_COLUMNS = "grid_columns"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val PREFIX_VIEW_MODE = "view_mode_"

    private lateinit var prefs: SharedPreferences

    private val _isTrashEnabled = mutableStateOf(true)
    var isTrashEnabled: Boolean
        get() = _isTrashEnabled.value
        set(value) {
            _isTrashEnabled.value = value
            prefs.edit().putBoolean(KEY_TRASH_ENABLED, value).apply()
        }

    private val _trashDeleteDays = mutableIntStateOf(30)
    var trashDeleteDays: Int
        get() = _trashDeleteDays.value
        set(value) {
            _trashDeleteDays.value = value
            prefs.edit().putInt(KEY_TRASH_DELETE_DAYS, value).apply()
        }

    private val _showHiddenFiles = mutableStateOf(false)
    var showHiddenFiles: Boolean
        get() = _showHiddenFiles.value
        set(value) {
            _showHiddenFiles.value = value
            prefs.edit().putBoolean(KEY_SHOW_HIDDEN_FILES, value).apply()
        }

    private val _useGlobalGrid = mutableStateOf(false)
    var useGlobalGrid: Boolean
        get() = _useGlobalGrid.value
        set(value) {
            _useGlobalGrid.value = value
            prefs.edit().putBoolean(KEY_USE_GLOBAL_GRID, value).apply()
        }

    private val _gridColumns = mutableIntStateOf(3)
    var gridColumns: Int
        get() = _gridColumns.value
        set(value) {
            _gridColumns.value = value
            prefs.edit().putInt(KEY_GRID_COLUMNS, value).apply()
        }

    private val _appTheme = mutableStateOf(AppTheme.SYSTEM)
    var appTheme: AppTheme
        get() = _appTheme.value
        set(value) {
            _appTheme.value = value
            prefs.edit().putString(KEY_THEME_MODE, value.name).apply()
        }

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isTrashEnabled.value = prefs.getBoolean(KEY_TRASH_ENABLED, true)
        _trashDeleteDays.value = prefs.getInt(KEY_TRASH_DELETE_DAYS, 30)
        _showHiddenFiles.value = prefs.getBoolean(KEY_SHOW_HIDDEN_FILES, false)
        _useGlobalGrid.value = prefs.getBoolean(KEY_USE_GLOBAL_GRID, false)
        _gridColumns.value = prefs.getInt(KEY_GRID_COLUMNS, 3)
        
        val themeName = prefs.getString(KEY_THEME_MODE, AppTheme.SYSTEM.name)
        _appTheme.value = try { AppTheme.valueOf(themeName ?: AppTheme.SYSTEM.name) } catch(e: Exception) { AppTheme.SYSTEM }
    }

    fun getViewMode(key: String): Boolean {
        if (useGlobalGrid) return true
        return prefs.getBoolean(PREFIX_VIEW_MODE + key, false)
    }

    fun setViewMode(key: String, isGrid: Boolean) {
        prefs.edit().putBoolean(PREFIX_VIEW_MODE + key, isGrid).apply()
    }
    
    // Check if grid is effectively enabled for a key
    fun isGridEnabled(key: String): Boolean {
        return useGlobalGrid || getViewMode(key)
    }
}
