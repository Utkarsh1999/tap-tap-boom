package com.taptapboom.ui.mvi

/**
 * One-shot side effects emitted by the ViewModel.
 * These are consumed once and not part of persistent state.
 */
sealed interface CanvasSideEffect {
    /**
     * Trigger audio playback for a preloaded sound.
     */
    data class PlaySound(val handle: Int, val pitch: Float = 1.0f) : CanvasSideEffect
}
