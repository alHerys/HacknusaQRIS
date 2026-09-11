package com.pamt.hacknusaqris.ui

import androidx.compose.ui.graphics.ImageBitmap
import com.pamt.hacknusaqris.result.VerificationResult

enum class StageState { PENDING, RUNNING, DONE }

data class Stages(
    val readMessage: StageState = StageState.PENDING,
    val decodeQr: StageState = StageState.PENDING,
    val checkPaymentData: StageState = StageState.PENDING,
    val compareDetails: StageState = StageState.PENDING
)

/** Only the failures that can actually break the live demo (CLAUDE.md 23). */
enum class FailureKind {
    MISSING_CAPTION,
    MISSING_IMAGE,
    NO_QR,
    PARSE_FAILED,
    ANALYSIS_FAILED
}

sealed interface UiState {

    /** Opened from the launcher rather than a share. */
    data object Idle : UiState

    data class Analyzing(
        val caption: String,
        val preview: ImageBitmap?,
        val stages: Stages
    ) : UiState

    data class Success(
        val caption: String,
        val preview: ImageBitmap?,
        val result: VerificationResult
    ) : UiState

    data class Failure(
        val kind: FailureKind,
        val caption: String?,
        val preview: ImageBitmap?
    ) : UiState {
        /** Only the Gemini step is worth retrying; the rest need a fresh share. */
        val retryable: Boolean get() = kind == FailureKind.ANALYSIS_FAILED
    }
}
