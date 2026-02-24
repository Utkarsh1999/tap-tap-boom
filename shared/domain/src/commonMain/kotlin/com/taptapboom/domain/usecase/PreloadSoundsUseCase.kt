package com.taptapboom.domain.usecase

import com.taptapboom.domain.audio.AudioEngine
import com.taptapboom.domain.repository.SoundRepository

/**
 * Preloads all sounds from the active sound pack into the audio engine's memory buffers.
 * This must be called during app initialization to ensure zero-latency playback.
 *
 * Returns a map of soundId → audioHandle for runtime lookup.
 */
class PreloadSoundsUseCase(
    private val soundRepository: SoundRepository,
    private val audioEngine: AudioEngine
) {
    /**
     * Load the sound pack and preload all audio files.
     * @return Map of Sound.id to audio engine handle
     */
    suspend operator fun invoke(): Map<String, Int> {
        val pack = soundRepository.loadSoundPack()
        val handles = mutableMapOf<String, Int>()

        pack.sounds.forEach { sound ->
            val handle = audioEngine.preload(sound.file)
            handles[sound.id] = handle
        }

        return handles
    }
}
