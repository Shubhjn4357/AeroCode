package com.aerotech.aerocode.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aerotech.aerocode.ui.theme.AccentTheme
import com.aerotech.aerocode.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aero_settings")

data class EditorSettings(
    val fontSizeSp: Int = 14,
    val tabSize: Int = 4,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = false,
    val autoSave: Boolean = true,
    val autoSaveDelayMs: Long = 800L
)

data class BuildSettings(
    val buildVariant: String = "debug",
    val parallelBuild: Boolean = true,
    val offlineMode: Boolean = false
)

data class PreviewSettings(
    val defaultDevice: String = "Pixel 9",
    val previewScale: Float = 1.0f,
    val autoRefresh: Boolean = true
)

class AeroPreferences(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_THEME = stringPreferencesKey("accent_theme")
        val FONT_SIZE = intPreferencesKey("font_size")
        val TAB_SIZE = intPreferencesKey("tab_size")
        val SHOW_LINE_NUMBERS = booleanPreferencesKey("show_line_numbers")
        val WORD_WRAP = booleanPreferencesKey("word_wrap")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val BUILD_VARIANT = stringPreferencesKey("build_variant")
        val PARALLEL_BUILD = booleanPreferencesKey("parallel_build")
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val DEFAULT_DEVICE = stringPreferencesKey("default_device")
        val PREVIEW_SCALE = floatPreferencesKey("preview_scale")
        val AUTO_REFRESH = booleanPreferencesKey("auto_refresh")
        val RECOVERY_FILE = stringPreferencesKey("recovery_file")
        val RECOVERY_CONTENT = stringPreferencesKey("recovery_content")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        val name = it[Keys.THEME_MODE] ?: ThemeMode.DARK.name
        try { ThemeMode.valueOf(name) } catch (e: Exception) { ThemeMode.DARK }
    }

    val accentTheme: Flow<AccentTheme> = context.dataStore.data.map {
        val name = it[Keys.ACCENT_THEME] ?: AccentTheme.STUDIO_BLUE.name
        try { AccentTheme.valueOf(name) } catch (e: Exception) { AccentTheme.STUDIO_BLUE }
    }

    val editorSettings: Flow<EditorSettings> = context.dataStore.data.map {
        EditorSettings(
            fontSizeSp = it[Keys.FONT_SIZE] ?: 14,
            tabSize = it[Keys.TAB_SIZE] ?: 4,
            showLineNumbers = it[Keys.SHOW_LINE_NUMBERS] ?: true,
            wordWrap = it[Keys.WORD_WRAP] ?: false,
            autoSave = it[Keys.AUTO_SAVE] ?: true
        )
    }

    val buildSettings: Flow<BuildSettings> = context.dataStore.data.map {
        BuildSettings(
            buildVariant = it[Keys.BUILD_VARIANT] ?: "debug",
            parallelBuild = it[Keys.PARALLEL_BUILD] ?: true,
            offlineMode = it[Keys.OFFLINE_MODE] ?: false
        )
    }

    val previewSettings: Flow<PreviewSettings> = context.dataStore.data.map {
        PreviewSettings(
            defaultDevice = it[Keys.DEFAULT_DEVICE] ?: "Pixel 9",
            previewScale = it[Keys.PREVIEW_SCALE] ?: 1.0f,
            autoRefresh = it[Keys.AUTO_REFRESH] ?: true
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentTheme(accent: AccentTheme) {
        context.dataStore.edit { it[Keys.ACCENT_THEME] = accent.name }
    }

    suspend fun setEditorFontSize(size: Int) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = size }
    }

    suspend fun setTabSize(size: Int) {
        context.dataStore.edit { it[Keys.TAB_SIZE] = size }
    }

    suspend fun setShowLineNumbers(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_LINE_NUMBERS] = show }
    }

    suspend fun setWordWrap(wrap: Boolean) {
        context.dataStore.edit { it[Keys.WORD_WRAP] = wrap }
    }

    suspend fun setAutoSave(autoSave: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SAVE] = autoSave }
    }

    suspend fun setBuildVariant(variant: String) {
        context.dataStore.edit { it[Keys.BUILD_VARIANT] = variant }
    }

    suspend fun setPreviewScale(scale: Float) {
        context.dataStore.edit { it[Keys.PREVIEW_SCALE] = scale }
    }

    suspend fun recordCrashRecovery(filePath: String, content: String) {
        context.dataStore.edit {
            it[Keys.RECOVERY_FILE] = filePath
            it[Keys.RECOVERY_CONTENT] = content
        }
    }

    suspend fun clearCrashRecovery() {
        context.dataStore.edit {
            it.remove(Keys.RECOVERY_FILE)
            it.remove(Keys.RECOVERY_CONTENT)
        }
    }
}
