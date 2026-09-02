package com.sn00bol.basda.ui.utils

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "basda_settings"
    
    private const val KEY_TRASH_ENABLED = "trash_enabled"
    private const val KEY_TRASH_DELETE_DAYS = "trash_delete_days"
    private const val KEY_SHOW_HIDDEN_FILES = "show_hidden_files"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var isTrashEnabled: Boolean
        get() = prefs.getBoolean(KEY_TRASH_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_TRASH_ENABLED, value).apply()

    var trashDeleteDays: Int
        get() = prefs.getInt(KEY_TRASH_DELETE_DAYS, 30)
        set(value) = prefs.edit().putInt(KEY_TRASH_DELETE_DAYS, value).apply()

    var showHiddenFiles: Boolean
        get() = prefs.getBoolean(KEY_SHOW_HIDDEN_FILES, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_HIDDEN_FILES, value).apply()
}
