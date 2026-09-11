package com.pamt.hacknusaqris.qr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import kotlin.math.max

/** The image could not be read or decoded at all (as opposed to containing no QR). */
class ImageReadException(cause: Throwable) : Exception(cause)

private const val MAX_DIMENSION = 2048

/**
 * Reads the shared content:// URI exactly once, into an in-memory bitmap.
 *
 * The read grant WhatsApp attaches to the share intent is transient and tied to this
 * Activity's intent, so everything downstream (QR decode, preview, retry) works from
 * this bitmap and never re-opens the URI.
 *
 * Downsampling matters: WhatsApp images can be several thousand pixels on a side, and
 * decoding one at full size risks OOM on a mid-range demo phone.
 */
suspend fun loadSharedImage(context: Context, uri: Uri): Bitmap = withContext(Dispatchers.IO) {
    val bytes = try {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw ImageReadException(IllegalStateException("null input stream for $uri"))
    } catch (e: SecurityException) {
        throw ImageReadException(e)      // read grant already gone
    } catch (e: FileNotFoundException) {
        throw ImageReadException(e)
    }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

    var sample = 1
    while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_DIMENSION) sample *= 2

    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply {
        inSampleSize = sample
        // ARGB_8888 is required: the ZXing fallback calls Bitmap.getPixels(), which
        // throws on HARDWARE-backed bitmaps. BitmapFactory never produces those, but
        // anyone switching to ImageDecoder must set ALLOCATOR_SOFTWARE.
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }) ?: throw ImageReadException(IllegalStateException("not a decodable image"))
}
