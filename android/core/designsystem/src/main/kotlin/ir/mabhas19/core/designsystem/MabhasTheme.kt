package ir.mabhas19.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColors = lightColorScheme(
    primary = Color(0xFF009B60),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8F5DF),
    onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFFB96C00),
    onSecondary = Color.White,
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFEEF2F6),
    onSurfaceVariant = Color(0xFF667085),
    outline = Color(0xFFCBD5E1)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF00DF81),
    onPrimary = Color(0xFF00281A),
    primaryContainer = Color(0xFF00513B),
    onPrimaryContainer = Color(0xFF85F8C6),
    secondary = Color(0xFFF2AA22),
    onSecondary = Color(0xFF2A1B00),
    background = Color(0xFF040507),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0D1216),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF151E24),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF2A3641)
)

@Composable
fun MabhasTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
    }
}
