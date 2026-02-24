package com.taptapboom.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a single sound entry within a sound pack.
 */
@Serializable
data class Sound(
    val id: String,
    val label: String,
    val file: String,
    val animationType: AnimationType,
    val color: String,
    val keyMapping: String
)
