package com.pamt.hacknusaqris.qr

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.ChecksumException
import com.google.zxing.DecodeHintType
import com.google.zxing.FormatException
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "QrisVerifier"

interface QrDecoder {
    /**
     * @return the raw QR payload, or null if the image contained no readable QR.
     * @throws ImageReadException if the image itself could not be read or processed.
     */
    suspend fun decode(bitmap: Bitmap): String?
}

/**
 * ML Kit first, ZXing as a fallback.
 *
 * The fallback is not expected to fire: the fixtures are 1200px ECC-M codes, which have
 * large headroom against WhatsApp's JPEG recompression. It exists because the risk is
 * asymmetric -- if ML Kit fails on the judging device the whole pipeline dies at step two.
 */
class MlKitThenZxingQrDecoder : QrDecoder {

    private val scanner: BarcodeScanner by lazy {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    override suspend fun decode(bitmap: Bitmap): String? = withContext(Dispatchers.Default) {
        val fromMlKit = try {
            scanner.processAwait(InputImage.fromBitmap(bitmap, 0))
                .firstNotNullOfOrNull { it.rawValue?.takeIf(String::isNotBlank) }
        } catch (e: Throwable) {
            throw ImageReadException(e)
        }

        if (fromMlKit != null) {
            Log.d(TAG, "QR decoded by ML Kit (${fromMlKit.length} chars)")
            return@withContext fromMlKit
        }

        // Only reached when ML Kit found no QR. If the image itself were unreadable ML Kit
        // would have thrown above, and ZXing would fail identically.
        val fromZxing = decodeWithZxing(bitmap)
        if (fromZxing != null) {
            Log.w(TAG, "QR decoded by ZXing FALLBACK (${fromZxing.length} chars) -- ML Kit missed it")
        } else {
            Log.d(TAG, "no QR found by either decoder")
        }
        fromZxing
    }

    private fun decodeWithZxing(bitmap: Bitmap): String? {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)

        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.CHARACTER_SET to "UTF-8"
        )

        // Hybrid handles uneven lighting; GlobalHistogram handles the flat, evenly-lit,
        // JPEG-ringed images WhatsApp produces.
        val binarizers = listOf(HybridBinarizer(source), GlobalHistogramBinarizer(source))
        for (binarizer in binarizers) {
            try {
                val reader = MultiFormatReader().apply { setHints(hints) }
                return reader.decodeWithState(BinaryBitmap(binarizer)).text
                    ?.takeIf(String::isNotBlank)
            } catch (_: NotFoundException) {
                // try the next binarizer
            } catch (_: ChecksumException) {
            } catch (_: FormatException) {
            }
        }
        return null
    }
}

private suspend fun BarcodeScanner.processAwait(image: InputImage): List<Barcode> =
    suspendCancellableCoroutine { cont ->
        process(image)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
            .addOnCanceledListener { cont.cancel() }
    }
