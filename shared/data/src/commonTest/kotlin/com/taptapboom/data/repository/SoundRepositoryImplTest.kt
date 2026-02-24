package com.taptapboom.data.repository

import com.taptapboom.data.loader.SoundPackLoader
import com.taptapboom.domain.model.AnimationType
import com.taptapboom.domain.model.Sound
import com.taptapboom.domain.model.SoundPack
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for SoundRepositoryImpl.
 */
class SoundRepositoryImplTest {

    private val testJson = """
    {
        "packId": "test",
        "packName": "Test Pack",
        "version": 1,
        "sounds": [
            {"id":"s01","label":"Kick","file":"kick.wav","animationType":"ripple","color":"#FF6B6B","keyMapping":"Q"},
            {"id":"s02","label":"Snare","file":"snare.wav","animationType":"burst","color":"#4ECDC4","keyMapping":"W"},
            {"id":"s03","label":"HiHat","file":"hihat.wav","animationType":"scatter","color":"#45B7D1","keyMapping":"E"}
        ]
    }
    """.trimIndent()

    private fun createRepo(): SoundRepositoryImpl {
        val loader = SoundPackLoader { testJson }
        return SoundRepositoryImpl(loader)
    }

    @Test
    fun `loadSoundPack returns correct pack`() = runTest {
        val repo = createRepo()
        val pack = repo.loadSoundPack()
        assertEquals("test", pack.packId)
        assertEquals(3, pack.sounds.size)
    }

    @Test
    fun `getSoundForKey returns correct sound after loading`() = runTest {
        val repo = createRepo()
        repo.loadSoundPack()

        val sound = repo.getSoundForKey("Q")
        assertNotNull(sound)
        assertEquals("s01", sound.id)
        assertEquals("Kick", sound.label)
    }

    @Test
    fun `getSoundForKey is case-insensitive`() = runTest {
        val repo = createRepo()
        repo.loadSoundPack()

        val sound = repo.getSoundForKey("q")
        assertNotNull(sound)
        assertEquals("s01", sound.id)
    }

    @Test
    fun `getSoundForKey returns null for unmapped key`() = runTest {
        val repo = createRepo()
        repo.loadSoundPack()

        assertNull(repo.getSoundForKey("Z"))
    }

    @Test
    fun `getSoundForKey returns null before loading`() {
        val repo = createRepo()
        // Not loaded yet
        assertNull(repo.getSoundForKey("Q"))
    }

    @Test
    fun `getSoundByIndex returns correct sound`() = runTest {
        val repo = createRepo()
        repo.loadSoundPack()

        assertEquals("s01", repo.getSoundByIndex(0)?.id)
        assertEquals("s02", repo.getSoundByIndex(1)?.id)
        assertEquals("s03", repo.getSoundByIndex(2)?.id)
    }

    @Test
    fun `getSoundByIndex wraps around with modulo`() = runTest {
        val repo = createRepo()
        repo.loadSoundPack()

        // Index 3 should wrap to 0 (3 % 3 = 0)
        assertEquals("s01", repo.getSoundByIndex(3)?.id)
    }

    @Test
    fun `getAllSounds returns all sounds after loading`() = runTest {
        val repo = createRepo()
        repo.loadSoundPack()

        val all = repo.getAllSounds()
        assertEquals(3, all.size)
    }

    @Test
    fun `getAllSounds returns empty before loading`() {
        val repo = createRepo()
        assertEquals(0, repo.getAllSounds().size)
    }
}
