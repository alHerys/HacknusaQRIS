package com.pamt.hacknusaqris.qr

import com.pamt.hacknusaqris.fixtures.QrisFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrcValidatorTest {

    private fun crcIndexOf(payload: String): Int =
        EmvTlv.parse(payload)!!.first { it.tag == "63" }.startIndex

    @Test
    fun `canonical CCITT-FALSE check value`() {
        assertEquals(0x29B1, CrcValidator.crc16("123456789".toByteArray()))
        assertEquals("29B1", CrcValidator.crc16("123456789"))
    }

    @Test
    fun `fixture A crc is valid`() {
        val payload = QrisFixtures.payloadA
        assertTrue(CrcValidator.validate(payload, crcIndexOf(payload)).valid)
    }

    @Test
    fun `fixture B crc is valid`() {
        val payload = QrisFixtures.payloadB
        assertTrue(CrcValidator.validate(payload, crcIndexOf(payload)).valid)
    }

    @Test
    fun `mutating the merchant name invalidates the crc`() {
        val payload = QrisFixtures.payloadA
        val index = crcIndexOf(payload)
        // Same length, different content -- so every offset, including the CRC index, holds.
        val tampered = payload.replace("COMPFEST UI", "COMPFEST UX")
        assertEquals(payload.length, tampered.length)

        val check = CrcValidator.validate(tampered, index)
        assertFalse(check.valid)
        assertEquals(payload.takeLast(4), check.embedded)
    }

    @Test
    fun `lowercase embedded crc is still accepted`() {
        val payload = QrisFixtures.payloadA
        val index = crcIndexOf(payload)
        val lowercased = payload.dropLast(4) + payload.takeLast(4).lowercase()

        assertTrue(CrcValidator.validate(lowercased, index).valid)
    }

    @Test
    fun `payload shorter than the crc field is rejected without crashing`() {
        val check = CrcValidator.validate("6304", 0)
        assertFalse(check.valid)
    }

    @Test
    fun `index not pointing at a crc header is rejected`() {
        val payload = QrisFixtures.payloadA
        assertFalse(CrcValidator.validate(payload, 0).valid)
    }

    @Test
    fun `negative index is rejected`() {
        assertFalse(CrcValidator.validate(QrisFixtures.payloadA, -1).valid)
    }
}
