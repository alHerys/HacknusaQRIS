package com.pamt.hacknusaqris.qr

import com.pamt.hacknusaqris.fixtures.QrisFixtures
import com.pamt.hacknusaqris.fixtures.QrisFixtures.tlv
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class QrisParserTest {

    @Test
    fun `fixture A extracts the expected payment fields`() {
        val parsed = QrisParser.parse(QrisFixtures.payloadA)
        assertNotNull(parsed)
        assertEquals("COMPFEST UI", parsed!!.merchantName)
        assertEquals(0, BigDecimal("150000").compareTo(parsed.amount))
        assertEquals("360", parsed.currencyCode)
        assertEquals("ID", parsed.countryCode)
        assertEquals("JAKARTA", parsed.merchantCity)
        assertTrue(parsed.crcValid)
    }

    @Test
    fun `fixture B extracts a different merchant and the same amount`() {
        val parsed = QrisParser.parse(QrisFixtures.payloadB)
        assertNotNull(parsed)
        assertEquals("XYZ DIGITAL STORE", parsed!!.merchantName)
        assertEquals(0, BigDecimal("150000").compareTo(parsed.amount))
        assertTrue(parsed.crcValid)
    }

    @Test
    fun `decimal amount compares equal to the whole-number form`() {
        val parsed = QrisParser.parse(QrisFixtures.buildPayload("COMPFEST UI", amount = "150000.00"))
        assertNotNull(parsed)
        assertEquals(0, BigDecimal("150000").compareTo(parsed!!.amount))
    }

    @Test
    fun `merchant name is trimmed`() {
        val parsed = QrisParser.parse(QrisFixtures.buildPayload("COMPFEST UI  "))
        assertEquals("COMPFEST UI", parsed?.merchantName)
    }

    @Test
    fun `missing merchant name fails to parse`() {
        val payload = withoutTag(QrisFixtures.payloadA, "59", "COMPFEST UI")
        assertNull(QrisParser.parse(payload))
    }

    @Test
    fun `missing payload format indicator fails to parse`() {
        val payload = withoutTag(QrisFixtures.payloadA, "00", "01")
        assertNull(QrisParser.parse(payload))
    }

    @Test
    fun `missing crc fails to parse`() {
        val payload = QrisFixtures.payloadA
        assertNull(QrisParser.parse(payload.dropLast(8)))
    }

    @Test
    fun `non-numeric amount yields a null amount rather than a crash`() {
        val payload = QrisFixtures.buildPayload("COMPFEST UI", amount = "ABCDEF")
        val parsed = QrisParser.parse(payload)
        assertNotNull(parsed)
        assertNull(parsed!!.amount)
    }

    @Test
    fun `a decoded QR that is not a payment code fails to parse`() {
        assertNull(QrisParser.parse("https://example.com/pay?amount=150000"))
    }

    /**
     * Removes one top-level field and recomputes the CRC, so the resulting payload is
     * still structurally well-formed -- the parse must fail on the missing field itself,
     * not incidentally on a broken checksum.
     */
    private fun withoutTag(payload: String, tag: String, value: String): String {
        val body = payload.dropLast(8).replace(tlv(tag, value), "")
        val withCrcHeader = body + "6304"
        return withCrcHeader + CrcValidator.crc16(withCrcHeader)
    }
}
