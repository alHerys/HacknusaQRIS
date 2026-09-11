package com.pamt.hacknusaqris.qr

import java.math.BigDecimal

/**
 * The machine-readable payment instruction, extracted locally.
 *
 * These values are authoritative for the whole app: Gemini never replaces or rewrites them.
 */
data class ParsedQrPayment(
    val rawPayload: String,
    val merchantName: String?,
    val amount: BigDecimal?,
    val currencyCode: String?,
    val countryCode: String?,
    val merchantCity: String?,
    val crcValue: String?,
    val crcValid: Boolean
)
