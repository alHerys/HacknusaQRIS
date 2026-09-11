package com.pamt.hacknusaqris.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException

private const val TAG = "QrisVerifier"

class GeminiPaymentAnalyzer {

    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = AiConfig.MODEL_NAME,
            generationConfig = generationConfig {
                // The single highest-leverage line here: CLAUDE.md 27 requires the same JSON
                // on repeated runs, and a default temperature makes that a coin flip.
                temperature = 0f
                responseMimeType = "application/json"
                responseSchema = AnalysisSchema.schema
            },
            systemInstruction = content { text(GeminiPrompt.SYSTEM_INSTRUCTION) }
        )
    }

    suspend fun analyze(evidence: AnalysisEvidence): GeminiAnalysisResult {
        val prompt = GeminiPrompt.buildEvidenceJson(evidence)
        val raw = try {
            model.generateContent(prompt).text
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            // Essential: an App Check misconfiguration surfaces as an opaque 403, and
            // logcat is the only place that says so.
            Log.w(TAG, "Gemini call failed: ${e::class.simpleName}: ${e.message}", e)
            throw AiAnalysisException(e)
        } ?: throw AiAnalysisException(IllegalStateException("empty response text"))

        return try {
            GeminiResponseParser.parse(raw)
        } catch (e: Throwable) {
            Log.w(TAG, "Gemini response was not parseable: $raw", e)
            throw AiAnalysisException(e)
        }
    }
}
