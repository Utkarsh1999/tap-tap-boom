# tap-tap-boom: Project Backlog

This document tracks the execution flow, epics, and task assignments for the **tap-tap-boom** project across the organization.

## Epic 1: Project Scoping and Foundation ✅
- [x] **Task 1.1:** Write Product Requirements Document (PRD) including UX Philosophy and MVP Scope.
- [x] **Task 1.2:** Write Architecture Definition Document (ADD) and technical stack boundaries.
- [x] **Task 1.3:** Define Quality & Test Strategy, detailing audio latency testing approach.

## Epic 2: Project Setup & CI/CD Pipeline ✅
- [x] **Task 2.1:** Bootstrap Kotlin Compose Multiplatform (KMP) application structure.
- [x] **Task 2.2:** Setup base CI pipeline (`.github/workflows/ci.yml`).
- [x] **Task 2.4:** Architecture code review and review bug fixes.

## Epic 3: Core Audio Engine & Visuals ✅
- [x] **Task 3.1–3.4:** Implement low-latency audio engine, Canvas UI, and unit tests.
- [x] **Task 3.5:** Verify compilation and architecture across multiple platforms.

## Epic 4: Logic & Asset Management ✅
- [x] **Task 4.1:** Curate synthesized sound pack and define asset schema.
- [x] **Task 4.2:** Implement MVI State Management logic.
- [x] **Task 4.3:** Multi-touch edge-case testing and UI regression scripts.

## Epic 5: Polish & Release Readiness ✅
- [x] **Task 5.1:** Success metrics and analytics integration.
- [x] **Task 5.2:** Performance profiling and R8 minification.
- [x] **Task 5.6:** Aesthetic harmonic enrichment (Premium palette + FM synthesis).
- [x] **Task 5.7:** "Smoothness" pass: AD envelopes to prevent audio pops.

## Epic 6: Musical Instrument Mode ✅
- [x] **Task 6.1:** Implement Grid-based sound pad logic (4x3 pads).
- [x] **Task 6.2:** Add visual grid feedback and pad highlights.
- [x] **Task 6.3:** Implement drag-to-rotate sound gesture logic per pad.
- [x] **Task 6.4:** Verify predictability and multi-touch stability on grid layout.

## Epic 7: Feel & Juice (Dopamine & Haptics) ✅
- [x] **Task 7.1:** Implement KMP `HapticEngine` and Android `Vibrator` implementation.
- [x] **Task 7.2:** Design "Dopamine Hits": Screen shake and color intensity spikes.
- [x] **Task 7.3:** Add "Combo Meter" logic for scaling interaction energy.
- [x] **Task 7.4:** Audio detuning for organic feel during rapid patterns.

---

## Future Roadmaps (Phase 2) 🔄
- [ ] Record & Export functionality.
- [ ] Multi-player synchronized jamming (KMP WebSockets).
- [ ] Advanced Synthesis (Wavetable, Granular).
