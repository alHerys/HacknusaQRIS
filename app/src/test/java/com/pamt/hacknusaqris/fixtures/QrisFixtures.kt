package com.pamt.hacknusaqris.fixtures

import com.pamt.hacknusaqris.qr.CrcValidator

/**
 * The two controlled demo payloads.
 *
 * Every value here is fictional. No real merchant, NMID, or acquirer is referenced, so
 * these codes cannot cause an accidental real payment during development.
 *
 * Fixture A and Fixture B differ in tag 59 (merchant name) and nothing else -- the amount
 * is deliberately identical, which is what proves the app compares individual fields
 * rather than simply flagging every QR.
 */
object QrisFixtures {

    const val MERCHANT_A = "COMPFEST UI"
    const val MERCHANT_B = "XYZ DIGITAL STORE"
    const val AMOUNT = "150000"

    const val CAPTION = "Bayar registrasi Rp150.000 ke COMPFEST UI"

    val payloadA: String get() = buildPayload(MERCHANT_A)
    val payloadB: String get() = buildPayload(MERCHANT_B)

    fun tlv(tag: String, value: String): String {
        require(value.length <= 99) { "EMV top-level values are at most 99 chars" }
        return tag + "%02d".format(value.length) + value
    }

    fun buildPayload(
        merchant: String,
        amount: String = AMOUNT,
        currency: String = "360",
        country: String = "ID"
    ): String {
        val merchantAccount = tlv("00", "ID.CO.QRIS.WWW") +
            tlv("01", "ID1020012345678") +
            tlv("02", "UMI")

        val centralRepository = tlv("00", "ID.CO.QRIS.WWW") +
            tlv("02", "ID1020012345678") +
            tlv("03", "UMI")

        val additionalData = tlv("01", "REG2026")

        val body = buildString {
            append(tlv("00", "01"))                 // payload format indicator
            append(tlv("01", "12"))                 // point of initiation: 12 = dynamic
            append(tlv("26", merchantAccount))
            append(tlv("51", centralRepository))
            append(tlv("52", "5499"))               // merchant category code
            append(tlv("53", currency))
            append(tlv("54", amount))
            append(tlv("58", country))
            append(tlv("59", merchant))
            append(tlv("60", "JAKARTA"))
            append(tlv("61", "12345"))
            append(tlv("62", additionalData))
        }

        // The CRC covers everything up to and including the "6304" header itself.
        val withCrcHeader = body + "6304"
        // Uses the PRODUCTION validator, so the fixtures are consistent by construction
        // rather than by a hand-computed constant that could silently drift.
        return withCrcHeader + CrcValidator.crc16(withCrcHeader)
    }
}
