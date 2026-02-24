package com.taptapboom.ui.mvi

/**
 * User intents for the interactive canvas.
 * Follows the MVI pattern — all user/system events are modeled as sealed intents.
 */
sealed interface CanvasIntent {
    /**
     * User tapped the canvas at coordinates (x, y).
     * @param pointerId identifies the finger for multi-touch
     */
    data class Tap(val x: Float, val y: Float, val pointerId: Int = 0) : CanvasIntent

    /**
     * User pressed a key (desktop/keyboard mode).
     */
    data class KeyPress(val key: Char) : CanvasIntent

    /**
     * An animation has completed its lifecycle and should be removed.
     */
    data class AnimationCompleted(val animationId: String) : CanvasIntent
}
