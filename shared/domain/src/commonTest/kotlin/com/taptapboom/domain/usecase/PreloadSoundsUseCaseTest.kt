package com.taptapboom.domain.usecase

import com.taptapboom.domain.audio.AudioEngine
import com.taptapboom.domain.model.AnimationType
import com.taptapboom.domain.model.Sound
import com.taptapboom.domain.model.SoundPack
import com.taptapboom.domain.repository.SoundRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for PreloadSoundsUseCase.
 * Verifies that all sounds are preloaded and handles are correctly mapped.
 */
class PreloadSoundsUseCaseTest {

    private val testSounds = listOf(
        Sound("s01", "Kick", "kick.wav", AnimationType.RIPPLE, "#FF6B6B", "Q"),
        Sound("s02", "Snare", "snare.wav", AnimationType.BURST, "#4ECDC4", "W"),
    )

    private val testPack = SoundPack("test-pack", "Test Pack", 1, testSounds)

    @Test
    fun `preload returns handle map for all sounds`() = runTest {
        val repo = object : SoundRepository {
            override suspend fun loadSoundPack() = testPack
            override fun getSoundForKey(key: String) = null
            override fun getSoundByIndex(index: Int) = null
            override fun getAllSounds() = testSounds
        }

        var preloadCount = 0
        val engine = object : AudioEngine {
            override suspend fun preload(assetPath: String): Int {
                preloadCount++
                return preloadCount // Return incrementing handles
            }
            override fun play(handle: Int, pitch: Float) {}
            override fun stopAll() {}
            override fun release() {}
        }

        val useCase = PreloadSoundsUseCase(repo, engine)
        val handles = useCase()

        assertEquals(2, handles.size)
        assertEquals(1, handles["s01"])
        assertEquals(2, handles["s02"])
        assertEquals(2, preloadCount)
    }

    @Test
    fun `preload with empty pack returns empty map`() = runTest {
        val emptyPack = SoundPack("empty", "Empty", 1, emptyList())
        val repo = object : SoundRepository {
            override suspend fun loadSoundPack() = emptyPack
            override fun getSoundForKey(key: String) = null
            override fun getSoundByIndex(index: Int) = null
            override fun getAllSounds() = emptyList<Sound>()
        }

        val engine = object : AudioEngine {
            override suspend fun preload(assetPath: String) = -1
            override fun play(handle: Int, pitch: Float) {}
            override fun stopAll() {}
            override fun release() {}
        }

        val useCase = PreloadSoundsUseCase(repo, engine)
        val handles = useCase()

        assertTrue(handles.isEmpty())
    }

    @Test
    fun `preload calls engine with correct file paths`() = runTest {
        val preloadedPaths = mutableListOf<String>()
        val repo = object : SoundRepository {
            override suspend fun loadSoundPack() = testPack
            override fun getSoundForKey(key: String) = null
            override fun getSoundByIndex(index: Int) = null
            override fun getAllSounds() = testSounds
        }

        val engine = object : AudioEngine {
            override suspend fun preload(assetPath: String): Int {
                preloadedPaths.add(assetPath)
                return 1
            }
            override fun play(handle: Int, pitch: Float) {}
            override fun stopAll() {}
            override fun release() {}
        }

        val useCase = PreloadSoundsUseCase(repo, engine)
        useCase()

        assertEquals(listOf("kick.wav", "snare.wav"), preloadedPaths)
    }
}
