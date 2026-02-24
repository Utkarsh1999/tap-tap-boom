package com.taptapboom.domain.usecase

import com.taptapboom.domain.model.InteractionEvent
import com.taptapboom.domain.model.Sound
import com.taptapboom.domain.repository.SoundRepository
import kotlin.math.abs

/**
 * Resolves an interaction event into the appropriate Sound mapping.
 * Handles both key-press and touch-based interactions.
 */
class TriggerInteractionUseCase(
    private val soundRepository: SoundRepository
) {
    /**
     * Map an interaction event to its sound.
     * Keep legacy spatial hashing as a fallback for non-grid interactions if needed.
     */
    operator fun invoke(event: InteractionEvent): Sound? {
        if (event.key != null) return getSoundForKey(event.key.uppercase())
        
        val sounds = soundRepository.getAllSounds()
        if (sounds.isEmpty()) return null
        val hash = abs((event.x * 7 + event.y * 13 + event.pointerId * 31).toInt())
        val index = hash % sounds.size
        return soundRepository.getSoundByIndex(index)
    }

    fun getSoundById(id: String): Sound? = soundRepository.getAllSounds().find { it.id == id }

    fun getSoundForKey(key: String): Sound? = soundRepository.getSoundForKey(key)
}
