package com.pamt.hacknusaqris.result

import java.util.Locale

object MerchantNormalizer {

    private val NON_ALNUM = Regex("[^A-Z0-9 ]")
    private val SPACES = Regex("\\s+")

    /** Locale.ROOT, not the default: a Turkish-locale device maps 'i' to 'İ'. */
    fun normalize(value: String): String =
        value.uppercase(Locale.ROOT)
            .replace(NON_ALNUM, " ")
            .replace(SPACES, " ")
            .trim()

    fun matches(claimed: String, qrMerchant: String): Boolean {
        val a = normalize(claimed)
        val b = normalize(qrMerchant)
        if (a.isEmpty() || b.isEmpty()) return false
        if (a == b) return true

        // Demo-scoped tolerance only (CLAUDE.md 19 forbids real entity resolution): accept
        // when one normalized name fully contains the other and the shorter is >= 4 chars.
        // This keeps Fixture A passing if Gemini returns "COMPFEST" instead of "COMPFEST UI".
        // It cannot produce a false MATCH for Fixture B, whose names share no substring.
        val shorter = if (a.length <= b.length) a else b
        val longer = if (a.length <= b.length) b else a
        return shorter.length >= 4 && longer.contains(shorter)
    }
}
