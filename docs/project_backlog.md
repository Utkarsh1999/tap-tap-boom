# tap-tap-boom: Project Backlog

This document tracks the execution flow, epics, and task assignments for the **tap-tap-boom** project across the organization.

## Epic 1: Project Scoping and Foundation (Current Phase)
- [x] **Task 1.1:** Write Product Requirements Document (PRD) including UX Philosophy and MVP Scope.
  - **Assignee:** Rahul the PM
  - **Status:** ✅ Done → `docs/prd.md`
- [x] **Task 1.2:** Write Architecture Definition Document (ADD) and technical stack boundaries.
  - **Assignee:** Utkarsh the Engineer
  - **Status:** ✅ Done → `docs/architecture.md`
- [x] **Task 1.3:** Define Quality & Test Strategy, detailing audio latency testing approach exactly.
  - **Assignee:** Ashu the QA
  - **Status:** ✅ Done → `docs/test_strategy.md`

## Epic 2: Project Setup & CI/CD Pipeline
- [x] **Task 2.1:** Bootstrap Kotlin Compose Multiplatform (KMP) application structure (Android, iOS).
  - **Assignee:** Utkarsh the Engineer
  - **Status:** ✅ Done — Full project scaffolded with 24 Kotlin source files
- [x] **Task 2.2:** Setup base Continuous Integration (CI) pipeline (e.g., Gradle build checks, Ktfmt).
  - **Assignee:** Utkarsh the Engineer
  - **Status:** ✅ Done → `.github/workflows/ci.yml`
- [ ] **Task 2.3:** Provide baseline performance test framework configuration.
  - **Assignee:** Ashu the QA
  - **Status:** To Do

## Epic 3: Core Audio Engine & Visuals (MVP Core)
- [ ] **Task 3.1:** Define `AudioEngine` expect/actual abstractions in the common module.
  - **Assignee:** Utkarsh the Engineer
  - **Status:** To Do
- [ ] **Task 3.2:** Implement ultra-low latency audio backend for Android (Oboe / AudioTrack).
  - **Assignee:** Utkarsh the Engineer
  - **Status:** To Do
- [ ] **Task 3.3:** Implement audio backend for iOS (AVAudioEngine).
  - **Assignee:** Utkarsh the Engineer
  - **Status:** To Do
- [ ] **Task 3.4:** Create base Compose Multiplatform Canvas with tap/key-press listeners.
  - **Assignee:** Utkarsh the Engineer
  - **Status:** To Do
- [ ] **Task 3.5:** Write audio latency integration tests and automated touch-simulation scripts.
  - **Assignee:** Ashu the QA
  - **Status:** To Do

## Epic 4: Logic & Asset Management
- [ ] **Task 4.1:** Curate base sound pack, define JSON schema for mapping inputs to sounds/colors.
  - **Assignee:** Rahul the PM
  - **Status:** To Do
- [ ] **Task 4.2:** Implement StateFlow MVI architecture to map UI Intents into rendering states.
  - **Assignee:** Utkarsh the Engineer
  - **Status:** To Do
- [ ] **Task 4.3:** Edge-case testing (10-finger rapid tap, backgrounding app mid-sound, offline).
  - **Assignee:** Ashu the QA
  - **Status:** To Do

## Epic 5: Polish & Release Readiness
- [ ] **Task 5.1:** Outline final Success Metrics and analytics hooks.
  - **Assignee:** Rahul the PM
  - **Status:** To Do
- [ ] **Task 5.2:** Memory profiling, frame-rate optimizations, and stripping dead code.
  - **Assignee:** Utkarsh the Engineer
  - **Status:** To Do
- [ ] **Task 5.3:** Execute full regression suite against release builds; assess against release criteria.
  - **Assignee:** Ashu the QA
  - **Status:** To Do
- [ ] **Task 5.4:** Final project sign-off and deployment trigger.
  - **Assignee:** Rahul the PM
  - **Status:** To Do
