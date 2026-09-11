package com.pamt.hacknusaqris.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pamt.hacknusaqris.result.Comparison
import com.pamt.hacknusaqris.result.OverallStatus
import com.pamt.hacknusaqris.result.VerificationResult
import com.pamt.hacknusaqris.ui.components.CheckRow
import com.pamt.hacknusaqris.ui.components.DisclaimerBlock
import com.pamt.hacknusaqris.ui.components.EvidenceRow
import com.pamt.hacknusaqris.ui.components.PanelCard
import com.pamt.hacknusaqris.ui.components.SectionCap
import com.pamt.hacknusaqris.ui.components.ThinDivider
import com.pamt.hacknusaqris.ui.components.VerdictBanner
import com.pamt.hacknusaqris.ui.theme.VerifierText
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ResultScreen(
    state: UiState.Success,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = state.result

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VerdictBanner(
                headline = headlineFor(result.overall),
                subtitle = subtitleFor(result.overall),
                tone = toneFor(result.overall)
            )

            PanelCard(contentPadding = 8.dp) {
                EvidenceRow(
                    label = "Recipient",
                    messageValue = result.claimedRecipient ?: "Not stated",
                    qrValue = result.qrMerchant ?: "Not present",
                    comparison = result.recipientComparison
                )
                ThinDivider()
                EvidenceRow(
                    label = "Amount",
                    messageValue = formatAmount(result.claimedAmount),
                    qrValue = formatAmount(result.qrAmount),
                    comparison = result.amountComparison
                )
            }

            PanelCard {
                SectionCap("Checks")
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    CheckRow(
                        label = checkLabel("Recipient", result.recipientComparison),
                        ok = result.recipientComparison == Comparison.MATCH
                    )
                    CheckRow(
                        label = checkLabel("Amount", result.amountComparison),
                        ok = result.amountComparison == Comparison.MATCH
                    )
                    // CRC proves internal payload integrity and nothing more. It is
                    // deliberately shown as its own line, never folded into the verdict.
                    CheckRow(
                        label = if (result.crcValid) "QR CRC valid" else "QR CRC invalid",
                        ok = result.crcValid
                    )
                }
            }

            PanelCard {
                SectionCap(
                    if (result.overall == OverallStatus.DISCREPANCY) {
                        "Why this matters"
                    } else {
                        "Explanation"
                    }
                )
                Text(
                    result.explanation,
                    style = VerifierText.Body,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            DisclaimerBlock(DISCLAIMER)
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Done")
            }
        }
    }
}

private const val DISCLAIMER =
    "This result only reflects the details we compared. It does not authenticate the " +
        "sender and does not prove that the request is or is not legitimate. Before paying, " +
        "verify the recipient shown by your banking or e-wallet application."

private fun headlineFor(status: OverallStatus) = when (status) {
    OverallStatus.CONSISTENT -> "NO DISCREPANCY FOUND"
    OverallStatus.DISCREPANCY -> "PAYMENT DETAILS DO NOT MATCH"
    OverallStatus.INSUFFICIENT_CONTEXT -> "NOT ENOUGH INFORMATION"
}

private fun subtitleFor(status: OverallStatus) = when (status) {
    OverallStatus.CONSISTENT ->
        "The payment details in the message are consistent with the payment " +
            "instruction encoded in the QR."

    OverallStatus.DISCREPANCY ->
        "The payment request contains information that conflicts with the payment " +
            "instruction encoded in the QR."

    OverallStatus.INSUFFICIENT_CONTEXT ->
        "The message did not state enough payment details to compare against the QR."
}

private fun toneFor(status: OverallStatus) = when (status) {
    OverallStatus.CONSISTENT -> Comparison.MATCH
    OverallStatus.DISCREPANCY -> Comparison.MISMATCH
    OverallStatus.INSUFFICIENT_CONTEXT -> Comparison.UNKNOWN
}

private fun checkLabel(field: String, comparison: Comparison) = when (comparison) {
    Comparison.MATCH -> "$field matches"
    Comparison.MISMATCH -> "$field mismatch"
    Comparison.UNKNOWN -> "$field not stated in the message"
}

private val rupiah: NumberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))

private fun formatAmount(amount: BigDecimal?): String =
    amount?.let { "Rp${rupiah.format(it)}" } ?: "Not stated"
