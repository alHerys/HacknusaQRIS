package com.pamt.hacknusaqris

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pamt.hacknusaqris.ui.VerificationViewModel
import com.pamt.hacknusaqris.ui.VerifierApp
import com.pamt.hacknusaqris.ui.theme.QrisVerifierTheme

class MainActivity : ComponentActivity() {

    private val viewModel: VerificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Only on a genuinely new launch. On Activity recreation the ViewModel survives and
        // already holds the result, and re-running the pipeline would re-read a URI whose
        // grant may be gone.
        if (savedInstanceState == null) {
            viewModel.onSharedIntent(intent)
        }

        setContent {
            QrisVerifierTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VerifierApp(
                        state = state,
                        onRetry = viewModel::retryAnalysis,
                        onDone = ::finish,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Mandatory. Without it getIntent() keeps returning the FIRST intent forever, so a
        // second share (Fixture B) would silently be analysed as the first one (Fixture A).
        setIntent(intent)
        viewModel.onSharedIntent(intent)
    }
}
