package com.pamt.hacknusaqris.fixtures

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.pamt.hacknusaqris.qr.CrcValidator
import com.pamt.hacknusaqris.qr.QrisParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.math.BigDecimal

/**
 * Dev tool, not a shipped feature: renders the two demo QR images.
 *
 * Run with:
 *   ./gradlew :app:testDebugUnitTest --tests "*QrisFixtureGeneratorTest*"
 *
 * PNGs land in app/build/fixtures/ and the absolute paths are printed.
 */
class QrisFixtureGeneratorTest {

    /** The definitive assertion for CRC-16/CCITT-FALSE. */
    @Test
    fun `crc16 matches the canonical CCITT-FALSE check value`() {
        assertEquals(0x29B1, CrcValidator.crc16("123456789".toByteArray()))
    }

    @Test
    fun `generate fixture A and fixture B`() {
        val a = QrisFixtures.payloadA
        val b = QrisFixtures.payloadB

        for ((name, payload) in listOf("A" to a, "B" to b)) {
            val parsed = QrisParser.parse(payload)
            assertNotNull("Fixture $name must parse", parsed)
            assertTrue("Fixture $name CRC must be valid", parsed!!.crcValid)
            assertEquals(0, BigDecimal(QrisFixtures.AMOUNT).compareTo(parsed.amount))
            assertEquals("360", parsed.currencyCode)
            assertEquals("ID", parsed.countryCode)
        }

        assertEquals(QrisFixtures.MERCHANT_A, QrisParser.parse(a)!!.merchantName)
        assertEquals(QrisFixtures.MERCHANT_B, QrisParser.parse(b)!!.merchantName)

        println("FIXTURE A payload = $a")
        println("FIXTURE B payload = $b")

        writeQrPng(a, "fixture_a_compfest.png")
        writeQrPng(b, "fixture_b_xyz.png")
    }

    private fun writeQrPng(payload: String, fileName: String) {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            // ECC M gives one level of headroom against WhatsApp's JPEG recompression,
            // and it is what real QRIS codes use.
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 4
        )
        val matrix = MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, 1200, 1200, hints)
        val image = MatrixToImageWriter.toBufferedImage(matrix)

        // Round-trip on the host JVM: proves the emitted image really contains the payload,
        // with no device and no adb involved.
        val decoded = MultiFormatReader()
            .decode(BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(image))))
            .text
        assertEquals("round-trip decode of $fileName", payload, decoded)

        val outDir = File("build/fixtures").apply { mkdirs() }
        val out = outDir.resolve(fileName)
        MatrixToImageWriter.writeToPath(matrix, "PNG", out.toPath())
        println("FIXTURE -> ${out.absolutePath}")
    }
}
