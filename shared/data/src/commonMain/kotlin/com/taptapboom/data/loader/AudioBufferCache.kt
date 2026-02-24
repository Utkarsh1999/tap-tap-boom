package com.taptapboom.data.loader

/**
 * In-memory cache for decoded audio buffer handles.
 * Maps asset paths to their preloaded audio engine handle IDs.
 */
class AudioBufferCache(
    private val maxSize: Int = 50
) {
    private val cache = LinkedHashMap<String, Int>(maxSize, 0.75f, true)

    fun put(assetPath: String, handle: Int) {
        if (cache.size >= maxSize) {
            val eldest = cache.entries.first()
            cache.remove(eldest.key)
        }
        cache[assetPath] = handle
    }

    fun get(assetPath: String): Int? = cache[assetPath]

    fun contains(assetPath: String): Boolean = cache.containsKey(assetPath)

    fun clear() = cache.clear()

    val size: Int get() = cache.size
}
