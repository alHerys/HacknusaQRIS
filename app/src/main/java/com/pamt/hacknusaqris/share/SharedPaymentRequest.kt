package com.pamt.hacknusaqris.share

import android.net.Uri

/**
 * The only thing the verifier ever receives: whatever the user explicitly shared.
 *
 * Both fields are nullable because the two failure modes the demo has to explain
 * ("no caption" and "no image") are exactly these two being absent.
 */
data class SharedPaymentRequest(
    val caption: String?,
    val imageUri: Uri?
)
