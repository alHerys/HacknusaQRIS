package com.pamt.hacknusaqris.ai

import com.pamt.hacknusaqris.qr.CrcCheck
import com.pamt.hacknusaqris.qr.ParsedQrPayment

/**
 * Everything Gemini is asked for.
 *
 * Only [claimedRecipient], [claimedAmount], [claimedCurrency] and [explanation] are actually
 * consumed. The comparison and QR fields are requested for spec fidelity (CLAUDE.md 17) and
 * because committing to them before writing the prose improves the explanation, but
 * LocalResultValidator recomputes every verdict locally and discards these.
 */
data class GeminiAnalysisResult(
    val claimedRecipient: String?,
    val claimedAmount: Long?,
    val claimedCurrency: String?,
    val qrMerchant: String?,
    val qrAmount: Long?,
    val recipientComparison: String,
    val amountComparison: String,
    val crcStatus: String,
    val overallStatus: String,
    val explanation: String
)

/** The locally-derived evidence handed to Gemini. Cached so Retry never re-reads the URI. */
data class AnalysisEvidence(
    val caption: String,
    val parsed: ParsedQrPayment,
    val crc: CrcCheck
)

class AiAnalysisException(cause: Throwable) : Exception(cause)
