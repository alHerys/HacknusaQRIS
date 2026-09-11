package com.pamt.hacknusaqris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pamt.hacknusaqris.result.Comparison
import com.pamt.hacknusaqris.ui.StageState
import com.pamt.hacknusaqris.ui.theme.Accent
import com.pamt.hacknusaqris.ui.theme.Canvas
import com.pamt.hacknusaqris.ui.theme.Hairline
import com.pamt.hacknusaqris.ui.theme.InkMuted
import com.pamt.hacknusaqris.ui.theme.Surface
import com.pamt.hacknusaqris.ui.theme.VerifierColors
import com.pamt.hacknusaqris.ui.theme.VerifierText

@Composable
fun SectionCap(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = VerifierText.SectionCap, modifier = modifier)
}

@Composable
fun PanelCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Hairline, RoundedCornerShape(14.dp))
            .padding(contentPadding),
        content = content
    )
}

/**
 * The verdict headline. Full-bleed colour band, one line of supporting text, nothing else.
 */
@Composable
fun VerdictBanner(
    headline: String,
    subtitle: String,
    tone: Comparison,
    modifier: Modifier = Modifier
) {
    val (fg, bg) = toneColors(tone)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(headline, style = VerifierText.Verdict.copy(color = fg))
        Text(subtitle, style = VerifierText.Body.copy(color = fg))
    }
}

/**
 * One compared field: what the message claimed on the left, what the QR encodes on the
 * right, and the verdict glyph in the middle gutter -- on the horizontal axis the eye is
 * already travelling, so the comparison and its result are one saccade rather than three.
 *
 * The whole row tints when the pair disagrees, making the mismatched row the only coloured
 * band on the screen.
 */
@Composable
fun EvidenceRow(
    label: String,
    messageValue: String,
    qrValue: String,
    comparison: Comparison,
    modifier: Modifier = Modifier
) {
    val (fg, bg) = toneColors(comparison)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (comparison == Comparison.MATCH) Color.Transparent else bg)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionCap(label)
            Text(
                comparisonLabel(comparison),
                style = VerifierText.SectionCap.copy(color = fg)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Message", style = VerifierText.FieldLabel)
                // Never ellipsize: a truncated merchant name in a merchant-comparison tool
                // is a bug, not a layout compromise.
                Text(messageValue, style = VerifierText.FieldValue, maxLines = 3)
            }
            Box(
                modifier = Modifier.width(40.dp).padding(top = 18.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    glyphFor(comparison),
                    style = VerifierText.FieldValue.copy(color = fg, textAlign = TextAlign.Center)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("QR", style = VerifierText.FieldLabel)
                Text(qrValue, style = VerifierText.FieldValueMono, maxLines = 3)
            }
        }
    }
}

@Composable
fun CheckRow(label: String, ok: Boolean, modifier: Modifier = Modifier) {
    val fg = if (ok) VerifierColors.Match else VerifierColors.Mismatch
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(if (ok) "✓" else "⚠", style = VerifierText.FieldValue.copy(color = fg))
        Text(label, style = VerifierText.Body)
    }
}

@Composable
fun StageChecklist(
    stages: List<Pair<String, StageState>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        stages.forEach { (label, state) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                    when (state) {
                        StageState.DONE ->
                            Text(
                                "✓",
                                style = VerifierText.FieldValue.copy(color = VerifierColors.Match)
                            )

                        StageState.RUNNING ->
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = Accent
                            )

                        StageState.PENDING ->
                            Text("○", style = VerifierText.FieldValue.copy(color = Hairline))
                    }
                }
                Text(
                    label,
                    style = if (state == StageState.PENDING) {
                        VerifierText.Body.copy(color = InkMuted)
                    } else {
                        VerifierText.Body
                    }
                )
            }
        }
    }
}

/** Visually recessed so it never competes with the verdict. */
@Composable
fun DisclaimerBlock(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Canvas)
            .border(1.dp, Hairline, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(text, style = VerifierText.FieldLabel)
    }
}

@Composable
fun ThinDivider() = HorizontalDivider(color = Hairline)

private fun toneColors(comparison: Comparison): Pair<Color, Color> = when (comparison) {
    Comparison.MATCH -> VerifierColors.Match to VerifierColors.MatchBg
    Comparison.MISMATCH -> VerifierColors.Mismatch to VerifierColors.MismatchBg
    Comparison.UNKNOWN -> VerifierColors.Unknown to VerifierColors.UnknownBg
}

private fun glyphFor(comparison: Comparison) = when (comparison) {
    Comparison.MATCH -> "✓"
    Comparison.MISMATCH -> "⚠"
    Comparison.UNKNOWN -> "—"
}

private fun comparisonLabel(comparison: Comparison) = when (comparison) {
    Comparison.MATCH -> "MATCH"
    Comparison.MISMATCH -> "MISMATCH"
    Comparison.UNKNOWN -> "NOT STATED"
}
