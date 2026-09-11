package com.pamt.hacknusaqris.result

import com.pamt.hacknusaqris.ai.AnalysisEvidence
import com.pamt.hacknusaqris.ai.GeminiAnalysisResult
import com.pamt.hacknusaqris.qr.CrcCheck
import com.pamt.hacknusaqris.qr.ParsedQrPayment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class LocalResultValidatorTest {

    private fun evidence(
        merchant: String? = "COMPFEST UI",
        amount: String? = "150000",
        crcValid: Boolean = true
    ) = AnalysisEvidence(
        caption = "Bayar registrasi Rp150.000 ke COMPFEST UI",
        parsed = ParsedQrPayment(
            rawPayload = "<irrelevant>",
            merchantName = merchant,
            amount = amount?.let { BigDecimal(it) },
            currencyCode = "360",
            countryCode = "ID",
            merchantCity = "JAKARTA",
            crcValue = "AE80",
            crcValid = crcValid
        ),
        crc = CrcCheck(valid = crcValid, embedded = "AE80", calculated = "AE80")
    )

    private fun ai(
        recipient: String? = "COMPFEST UI",
        amount: Long? = 150_000L,
        recipientComparison: String = "MATCH",
        amountComparison: String = "MATCH",
        crcStatus: String = "VALID",
        overallStatus: String = "CONSISTENT",
        explanation: String = "The recipient and amount agree."
    ) = GeminiAnalysisResult(
        claimedRecipient = recipient,
        claimedAmount = amount,
        claimedCurrency = "360",
        qrMerchant = "COMPFEST UI",
        qrAmount = 150_000L,
        recipientComparison = recipientComparison,
        amountComparison = amountComparison,
        crcStatus = crcStatus,
        overallStatus = overallStatus,
        explanation = explanation
    )

    // --- CLAUDE.md 28 core cases -------------------------------------------------

    @Test
    fun `same recipient and same amount is CONSISTENT`() {
        val result = LocalResultValidator.combine(evidence(), ai())
        assertEquals(OverallStatus.CONSISTENT, result.overall)
        assertEquals(Comparison.MATCH, result.recipientComparison)
        assertEquals(Comparison.MATCH, result.amountComparison)
    }

    @Test
    fun `different recipient and same amount is DISCREPANCY`() {
        val result = LocalResultValidator.combine(
            evidence(merchant = "XYZ DIGITAL STORE"),
            ai()
        )
        assertEquals(OverallStatus.DISCREPANCY, result.overall)
        assertEquals(Comparison.MISMATCH, result.recipientComparison)
        assertEquals(Comparison.MATCH, result.amountComparison)
    }

    @Test
    fun `same recipient and different amount is DISCREPANCY`() {
        val result = LocalResultValidator.combine(
            evidence(amount = "250000"),
            ai()
        )
        assertEquals(OverallStatus.DISCREPANCY, result.overall)
        assertEquals(Comparison.MISMATCH, result.amountComparison)
    }

    @Test
    fun `no extractable claims is INSUFFICIENT_CONTEXT`() {
        val result = LocalResultValidator.combine(
            evidence(),
            ai(recipient = null, amount = null, overallStatus = "INSUFFICIENT_CONTEXT")
        )
        assertEquals(OverallStatus.INSUFFICIENT_CONTEXT, result.overall)
        assertEquals(Comparison.UNKNOWN, result.recipientComparison)
        assertEquals(Comparison.UNKNOWN, result.amountComparison)
    }

    // --- Phase 6 override tests: Gemini must not be able to change the verdict ----

    @Test
    fun `Gemini claiming amounts MATCH cannot override differing amounts`() {
        val result = LocalResultValidator.combine(
            evidence(amount = "250000"),
            ai(amountComparison = "MATCH", overallStatus = "CONSISTENT")
        )
        assertEquals(Comparison.MISMATCH, result.amountComparison)
        assertEquals(OverallStatus.DISCREPANCY, result.overall)
    }

    @Test
    fun `Gemini claiming CRC VALID cannot override a locally invalid CRC`() {
        val result = LocalResultValidator.combine(
            evidence(crcValid = false),
            ai(crcStatus = "VALID")
        )
        assertFalse(result.crcValid)
    }

    @Test
    fun `Gemini cannot rewrite the QR merchant`() {
        val result = LocalResultValidator.combine(
            evidence(merchant = "XYZ DIGITAL STORE"),
            ai() // ai.qrMerchant says "COMPFEST UI"
        )
        assertEquals("XYZ DIGITAL STORE", result.qrMerchant)
    }

    @Test
    fun `Gemini claiming recipients MATCH cannot override differing names`() {
        val result = LocalResultValidator.combine(
            evidence(merchant = "XYZ DIGITAL STORE"),
            ai(recipientComparison = "MATCH", overallStatus = "CONSISTENT")
        )
        assertEquals(Comparison.MISMATCH, result.recipientComparison)
        assertEquals(OverallStatus.DISCREPANCY, result.overall)
    }

    // --- CLAUDE.md 34 language guarantee -----------------------------------------

    @Test
    fun `an explanation containing a forbidden word is replaced`() {
        val result = LocalResultValidator.combine(
            evidence(merchant = "XYZ DIGITAL STORE"),
            ai(explanation = "This is a scam, do not pay.")
        )
        assertFalse(result.explanation.contains("scam", ignoreCase = true))
        assertTrue(result.explanation.contains("do not agree"))
    }

    @Test
    fun `an explanation calling the request safe is replaced`() {
        val result = LocalResultValidator.combine(
            evidence(),
            ai(explanation = "Everything matches, it is safe to pay.")
        )
        assertFalse(result.explanation.contains("safe", ignoreCase = true))
    }

    @Test
    fun `a clean explanation is preserved`() {
        val text = "The recipient named in the message differs from the QR merchant."
        val result = LocalResultValidator.combine(
            evidence(merchant = "XYZ DIGITAL STORE"),
            ai(explanation = text)
        )
        assertEquals(text, result.explanation)
    }

    // --- numeric handling ---------------------------------------------------------

    @Test
    fun `scale differences do not create a false mismatch`() {
        val result = LocalResultValidator.combine(evidence(amount = "150000.00"), ai())
        assertEquals(Comparison.MATCH, result.amountComparison)
    }

    @Test
    fun `a missing QR amount yields UNKNOWN rather than MISMATCH`() {
        val result = LocalResultValidator.combine(evidence(amount = null), ai())
        assertEquals(Comparison.UNKNOWN, result.amountComparison)
        // Recipient still matched, so the overall verdict is still CONSISTENT.
        assertEquals(OverallStatus.CONSISTENT, result.overall)
    }
}
