package com.taptapboom.ui.mvi

/**
 * User intents for the interactive canvas.
 * Follows the MVI pattern — all user/system events are modeled as sealed intents.
 */
sealed interface CanvasIntent {
    /**
     * User tapped the canvas.
     * @param x, y absolute coordinates
     * @param width, height screen dimensions to resolve grid
     */
    data class TapPad(val x: Float, val y: Float, val screenWidth: Float, val screenHeight: Float, val pointerId: Int = 0) : CanvasIntent

    /**
     * User dragged/swiped in a pad to cycle its sound.
     */
    data class RotateSound(val x: Float, val y: Float, val screenWidth: Float, val screenHeight: Float) : CanvasIntent

    /**
     * User pressed a key (desktop/keyboard mode).
     */
    data class KeyPress(val key: Char) : CanvasIntent
}
