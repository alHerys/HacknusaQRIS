package com.pamt.hacknusaqris.ui.theme

import androidx.compose.ui.graphics.Color

// Neutral surface tokens.
val Canvas = Color(0xFFF4F6F8)
val Surface = Color(0xFFFFFFFF)
val Hairline = Color(0xFFE3E7EC)
val Ink = Color(0xFF0F1115)
val InkMuted = Color(0xFF5B6472)

/** Used roughly twice per screen: the primary button and the running indicator. */
val Accent = Color(0xFF1B4DFF)

/**
 * Verdict colors. These are semantic, not theme roles, which is why they live here rather
 * than being forced into MaterialTheme.colorScheme.
 */
object VerifierColors {
    val Match = Color(0xFF0F7A4A)
    val MatchBg = Color(0xFFE7F5EE)

    /**
     * Burnt orange, deliberately not a fire-engine red. CLAUDE.md 34 forbids alarm framing,
     * and the approved copy ("PAYMENT DETAILS DO NOT MATCH") is measured. A #D32F2F red
     * would visually contradict that copy and push a judge toward reading a fraud verdict
     * the app explicitly does not make. This reads as conflict / attention required.
     */
    val Mismatch = Color(0xFFC2410C)
    val MismatchBg = Color(0xFFFDF0E7)

    val Unknown = InkMuted
    val UnknownBg = Color(0xFFF0F2F5)
}
