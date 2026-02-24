package com.taptapboom.data.loader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for AudioBufferCache.
 * Verifies put/get, LRU eviction, and size constraints.
 */
class AudioBufferCacheTest {

    @Test
    fun `put and get returns correct handle`() {
        val cache = AudioBufferCache()
        cache.put("kick.wav", 42)
        assertEquals(42, cache.get("kick.wav"))
    }

    @Test
    fun `get returns null for missing key`() {
        val cache = AudioBufferCache()
        assertNull(cache.get("nonexistent.wav"))
    }

    @Test
    fun `contains returns true for cached item`() {
        val cache = AudioBufferCache()
        cache.put("kick.wav", 1)
        assertTrue(cache.contains("kick.wav"))
        assertFalse(cache.contains("snare.wav"))
    }

    @Test
    fun `cache respects max size and evicts oldest`() {
        val cache = AudioBufferCache(maxSize = 3)
        cache.put("a.wav", 1)
        cache.put("b.wav", 2)
        cache.put("c.wav", 3)
        assertEquals(3, cache.size)

        // Adding a 4th should evict the first
        cache.put("d.wav", 4)
        assertEquals(3, cache.size)
        assertNull(cache.get("a.wav")) // Evicted
        assertEquals(4, cache.get("d.wav"))
    }

    @Test
    fun `clear removes all entries`() {
        val cache = AudioBufferCache()
        cache.put("a.wav", 1)
        cache.put("b.wav", 2)
        cache.clear()
        assertEquals(0, cache.size)
        assertNull(cache.get("a.wav"))
    }

    @Test
    fun `overwriting existing key updates value`() {
        val cache = AudioBufferCache()
        cache.put("kick.wav", 1)
        cache.put("kick.wav", 99)
        assertEquals(99, cache.get("kick.wav"))
        assertEquals(1, cache.size) // No duplicate entry
    }
}
