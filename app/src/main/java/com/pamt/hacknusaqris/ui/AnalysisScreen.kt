package com.pamt.hacknusaqris.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pamt.hacknusaqris.ui.components.PanelCard
import com.pamt.hacknusaqris.ui.components.SectionCap
import com.pamt.hacknusaqris.ui.components.StageChecklist
import com.pamt.hacknusaqris.ui.theme.VerifierText

@Composable
fun AnalysisScreen(state: UiState.Analyzing, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Payment request received", style = VerifierText.Verdict)

        PanelCard {
            SectionCap("Message")
            Text(
                "“${state.caption}”",
                style = VerifierText.Body,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        state.preview?.let { preview ->
            PanelCard {
                SectionCap("QR")
                Image(
                    bitmap = preview,
                    contentDescription = "Shared payment QR code",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .padding(top = 8.dp)
                )
            }
        }

        PanelCard {
            SectionCap("Analyzing payment request")
            StageChecklist(
                stages = listOf(
                    "Reading message" to state.stages.readMessage,
                    "Decoding QR" to state.stages.decodeQr,
                    "Checking payment data" to state.stages.checkPaymentData,
                    "Comparing payment details" to state.stages.compareDetails
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
