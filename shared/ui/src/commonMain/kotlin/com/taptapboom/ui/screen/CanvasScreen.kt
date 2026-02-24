package com.taptapboom.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.taptapboom.ui.animation.AnimationRenderer.renderAnimation
import com.taptapboom.ui.mvi.CanvasIntent
import com.taptapboom.ui.viewmodel.CanvasViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.TimeSource

/**
 * The main interactive canvas screen.
 * Full-screen, zero-UI. Taps trigger animations and sounds.
 *
 * Multi-touch: uses awaitPointerEventScope to capture all pointer inputs.
 * Key press origin sentinel (-1f, -1f) is mapped to screen center.
 * Audio playback is handled by ViewModel internally — no AudioEngine reference here.
 */
@Composable
fun CanvasScreen(
    viewModel: CanvasViewModel
) {
    val state by viewModel.state.collectAsState()

    val timeSource = remember { TimeSource.Monotonic }
    val startMark = remember { timeSource.markNow() }

    // Animation driver: runs continuously, reads state directly
    LaunchedEffect(Unit) {
        while (isActive) {
            val now = startMark.elapsedNow().inWholeNanoseconds
            viewModel.updateAnimations(now)
            delay(16) // ~60fps
        }
    }

    // Background color from hue
    val bgColor = remember(state.backgroundHue) {
        Color.hsl(state.backgroundHue, 0.15f, 0.08f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .pointerInput(Unit) {
                // Multi-touch support: captures all pointer events
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press) {
                            event.changes.forEach { change ->
                                viewModel.onIntent(
                                    CanvasIntent.Tap(
                                        x = change.position.x,
                                        y = change.position.y,
                                        pointerId = change.id.value.toInt()
                                    )
                                )
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Map key press sentinel origin (-1,-1) to screen center
            state.animations.forEach { animation ->
                val resolvedAnimation = if (animation.origin.x < 0f && animation.origin.y < 0f) {
                    animation.copy(origin = Offset(size.width / 2f, size.height / 2f))
                } else {
                    animation
                }
                renderAnimation(resolvedAnimation)
            }
        }
    }
}
