package com.pamt.hacknusaqris.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pamt.hacknusaqris.ui.theme.InkMuted
import com.pamt.hacknusaqris.ui.theme.VerifierText

@Composable
fun ErrorScreen(
    state: UiState.Failure,
    onRetry: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f).padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(headlineFor(state.kind), style = VerifierText.Verdict)
            Text(
                bodyFor(state.kind),
                style = VerifierText.Body.copy(color = InkMuted),
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.retryable) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Retry")
                }
                OutlinedButton(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Done")
                }
            } else {
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

private fun headlineFor(kind: FailureKind) = when (kind) {
    FailureKind.MISSING_CAPTION -> "PAYMENT MESSAGE NOT RECEIVED"
    FailureKind.MISSING_IMAGE -> "QR IMAGE NOT RECEIVED"
    FailureKind.NO_QR -> "NO QR FOUND"
    FailureKind.PARSE_FAILED -> "QR PAYMENT DATA COULD NOT BE READ"
    FailureKind.ANALYSIS_FAILED -> "ANALYSIS COULD NOT COMPLETE"
}

private fun bodyFor(kind: FailureKind) = when (kind) {
    FailureKind.MISSING_CAPTION ->
        "WhatsApp shared the QR image but did not provide the accompanying caption.\n\n" +
            "Return to WhatsApp and share the image message again."

    FailureKind.MISSING_IMAGE ->
        "The shared item did not contain a QR image that could be read."

    FailureKind.NO_QR ->
        "The shared image did not contain a readable QR code."

    FailureKind.PARSE_FAILED ->
        "A QR was detected, but the demo could not parse the expected payment fields."

    FailureKind.ANALYSIS_FAILED ->
        "The payment data was decoded, but the AI analysis could not be reached."
}
