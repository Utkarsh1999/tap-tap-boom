package com.taptapboom.ui.mvi

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.taptapboom.domain.model.AnimationType

/**
 * Immutable state of the interactive canvas.
 * The Canvas composable reads this state on every frame to render.
 */
data class CanvasState(
    val isLoading: Boolean = true,
    val animations: List<ActiveAnimation> = emptyList(),
    val backgroundHue: Float = 220f
)

/**
 * Represents a single active animation being rendered on the canvas.
 */
data class ActiveAnimation(
    val id: String,
    val type: AnimationType,
    val origin: Offset,
    val color: Color,
    val progress: Float = 0f,
    val startTimeNanos: Long = 0L,
    val durationMs: Long = 800L
)
