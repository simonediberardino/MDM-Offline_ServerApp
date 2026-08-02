package com.dbg.mdm_serverapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** WinUI / Fluent Design System tokens (light theme). */
val FluentAccent = Color(0xFF005FB8)
val FluentAccentLight = Color(0xFF0078D4)
val FluentOnAccent = Color(0xFFFFFFFF)
val FluentLayerDefault = Color(0xFFF3F3F3)
val FluentCard = Color(0xFFFFFFFF)
val FluentSmoke = Color(0xFFF9F9F9)
val FluentStroke = Color(0xFFE5E5E5)
val FluentText = Color(0xFF1A1A1A)
val FluentTextSecondary = Color(0xFF5D5D5D)
val FluentNavSelected = Color(0x1A005FB8)
val FluentNavHover = Color(0x0F000000)
val FluentSuccess = Color(0xFF0F7B0F)
val FluentError = Color(0xFFC42B1C)
val FluentInfoBarBg = Color(0xFFF0F6FC)
val FluentInfoBarStroke = Color(0xFFB4D6FA)
val FluentBadgeBg = Color(0xFFE8F3FC)
val FluentBadgeText = Color(0xFF003E79)

val ControlCorner = RoundedCornerShape(4.dp)
val OverlayCorner = RoundedCornerShape(8.dp)
val CardCorner = RoundedCornerShape(8.dp)

private val FluentColorScheme = lightColorScheme(
    primary = FluentAccent,
    onPrimary = FluentOnAccent,
    primaryContainer = FluentNavSelected,
    onPrimaryContainer = Color(0xFF003E79),
    secondary = FluentTextSecondary,
    onSecondary = FluentOnAccent,
    background = FluentLayerDefault,
    onBackground = FluentText,
    surface = FluentCard,
    onSurface = FluentText,
    surfaceVariant = FluentSmoke,
    onSurfaceVariant = FluentTextSecondary,
    outline = FluentStroke,
    outlineVariant = FluentStroke,
    error = FluentError,
)

private val FluentTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

private val FluentShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = ControlCorner,
    medium = OverlayCorner,
    large = CardCorner,
    extraLarge = RoundedCornerShape(12.dp),
)

@Composable
fun WindowsAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FluentColorScheme,
        typography = FluentTypography,
        shapes = FluentShapes,
        content = content,
    )
}

val WindowsAccent get() = FluentAccent
val WindowsAccentHover get() = FluentAccentLight
val WindowsWindowBg get() = FluentLayerDefault
val WindowsPaneBg get() = FluentCard
val WindowsSidebarBg get() = FluentSmoke
val WindowsBorder get() = FluentStroke
val WindowsText get() = FluentText
val WindowsMuted get() = FluentTextSecondary
val WindowsSuccess get() = FluentSuccess
val WindowsRowHover get() = FluentNavHover
val WindowsRowSelected get() = FluentNavSelected
