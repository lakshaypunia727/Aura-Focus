package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppThemeSetting
import com.example.data.model.DarkModeSetting

fun getThemeColorScheme(
    theme: AppThemeSetting,
    darkModeSetting: DarkModeSetting,
    systemInDark: Boolean
): Pair<ColorScheme, Boolean> {
    val isDark = when (darkModeSetting) {
        DarkModeSetting.SYSTEM -> systemInDark
        DarkModeSetting.LIGHT -> false
        DarkModeSetting.DARK -> true
        DarkModeSetting.OLED_BLACK -> true
    }

    val isOled = darkModeSetting == DarkModeSetting.OLED_BLACK

    val scheme = if (isDark) {
        when (theme) {
            AppThemeSetting.ZEN_FOREST -> darkColorScheme(
                primary = ZenForestPrimaryDark,
                secondary = ZenForestSecondary,
                tertiary = Color(0xFFA7F3D0),
                background = if (isOled) OledBlackBg else ZenForestBgDark,
                surface = if (isOled) OledBlackSurface else ZenForestSurfaceDark,
                surfaceVariant = if (isOled) OledBlackSurfaceVariant else Color(0xFF1E2D23),
                onPrimary = Color(0xFF062310),
                onBackground = Color(0xFFE8F5E9),
                onSurface = Color(0xFFE8F5E9),
                onSurfaceVariant = Color(0xFFB0C9B6)
            )
            AppThemeSetting.SUNSET_GLOW -> darkColorScheme(
                primary = SunsetPrimaryDark,
                secondary = SunsetSecondary,
                tertiary = Color(0xFFFB923C),
                background = if (isOled) OledBlackBg else SunsetBgDark,
                surface = if (isOled) OledBlackSurface else SunsetSurfaceDark,
                surfaceVariant = if (isOled) OledBlackSurfaceVariant else Color(0xFF321C20),
                onPrimary = Color(0xFF380709),
                onBackground = Color(0xFFFDE8E8),
                onSurface = Color(0xFFFDE8E8),
                onSurfaceVariant = Color(0xFFDCA8AA)
            )
            AppThemeSetting.MIDNIGHT_NEON -> darkColorScheme(
                primary = NeonPrimaryDark,
                secondary = NeonSecondary,
                tertiary = Color(0xFFE879F9),
                background = if (isOled) OledBlackBg else NeonBgDark,
                surface = if (isOled) OledBlackSurface else NeonSurfaceDark,
                surfaceVariant = if (isOled) OledBlackSurfaceVariant else Color(0xFF221F35),
                onPrimary = Color(0xFF23053D),
                onBackground = Color(0xFFF3E8FF),
                onSurface = Color(0xFFF3E8FF),
                onSurfaceVariant = Color(0xFFBEB2D6)
            )
            AppThemeSetting.SAKURA_ROSE -> darkColorScheme(
                primary = SakuraPrimaryDark,
                secondary = SakuraSecondary,
                tertiary = Color(0xFFFDA4AF),
                background = if (isOled) OledBlackBg else SakuraBgDark,
                surface = if (isOled) OledBlackSurface else SakuraSurfaceDark,
                surfaceVariant = if (isOled) OledBlackSurfaceVariant else Color(0xFF2D1B27),
                onPrimary = Color(0xFF3D0822),
                onBackground = Color(0xFFFDF2F8),
                onSurface = Color(0xFFFDF2F8),
                onSurfaceVariant = Color(0xFFD8B4C8)
            )
            AppThemeSetting.OCEAN_DEEP -> darkColorScheme(
                primary = OceanPrimaryDark,
                secondary = OceanSecondary,
                tertiary = Color(0xFF7DD3FC),
                background = if (isOled) OledBlackBg else OceanBgDark,
                surface = if (isOled) OledBlackSurface else OceanSurfaceDark,
                surfaceVariant = if (isOled) OledBlackSurfaceVariant else Color(0xFF17283A),
                onPrimary = Color(0xFF04243B),
                onBackground = Color(0xFFE0F2FE),
                onSurface = Color(0xFFE0F2FE),
                onSurfaceVariant = Color(0xFFA5C7DC)
            )
        }
    } else {
        when (theme) {
            AppThemeSetting.ZEN_FOREST -> lightColorScheme(
                primary = ZenForestPrimaryLight,
                secondary = ZenForestSecondary,
                background = ZenForestBgLight,
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFE6F3EA),
                onPrimary = Color.White,
                onBackground = Color(0xFF152219),
                onSurface = Color(0xFF152219),
                onSurfaceVariant = Color(0xFF385341)
            )
            AppThemeSetting.SUNSET_GLOW -> lightColorScheme(
                primary = SunsetPrimaryLight,
                secondary = SunsetSecondary,
                background = SunsetBgLight,
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFFCEBEB),
                onPrimary = Color.White,
                onBackground = Color(0xFF291516),
                onSurface = Color(0xFF291516),
                onSurfaceVariant = Color(0xFF6B3639)
            )
            AppThemeSetting.MIDNIGHT_NEON -> lightColorScheme(
                primary = NeonPrimaryLight,
                secondary = NeonSecondary,
                background = NeonBgLight,
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFECE7FA),
                onPrimary = Color.White,
                onBackground = Color(0xFF19162B),
                onSurface = Color(0xFF19162B),
                onSurfaceVariant = Color(0xFF4B426B)
            )
            AppThemeSetting.SAKURA_ROSE -> lightColorScheme(
                primary = SakuraPrimaryLight,
                secondary = SakuraSecondary,
                background = SakuraBgLight,
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFFCE7F0),
                onPrimary = Color.White,
                onBackground = Color(0xFF2B1421),
                onSurface = Color(0xFF2B1421),
                onSurfaceVariant = Color(0xFF673653)
            )
            AppThemeSetting.OCEAN_DEEP -> lightColorScheme(
                primary = OceanPrimaryLight,
                secondary = OceanSecondary,
                background = OceanBgLight,
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFE0F0FA),
                onPrimary = Color.White,
                onBackground = Color(0xFF122434),
                onSurface = Color(0xFF122434),
                onSurfaceVariant = Color(0xFF31526E)
            )
        }
    }

    return Pair(scheme, isDark)
}

@Composable
fun AuraFocusTheme(
    theme: AppThemeSetting = AppThemeSetting.ZEN_FOREST,
    darkModeSetting: DarkModeSetting = DarkModeSetting.DARK,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val (colorScheme, _) = getThemeColorScheme(theme, darkModeSetting, systemInDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
