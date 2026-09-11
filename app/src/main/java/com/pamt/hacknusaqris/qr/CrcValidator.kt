package com.pamt.hacknusaqris.qr

import java.nio.charset.Charset

data class CrcCheck(
    val valid: Boolean,
    val embedded: String?,
    val calculated: String?
)

/**
 * Validates the CRC already embedded in a QRIS payload. Entirely local and deterministic.
 *
 * This proves internal payload integrity and nothing else. A valid CRC does NOT mean the
 * merchant is legitimate or that the payment is safe.
 */
object CrcValidator {

    /**
     * CRC-16/CCITT-FALSE: polynomial 0x1021, init 0xFFFF, no input/output reflection,
     * no final XOR. Canonical check value: crc16("123456789") == 0x29B1.
     */
    fun crc16(bytes: ByteArray): Int {
        var crc = 0xFFFF
        for (b in bytes) {
            crc = crc xor ((b.toInt() and 0xFF) shl 8)
            repeat(8) {
                crc = if ((crc and 0x8000) != 0) (crc shl 1) xor 0x1021 else (crc shl 1)
                crc = crc and 0xFFFF
            }
        }
        return crc and 0xFFFF
    }

    fun crc16(data: String, charset: Charset = Charsets.UTF_8): String =
        "%04X".format(crc16(data.toByteArray(charset)))

    /**
     * @param crcTagStartIndex index of the '6' in the trailing "6304" header, as reported
     *   by [EmvTlv]. Deriving it from the parse is exact; searching for the literal "6304"
     *   is not, because that sequence can occur inside another field's value.
     */
    fun validate(payload: String, crcTagStartIndex: Int): CrcCheck {
        if (crcTagStartIndex < 0 || crcTagStartIndex + 8 > payload.length) {
            return CrcCheck(valid = false, embedded = null, calculated = null)
        }
        if (payload.substring(crcTagStartIndex, crcTagStartIndex + 4) != "6304") {
            return CrcCheck(valid = false, embedded = null, calculated = null)
        }

        val embedded = payload.substring(crcTagStartIndex + 4, crcTagStartIndex + 8)
        val data = payload.substring(0, crcTagStartIndex + 4)   // the "6304" header is included

        val utf8 = crc16(data, Charsets.UTF_8)
        if (utf8.equals(embedded, ignoreCase = true)) return CrcCheck(true, embedded, utf8)

        // Some generators encode the QR text as ISO-8859-1. For pure-ASCII payloads the two
        // are byte-identical, so this only matters when a merchant name carries non-ASCII.
        val latin1 = crc16(data, Charsets.ISO_8859_1)
        if (latin1.equals(embedded, ignoreCase = true)) return CrcCheck(true, embedded, latin1)

        return CrcCheck(valid = false, embedded = embedded, calculated = utf8)
    }
}
