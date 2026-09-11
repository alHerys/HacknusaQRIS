package com.pamt.hacknusaqris.ai

import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeminiResponseParserTest {

    private val valid = """
        {
          "claimedRecipient": "COMPFEST UI",
          "claimedAmount": 150000,
          "claimedCurrency": "360",
          "qrMerchant": "XYZ DIGITAL STORE",
          "qrAmount": 150000,
          "recipientComparison": "MISMATCH",
          "amountComparison": "MATCH",
          "crcStatus": "VALID",
          "overallStatus": "DISCREPANCY",
          "explanation": "The recipient named in the message differs from the QR merchant."
        }
    """.trimIndent()

    @Test
    fun `parses a well-formed response`() {
        val result = GeminiResponseParser.parse(valid)
        assertEquals("COMPFEST UI", result.claimedRecipient)
        assertEquals(150000L, result.claimedAmount)
        assertEquals("360", result.claimedCurrency)
        assertEquals("MISMATCH", result.recipientComparison)
        assertEquals("MATCH", result.amountComparison)
        assertEquals("DISCREPANCY", result.overallStatus)
    }

    @Test
    fun `json nulls become kotlin nulls, not the string null`() {
        val json = """
            {"claimedRecipient": null, "claimedAmount": null, "claimedCurrency": null,
             "qrMerchant": null, "qrAmount": null,
             "recipientComparison": "UNKNOWN", "amountComparison": "UNKNOWN",
             "crcStatus": "VALID", "overallStatus": "INSUFFICIENT_CONTEXT",
             "explanation": "No recipient stated."}
        """.trimIndent()

        val result = GeminiResponseParser.parse(json)
        assertNull(result.claimedRecipient)
        assertNull(result.claimedAmount)
        assertNull(result.claimedCurrency)
    }

    @Test
    fun `strips a markdown code fence`() {
        val fenced = "```json\n$valid\n```"
        assertEquals("COMPFEST UI", GeminiResponseParser.parse(fenced).claimedRecipient)
    }

    @Test
    fun `an amount returned as an Indonesian-formatted string is recovered`() {
        val json = """{"claimedAmount": "150.000", "explanation": "x"}"""
        assertEquals(150000L, GeminiResponseParser.parse(json).claimedAmount)
    }

    @Test
    fun `missing keys fall back to documented defaults`() {
        val result = GeminiResponseParser.parse("""{"explanation": "x"}""")
        assertNull(result.claimedRecipient)
        assertEquals("UNKNOWN", result.recipientComparison)
        assertEquals("UNKNOWN", result.amountComparison)
        assertEquals("INSUFFICIENT_CONTEXT", result.overallStatus)
    }

    @Test(expected = JSONException::class)
    fun `malformed json throws rather than returning garbage`() {
        GeminiResponseParser.parse("not json at all")
    }
}
