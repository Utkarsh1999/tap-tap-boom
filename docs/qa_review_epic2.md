# QA Code Review — Epic 2 Scaffold
### Reviewer: Ashu the QA
### Date: 2026-02-24
### Build: Pre-compilation scaffold review

---

## Review Scope

Reviewed all **24 Kotlin source files**, **4 Gradle build files**, **1 CI pipeline**, and **1 sound pack manifest** produced by Utkarsh the Engineer in Epic 2.

---

## Findings

### 🔴 P1 — Must Fix Before Merge

| # | File | Issue | Impact |
|:--|:-----|:------|:-------|
| 1 | `CanvasViewModel.kt:170` | `System.nanoTime()` is **JVM-only** — will crash on iOS/native targets. Must use `expect/actual` or a KMP-compatible clock. | **Build break on iOS** |
| 2 | `CanvasScreen.kt:54` | `System.nanoTime()` again in animation driver — same KMP incompatibility. | **Build break on iOS** |
| 3 | `CanvasScreen.kt:3-7` | Unused imports: `LinearEasing`, `animateFloat`, `infiniteRepeatable`, `rememberInfiniteTransition`, `tween`. Dead code violates coding standards. | **CI lint failure** |
| 4 | `CanvasScreen.kt:77-86` | `detectTapGestures` only captures **single taps** — no multi-touch. PRD US-04 requires up to 5 simultaneous touches. Should use `awaitPointerEventScope` + `PointerEventPass`. | **Functional gap vs PRD** |
| 5 | `AnimationRenderer.kt` | `Offset(x, y)` **allocates new objects inside draw loops** (e.g., Burst line 70, Spiral line 88, etc.). Architecture doc mandates zero allocations in `onDraw`. | **Performance: GC pressure at 60fps** |

### 🟡 P2 — Should Fix This Sprint

| # | File | Issue | Impact |
|:--|:-----|:------|:-------|
| 6 | `CanvasViewModel.kt:117` | Key press origin `Offset(0.5f, 0.5f)` is normalized but `CanvasScreen` never scales it to actual screen size — animation will render in top-left corner. | **Visual bug** |
| 7 | `CanvasScreen.kt:52` | `LaunchedEffect(state.animations)` — key is a `List` reference. Since `state.copy()` creates new list instances, this will **restart the LaunchedEffect on every state update**, potentially causing animation jitter. Should use a stable key like `Unit` with internal checks. | **Animation stutter** |
| 8 | `AndroidAudioEngine.kt:46-50` | `preload()` calls `context.assets.openFd()` but doesn't handle `FileNotFoundException` — if a WAV file is missing, pre-load silently crashes. Needs try/catch per the graceful degradation pattern in the ViewModel. | **Crash on missing asset** |
| 9 | `TriggerInteractionUseCase.kt:28` | Touch mapping uses `pointerId % sounds.size`. Since `detectTapGestures` always passes `pointerId = 0`, every tap triggers the **same sound** (index 0 = "Kick"). Touch events need variety — should hash `(x, y)` position into index. | **UX: monotonous audio** |
| 10 | `SoundPackLoader.kt` | No error handling for malformed JSON. Edge case E-07 from test strategy requires graceful fallback. | **Crash on corrupt data** |

### 🟢 P3 — Nice to Have

| # | File | Issue |
|:--|:-----|:------|
| 11 | `AnimationRenderer.kt` | `Math.PI` / `kotlin.math.cos` — should consistently use `kotlin.math` API (not `java.lang.Math`) for KMP compatibility. |
| 12 | `build.gradle.kts` (androidApp) | `kotlinOptions { jvmTarget }` is deprecated — should migrate to `compilerOptions { jvmTarget }`. |
| 13 | Project | No unit tests exist yet — no `commonTest` source files. Test coverage is 0%. |

---

## Architecture Alignment Check

| Criterion | Status | Notes |
|:----------|:------:|:------|
| Clean Architecture layers | ✅ | Domain → Data → UI dependency direction correct |
| MVI unidirectional flow | ✅ | Intent → Reducer → State → Render pipeline is proper |
| Audio abstraction (interface) | ✅ | `AudioEngine` interface in domain, `AndroidAudioEngine` in platform |
| Koin DI wiring | ✅ | Modules exist for all layers; wiring looks correct |
| Zero-allocation Canvas draw | ❌ | `Offset` allocations in draw loops (Finding #5) |
| KMP cross-platform safety | ❌ | `System.nanoTime()` JVM-only usage (Findings #1, #2) |
| Multi-touch support | ❌ | Only `detectTapGestures` — single touch only (Finding #4) |
| Error handling / graceful degradation | ⚠️ | ViewModel handles preload errors; AudioEngine/Loader do not |

---

## Sign-off Decision

### ❌ NOT APPROVED for merge

**Rationale:** 5 P1 issues found. The code will **not compile on iOS** due to `System.nanoTime()`, does not support **multi-touch** (a P0 requirement per PRD US-04), and violates the architecture's zero-allocation rendering contract.

### Required Before Approval
1. Replace `System.nanoTime()` with KMP-compatible time source
2. Implement multi-touch pointer input handling
3. Remove unused imports
4. Reduce object allocation in `AnimationRenderer` draw loops
5. Fix key press origin scaling in `CanvasViewModel`

### Recommended Before Approval
6. Add position-based sound mapping for touch events
7. Add error handling in `AndroidAudioEngine.preload()` and `SoundPackLoader`
8. Fix `LaunchedEffect` key issue in `CanvasScreen`

---

*Review by Ashu the QA. Utkarsh the Engineer to address findings and re-submit for review.*
