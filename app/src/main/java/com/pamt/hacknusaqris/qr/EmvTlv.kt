package com.pamt.hacknusaqris.qr

data class TlvElement(
    val tag: String,
    val length: Int,
    val value: String,
    /** Index of the first character of the TAG within the payload. */
    val startIndex: Int
)

/**
 * Minimal EMVCo TLV reader: 2-character tag, 2-character length, N-character value.
 *
 * Nested templates (26, 51, 62 ...) are recorded as opaque top-level elements and never
 * descended into -- the demo needs nothing inside them.
 */
object EmvTlv {

    /** @return the top-level elements in order, or null if the payload is not well-formed. */
    fun parse(payload: String): List<TlvElement>? {
        if (payload.isEmpty()) return null

        val out = ArrayList<TlvElement>(16)
        var i = 0
        while (i < payload.length) {
            if (i + 4 > payload.length) return null                 // truncated tag/length header

            val tag = payload.substring(i, i + 2)
            if (!tag.isAsciiDigits()) return null

            val lengthText = payload.substring(i + 2, i + 4)
            if (!lengthText.isAsciiDigits()) return null

            val length = lengthText.toInt()                          // safe: always 00..99
            val valueStart = i + 4
            val valueEnd = valueStart + length
            if (valueEnd > payload.length) return null               // length runs past the end

            out.add(TlvElement(tag, length, payload.substring(valueStart, valueEnd), i))
            i = valueEnd                                             // always advances >= 4
        }
        return out.takeIf { it.isNotEmpty() }
    }

    // Deliberately not Char.isDigit(): that is Unicode-aware and accepts e.g. Arabic-Indic
    // digits, which String.toInt() would then happily convert.
    private fun String.isAsciiDigits(): Boolean = all { it in '0'..'9' }
}
