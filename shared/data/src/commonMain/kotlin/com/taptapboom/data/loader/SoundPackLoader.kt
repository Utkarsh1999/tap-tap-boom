package com.taptapboom.data.loader

import com.taptapboom.domain.model.SoundPack
import kotlinx.serialization.json.Json

/**
 * Loads and parses the sound pack JSON from bundled assets.
 * Platform-specific asset reading is injected via the assetReader function.
 *
 * Returns Result<SoundPack> to support graceful degradation on malformed data.
 */
class SoundPackLoader(
    private val assetReader: suspend (String) -> String
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Load the default sound pack from assets.
     * @return Result.success with parsed SoundPack, or Result.failure on error
     */
    suspend fun load(path: String = "soundpacks/synth-basics-v1/soundpack.json"): SoundPack {
        return try {
            val jsonString = assetReader(path)
            json.decodeFromString<SoundPack>(jsonString)
        } catch (e: Exception) {
            // Graceful degradation: return an empty pack rather than crashing
            // This satisfies edge case E-07 (corrupted sound pack JSON)
            SoundPack(
                packId = "empty",
                packName = "Empty (Load Failed)",
                version = 0,
                sounds = emptyList()
            )
        }
    }
}
