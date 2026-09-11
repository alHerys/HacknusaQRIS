package com.pamt.hacknusaqris

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Debug builds only.
 *
 * On first run this prints a debug secret to logcat (tag DebugAppCheckProvider). That secret
 * must be registered in Firebase Console > App Check > Apps > Manage debug tokens before
 * App Check enforcement will accept this install. A fresh install or "clear data" generates
 * a NEW secret, so the registration step repeats after every reinstall.
 */
fun Application.installAppCheck() {
    Firebase.appCheck.installAppCheckProviderFactory(
        DebugAppCheckProviderFactory.getInstance()
    )
}
