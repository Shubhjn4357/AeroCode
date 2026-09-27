package com.aerotech.aerocode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.aerotech.aerocode.navigation.AeroNavigationApp
import com.aerotech.aerocode.ui.theme.AccentTheme
import com.aerotech.aerocode.ui.theme.AeroCodeTheme
import com.aerotech.aerocode.ui.theme.AeroTheme
import com.aerotech.aerocode.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = AeroCodeApplication.instance

        setContent {
            val themeMode by app.preferences.themeMode.collectAsState(initial = ThemeMode.DARK)
            val accentTheme by app.preferences.accentTheme.collectAsState(initial = AccentTheme.CYAN)

            AeroCodeTheme(
                themeMode = themeMode,
                accent = accentTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AeroTheme.colors.background
                ) {
                    AeroNavigationApp()
                }
            }
        }
    }
}
