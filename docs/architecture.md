# Architecture Definition Document (ADD)
### tap-tap-boom — Technical Architecture
**Author:** Utkarsh the Engineer
**Date:** 2026-02-24
**Version:** 1.0

---

## 1. Architecture Overview

The application follows **Clean Architecture** with an **MVI (Model-View-Intent)** presentation pattern, ensuring unidirectional data flow — critical for synchronizing audio triggers with visual state.

```
┌─────────────────────────────────────────────────────────────┐
│                      Platform Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  androidApp   │  │   iosApp     │  │  desktopApp  │      │
│  │  (Oboe/JNI)  │  │(AVAudioEngine)│ │  (JVM Audio) │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                 │                  │              │
│         └────────────┬────┘──────────────────┘              │
│                      │                                      │
│  ┌───────────────────▼──────────────────────────────────┐   │
│  │                  :shared:ui                           │   │
│  │  Compose Multiplatform Canvas · ViewModels · MVI      │   │
│  └───────────────────┬──────────────────────────────────┘   │
│                      │                                      │
│  ┌───────────────────▼──────────────────────────────────┐   │
│  │                :shared:domain                         │   │
│  │  Use Cases · Entities · AudioEngine Interface         │   │
│  └───────────────────┬──────────────────────────────────┘   │
│                      │                                      │
│  ┌───────────────────▼──────────────────────────────────┐   │
│  │                :shared:data                           │   │
│  │  Repositories · SoundPack Loader · Asset Manager      │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Module Breakdown

### 2.1 `:shared:domain`
**Purpose:** Pure Kotlin business logic. Zero platform dependencies.

**Contents:**
- `model/Sound.kt` — Data class: id, label, file path, animation type, color.
- `model/AnimationType.kt` — Sealed class of animation types (Ripple, Burst, Spiral, etc.).
- `model/InteractionEvent.kt` — Represents a tap/keypress event with position, timestamp, mapped sound ID.
- `usecase/TriggerInteractionUseCase.kt` — Resolves input → sound + animation mapping.
- `usecase/PreloadSoundsUseCase.kt` — Orchestrates buffer preloading on app start.
- `repository/SoundRepository.kt` — Interface; implemented in `:shared:data`.
- `audio/AudioEngine.kt` — `expect interface` for platform audio playback.

### 2.2 `:shared:data`
**Purpose:** Data access, asset management, JSON parsing.

**Contents:**
- `repository/SoundRepositoryImpl.kt` — Loads sound pack JSON, resolves file paths.
- `loader/SoundPackLoader.kt` — Parses `soundpack.json` from bundled assets.
- `loader/AudioBufferCache.kt` — In-memory LRU cache of decoded audio buffers.

### 2.3 `:shared:ui`
**Purpose:** Compose Multiplatform UI, MVI state machine, animations.

**Contents:**
- `screen/CanvasScreen.kt` — Full-screen Compose Canvas; handles touch/key events.
- `viewmodel/CanvasViewModel.kt` — MVI ViewModel; processes Intents, emits States.
- `mvi/CanvasIntent.kt` — Sealed interface: `Tap(x, y, pointerId)`, `KeyPress(key)`.
- `mvi/CanvasState.kt` — Immutable data class: list of active animations, background hue, loading status.
- `mvi/CanvasSideEffect.kt` — One-shot effects: `PlaySound(soundId)`.
- `animation/AnimationRenderer.kt` — Dispatches to specific animation drawers based on `AnimationType`.
- `animation/renderers/` — Individual animation implementations:
  - `RippleRenderer.kt`, `BurstRenderer.kt`, `SpiralRenderer.kt`, `WaveRenderer.kt`, `ScatterRenderer.kt`, `PulseRenderer.kt`, `BloomRenderer.kt`, `ShatterRenderer.kt`, `OrbitRenderer.kt`, `FlashRenderer.kt`

### 2.4 `:androidApp`
**Purpose:** Android entry point + platform audio.

**Contents:**
- `MainActivity.kt` — Sets Compose content, initializes Koin.
- `audio/AndroidAudioEngine.kt` — `actual` implementation using Oboe (via JNI) or `AudioTrack` fallback.
- `di/AndroidModule.kt` — Koin module binding `AudioEngine` to `AndroidAudioEngine`.

### 2.5 `:iosApp`
**Purpose:** iOS entry point + platform audio.

**Contents:**
- `MainViewController.kt` — Compose entry point for iOS.
- `audio/IosAudioEngine.kt` — `actual` implementation using `AVAudioEngine` via Kotlin/Native interop.
- `di/IosModule.kt` — Koin module binding `AudioEngine` to `IosAudioEngine`.

### 2.6 `:desktopApp` (V3 — scaffolded now)
**Purpose:** JVM Desktop entry point.

**Contents:**
- `Main.kt` — Window setup, keyboard listeners.
- `audio/DesktopAudioEngine.kt` — `actual` implementation using `javax.sound.sampled`.

---

## 3. Folder Structure

```
tap-tap-boom/
├── build.gradle.kts                     # Root build config
├── settings.gradle.kts                  # Module declarations
├── gradle.properties                    # KMP flags, memory settings
│
├── shared/
│   ├── domain/
│   │   ├── build.gradle.kts
│   │   └── src/commonMain/kotlin/com/taptapboom/domain/
│   │       ├── model/
│   │       │   ├── Sound.kt
│   │       │   ├── AnimationType.kt
│   │       │   └── InteractionEvent.kt
│   │       ├── usecase/
│   │       │   ├── TriggerInteractionUseCase.kt
│   │       │   └── PreloadSoundsUseCase.kt
│   │       ├── repository/
│   │       │   └── SoundRepository.kt
│   │       └── audio/
│   │           └── AudioEngine.kt
│   │
│   ├── data/
│   │   ├── build.gradle.kts
│   │   └── src/commonMain/kotlin/com/taptapboom/data/
│   │       ├── repository/
│   │       │   └── SoundRepositoryImpl.kt
│   │       └── loader/
│   │           ├── SoundPackLoader.kt
│   │           └── AudioBufferCache.kt
│   │
│   └── ui/
│       ├── build.gradle.kts
│       └── src/commonMain/kotlin/com/taptapboom/ui/
│           ├── screen/
│           │   └── CanvasScreen.kt
│           ├── viewmodel/
│           │   └── CanvasViewModel.kt
│           ├── mvi/
│           │   ├── CanvasIntent.kt
│           │   ├── CanvasState.kt
│           │   └── CanvasSideEffect.kt
│           └── animation/
│               ├── AnimationRenderer.kt
│               └── renderers/
│                   ├── RippleRenderer.kt
│                   ├── BurstRenderer.kt
│                   ├── SpiralRenderer.kt
│                   ├── WaveRenderer.kt
│                   ├── ScatterRenderer.kt
│                   ├── PulseRenderer.kt
│                   ├── BloomRenderer.kt
│                   ├── ShatterRenderer.kt
│                   ├── OrbitRenderer.kt
│                   └── FlashRenderer.kt
│
├── androidApp/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/taptapboom/android/
│       │   ├── MainActivity.kt
│       │   ├── audio/
│       │   │   └── AndroidAudioEngine.kt
│       │   └── di/
│       │       └── AndroidModule.kt
│       └── assets/
│           └── soundpacks/
│               └── synth-basics-v1/
│                   ├── soundpack.json
│                   ├── kick.wav
│                   ├── snare.wav
│                   └── ... (10 files)
│
├── iosApp/
│   ├── build.gradle.kts
│   └── src/
│       ├── iosMain/kotlin/com/taptapboom/ios/
│       │   ├── MainViewController.kt
│       │   ├── audio/
│       │   │   └── IosAudioEngine.kt
│       │   └── di/
│       │       └── IosModule.kt
│       └── iosApp/ (Xcode project wrapper)
│
├── desktopApp/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/taptapboom/desktop/
│       ├── Main.kt
│       ├── audio/
│       │   └── DesktopAudioEngine.kt
│       └── di/
│           └── DesktopModule.kt
│
└── docs/
    ├── agent_organization.md
    ├── architecture.md            ← this file
    ├── prd.md
    ├── test_strategy.md
    └── project_backlog.md
