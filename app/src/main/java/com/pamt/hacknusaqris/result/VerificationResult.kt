package com.pamt.hacknusaqris.result

import java.math.BigDecimal

enum class Comparison { MATCH, MISMATCH, UNKNOWN }

enum class OverallStatus { CONSISTENT, DISCREPANCY, INSUFFICIENT_CONTEXT }

data class VerificationResult(
    val overall: OverallStatus,
    /** From Gemini's natural-language extraction. */
    val claimedRecipient: String?,
    val claimedAmount: BigDecimal?,
    /** Always from QrisParser -- never replaced by anything the model returned. */
    val qrMerchant: String?,
    val qrAmount: BigDecimal?,
    val qrCurrencyCode: String?,
    /** Computed locally. */
    val recipientComparison: Comparison,
    val amountComparison: Comparison,
    /** Always from CrcValidator. */
    val crcValid: Boolean,
    val explanation: String
)
