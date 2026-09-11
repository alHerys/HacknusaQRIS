package com.pamt.hacknusaqris.qr

import com.pamt.hacknusaqris.fixtures.QrisFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EmvTlvTest {

    @Test
    fun `parses the fixture and reports exact offsets`() {
        val payload = QrisFixtures.payloadA
        val elements = EmvTlv.parse(payload)
        assertNotNull(elements)

        val byTag = elements!!.associateBy { it.tag }
        assertEquals("01", byTag["00"]?.value)
        assertEquals("12", byTag["01"]?.value)
        assertEquals("360", byTag["53"]?.value)
        assertEquals("150000", byTag["54"]?.value)
        assertEquals("COMPFEST UI", byTag["59"]?.value)

        // Every element's startIndex must actually point at its own tag.
        for (element in elements) {
            assertEquals(
                element.tag,
                payload.substring(element.startIndex, element.startIndex + 2)
            )
        }
    }

    @Test
    fun `nested templates are kept opaque, not descended into`() {
        val elements = EmvTlv.parse(QrisFixtures.payloadA)!!
        val template = elements.first { it.tag == "26" }
        assertEquals("ID.CO.QRIS.WWW", template.value.substring(4, 18))
    }

    @Test
    fun `non-numeric length is rejected`() {
        assertNull(EmvTlv.parse("59XXCOMPFEST UI"))
    }

    @Test
    fun `length running past the end is rejected`() {
        assertNull(EmvTlv.parse("5999COMPFEST UI"))
    }

    @Test
    fun `truncated header is rejected`() {
        assertNull(EmvTlv.parse("0002015"))
    }

    @Test
    fun `empty payload is rejected`() {
        assertNull(EmvTlv.parse(""))
    }

    @Test
    fun `non-EMV text is rejected`() {
        assertNull(EmvTlv.parse("https://example.com/pay"))
    }

    @Test
    fun `zero-length value is accepted`() {
        val elements = EmvTlv.parse("6200")
        assertEquals(1, elements?.size)
        assertEquals("", elements!!.first().value)
    }

    @Test
    fun `unicode digits in the length are rejected`() {
        // Arabic-Indic "05". Char.isDigit() would accept these; the parser must not.
        assertNull(EmvTlv.parse("59٠٥ABCDE"))
    }
}
