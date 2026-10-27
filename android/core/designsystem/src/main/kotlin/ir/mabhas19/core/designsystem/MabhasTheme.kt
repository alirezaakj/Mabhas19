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
    primary = Color(0xFF006B4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8F5DF),
    onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFF785900),
    background = Color(0xFFF5F7FA),
    surface = Color(0xFFF5F7FA)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF66DBAA),
    onPrimary = Color(0xFF003827),
    primaryContainer = Color(0xFF00513B),
    onPrimaryContainer = Color(0xFF85F8C6),
    secondary = Color(0xFFF2C15C),
    background = Color(0xFF0D1216),
    surface = Color(0xFF11181D)
)

@Composable
fun MabhasTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
    }
}
