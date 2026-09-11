package com.pamt.hacknusaqris

import android.app.Application

/**
 * Release builds intentionally install no App Check provider.
 *
 * A real release would use Play Integrity, but this is a hackathon demo that only ever ships
 * the debug build (CLAUDE.md 6: do not spend demo time on production attestation). This stub
 * exists so the release variant compiles rather than silently depending on debug-only code.
 */
@Suppress("unused")
fun Application.installAppCheck() {
    // No-op.
}
