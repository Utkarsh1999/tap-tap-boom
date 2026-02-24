package com.taptapboom.ui.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuid4
import com.taptapboom.domain.analytics.AnalyticsLogger
import com.taptapboom.domain.audio.AudioEngine
import com.taptapboom.domain.haptic.HapticEngine
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
    private val analyticsLogger: AnalyticsLogger,
    private val hapticEngine: HapticEngine
) : ViewModel() {

    private val _state = MutableStateFlow(CanvasState())
    val state: StateFlow<CanvasState> = _state.asStateFlow()

    private val _sideEffects = Channel<CanvasSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<CanvasSideEffect> = _sideEffects.receiveAsFlow()

    /** Maps sound ID → audio engine handle for instant playback */
    private var audioHandles: Map<String, Int> = emptyMap()
    private var soundIds: List<String> = emptyList()

    private val timeSource = TimeSource.Monotonic
    private val startMark = timeSource.markNow()

    // Energy constants
    private val ENERGY_INCREMENT = 0.15f
    private val ENERGY_DECAY_RATE = 0.8f // % per second

    init {
        analyticsLogger.logAppOpen()
        loadSounds()
        collectSideEffects()
    }

    private fun loadSounds() {
        viewModelScope.launch {
            try {
                audioHandles = preloadSounds()
                soundIds = audioHandles.keys.toList().sorted()
                
                // Initialize grid assignments (4x3)
                val initialAssignments = mutableMapOf<Pair<Int, Int>, String>()
                for (row in 0 until _state.value.gridRows) {
                    for (col in 0 until _state.value.gridCols) {
                        val index = (row * _state.value.gridCols + col) % soundIds.size
                        initialAssignments[row to col] = soundIds[index]
                    }
                }
                
                _state.update { it.copy(
                    isLoading = false,
                    padAssignments = initialAssignments
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    /** Audio playback handled internally — Composables never touch AudioEngine */
    private fun collectSideEffects() {
        viewModelScope.launch {
            sideEffects.collect { effect ->
                when (effect) {
                    is CanvasSideEffect.PlaySound -> audioEngine.play(effect.handle, effect.pitch)
                }
            }
        }
    }

    /**
     * Process a user intent through the MVI reducer.
     */
    fun onIntent(intent: CanvasIntent) {
        when (intent) {
            is CanvasIntent.TapPad -> handleTapPad(intent)
            is CanvasIntent.RotateSound -> handleRotateSound(intent)
            is CanvasIntent.KeyPress -> handleKeyPress(intent)
        }
    }

    private fun handleTapPad(tap: CanvasIntent.TapPad) {
        val cell = getGridCell(tap.x, tap.y, tap.screenWidth, tap.screenHeight)
        val soundId = _state.value.padAssignments[cell] ?: return
        
        // Build energy and trigger haptics
        val currentEnergy = _state.value.energy
        val newEnergy = (currentEnergy + ENERGY_INCREMENT).coerceIn(0f, 1f)
        hapticEngine.impact(intensity = 0.5f + (newEnergy * 0.5f))

        // Highlight the pad
        _state.update { it.copy(
            highlightedPads = it.highlightedPads + (cell to currentNanoTime() + 150_000_000L),
            energy = newEnergy
        ) }

        val sound = triggerInteraction.getSoundById(soundId) ?: return
        triggerAnimation(sound, Offset(tap.x, tap.y), newEnergy)
        analyticsLogger.logInteraction(1)
    }

    private fun handleRotateSound(rotation: CanvasIntent.RotateSound) {
        val cell = getGridCell(rotation.x, rotation.y, rotation.screenWidth, rotation.screenHeight)
        val currentSound = _state.value.padAssignments[cell] ?: return
        
        val currentIndex = soundIds.indexOf(currentSound)
        val nextIndex = (currentIndex + 1) % soundIds.size
        val nextSound = soundIds[nextIndex]

        _state.update { it.copy(
            padAssignments = it.padAssignments + (cell to nextSound)
        ) }
    }

    private fun handleKeyPress(keyPress: CanvasIntent.KeyPress) {
        val sound = triggerInteraction.getSoundForKey(keyPress.key.toString().uppercase()) ?: return
        // Use -1f, -1f as sentinel; Canvas will map to screen center
        triggerAnimation(sound, Offset(-1f, -1f), _state.value.energy)
        analyticsLogger.logInteraction(1)
    }

    private fun triggerAnimation(sound: com.taptapboom.domain.model.Sound, origin: Offset, energy: Float) {
        val soundId = sound.id
        analyticsLogger.logSoundPlayed(soundId)

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
            if (nextHue in 40f..160f) nextHue = 180f
            current.copy(
                animations = current.animations + animation,
                backgroundHue = nextHue
            )
        }

        audioHandles[soundId]?.let { handle ->
            // Audio Detuning: randomize pitch based on energy for more organic feel
            val pitchRange = energy * 0.15f
            val actualPitch = 1.0f + (kotlin.random.Random.nextFloat() * 2 - 1) * pitchRange
            _sideEffects.trySend(CanvasSideEffect.PlaySound(handle, actualPitch))
        }
    }

    private fun getGridCell(x: Float, y: Float, width: Float, height: Float): Pair<Int, Int> {
        val col = (x / (width / _state.value.gridCols)).toInt().coerceIn(0, _state.value.gridCols - 1)
        val row = (y / (height / _state.value.gridRows)).toInt().coerceIn(0, _state.value.gridRows - 1)
        return row to col
    }

    /**
     * Batch update all animation progresses and remove completed ones.
     * Also clears highlights and decays energy/shake.
     */
    fun updateAnimations(nowNanos: Long) {
        _state.update { current ->
            val updatedAnimations = current.animations.mapNotNull { anim ->
                val elapsed = (nowNanos - anim.startTimeNanos) / 1_000_000f
                val progress = (elapsed / anim.durationMs).coerceIn(0f, 1f)
                if (progress >= 1f) null else anim.copy(progress = progress)
            }
            
            val updatedHighlights = current.highlightedPads.filterValues { it > nowNanos }
            
            // Energy Decay: 0.8% decay per second
            val decay = ENERGY_DECAY_RATE * (16f / 1000f) // Assuming ~16ms frame
            val newEnergy = (current.energy - decay).coerceAtLeast(0f)
            
            // Screen Shake calculation based on energy
            val shakeMagnitude = newEnergy * 15f
            val shakeOffset = if (newEnergy > 0.1f) {
                Offset(
                    x = (kotlin.random.Random.nextFloat() * 2 - 1) * shakeMagnitude,
                    y = (kotlin.random.Random.nextFloat() * 2 - 1) * shakeMagnitude
                )
            } else Offset.Zero
            
            if (updatedAnimations.size == current.animations.size && 
                updatedHighlights.size == current.highlightedPads.size &&
                newEnergy == current.energy &&
                shakeOffset == current.screenShakeOffset &&
                updatedAnimations.zip(current.animations).all { it.first.progress == it.second.progress }) {
                current
            } else {
                current.copy(
                    animations = updatedAnimations,
                    highlightedPads = updatedHighlights,
                    energy = newEnergy,
                    screenShakeOffset = shakeOffset
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
        hapticEngine.release()
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
