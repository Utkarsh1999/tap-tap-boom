package com.taptapboom.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.taptapboom.ui.screen.CanvasScreen
import com.taptapboom.ui.viewmodel.CanvasViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main entry point for the Android app.
 * Zero UI — just a fullscreen interactive canvas.
 * Audio playback is handled internally by CanvasViewModel.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: CanvasViewModel = koinViewModel()
            CanvasScreen(viewModel = viewModel)
        }
    }
}
