package com.pamt.hacknusaqris.share

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric is needed here because Intent/Uri/ClipData cannot be constructed on a bare
 * JVM -- and this is the one class that can silently return null on the judging device.
 */
@RunWith(RobolectricTestRunner::class)
class ShareIntentReaderTest {

    private val imageUri: Uri = Uri.parse("content://com.whatsapp.provider.media/item/1")
    private val caption = "Bayar registrasi Rp150.000 ke COMPFEST UI"

    private fun sendIntent(block: Intent.() -> Unit): Intent =
        Intent(Intent.ACTION_SEND).apply { type = "image/jpeg" }.apply(block)

    @Test
    fun `image and caption via extras`() {
        val result = ShareIntentReader.read(sendIntent {
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, caption)
        })

        assertEquals(caption, result.caption)
        assertEquals(imageUri, result.imageUri)
    }

    @Test
    fun `image without caption`() {
        val result = ShareIntentReader.read(sendIntent { putExtra(Intent.EXTRA_STREAM, imageUri) })

        assertNull(result.caption)
        assertEquals(imageUri, result.imageUri)
    }

    @Test
    fun `caption without image`() {
        val result = ShareIntentReader.read(sendIntent { putExtra(Intent.EXTRA_TEXT, caption) })

        assertEquals(caption, result.caption)
        assertNull(result.imageUri)
    }

    /** This is the test that justifies getCharSequenceExtra; getStringExtra returns null here. */
    @Test
    fun `caption delivered as a SpannableString is still read`() {
        val result = ShareIntentReader.read(sendIntent {
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, SpannableString(caption))
        })

        assertEquals(caption, result.caption)
    }

    @Test
    fun `uri recovered from clipData when EXTRA_STREAM is absent`() {
        // Built by hand rather than via ClipData.newUri(), which needs a live
        // ContentResolver to sniff the MIME type.
        val result = ShareIntentReader.read(sendIntent {
            clipData = ClipData(
                ClipDescription("image", arrayOf("image/jpeg")),
                ClipData.Item(imageUri)
            )
        })

        assertEquals(imageUri, result.imageUri)
    }

    @Test
    fun `caption recovered from clipData when EXTRA_TEXT is absent`() {
        val result = ShareIntentReader.read(sendIntent {
            putExtra(Intent.EXTRA_STREAM, imageUri)
            clipData = ClipData(
                ClipDescription("label", arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)),
                ClipData.Item(caption)
            )
        })

        assertEquals(caption, result.caption)
    }

    @Test
    fun `whitespace-only caption is treated as absent`() {
        val result = ShareIntentReader.read(sendIntent {
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, "   \n  ")
        })

        assertNull(result.caption)
    }

    @Test
    fun `caption is trimmed`() {
        val result = ShareIntentReader.read(sendIntent {
            putExtra(Intent.EXTRA_TEXT, "  $caption  ")
        })

        assertEquals(caption, result.caption)
    }

    @Test
    fun `non-send action yields nothing`() {
        val result = ShareIntentReader.read(Intent(Intent.ACTION_VIEW).apply {
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, caption)
        })

        assertNull(result.caption)
        assertNull(result.imageUri)
    }

    @Test
    fun `null intent yields nothing`() {
        val result = ShareIntentReader.read(null)

        assertNull(result.caption)
        assertNull(result.imageUri)
    }
}
