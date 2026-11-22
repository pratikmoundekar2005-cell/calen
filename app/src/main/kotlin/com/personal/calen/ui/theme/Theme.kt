package com.personal.calen.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CalenPalette(
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textSubdued: Color,
    val divider: Color,
    val selectedDateFill: Color,
    val selectedDateText: Color,
    val todayIndicator: Color,
    val eventDot: Color,
    val isDark: Boolean
)

val LocalCalenPalette = staticCompositionLocalOf {
    CalenPalette(
        background = CalenWhite,
        surface = CalenWhite,
        textPrimary = CalenBlack,
        textSecondary = CalenGrey,
        textSubdued = CalenCadetGrey,
        divider = CalenCadetGrey,
        selectedDateFill = CalenBlack,
        selectedDateText = CalenWhite,
        todayIndicator = CalenBlack,
        eventDot = CalenBlack,
        isDark = false
    )
}

private val LightCalenPalette = CalenPalette(
    background = CalenWhite,
    surface = CalenWhite,
    textPrimary = CalenBlack,
    textSecondary = CalenGrey,
    textSubdued = CalenCadetGrey,
    divider = CalenCadetGrey,
    selectedDateFill = CalenBlack,
    selectedDateText = CalenWhite,
    todayIndicator = CalenBlack,
    eventDot = CalenBlack,
    isDark = false
)

private val DarkCalenPalette = CalenPalette(
    background = CalenBlack,
    surface = CalenBlack,
    textPrimary = CalenWhite,
    textSecondary = CalenCadetGrey,
    textSubdued = CalenGrey,
    divider = CalenGrey,
    selectedDateFill = CalenWhite,
    selectedDateText = CalenBlack,
    todayIndicator = CalenWhite,
    eventDot = CalenWhite,
    isDark = true
)

private val LightColorScheme = lightColorScheme(
    primary = CalenBlack,
    onPrimary = CalenWhite,
    background = CalenWhite,
    onBackground = CalenBlack,
    surface = CalenWhite,
    onSurface = CalenBlack,
    surfaceVariant = CalenWhite,
    onSurfaceVariant = CalenGrey,
    outline = CalenCadetGrey,
    outlineVariant = CalenCadetGrey
)

private val DarkColorScheme = darkColorScheme(
    primary = CalenWhite,
    onPrimary = CalenBlack,
    background = CalenBlack,
    onBackground = CalenWhite,
    surface = CalenBlack,
    onSurface = CalenWhite,
    surfaceVariant = CalenBlack,
    onSurfaceVariant = CalenCadetGrey,
    outline = CalenGrey,
    outlineVariant = CalenGrey
)

object CalenTheme {
    val colors: CalenPalette
        @Composable
        get() = LocalCalenPalette.current
}

@Composable
fun CalenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkCalenPalette else LightCalenPalette
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalCalenPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
