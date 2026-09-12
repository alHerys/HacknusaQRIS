package com.pamt.hacknusaqris.ai

object AiConfig {
    // Docs currently list gemini-3.7-flash (CLAUDE.md's example of 3.6-flash was
    // illustrative). This is the only place the model name appears -- if it's ever
    // unavailable in the project, change only this constant.
    const val MODEL_NAME = "gemini-3.1-flash-lite"
    const val TIMEOUT_MS = 25_000L
}
