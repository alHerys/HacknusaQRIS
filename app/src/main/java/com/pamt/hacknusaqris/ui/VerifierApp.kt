package com.pamt.hacknusaqris.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VerifierApp(
    state: UiState,
    onRetry: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
        },
        label = "verifier-state",
        modifier = modifier
    ) { current ->
        when (current) {
            is UiState.Idle -> IdleScreen()
            is UiState.Analyzing -> AnalysisScreen(current)
            is UiState.Success -> ResultScreen(current, onDone = onDone)
            is UiState.Failure -> ErrorScreen(current, onRetry = onRetry, onDone = onDone)
        }
    }
}
