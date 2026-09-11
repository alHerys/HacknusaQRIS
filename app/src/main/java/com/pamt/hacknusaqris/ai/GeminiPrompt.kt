package com.pamt.hacknusaqris.ai

import org.json.JSONObject
import java.math.RoundingMode

object GeminiPrompt {

    val SYSTEM_INSTRUCTION = """
        You are analyzing consistency between a human-facing payment request
        and machine-readable payment data.

        Extract the intended recipient, amount, and currency from the message.

        Compare the extracted claims only with the supplied QR evidence.

        Do not infer sender identity.
        Do not infer merchant legitimacy.
        Do not classify fraud.
        Do not use outside knowledge.
        Do not call something safe.
        Do not invent missing information.

        Return the required structured response only.

        Field rules:
        - claimedRecipient: the recipient name exactly as written in the message, uppercased,
          with no words added or removed. If the message names no recipient, return null.
        - claimedAmount: the amount as a whole number. Indonesian number formatting uses "."
          as a thousands separator, so "Rp150.000" is 150000 and "Rp1.500.000" is 1500000.
          If the message states no amount, return null.
        - claimedCurrency: the ISO 4217 numeric code when the message names a currency
          ("Rp" or "IDR" -> "360"). Otherwise null.
        - qrMerchant and qrAmount: copy the supplied QR evidence values unchanged.
          Never rewrite, correct, or reformat them.
        - explanation: one or two sentences of plain English, under 200 characters, stating
          only which compared fields agree or disagree. Never use the words scam, fraud,
          safe, dangerous, trusted, legitimate, or verified.
    """.trimIndent()

    /** The user turn is the evidence and nothing else: no restated rules, no examples. */
    fun buildEvidenceJson(evidence: AnalysisEvidence): String =
        JSONObject().apply {
            put("messageCaption", evidence.caption)
            put(
                "qrEvidence",
                JSONObject().apply {
                    put("merchantName", evidence.parsed.merchantName ?: JSONObject.NULL)
                    put(
                        "amount",
                        evidence.parsed.amount
                            ?.setScale(0, RoundingMode.DOWN)
                            ?.toLong()
                            ?: JSONObject.NULL
                    )
                    put("currencyCode", evidence.parsed.currencyCode ?: JSONObject.NULL)
                    put("countryCode", evidence.parsed.countryCode ?: JSONObject.NULL)
                    put("crcValid", evidence.crc.valid)
                }
            )
        }.toString()
}
