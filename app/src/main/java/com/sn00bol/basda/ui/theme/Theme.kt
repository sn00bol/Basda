package com.sn00bol.basda.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sn00bol.basda.ui.utils.AppTheme
import com.sn00bol.basda.ui.utils.SettingsManager

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1A73E8).copy(alpha = 0.3f), // Xanh dương nhẹ nhàng hơn cho Dark mode
    onPrimaryContainer = Color.White,
    secondary = BlueSecondaryDark,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1A73E8).copy(alpha = 0.2f),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF63FFFF),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF444444),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = BluePrimary,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    tertiary = BlueTertiary,
    onTertiary = Color.White,
    surfaceVariant = Color(0xFFE3F2FD),
    onSurfaceVariant = BluePrimary,
    background = MainMenuBackground,
    surface = LightSurface,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun BasdaTheme(
    darkTheme: Boolean = when(SettingsManager.appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    },
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            var activity = context
            while (activity is android.content.ContextWrapper && activity !is android.app.Activity) {
                activity = activity.baseContext
            }
            
            if (activity is android.app.Activity) {
                val window = activity.window
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