```

---

## 4. Audio Abstraction Layer

### 4.1 Common Interface

```kotlin
// :shared:domain — src/commonMain
expect interface AudioEngine {
    /** Preload a sound into a memory buffer. Returns a handle ID. */
    suspend fun preload(assetPath: String): Int

    /** Play a previously preloaded sound by handle. Fire-and-forget, non-blocking. */
    fun play(handle: Int)

    /** Release all buffers and native resources. */
    fun release()
}
```

### 4.2 Android Actual (Oboe via JNI)

```kotlin
// :androidApp
actual class AndroidAudioEngine(private val context: Context) : AudioEngine {
    // Uses Google Oboe C++ library via JNI bridge
    // AAudio on API 27+, OpenSL ES fallback on API 24-26
    // Pre-decoded PCM buffers stored in native heap
    // play() triggers a callback on the audio thread — zero GC pressure

    external fun nativePreload(fd: Int, offset: Long, length: Long): Int
    external fun nativePlay(handle: Int)
    external fun nativeRelease()
}
```

### 4.3 iOS Actual (AVAudioEngine)

```kotlin
// :iosApp
actual class IosAudioEngine : AudioEngine {
    // Uses AVAudioEngine with AVAudioPlayerNode per sound
    // Buffers pre-loaded into AVAudioPCMBuffer
    // play() schedules buffer on player node — hardware-level latency (~10ms)
    // Category: .playback with .mixWithOthers option
}
```

---

## 5. Animation Rendering Strategy

### 5.1 Core Approach
All animations are drawn directly on the Compose `Canvas` using `DrawScope` primitives (`drawCircle`, `drawArc`, `drawPath`, etc.). No Composable recomposition is involved in the hot path.

### 5.2 Animation Lifecycle
```
Tap Event → CanvasIntent.Tap → ViewModel reduces state →
  new ActiveAnimation added to State.animations list →
    Canvas reads State, draws each active animation at current progress →
      Animatable coroutine drives progress 0f → 1f over duration →
        On completion: Internal state driver removes it automatically from state during update cycle.
