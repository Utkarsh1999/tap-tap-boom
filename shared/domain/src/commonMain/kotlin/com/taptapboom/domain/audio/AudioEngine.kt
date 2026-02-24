package com.taptapboom.domain.audio

/**
 * Platform-agnostic interface for audio playback.
 * Each platform provides an `actual` implementation optimized for low latency.
 *
 * Android: Oboe/AudioTrack via JNI
 * iOS: AVAudioEngine with AVAudioPlayerNode
 * Desktop: javax.sound.sampled
 */
interface AudioEngine {
    /**
     * Preload a sound asset into a memory buffer for instant playback.
     * @param assetPath path to the audio file within bundled assets
     * @return handle ID for referencing this sound in play() calls
     */
    suspend fun preload(assetPath: String): Int

    /**
     * Play a previously preloaded sound. Fire-and-forget, non-blocking.
     * Must be safe to call from any thread.
     * @param handle the ID returned from preload()
     * @param pitch playback rate/pitch: 0.5 (half) to 2.0 (double). 1.0 is normal.
     */
    fun play(handle: Int, pitch: Float = 1.0f)

    /**
     * Stop all currently playing sounds immediately.
     */
    fun stopAll()

    /**
     * Release all audio buffers and native resources.
     * Call when the app is being destroyed.
     */
    fun release()
}
