package com.pamt.hacknusaqris.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pamt.hacknusaqris.ui.theme.InkMuted
import com.pamt.hacknusaqris.ui.theme.VerifierText

@Composable
fun IdleScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text("QRIS Verifier", style = VerifierText.Verdict)
        Text(
            "To use this demo, share a QRIS payment request from WhatsApp " +
                "to QRIS Verifier.",
            style = VerifierText.Body.copy(color = InkMuted),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
