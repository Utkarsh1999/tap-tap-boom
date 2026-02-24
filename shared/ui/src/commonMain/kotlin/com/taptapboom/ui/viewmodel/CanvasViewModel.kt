package com.taptapboom.ui.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuid4
import com.taptapboom.domain.analytics.AnalyticsLogger
import com.taptapboom.domain.audio.AudioEngine
import com.taptapboom.domain.model.InteractionEvent
import com.taptapboom.domain.usecase.PreloadSoundsUseCase
import com.taptapboom.domain.usecase.TriggerInteractionUseCase
import com.taptapboom.ui.mvi.ActiveAnimation
import com.taptapboom.ui.mvi.CanvasIntent
import com.taptapboom.ui.mvi.CanvasSideEffect
import com.taptapboom.ui.mvi.CanvasState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

/**
 * MVI ViewModel for the interactive canvas.
 *
 * Processes user intents (taps, key presses) and produces:
 * - Updated immutable state (for the Canvas to render)
 * - One-shot side effects (for audio playback)
 */
class CanvasViewModel(
    private val triggerInteraction: TriggerInteractionUseCase,
    private val preloadSounds: PreloadSoundsUseCase,
    private val audioEngine: AudioEngine,
    private val analyticsLogger: AnalyticsLogger
) : ViewModel() {

    private val _state = MutableStateFlow(CanvasState())
    val state: StateFlow<CanvasState> = _state.asStateFlow()

    private val _sideEffects = Channel<CanvasSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<CanvasSideEffect> = _sideEffects.receiveAsFlow()

    /** Maps sound ID → audio engine handle for instant playback */
    private var audioHandles: Map<String, Int> = emptyMap()

    private val timeSource = TimeSource.Monotonic
    private val startMark = timeSource.markNow()

    init {
        analyticsLogger.logAppOpen()
        loadSounds()
        collectSideEffects()
    }

    private fun loadSounds() {
        viewModelScope.launch {
            try {
                audioHandles = preloadSounds()
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                // Graceful degradation: animations still work, just no sound
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    /** Audio playback handled internally — Composables never touch AudioEngine */
    private fun collectSideEffects() {
        viewModelScope.launch {
            sideEffects.collect { effect ->
                when (effect) {
                    is CanvasSideEffect.PlaySound -> audioEngine.play(effect.handle)
                }
            }
        }
    }

    /**
     * Process a user intent through the MVI reducer.
     */
    fun onIntent(intent: CanvasIntent) {
        when (intent) {
            is CanvasIntent.Tap -> handleTap(intent)
            is CanvasIntent.KeyPress -> handleKeyPress(intent)
        }
    }

    private fun handleTap(tap: CanvasIntent.Tap) {
        val event = InteractionEvent(
            x = tap.x,
            y = tap.y,
            pointerId = tap.pointerId,
            timestampNanos = currentNanoTime()
        )
        triggerAnimation(event, Offset(tap.x, tap.y))
        
        // Normally we'd track multi-touch pointer count. Using 1 for simple tap representation.
        analyticsLogger.logInteraction(1)
    }

    private fun handleKeyPress(keyPress: CanvasIntent.KeyPress) {
        val event = InteractionEvent(
            x = 0f, y = 0f,
            key = keyPress.key,
            timestampNanos = currentNanoTime()
        )
        // Use -1f, -1f as sentinel; Canvas will map to screen center
        triggerAnimation(event, Offset(-1f, -1f))
        analyticsLogger.logInteraction(1)
    }

    /**
     * Common animation trigger logic — extracted to eliminate duplication.
     * @param origin use Offset(-1f, -1f) for key presses (Canvas maps to center)
     */
    private fun triggerAnimation(event: InteractionEvent, origin: Offset) {
        val sound = triggerInteraction(event) ?: return

        analyticsLogger.logSoundPlayed(sound.id)

        val animation = ActiveAnimation(
            id = uuid4().toString(),
            type = sound.animationType,
            origin = origin,
            color = parseColor(sound.color),
            progress = 0f,
            startTimeNanos = currentNanoTime()
        )

        _state.update { current ->
            var nextHue = (current.backgroundHue + 20f) % 360f
            // Skip yellow/green range (40 - 160) for a more premium cyber/dark aesthetic
            if (nextHue in 40f..160f) {
                nextHue = 180f // Jump to Cyan/Blue
            }
            current.copy(
                animations = current.animations + animation,
                backgroundHue = nextHue
            )
        }

        audioHandles[sound.id]?.let { handle ->
            _sideEffects.trySend(CanvasSideEffect.PlaySound(handle))
        }
    }

    /**
     * Batch update all animation progresses and remove completed ones in a single state update.
     * This significantly reduces recompositions and UI thread load during rapid tapping.
     */
    fun updateAnimations(nowNanos: Long) {
        _state.update { current ->
            val updatedAnimations = current.animations.mapNotNull { anim ->
                val elapsed = (nowNanos - anim.startTimeNanos) / 1_000_000f
                val progress = (elapsed / anim.durationMs).coerceIn(0f, 1f)
                
                if (progress >= 1f) null else anim.copy(progress = progress)
            }
            
            if (updatedAnimations.size == current.animations.size && 
                updatedAnimations.zip(current.animations).all { it.first.progress == it.second.progress }) {
                current
            } else {
                current.copy(animations = updatedAnimations)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }

    private fun parseColor(hex: String): Color {
        val colorLong = hex.removePrefix("#").toLong(16)
        return Color(
            red = ((colorLong shr 16) and 0xFF).toInt(),
            green = ((colorLong shr 8) and 0xFF).toInt(),
            blue = (colorLong and 0xFF).toInt()
        )
    }

    private fun currentNanoTime(): Long = startMark.elapsedNow().inWholeNanoseconds
}
