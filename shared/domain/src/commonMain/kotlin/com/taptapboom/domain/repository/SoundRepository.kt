package com.taptapboom.domain.repository

import com.taptapboom.domain.model.Sound
import com.taptapboom.domain.model.SoundPack

/**
 * Repository interface for accessing sound pack data.
 */
interface SoundRepository {
    /**
     * Load the active sound pack from bundled assets.
     */
    suspend fun loadSoundPack(): SoundPack

    /**
     * Resolve a key mapping character to its corresponding sound.
     * @return Sound if found, null otherwise
     */
    fun getSoundForKey(key: String): Sound?

    /**
     * Get a sound by its index position (for touch-based mapping).
     */
    fun getSoundByIndex(index: Int): Sound?

    /**
     * Get all sounds in the current pack.
     */
    fun getAllSounds(): List<Sound>
}
