package com.pamt.hacknusaqris.qr

import java.math.BigDecimal

/**
 * Extracts the payment fields the demo compares. Only the tags actually needed are read;
 * everything else is left as opaque TLV.
 */
object QrisParser {

    private const val TAG_PAYLOAD_FORMAT = "00"
    private const val TAG_CURRENCY = "53"
    private const val TAG_AMOUNT = "54"
    private const val TAG_COUNTRY = "58"
    private const val TAG_MERCHANT_NAME = "59"
    private const val TAG_MERCHANT_CITY = "60"
    private const val TAG_CRC = "63"

    /**
     * @return null when the payload is not a usable EMV/QRIS payment code. Decoding a QR
     *   successfully does not make its contents a payment instruction.
     */
    fun parse(rawPayload: String): ParsedQrPayment? {
        val elements = EmvTlv.parse(rawPayload) ?: return null
        val byTag = elements.associateBy { it.tag }

        // Minimum viability: it must be an EMV code, name a merchant, and carry a CRC.
        // The VALUE of tag 00 is deliberately not asserted to equal "01" -- presence is
        // required, exact value is not, so a slightly non-conformant real QRIS still parses.
        if (TAG_PAYLOAD_FORMAT !in byTag) return null
        val merchant = byTag[TAG_MERCHANT_NAME]?.value?.trim()?.takeIf { it.isNotEmpty() }
            ?: return null
        val crcElement = byTag[TAG_CRC] ?: return null

        val crc = CrcValidator.validate(rawPayload, crcElement.startIndex)

        return ParsedQrPayment(
            rawPayload = rawPayload,
            merchantName = merchant,
            amount = byTag[TAG_AMOUNT]?.value?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { runCatching { BigDecimal(it) }.getOrNull() },
            currencyCode = byTag[TAG_CURRENCY]?.value?.trim(),
            countryCode = byTag[TAG_COUNTRY]?.value?.trim(),
            merchantCity = byTag[TAG_MERCHANT_CITY]?.value?.trim(),
            crcValue = crc.embedded,
            crcValid = crc.valid
        )
    }

    /** Convenience for callers that also need the full [CrcCheck], not just the boolean. */
    fun crcCheckFor(rawPayload: String): CrcCheck {
        val crcElement = EmvTlv.parse(rawPayload)?.firstOrNull { it.tag == TAG_CRC }
            ?: return CrcCheck(valid = false, embedded = null, calculated = null)
        return CrcValidator.validate(rawPayload, crcElement.startIndex)
    }
}
