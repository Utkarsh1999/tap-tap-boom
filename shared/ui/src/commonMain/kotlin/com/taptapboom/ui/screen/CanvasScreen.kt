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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
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

    // Background color from hue: using lower saturation and lightness for a premium dark feel
    val bgColor = remember(state.backgroundHue) {
        Color.hsl(state.backgroundHue, 0.25f, 0.04f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .graphicsLayer {
                // Apply Screen Shake
                translationX = state.screenShakeOffset.x
                translationY = state.screenShakeOffset.y
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val size = this.size
                        
                        when (event.type) {
                            PointerEventType.Press -> {
                                event.changes.forEach { change ->
                                    viewModel.onIntent(
                                        CanvasIntent.TapPad(
                                            x = change.position.x,
                                            y = change.position.y,
                                            screenWidth = size.width.toFloat(),
                                            screenHeight = size.height.toFloat(),
                                            pointerId = change.id.value.toInt()
                                        )
                                    )
                                }
                            }
                            PointerEventType.Move -> {
                                // Rotate sound on drag
                                event.changes.forEach { change ->
                                    if (change.positionChange().getDistance() > 10f) {
                                        viewModel.onIntent(
                                            CanvasIntent.RotateSound(
                                                x = change.position.x,
                                                y = change.position.y,
                                                screenWidth = size.width.toFloat(),
                                                screenHeight = size.height.toFloat()
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / state.gridCols
            val cellHeight = size.height / state.gridRows

            // 1. Render Grid Highlights (from state)
            state.highlightedPads.forEach { (cell, _) ->
                val (row, col) = cell
                val intensity = 0.1f + (state.energy * 0.4f)
                drawRect(
                    color = Color.White.copy(alpha = intensity),
                    topLeft = Offset(col * cellWidth, row * cellHeight),
                    size = androidx.compose.ui.geometry.Size(cellWidth, cellHeight)
                )
            }

            // 2. Render Grid Lines
            val gridAlpha = 0.05f + (state.energy * 0.15f)
            for (i in 1 until state.gridCols) {
                val x = i * cellWidth
                drawLine(
                    color = Color.White.copy(alpha = gridAlpha),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }
            for (i in 1 until state.gridRows) {
                val y = i * cellHeight
                drawLine(
                    color = Color.White.copy(alpha = gridAlpha),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            // 3. Render Animations
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
