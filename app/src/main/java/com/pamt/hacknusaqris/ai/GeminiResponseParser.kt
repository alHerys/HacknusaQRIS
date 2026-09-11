package com.pamt.hacknusaqris.ai

import org.json.JSONObject

object GeminiResponseParser {

    fun parse(raw: String): GeminiAnalysisResult {
        val json = JSONObject(stripFences(raw))
        return GeminiAnalysisResult(
            claimedRecipient = json.stringOrNull("claimedRecipient"),
            claimedAmount = json.longOrNull("claimedAmount"),
            claimedCurrency = json.stringOrNull("claimedCurrency"),
            qrMerchant = json.stringOrNull("qrMerchant"),
            qrAmount = json.longOrNull("qrAmount"),
            recipientComparison = json.stringOrNull("recipientComparison") ?: "UNKNOWN",
            amountComparison = json.stringOrNull("amountComparison") ?: "UNKNOWN",
            crcStatus = json.stringOrNull("crcStatus") ?: "UNKNOWN",
            overallStatus = json.stringOrNull("overallStatus") ?: "INSUFFICIENT_CONTEXT",
            explanation = json.stringOrNull("explanation").orEmpty()
        )
    }

    // responseMimeType=application/json should prevent fencing, but three lines here beats
    // an unrecoverable failure on stage.
    private fun stripFences(raw: String): String =
        raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

    // isNull() is the load-bearing guard: optString() on a JSON null returns the literal
    // string "null" on some Android versions.
    private fun JSONObject.stringOrNull(key: String): String? =
        if (!has(key) || isNull(key)) null
        else optString(key).trim().ifEmpty { null }

    private fun JSONObject.longOrNull(key: String): Long? =
        if (!has(key) || isNull(key)) null
        else when (val value = opt(key)) {
            is Number -> value.toLong()
            // Defensive: the model can still emit "150.000" as a string despite the schema.
            is String -> value.replace(Regex("\\D"), "").toLongOrNull()
            else -> null
        }
}
