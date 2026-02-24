package com.taptapboom.android.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.taptapboom.domain.audio.AudioEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android AudioEngine implementation using SoundPool for low-latency playback.
 *
 * SoundPool is the recommended API for short, latency-sensitive audio clips.
 * For even lower latency on supported devices, a future iteration can use
 * Google's Oboe library via JNI.
 *
 * Performance characteristics:
 * - Pre-decoded PCM buffers in memory
 * - Hardware-accelerated mixing
 * - Typical latency: 20-80ms depending on device
 */
class AndroidAudioEngine(
    private val context: Context
) : AudioEngine {

    companion object {
        private const val TAG = "AndroidAudioEngine"
    }

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(32) // Increased from 10 to support rapid multi-touch without dropping sounds
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSounds = mutableMapOf<Int, Boolean>()

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds[sampleId] = true
            } else {
                Log.e(TAG, "Failed to load sound with sampleId=$sampleId, status=$status")
            }
        }
    }

    override suspend fun preload(assetPath: String): Int = withContext(Dispatchers.IO) {
        try {
            val afd = context.assets.openFd("soundpacks/synth-basics-v1/$assetPath")
            val soundId = soundPool.load(afd, 1)
            afd.close()
            soundId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to preload sound: $assetPath", e)
            -1 // Sentinel: play() will silently no-op for invalid handles
        }
    }

    override fun play(handle: Int, pitch: Float) {
        if (handle < 0) return // Skip invalid handles from failed preloads
        // Fire-and-forget: play at full volume, 1x priority, variable rate
        soundPool.play(handle, 1.0f, 1.0f, 1, 0, pitch.coerceIn(0.5f, 2.0f))
    }

    override fun stopAll() {
        soundPool.autoPause()
    }

    override fun release() {
        soundPool.release()
    }
}
