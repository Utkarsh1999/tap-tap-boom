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
     * Key-press events use key mapping; touch events use position-based hashing.
     */
    operator fun invoke(event: InteractionEvent): Sound? {
        // Key press → direct key mapping
        event.key?.let { key ->
            return soundRepository.getSoundForKey(key.uppercase())
        }

        // Touch event → hash position into sound index for variety
        // Divides screen into regions so different tap positions trigger different sounds
        val sounds = soundRepository.getAllSounds()
        if (sounds.isEmpty()) return null
        val hash = abs((event.x * 7 + event.y * 13 + event.pointerId * 31).toInt())
        val index = hash % sounds.size
        return soundRepository.getSoundByIndex(index)
    }
}