```

### 5.3 Performance Rules
1. **Zero allocations in `onDraw`.** All `Paint`, `Path`, and `Offset` objects are pre-allocated and reused.
2. **Animations driven by `withFrameNanos`.** Not by recomposition.
3. **Each `ActiveAnimation` is an immutable data class** with a `progress: Float` field.
4. **Canvas only redraws when** `State.animations` list changes (structural equality check).

### 5.4 Animation Data Model

```kotlin
data class ActiveAnimation(
    val id: String,            // UUID
    val type: AnimationType,   // Ripple, Burst, etc.
    val origin: Offset,        // Touch point
    val color: Color,
    val progress: Float,       // 0f..1f
    val startTimeNanos: Long
)
```

---

## 6. State Management (MVI)

### 6.1 Unidirectional Data Flow

```
User Input ──► Intent ──► ViewModel.reduce() ──► New State ──► Canvas renders
                                │
                                └──► SideEffect ──► AudioEngine.play()
```

### 6.2 Core Types

```kotlin
// Intents
sealed interface CanvasIntent {
    data class Tap(val x: Float, val y: Float, val pointerId: Int) : CanvasIntent
    data class KeyPress(val key: Char) : CanvasIntent
}

// State
data class CanvasState(
    val isLoading: Boolean = true,
    val animations: List<ActiveAnimation> = emptyList(),
    val backgroundHue: Float = 0f
)

