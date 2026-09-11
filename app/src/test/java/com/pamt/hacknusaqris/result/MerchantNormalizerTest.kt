package com.pamt.hacknusaqris.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MerchantNormalizerTest {

    @Test
    fun `normalizes case, punctuation and repeated spaces`() {
        assertEquals("COMPFEST UI", MerchantNormalizer.normalize("  compfest   ui  "))
        assertEquals("COMPFEST UI", MerchantNormalizer.normalize("Compfest-UI"))
    }

    @Test
    fun `case differences still match`() {
        assertTrue(MerchantNormalizer.matches("compfest ui", "COMPFEST UI"))
    }

    @Test
    fun `repeated spaces still match`() {
        assertTrue(MerchantNormalizer.matches("COMPFEST  UI", "COMPFEST UI"))
    }

    @Test
    fun `punctuation differences still match`() {
        assertTrue(MerchantNormalizer.matches("COMPFEST-UI", "COMPFEST UI"))
    }

    @Test
    fun `a truncated extraction still matches by containment`() {
        assertTrue(MerchantNormalizer.matches("COMPFEST", "COMPFEST UI"))
    }

    @Test
    fun `the two demo fixtures do not match each other`() {
        assertFalse(MerchantNormalizer.matches("COMPFEST UI", "XYZ DIGITAL STORE"))
    }

    @Test
    fun `a very short substring must not produce a false match`() {
        // Without the >= 4 char guard, "UI" would be contained in "COMPFEST UI".
        assertFalse(MerchantNormalizer.matches("UI", "COMPFEST UI"))
    }

    @Test
    fun `blank input never matches`() {
        assertFalse(MerchantNormalizer.matches("   ", "COMPFEST UI"))
        assertFalse(MerchantNormalizer.matches("COMPFEST UI", "!!!"))
    }
}
