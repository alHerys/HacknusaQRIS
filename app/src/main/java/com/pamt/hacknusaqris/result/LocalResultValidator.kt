package com.pamt.hacknusaqris.result

import com.pamt.hacknusaqris.ai.AnalysisEvidence
import com.pamt.hacknusaqris.ai.GeminiAnalysisResult
import java.math.BigDecimal

/**
 * Turns Gemini's language interpretation plus local QR evidence into the final verdict.
 *
 * Gemini owns exactly one thing here: reading the caption. Every comparison, the CRC status,
 * and the overall verdict are computed from local data, so a model flip on stage cannot
 * change the headline (CLAUDE.md 18).
 */
object LocalResultValidator {

    fun combine(evidence: AnalysisEvidence, ai: GeminiAnalysisResult): VerificationResult {
        val qrMerchant = evidence.parsed.merchantName
        val qrAmount = evidence.parsed.amount
        val claimedAmount = ai.claimedAmount?.let { BigDecimal.valueOf(it) }

        // compareTo, not equals: BigDecimal("150000.00") != BigDecimal("150000") under equals.
        val amountComparison = when {
            claimedAmount == null || qrAmount == null -> Comparison.UNKNOWN
            claimedAmount.compareTo(qrAmount) == 0 -> Comparison.MATCH
            else -> Comparison.MISMATCH
        }

        val recipientComparison = when {
            ai.claimedRecipient.isNullOrBlank() || qrMerchant.isNullOrBlank() -> Comparison.UNKNOWN
            MerchantNormalizer.matches(ai.claimedRecipient, qrMerchant) -> Comparison.MATCH
            else -> Comparison.MISMATCH
        }

        // Branch order matters: reaching branch 2 already guarantees nothing mismatched.
        // CRC is deliberately absent -- an internally corrupt QR is a different claim from
        // "the message contradicts the QR", and CLAUDE.md 15 insists on that distinction.
        val overall = when {
            recipientComparison == Comparison.MISMATCH ||
                amountComparison == Comparison.MISMATCH -> OverallStatus.DISCREPANCY

            recipientComparison == Comparison.MATCH ||
                amountComparison == Comparison.MATCH -> OverallStatus.CONSISTENT

            else -> OverallStatus.INSUFFICIENT_CONTEXT
        }

        return VerificationResult(
            overall = overall,
            claimedRecipient = ai.claimedRecipient?.trim(),
            claimedAmount = claimedAmount,
            qrMerchant = qrMerchant,          // ai.qrMerchant deliberately unused
            qrAmount = qrAmount,              // ai.qrAmount deliberately unused
            qrCurrencyCode = evidence.parsed.currencyCode,
            recipientComparison = recipientComparison,
            amountComparison = amountComparison,
            crcValid = evidence.crc.valid,    // ai.crcStatus deliberately unused
            explanation = sanitize(ai.explanation, overall)
        )
    }

    private val FORBIDDEN = Regex(
        "\\b(scam|scams|fraud|fraudulent|penipuan|safe|aman|legitimate|legit|verified|" +
            "trusted|dangerous)\\b",
        RegexOption.IGNORE_CASE
    )

    /**
     * CLAUDE.md 34 is a hard product rule, while the system instruction is only a request.
     * Six lines turn the request into a guarantee.
     */
    private fun sanitize(explanation: String, overall: OverallStatus): String =
        if (explanation.isBlank() || FORBIDDEN.containsMatchIn(explanation)) {
            when (overall) {
                OverallStatus.CONSISTENT ->
                    "The payment details in the message agree with the payment instruction " +
                        "encoded in the QR."

                OverallStatus.DISCREPANCY ->
                    "The payment details in the message do not agree with the payment " +
                        "instruction encoded in the QR."

                OverallStatus.INSUFFICIENT_CONTEXT ->
                    "There was not enough information in the message to compare against the " +
                        "QR payment data."
            }
        } else {
            explanation.trim()
        }
}