// Side Effects (one-shot, not part of state)
sealed interface CanvasSideEffect {
    data class PlaySound(val handle: Int) : CanvasSideEffect
}
```

### 6.3 ViewModel Skeleton

```kotlin
class CanvasViewModel(
    private val triggerInteraction: TriggerInteractionUseCase,
    private val audioEngine: AudioEngine
) : ViewModel() {

    private val _state = MutableStateFlow(CanvasState())
    val state: StateFlow<CanvasState> = _state.asStateFlow()

    private val _sideEffects = Channel<CanvasSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<CanvasSideEffect> = _sideEffects.receiveAsFlow()

    fun onIntent(intent: CanvasIntent) {
        when (intent) {
            is CanvasIntent.Tap -> handleTap(intent)
            is CanvasIntent.KeyPress -> handleKeyPress(intent)
        }
    }

    private fun handleTap(tap: CanvasIntent.Tap) {
        val mapping = triggerInteraction(tap)
        val animation = ActiveAnimation(
            id = uuid4().toString(),
            type = mapping.animationType,
            origin = Offset(tap.x, tap.y),
            color = mapping.color,
            progress = 0f,
            startTimeNanos = System.nanoTime()
        )
        _state.update { it.copy(
            animations = it.animations + animation,
            backgroundHue = (it.backgroundHue + 15f) % 360f
        )}
        _sideEffects.trySend(CanvasSideEffect.PlaySound(mapping.audioHandle))
    }
}
```

---

## 7. Dependency Injection (Koin)

```kotlin
// :shared:domain
val domainModule = module {
    factory { TriggerInteractionUseCase(get()) }
    factory { PreloadSoundsUseCase(get(), get()) }
}

// :shared:data
val dataModule = module {
    single<SoundRepository> { SoundRepositoryImpl(get()) }
    single { SoundPackLoader() }
    single { AudioBufferCache() }
}

// :shared:ui
val uiModule = module {
    viewModel { CanvasViewModel(get(), get()) }
}

// Platform modules bind AudioEngine actual
// :androidApp
val androidModule = module {
    single<AudioEngine> { AndroidAudioEngine(androidContext()) }
}

// :iosApp
val iosModule = module {
    single<AudioEngine> { IosAudioEngine() }
}
```

**Initialization:** `startKoin { modules(domainModule, dataModule, uiModule, platformModule) }` called from each platform's entry point.

---

## 8. Testing Strategy (Engineering Perspective)

| Layer | Framework | What's Tested |
| :--- | :--- | :--- |
| `:shared:domain` | kotlin.test + MockK | Use case logic, mapping correctness |
| `:shared:data` | kotlin.test | JSON parsing, cache eviction |
| `:shared:ui` | Compose UI Test | Canvas renders correct number of animations, state transitions |
| `:androidApp` | JUnit + Robolectric | Koin module wiring, Android-specific bindings |
| Integration | Macrobenchmark | Frame rendering time under load |

---

## 9. Key Technical Decisions

| Decision | Rationale |
| :--- | :--- |
| **MVI over MVVM** | MVVM's two-way binding risks race conditions between audio trigger and animation state. MVI's strict unidirectional flow guarantees sync. |
| **Koin over Hilt** | Hilt is Android-only. Koin is pure Kotlin, works across all KMP targets. |
| **Oboe over MediaPlayer** | MediaPlayer has 100-200ms latency. Oboe targets < 10ms by using AAudio/OpenSL ES at the native layer. |
| **Canvas over Composable animations** | Composable recompositions are too expensive for 10+ concurrent animations at 60fps. Raw Canvas draw calls bypass the composition tree entirely. |
| **WAV over MP3/OGG** | WAV requires zero decoding at play time — critical for latency. File size trade-off is acceptable for 10 short samples (~2MB total). |
| **SKIE for iOS interop** | Exposes Kotlin StateFlow/sealed classes as native Swift types, eliminating manual bridging boilerplate. |

---

*Document maintained by Utkarsh the Engineer. Changes require Engineering review.*
