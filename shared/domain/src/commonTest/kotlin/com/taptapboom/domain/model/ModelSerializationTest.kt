package com.taptapboom.domain.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for domain model serialization and data integrity.
 */
class ModelSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `Sound serializes and deserializes correctly`() {
        val sound = Sound("s01", "Kick", "kick.wav", AnimationType.RIPPLE, "#FF6B6B", "Q")
        val jsonStr = json.encodeToString(Sound.serializer(), sound)
        val deserialized = json.decodeFromString(Sound.serializer(), jsonStr)
        assertEquals(sound, deserialized)
    }

    @Test
    fun `SoundPack serializes and deserializes correctly`() {
        val pack = SoundPack(
            packId = "test-pack",
            packName = "Test Pack",
            version = 1,
            sounds = listOf(
                Sound("s01", "Kick", "kick.wav", AnimationType.RIPPLE, "#FF6B6B", "Q"),
                Sound("s02", "Snare", "snare.wav", AnimationType.BURST, "#4ECDC4", "W")
            )
        )
        val jsonStr = json.encodeToString(SoundPack.serializer(), pack)
        val deserialized = json.decodeFromString(SoundPack.serializer(), jsonStr)
        assertEquals(pack, deserialized)
        assertEquals(2, deserialized.sounds.size)
    }

    @Test
    fun `AnimationType enum values match expected serialized names`() {
        val types = AnimationType.entries
        assertEquals(12, types.size)
        assertEquals(AnimationType.RIPPLE, types[0])
        assertEquals(AnimationType.FLASH, types[9])
        assertEquals(AnimationType.MIRROR, types[10])
        assertEquals(AnimationType.SLICE, types[11])
    }

    @Test
    fun `InteractionEvent default values are correct`() {
        val event = InteractionEvent(x = 10f, y = 20f)
        assertEquals(0, event.pointerId)
        assertEquals(null, event.key)
        assertEquals(0L, event.timestampNanos)
    }

    @Test
    fun `SoundPack JSON parses from real format`() {
        val realJson = """
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
                }
            ]
        }
        """.trimIndent()
        val pack = json.decodeFromString(SoundPack.serializer(), realJson)
        assertEquals("synth-basics-v1", pack.packId)
        assertEquals(1, pack.sounds.size)
        assertEquals(AnimationType.RIPPLE, pack.sounds[0].animationType)
    }
}
