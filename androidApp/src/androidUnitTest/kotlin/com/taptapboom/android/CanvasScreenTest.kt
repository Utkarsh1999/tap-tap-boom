package com.taptapboom.android

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import com.taptapboom.domain.audio.AudioEngine
import com.taptapboom.domain.model.AnimationType
import com.taptapboom.domain.model.Sound
import com.taptapboom.domain.model.SoundPack
import com.taptapboom.domain.repository.SoundRepository
import com.taptapboom.domain.usecase.PreloadSoundsUseCase
import com.taptapboom.domain.usecase.TriggerInteractionUseCase
import com.taptapboom.domain.analytics.AnalyticsLogger
import com.taptapboom.ui.screen.CanvasScreen
import com.taptapboom.ui.viewmodel.CanvasViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Android UI tests for CanvasScreen.
 * Tests edge cases like rapid multi-touch.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CanvasScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): CanvasViewModel {
        // Setup fakes for domain dependencies
        val testSounds = listOf(
            Sound("s01", "Kick", "kick.wav", AnimationType.RIPPLE, "#FF6B6B", "Q")
        )
        val testPack = SoundPack("test", "Test", 1, testSounds)
        
        val fakeRepo = object : SoundRepository {
            override suspend fun loadSoundPack() = testPack
            override fun getSoundForKey(key: String) = testSounds.firstOrNull { it.keyMapping == key }
            override fun getSoundByIndex(index: Int) = testSounds.getOrNull(index % testSounds.size)
            override fun getAllSounds() = testSounds
        }

        val fakeAudioEngine = object : AudioEngine {
            override suspend fun preload(assetPath: String) = 1
            override fun play(handle: Int, pitch: Float) {}
            override fun stopAll() {}
            override fun release() {}
        }
        
        val fakeAnalyticsLogger = object : AnalyticsLogger {
            override fun logAppOpen() {}
            override fun logSoundPlayed(soundId: String) {}
            override fun logInteraction(pointersCount: Int) {}
        }

        val fakeHapticEngine = object : com.taptapboom.domain.haptic.HapticEngine {
            override fun impact(intensity: Float) {}
            override fun vibrate(durationMs: Long) {}
            override fun release() {}
        }

        return CanvasViewModel(
            triggerInteraction = TriggerInteractionUseCase(fakeRepo),
            preloadSounds = PreloadSoundsUseCase(fakeRepo, fakeAudioEngine),
            audioEngine = fakeAudioEngine,
            analyticsLogger = fakeAnalyticsLogger,
            hapticEngine = fakeHapticEngine
        )
    }

    @Test
    fun rapid_multi_touch_does_not_crash_and_processes_intents() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            CanvasScreen(viewModel = viewModel)
        }

        // Wait for sound pack to load and grid to initialize
        composeTestRule.waitUntil(5000) { !viewModel.state.value.isLoading }

        // Simulate 10 simultaneous touches simulating edge case E-01
        composeTestRule.onRoot().performTouchInput {
            // Down events for 10 pointers
            for (i in 0..9) {
                down(pointerId = i, position = center + androidx.compose.ui.geometry.Offset(i * 10f, i * 10f))
            }
            // Move events
            for (i in 0..9) {
                moveTo(pointerId = i, position = center + androidx.compose.ui.geometry.Offset(-i * 10f, -i * 10f))
            }
            // Up events
            for (i in 0..9) {
                up(pointerId = i)
            }
        }

        // Wait for Compose loop to settle
        composeTestRule.waitForIdle()

        // Verify the ViewModel state absorbed the taps.
        // It should have registered multiple animations.
        val activeAnimations = viewModel.state.value.animations
        assert(activeAnimations.isNotEmpty()) { "Expected animations to be triggered by multi-touch" }
    }
}
