package com.taptapboom.data.loader

import com.taptapboom.domain.model.AnimationType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for SoundPackLoader.
 * Verifies JSON parsing, error handling, and graceful degradation.
 */
class SoundPackLoaderTest {

    @Test
    fun `valid JSON parses correctly`() = runTest {
        val validJson = """
        {
            "packId": "synth-basics-v1",
            "packName": "Synth Basics",
            "version": 1,
            "sounds": [
                {
                    "id": "s01",
                    "label": "Kick",
                    "file": "kick.wav",
                    "animationType": "ripple",
                    "color": "#FF6B6B",
                    "keyMapping": "Q"
                },
                {
                    "id": "s02",
                    "label": "Snare",
                    "file": "snare.wav",
                    "animationType": "burst",
                    "color": "#4ECDC4",
                    "keyMapping": "W"
                }
            ]
        }
        """.trimIndent()

        val loader = SoundPackLoader { validJson }
        val pack = loader.load()

        assertEquals("synth-basics-v1", pack.packId)
        assertEquals("Synth Basics", pack.packName)
        assertEquals(1, pack.version)
        assertEquals(2, pack.sounds.size)
        assertEquals("s01", pack.sounds[0].id)
        assertEquals(AnimationType.RIPPLE, pack.sounds[0].animationType)
        assertEquals(AnimationType.BURST, pack.sounds[1].animationType)
    }

    @Test
    fun `malformed JSON returns empty fallback pack (E-07)`() = runTest {
        val loader = SoundPackLoader { "{ invalid json !!!" }
        val pack = loader.load()

        assertEquals("empty", pack.packId)
        assertTrue(pack.sounds.isEmpty())
    }

    @Test
    fun `asset reader exception returns empty fallback pack`() = runTest {
        val loader = SoundPackLoader { throw RuntimeException("File not found") }
        val pack = loader.load()

        assertEquals("empty", pack.packId)
        assertTrue(pack.sounds.isEmpty())
    }

    @Test
    fun `JSON with unknown fields is parsed successfully`() = runTest {
        val jsonWithExtra = """
        {
            "packId": "test",
            "packName": "Test",
            "version": 1,
            "extraField": "should be ignored",
            "sounds": []
        }
        """.trimIndent()

        val loader = SoundPackLoader { jsonWithExtra }
        val pack = loader.load()

        assertEquals("test", pack.packId)
        assertTrue(pack.sounds.isEmpty())
    }

    @Test
    fun `all 10 animation types deserialize correctly`() = runTest {
        val allTypesJson = """
        {
            "packId": "full",
            "packName": "Full",
            "version": 1,
            "sounds": [
                {"id":"1","label":"A","file":"a.wav","animationType":"ripple","color":"#FFF","keyMapping":"Q"},
                {"id":"2","label":"B","file":"b.wav","animationType":"burst","color":"#FFF","keyMapping":"W"},
                {"id":"3","label":"C","file":"c.wav","animationType":"spiral","color":"#FFF","keyMapping":"E"},
                {"id":"4","label":"D","file":"d.wav","animationType":"wave","color":"#FFF","keyMapping":"R"},
                {"id":"5","label":"E","file":"e.wav","animationType":"scatter","color":"#FFF","keyMapping":"T"},
                {"id":"6","label":"F","file":"f.wav","animationType":"pulse","color":"#FFF","keyMapping":"A"},
                {"id":"7","label":"G","file":"g.wav","animationType":"bloom","color":"#FFF","keyMapping":"S"},
                {"id":"8","label":"H","file":"h.wav","animationType":"shatter","color":"#FFF","keyMapping":"D"},
                {"id":"9","label":"I","file":"i.wav","animationType":"orbit","color":"#FFF","keyMapping":"F"},
                {"id":"10","label":"J","file":"j.wav","animationType":"flash","color":"#FFF","keyMapping":"G"}
            ]
        }
        """.trimIndent()

        val loader = SoundPackLoader { allTypesJson }
        val pack = loader.load()

        assertEquals(10, pack.sounds.size)
        assertEquals(AnimationType.RIPPLE, pack.sounds[0].animationType)
        assertEquals(AnimationType.FLASH, pack.sounds[9].animationType)
    }
}
