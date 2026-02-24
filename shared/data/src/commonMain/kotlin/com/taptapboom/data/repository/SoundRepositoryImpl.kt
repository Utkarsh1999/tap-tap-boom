package com.taptapboom.data.repository

import com.taptapboom.domain.model.Sound
import com.taptapboom.domain.model.SoundPack
import com.taptapboom.domain.repository.SoundRepository
import com.taptapboom.data.loader.SoundPackLoader

/**
 * Implementation of SoundRepository.
 * Loads sound pack from bundled JSON assets and provides lookup methods.
 */
class SoundRepositoryImpl(
    private val soundPackLoader: SoundPackLoader
) : SoundRepository {

    private var currentPack: SoundPack? = null
    private var soundsByKey: Map<String, Sound> = emptyMap()
    private var soundsList: List<Sound> = emptyList()

    override suspend fun loadSoundPack(): SoundPack {
        val pack = soundPackLoader.load()
        currentPack = pack
        soundsList = pack.sounds
        soundsByKey = pack.sounds.associateBy { it.keyMapping.uppercase() }
        return pack
    }

    override fun getSoundForKey(key: String): Sound? {
        return soundsByKey[key.uppercase()]
    }

    override fun getSoundByIndex(index: Int): Sound? {
        if (soundsList.isEmpty()) return null
        return soundsList[index % soundsList.size]
    }

    override fun getAllSounds(): List<Sound> = soundsList
}
