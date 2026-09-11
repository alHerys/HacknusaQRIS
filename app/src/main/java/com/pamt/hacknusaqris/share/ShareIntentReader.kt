package com.pamt.hacknusaqris.share

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

/**
 * Extracts the shared image and its caption from an [Intent.ACTION_SEND] intent.
 *
 * This reads only what the sending app put in the share intent. It never touches
 * WhatsApp's storage, notifications, or conversation history.
 */
object ShareIntentReader {

    fun read(intent: Intent?): SharedPaymentRequest {
        if (intent == null || intent.action != Intent.ACTION_SEND) {
            return SharedPaymentRequest(caption = null, imageUri = null)
        }
        return SharedPaymentRequest(
            caption = extractCaption(intent),
            imageUri = extractImageUri(intent)
        )
    }

    private fun extractImageUri(intent: Intent): Uri? {
        // IntentCompat handles both the API 33 getParcelableExtra deprecation and the
        // Android 13 regression where the typed overload returns null for values that
        // unmarshal fine via the untyped one. A hand-rolled SDK_INT branch reproduces
        // that bug, so don't write one.
        IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            ?.let { return it }

        val clip = intent.clipData ?: return null
        for (i in 0 until clip.itemCount) {
            clip.getItemAt(i).uri?.let { return it }
        }
        return null
    }

    private fun extractCaption(intent: Intent): String? {
        // EXTRA_TEXT is documented as CharSequence. getStringExtra() returns null when the
        // sender put a Spanned/SpannableString, which some WhatsApp builds do -- and a
        // silently missing caption is the one failure this demo cannot recover from.
        intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.blankToNull()
            ?.let { return it }

        val clip = intent.clipData ?: return null
        for (i in 0 until clip.itemCount) {
            clip.getItemAt(i).text?.toString()?.blankToNull()?.let { return it }
        }
        return null
    }

    /**
     * Collapses absent and blank into null on purpose: the spec defines exactly one
     * user-facing message for "no caption", and there is nothing a user could do
     * differently for a whitespace-only one.
     */
    private fun String.blankToNull(): String? = trim().takeIf { it.isNotEmpty() }
}
