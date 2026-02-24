# Product Requirements Document (PRD)
### tap-tap-boom — Interactive Audio-Visual Experience
**Author:** Rahul the PM
**Date:** 2026-02-24
**Version:** 1.0

---

## 1. Vision & Problem Statement

People crave playful, creative outlets on their devices — simple apps that transform touch into art. **patatap.com** proved this concept on the web, but there is no high-quality, native, cross-platform equivalent that leverages device hardware for ultra-low latency audio and smooth 60fps animations.

**tap-tap-boom** fills this gap: a premium, offline-first, cross-platform interactive instrument where every tap or keypress triggers a perfectly synchronized visual animation and sound.

---

## 2. UX Philosophy

> **"Zero Friction. Pure Play."**

- **No onboarding, no tutorials.** The app opens directly to a blank interactive canvas.
- **Immediate feedback.** Every touch must produce instant (< 50ms) audio + visual response.
- **Minimalist elegance.** The canvas IS the app. No menus, no settings on the main screen.
- **Delight through discovery.** Different keys/positions produce different sounds and animations — users discover the mappings organically.

---

## 3. MVP Definition (V1.0)

### 3.1 In Scope

| Feature | Description |
| :--- | :--- |
| **Canvas Interaction** | Full-screen Compose Canvas. Taps (mobile) or key presses (desktop) trigger events. |
| **10 Base Animations** | Ripple, burst, spiral, wave, scatter, pulse, bloom, shatter, orbit, flash. Each mapped to a distinct input. |
| **1 Core Sound Pack** | "Synth Basics" — 10 synthesized tones/percussive sounds, pre-loaded into memory buffers. |
| **Audio-Visual Sync** | Sound and animation begin in the same frame (< 16ms delta). |
| **Multi-Touch Support** | Up to 5 simultaneous touch points, each triggering independent animation + sound. |
| **Background Color Shift** | Canvas background subtly shifts hue with each interaction, creating a cumulative visual effect. |
| **Offline-First** | 100% functionality without network. All assets are bundled. |
| **Targets** | Android (API 24+), iOS (16+). |

### 3.2 Out of Scope for V1

- Desktop target (V3)
- Web target (V4)
- Custom sound pack downloads
- User recordings / export
- Social sharing
- Settings screen
- Accessibility features (V2 stretch)

---

## 4. Feature Roadmap

```
V1.0 — "First Note" (MVP)
├── Core canvas interaction
├── 10 animations + 1 sound pack
├── Android + iOS
└── Offline-first

V2.0 — "Crescendo"
├── 5 additional animation styles
├── 2 downloadable sound packs
├── Haptic feedback on iOS
├── Accessibility (VoiceOver labels)
└── Dark/light theme toggle

V3.0 — "Orchestra"
├── Desktop target (macOS, Windows, Linux)
├── Full keyboard mapping (A-Z → 26 unique combos)
├── User session recording & playback
└── Custom color palette selector

V4.0 — "Encore"
├── Web/WASM target
├── Community sound pack marketplace
├── Social sharing (video export of sessions)
└── Multiplayer jam mode (WebRTC)
```

---

## 5. User Stories

### Epic: Core Interaction
| ID | User Story | Acceptance Criteria | Priority |
| :--- | :--- | :--- | :--- |
| US-01 | As a user, I want to tap the screen and instantly hear a sound so that I feel creative control. | Sound plays < 50ms after touch. | P0 |
| US-02 | As a user, I want to see an animation bloom from my tap point so that I get visual feedback. | Animation starts in the same frame as the sound trigger. | P0 |
| US-03 | As a user, I want different keys/regions to produce different sounds so that I can create variety. | At least 10 distinct sound-animation pairs. | P0 |
| US-04 | As a user, I want to tap with multiple fingers and hear multiple sounds simultaneously so that I can create richer compositions. | Up to 5 simultaneous touches handled without audio clipping or frame drops. | P0 |
| US-05 | As a user, I want the background color to shift subtly with each tap so that the canvas feels alive. | Hue shifts by a small delta on each event; wraps smoothly. | P1 |

### Epic: Performance & Reliability
| ID | User Story | Acceptance Criteria | Priority |
| :--- | :--- | :--- | :--- |
| US-06 | As a user, I want the app to work without internet so that I can use it anywhere. | All assets bundled; no network calls at runtime. | P0 |
| US-07 | As a user, I want no lag or stutter so that the experience feels magical. | Sustained 60fps under 5-point multi-touch load. | P0 |
| US-08 | As a user, I want the app to launch quickly so that I can start playing immediately. | Cold start to interactive canvas < 2 seconds. | P1 |

---

## 6. Success Metrics

| Metric | Target | Measurement Method |
| :--- | :--- | :--- |
| **Audio Trigger Latency** | < 50ms (P95) | Instrumented logging: timestamp at Intent dispatch vs. AudioEngine.play() callback |
| **Frame Rate** | ≥ 58fps sustained | Macrobenchmark / systrace under 5-finger multi-touch load |
| **Crash-Free Sessions** | > 99.9% | Firebase Crashlytics |
| **Cold Start Time** | < 2 seconds | Automated benchmark on median spec device |
| **Average Session Duration** | > 3 minutes | Analytics event tracking |
| **App Size (Android APK)** | < 15 MB | CI artifact size check |
| **App Store Rating** | ≥ 4.5 stars | Organic reviews post-launch (3 month window) |

---

## 7. Performance Benchmarks

| Benchmark | Threshold | Test Device Baseline |
| :--- | :--- | :--- |
| Audio Latency (Android) | < 50ms P95 | Pixel 6a (mid-range reference) |
| Audio Latency (iOS) | < 20ms P95 | iPhone 12 |
| Frame Render Time | < 16.6ms per frame | Both platforms |
| Memory (steady state) | < 80 MB | Both platforms |
| Memory (peak, 5-touch) | < 120 MB | Both platforms |
| Battery Drain | < 5% per 10 min session | Both platforms (screen on) |

---

## 8. Sound Pack Schema (V1)

```json
{
  "packId": "synth-basics-v1",
  "packName": "Synth Basics",
  "version": 1,
  "sounds": [
    {
      "id": "s01",
      "label": "Kick",
      "file": "kick.wav",
      "animationType": "ripple",
      "color": "#FF6B6B",
      "keyMapping": "Q"
    },
    {
      "id": "s02",
      "label": "Snare",
      "file": "snare.wav",
      "animationType": "burst",
      "color": "#4ECDC4",
      "keyMapping": "W"
    }
  ]
}
```

> Full 10-sound mapping will be finalized during Sprint 1.

---

## 9. Risks Owned by Rahul the PM

| Risk | Mitigation |
| :--- | :--- |
| Feature creep during MVP | Strict scope freeze after this PRD is approved. Any new feature goes to V2+. |
| Sound licensing issues | Use royalty-free synthesized samples or generate via SFXR/Bfxr tools. |
| Android fragmentation hurting UX | Define "reference devices" for testing; accept degraded gracefully on very old hardware. |
| App Store rejection | Pre-review Apple guidelines for audio apps; ensure no background audio abuse. |

---

## 10. Open Questions

1. Should V1 include a subtle "tap anywhere to start" hint on first launch, or truly zero UI?
2. Should we ship a "reset canvas" gesture (e.g., long press)?
3. Haptic feedback on Android in V1 or defer to V2?

---

*Document maintained by Rahul the PM. Changes require PM approval.*
