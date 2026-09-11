package com.pamt.hacknusaqris.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Deliberately fixed: no dynamic color, and light regardless of the system setting.
 *
 * The entire product is a colour-coded verdict. Material You would tint MATCH/MISMATCH with
 * the phone's wallpaper palette, which destroys the meaning; and a phone left in dark mode
 * would render a design nobody rehearsed. Slides, screenshots and the live device must look
 * identical.
 */
private val VerifierColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = Surface,
    background = Canvas,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = Canvas,
    onSurfaceVariant = InkMuted,
    outline = Hairline,
    outlineVariant = Hairline,
    error = VerifierColors.Mismatch,
    onError = Surface
)

@Composable
fun QrisVerifierTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VerifierColorScheme,
        typography = VerifierTypography,
        content = content
    )
}
