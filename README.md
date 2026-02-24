# tap-tap-boom 🚀

An interactive audio-visual experience built with **Kotlin Compose Multiplatform**. Press physical keys or tap the screen to trigger high-fidelity, synchronized animations and procedural sounds.

Inspired by [patatap.com](https://patatap.com).

## ✨ Features

- **Low-Latency Audio**: Leverages Android's `SoundPool` for instant response.
- **Procedural Assets**: 10+ synthetic `.wav` files (kick, snare, synth notes) generated via DSP scripts.
- **Dynamic Visuals**: GPU-accelerated Compose Canvas animations (ripples, blooms, sweeps).
- **Analytics**: Built-in interaction tracking and engagement metrics.
- **Optimized**: Production-grade minification and resource shrinking via R8.

## 🏗️ Architecture

- **Clean Architecture + MVI** (Model-View-Intent) for predictable state management.
- **KMP Modules**:
    - `:shared:domain`: Pure logic, entities, and `AudioEngine` abstractions.
    - `:shared:data`: Sound pack loading and PCM buffer caching.
    - `:shared:ui`: Shared Canvas renderer and Unidirectional Data Flow ViewModels.
- **DI**: Powered by **Koin**.

## 🛠️ Development

### Prerequisites
- JDK 21
- Android Studio (Ladybug or later)

### Build & Run
```bash
# Debug APK
./gradlew :androidApp:assembleDebug

# Release APK (Minified)
./gradlew :androidApp:assembleRelease
```

### Continuous Verification
We maintain a full regression suite covering formatting, unit tests, and UI tests:
```bash
chmod +x scripts/run_regression.sh
./scripts/run_regression.sh
```

## 📚 Documentation
- [Product Requirements (PRD)](docs/prd.md)
- [Architecture Definition (ADD)](docs/architecture.md)
- [Quality & Test Strategy](docs/test_strategy.md)
- [Final Project Walkthrough](.gemini/antigravity/brain/c2e59c2a-c147-49f8-a644-0621dcc32222/walkthrough.md)
