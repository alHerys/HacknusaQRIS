package com.pamt.hacknusaqris.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.pamt.hacknusaqris.result.Comparison
import com.pamt.hacknusaqris.result.OverallStatus
import com.pamt.hacknusaqris.result.VerificationResult
import com.pamt.hacknusaqris.ui.theme.QrisVerifierTheme
import java.math.BigDecimal

/**
 * Design previews. Open any of these in Android Studio's split view to see the screens
 * without installing on a device. Not part of the shipped app.
 */

private val consistent = VerificationResult(
    overall = OverallStatus.CONSISTENT,
    claimedRecipient = "COMPFEST UI",
    claimedAmount = BigDecimal("150000"),
    qrMerchant = "COMPFEST UI",
    qrAmount = BigDecimal("150000"),
    qrCurrencyCode = "360",
    recipientComparison = Comparison.MATCH,
    amountComparison = Comparison.MATCH,
    crcValid = true,
    explanation = "The recipient and amount stated in the message agree with the " +
        "merchant and amount encoded in the QR."
)

private val discrepancy = VerificationResult(
    overall = OverallStatus.DISCREPANCY,
    claimedRecipient = "COMPFEST UI",
    claimedAmount = BigDecimal("150000"),
    qrMerchant = "XYZ DIGITAL STORE",
    qrAmount = BigDecimal("150000"),
    qrCurrencyCode = "360",
    recipientComparison = Comparison.MISMATCH,
    amountComparison = Comparison.MATCH,
    crcValid = true,
    explanation = "The message names COMPFEST UI as the recipient, but the QR encodes " +
        "XYZ DIGITAL STORE as the merchant. The amounts agree."
)

@Preview(name = "Result — consistent", showBackground = true, backgroundColor = 0xFFF4F6F8)
@Composable
private fun PreviewConsistent() = QrisVerifierTheme {
    ResultScreen(
        state = UiState.Success("Bayar registrasi Rp150.000 ke COMPFEST UI", null, consistent),
        onDone = {}
    )
}

@Preview(name = "Result — discrepancy", showBackground = true, backgroundColor = 0xFFF4F6F8)
@Composable
private fun PreviewDiscrepancy() = QrisVerifierTheme {
    ResultScreen(
        state = UiState.Success("Bayar registrasi Rp150.000 ke COMPFEST UI", null, discrepancy),
        onDone = {}
    )
}

@Preview(name = "Analyzing", showBackground = true, backgroundColor = 0xFFF4F6F8)
@Composable
private fun PreviewAnalyzing() = QrisVerifierTheme {
    AnalysisScreen(
        UiState.Analyzing(
            caption = "Bayar registrasi Rp150.000 ke COMPFEST UI",
            preview = null,
            stages = Stages(
                readMessage = StageState.DONE,
                decodeQr = StageState.DONE,
                checkPaymentData = StageState.DONE,
                compareDetails = StageState.RUNNING
            )
        )
    )
}

@Preview(name = "Error — no caption", showBackground = true, backgroundColor = 0xFFF4F6F8)
@Composable
private fun PreviewMissingCaption() = QrisVerifierTheme {
    ErrorScreen(
        UiState.Failure(FailureKind.MISSING_CAPTION, null, null),
        onRetry = {},
        onDone = {}
    )
}

@Preview(name = "Error — analysis failed", showBackground = true, backgroundColor = 0xFFF4F6F8)
@Composable
private fun PreviewAnalysisFailed() = QrisVerifierTheme {
    ErrorScreen(
        UiState.Failure(FailureKind.ANALYSIS_FAILED, "caption", null),
        onRetry = {},
        onDone = {}
    )
}

@Preview(name = "Idle", showBackground = true, backgroundColor = 0xFFF4F6F8)
@Composable
private fun PreviewIdle() = QrisVerifierTheme { IdleScreen() }
