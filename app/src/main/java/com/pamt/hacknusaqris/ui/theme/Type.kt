package com.pamt.hacknusaqris.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Six styles, one family. No downloadable font: nothing to fail to load on stage.
 */
object VerifierText {

    val Verdict = TextStyle(
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
        color = Ink
    )

    val SectionCap = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color = InkMuted
    )

    val FieldLabel = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
        color = InkMuted
    )

    val FieldValue = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
        color = Ink
    )

    /**
     * QR-side values only. The monospace shift is the cheapest strong signal in the app:
     * it says "this column is what the machine will actually do" before a word is read,
     * and it separates the two columns instantly in a wide shot on a projector.
     */
    val FieldValueMono = FieldValue.copy(fontFamily = FontFamily.Monospace)

    val Body = TextStyle(
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
        color = Ink
    )
}

/** Material slots, needed by Button and friends. */
val VerifierTypography = Typography(
    bodyLarge = VerifierText.Body,
    labelLarge = VerifierText.FieldValue
)
