package com.taptapboom.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a complete sound pack with metadata and sound entries.
 */
@Serializable
data class SoundPack(
    val packId: String,
    val packName: String,
    val version: Int,
    val sounds: List<Sound>
)
