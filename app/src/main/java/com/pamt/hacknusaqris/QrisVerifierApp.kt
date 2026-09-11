package com.pamt.hacknusaqris

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize

class QrisVerifierApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)

        // Variant-specific: the debug source set installs the debug App Check provider,
        // the release source set does nothing. This keeps firebase-appcheck-debug off the
        // release classpath entirely rather than shipping a debug attestation provider.
        installAppCheck()
    }
}
