package com.taptapboom.domain.model

/**
 * Represents a user interaction event (tap or key press).
 * Carries the position on the canvas and optional key character.
 */
data class InteractionEvent(
    val x: Float,
    val y: Float,
    val pointerId: Int = 0,
    val key: Char? = null,
    val timestampNanos: Long = 0L
)
