package com.pamt.hacknusaqris.ui

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pamt.hacknusaqris.ai.AiConfig
import com.pamt.hacknusaqris.ai.AnalysisEvidence
import com.pamt.hacknusaqris.ai.GeminiPaymentAnalyzer
import com.pamt.hacknusaqris.qr.ImageReadException
import com.pamt.hacknusaqris.qr.MlKitThenZxingQrDecoder
import com.pamt.hacknusaqris.qr.QrisParser
import com.pamt.hacknusaqris.qr.loadSharedImage
import com.pamt.hacknusaqris.result.LocalResultValidator
import com.pamt.hacknusaqris.share.ShareIntentReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "QrisVerifier"

class VerificationViewModel(app: Application) : AndroidViewModel(app) {

    private val decoder = MlKitThenZxingQrDecoder()
    private val analyzer = GeminiPaymentAnalyzer()

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Kept so Retry re-runs only the Gemini call and never re-reads the shared URI. */
    private var cachedEvidence: AnalysisEvidence? = null
    private var cachedPreview: ImageBitmap? = null

    private var job: Job? = null

    fun onSharedIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) {
            _state.value = UiState.Idle
            return
        }
        job?.cancel()
        cachedEvidence = null
        cachedPreview = null
        job = viewModelScope.launch { runPipeline(intent) }
    }

    fun retryAnalysis() {
        val evidence = cachedEvidence ?: return
        job?.cancel()
        job = viewModelScope.launch { runGemini(evidence) }
    }

    private suspend fun runPipeline(intent: Intent) {
        val request = ShareIntentReader.read(intent)
        Log.d(TAG, "share received: caption=${request.caption != null} uri=${request.imageUri}")

        if (request.imageUri == null) {
            fail(FailureKind.MISSING_IMAGE, request.caption)
            return
        }
        if (request.caption == null) {
            fail(FailureKind.MISSING_CAPTION, null)
            return
        }
        val caption = request.caption

        var stages = Stages(
            readMessage = StageState.DONE,
            decodeQr = StageState.RUNNING
        )
        _state.value = UiState.Analyzing(caption, null, stages)

        // Single URI read, immediately, while the transient grant is still alive.
        val bitmap = try {
            loadSharedImage(getApplication(), request.imageUri)
        } catch (e: ImageReadException) {
            Log.w(TAG, "image read failed", e)
            fail(FailureKind.MISSING_IMAGE, caption)
            return
        }
        cachedPreview = bitmap.asImageBitmap()
        _state.value = UiState.Analyzing(caption, cachedPreview, stages)

        val rawQr = try {
            decoder.decode(bitmap)
        } catch (e: ImageReadException) {
            Log.w(TAG, "decode failed on unreadable image", e)
            fail(FailureKind.MISSING_IMAGE, caption)
            return
        }
        if (rawQr == null) {
            fail(FailureKind.NO_QR, caption)
            return
        }

        stages = stages.copy(decodeQr = StageState.DONE, checkPaymentData = StageState.RUNNING)
        _state.value = UiState.Analyzing(caption, cachedPreview, stages)

        val parsed = QrisParser.parse(rawQr)
        if (parsed == null) {
            Log.w(TAG, "QR decoded but is not a QRIS payment code")
            fail(FailureKind.PARSE_FAILED, caption)
            return
        }
        val crc = QrisParser.crcCheckFor(rawQr)
        Log.d(
            TAG,
            "parsed merchant=${parsed.merchantName} amount=${parsed.amount} crcValid=${crc.valid}"
        )

        stages = stages.copy(
            checkPaymentData = StageState.DONE,
            compareDetails = StageState.RUNNING
        )
        _state.value = UiState.Analyzing(caption, cachedPreview, stages)

        val evidence = AnalysisEvidence(caption, parsed, crc)
        cachedEvidence = evidence
        runGemini(evidence)
    }

    private suspend fun runGemini(evidence: AnalysisEvidence) {
        _state.value = UiState.Analyzing(
            evidence.caption,
            cachedPreview,
            Stages(
                readMessage = StageState.DONE,
                decodeQr = StageState.DONE,
                checkPaymentData = StageState.DONE,
                compareDetails = StageState.RUNNING
            )
        )

        val ai = try {
            withTimeout(AiConfig.TIMEOUT_MS) { analyzer.analyze(evidence) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Log.w(TAG, "analysis failed", e)
            fail(FailureKind.ANALYSIS_FAILED, evidence.caption)
            return
        }

        val result = LocalResultValidator.combine(evidence, ai)
        Log.d(TAG, "verdict=${result.overall} recipient=${result.recipientComparison}")
        _state.value = UiState.Success(evidence.caption, cachedPreview, result)
    }

    private fun fail(kind: FailureKind, caption: String?) {
        Log.w(TAG, "pipeline failed: $kind")
        _state.value = UiState.Failure(kind, caption, cachedPreview)
    }
}
