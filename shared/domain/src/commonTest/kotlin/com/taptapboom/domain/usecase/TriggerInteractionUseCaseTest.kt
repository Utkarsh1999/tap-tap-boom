package com.taptapboom.domain.usecase

import com.taptapboom.domain.model.AnimationType
import com.taptapboom.domain.model.InteractionEvent
import com.taptapboom.domain.model.Sound
import com.taptapboom.domain.repository.SoundRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for TriggerInteractionUseCase.
 * Verifies key-press mapping, touch position hashing, and edge cases.
 */
class TriggerInteractionUseCaseTest {

    private val testSounds = listOf(
        Sound("s01", "Kick", "kick.wav", AnimationType.RIPPLE, "#FF6B6B", "Q"),
        Sound("s02", "Snare", "snare.wav", AnimationType.BURST, "#4ECDC4", "W"),
        Sound("s03", "HiHat", "hihat.wav", AnimationType.SCATTER, "#45B7D1", "E"),
    )

    private val fakeRepo = object : SoundRepository {
        override suspend fun loadSoundPack() = throw UnsupportedOperationException()
        override fun getSoundForKey(key: String) = testSounds.find { it.keyMapping == key }
        override fun getSoundByIndex(index: Int) = testSounds.getOrNull(index)
        override fun getAllSounds() = testSounds
    }

    private val useCase = TriggerInteractionUseCase(fakeRepo)

    // ── Key Press Mapping ────────────────────────────────

    @Test
    fun `key press Q maps to Kick sound`() {
        val event = InteractionEvent(x = 0f, y = 0f, key = 'Q')
        val result = useCase(event)
        assertNotNull(result)
        assertEquals("s01", result.id)
        assertEquals("Kick", result.label)
        assertEquals(AnimationType.RIPPLE, result.animationType)
    }

    @Test
    fun `key press W maps to Snare sound`() {
        val event = InteractionEvent(x = 0f, y = 0f, key = 'W')
        val result = useCase(event)
        assertNotNull(result)
        assertEquals("s02", result.id)
    }

    @Test
    fun `lowercase key press is case-insensitive`() {
        val event = InteractionEvent(x = 0f, y = 0f, key = 'q')
        val result = useCase(event)
        assertNotNull(result)
        assertEquals("s01", result.id)
    }

    @Test
    fun `unmapped key returns null`() {
        val event = InteractionEvent(x = 0f, y = 0f, key = 'Z')
        val result = useCase(event)
        assertNull(result)
    }

    // ── Touch Position Hashing ───────────────────────────

    @Test
    fun `touch event returns a non-null sound`() {
        val event = InteractionEvent(x = 100f, y = 200f, pointerId = 0)
        val result = useCase(event)
        assertNotNull(result)
    }

    @Test
    fun `different touch positions can map to different sounds`() {
        val results = mutableSetOf<String>()
        // Test a range of positions to verify variety
        for (x in 0..500 step 50) {
            for (y in 0..500 step 50) {
                val event = InteractionEvent(x = x.toFloat(), y = y.toFloat(), pointerId = 0)
                val result = useCase(event)
                if (result != null) results.add(result.id)
            }
        }
        // With 3 sounds and position-based hashing, we should hit multiple sounds
        assert(results.size > 1) { "Expected multiple different sounds, got: $results" }
    }

    @Test
    fun `same position always returns same sound (deterministic)`() {
        val event = InteractionEvent(x = 150f, y = 250f, pointerId = 0)
        val first = useCase(event)
        val second = useCase(event)
        assertEquals(first, second)
    }

    // ── Edge Cases ───────────────────────────────────────

    @Test
    fun `empty sound repository returns null for touch`() {
        val emptyRepo = object : SoundRepository {
            override suspend fun loadSoundPack() = throw UnsupportedOperationException()
            override fun getSoundForKey(key: String) = null
            override fun getSoundByIndex(index: Int) = null
            override fun getAllSounds() = emptyList<Sound>()
        }
        val emptyUseCase = TriggerInteractionUseCase(emptyRepo)
        val event = InteractionEvent(x = 100f, y = 200f, pointerId = 0)
        assertNull(emptyUseCase(event))
    }

    @Test
    fun `key press takes priority over position mapping`() {
        // Even with x/y set, if key is present, key mapping is used
        val event = InteractionEvent(x = 999f, y = 999f, key = 'E')
        val result = useCase(event)
        assertNotNull(result)
        assertEquals("s03", result.id)
    }
}
