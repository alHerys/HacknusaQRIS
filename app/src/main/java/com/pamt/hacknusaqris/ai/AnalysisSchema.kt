package com.pamt.hacknusaqris.ai

import com.google.firebase.ai.type.Schema

object AnalysisSchema {

    private val COMPARISON = listOf("MATCH", "MISMATCH", "UNKNOWN")
    private val CRC_STATUS = listOf("VALID", "INVALID", "UNKNOWN")
    private val OVERALL = listOf("CONSISTENT", "DISCREPANCY", "INSUFFICIENT_CONTEXT")

    /**
     * Field order is load-bearing: Schema.obj derives propertyOrdering from this map's
     * insertion order, and structured-output models generate fields in that order. Putting
     * the extracted claims first and the prose last gives a mini chain-of-thought inside the
     * JSON -- the model commits to values before it writes about them.
     *
     * optionalProperties is deliberately omitted, so every key is REQUIRED. Required +
     * nullable makes "the model answered null" distinguishable from "the model didn't answer".
     */
    val schema: Schema = Schema.obj(
        mapOf(
            "claimedRecipient" to Schema.string(nullable = true),
            "claimedAmount" to Schema.long(nullable = true),
            "claimedCurrency" to Schema.string(nullable = true),
            "qrMerchant" to Schema.string(nullable = true),
            "qrAmount" to Schema.long(nullable = true),
            "recipientComparison" to Schema.enumeration(COMPARISON),
            "amountComparison" to Schema.enumeration(COMPARISON),
            "crcStatus" to Schema.enumeration(CRC_STATUS),
            "overallStatus" to Schema.enumeration(OVERALL),
            "explanation" to Schema.string()
        )
    )
}
