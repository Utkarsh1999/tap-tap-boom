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
    val backgroundHue: Float = 220f,
    val gridRows: Int = 4,
    val gridCols: Int = 3,
    /** Maps (row, col) -> assigned Sound ID */
    val padAssignments: Map<Pair<Int, Int>, String> = emptyMap(),
    /** Maps (row, col) -> expiry time nanos for visual highlight */
    val highlightedPads: Map<Pair<Int, Int>, Long> = emptyMap(),
    /** Interaction intensity [0.0 - 1.0], builds with rapid taps */
    val energy: Float = 0f,
    /** Screen displacement for dopamine hits */
    val screenShakeOffset: Offset = Offset.Zero,
    /** Full-screen flash intensity [0.0 - 1.0] */
    val flashIntensity: Float = 0f
)

/**
 * Represents a single active animation being rendered on the canvas.
 */
data class ActiveAnimation(
    val id: String,
    val type: AnimationType,
    val origin: Offset,
    val color: Color,
    val isFullScreen: Boolean = false,
    val progress: Float = 0f,
    val startTimeNanos: Long = 0L,
    val durationMs: Long = 800L
)
