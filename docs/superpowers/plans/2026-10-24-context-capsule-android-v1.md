# CONTEXT CAPSULE Android 1.0 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver an offline-first native Android project-memory app that produces source-backed handoff packets for ChatGPT without a paid API.

**Architecture:** Kotlin/Compose app with Room-backed local project/decision/task data; an explicit read-only public GitHub connector; a pure deterministic capsule formatter; and Android Storage Access Framework / Sharesheet import-export. Network failure never prevents viewing cached local projects. All imported text remains untrusted until the owner approves its status.

**Tech Stack:** Kotlin, AGP/Gradle, Jetpack Compose Material 3, Room, coroutines/Flow, JUnit, Android SDK API 36, AndroidX Activity, AndroidX lifecycle, Ktor/OkHttp or Android's HTTP stack (choose one and pin versions before implementation).

**Spec:** `docs/superpowers/specs/2026-10-24-context-capsule-design.md`

## Global Constraints

- Device: Lenovo Legion Y70 2026, Android 16; target SDK 36; minSdk >= 26.
- Package: `dev.lolokeksu.contextcapsule`; no reuse of old packages.
- No root, paid OpenAI API, embedded AI API key, custom backend, unattended GitHub write access, ChatGPT scraping, or broad filesystem permission.
- On-demand read-only GitHub requests only; local notes never transmitted to GitHub.
- Deterministic Markdown and schema-versioned JSON; explicit reviewed Share/Copy and overwrite confirmation.
- Keep `main` protected by review process where possible. Start implementation in `feature/context-capsule-v1`.
- Log work in `docs/CHANGELOG.md` and link real CI runs; do not claim APK until verified.

## File map (planned)

- `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`: pinned build/plugins/dependencies
- `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`: installable application, explicit minimum permissions
- `app/src/main/java/dev/lolokeksu/contextcapsule/MainActivity.kt`: Compose host and navigation
- `app/src/main/java/dev/lolokeksu/contextcapsule/ui/ProjectScreens.kt`: project list and details
- `app/src/main/java/dev/lolokeksu/contextcapsule/ui/CapsuleScreen.kt`: preview and export UX
- `app/src/main/java/dev/lolokeksu/contextcapsule/domain/ProjectModels.kt`: immutable domain models and source statuses
- `app/src/main/java/dev/lolokeksu/contextcapsule/domain/CapsuleComposer.kt`: deterministic context packet generator
- `app/src/main/java/dev/lolokeksu/contextcapsule/data/ProjectDatabase.kt`: Room schema / DAOs and migrations
- `app/src/main/java/dev/lolokeksu/contextcapsule/data/ProjectRepository.kt`: storage/validation interface
- `app/src/main/java/dev/lolokeksu/contextcapsule/github/PublicGithubClient.kt`: read-only GitHub fetch adapter
- `app/src/main/java/dev/lolokeksu/contextcapsule/io/BackupCodec.kt`: versioned local backup parsing
- `app/src/main/java/dev/lolokeksu/contextcapsule/io/ShareCoordinator.kt`: explicit Android export/share integration
- `app/src/test/java/dev/lolokeksu/contextcapsule/`: unit tests by component
- `.github/workflows/android.yml`: Gradle unit tests and debug APK build; no signing secrets
- `docs/CHANGELOG.md`, `README.md`: changes and reproducible verification

## Review Focus: five failure modes to pin in tests

1. Stale GitHub data must never be labeled current after a network failure — Task 3.
2. Imported user notes cannot become `owner_confirmed` automatically — Tasks 2 and 4.
3. Invalid/conflicting backups cannot silently replace existing rows — Task 5.
4. Capsule source references and SHA must remain intact in compact mode — Task 2.
5. Android share targets may reject plain text; Copy and Save alternatives must remain available — Task 5.

---

### Task 1: Minimal Android foundation and CI

**Files:** Create build/Gradle/manifest/activity files, `.github/workflows/android.yml`, smoke tests.

**Interfaces:**
- Produces: `MainActivity : ComponentActivity`, `ContextCapsuleApp()` composable, application ID `dev.lolokeksu.contextcapsule`.

- [ ] **Step 1: Write an Android instrumentation smoke test** that launches the app and verifies a projects heading and empty-state action.
- [ ] **Step 2: Verify the test fails or does not compile** before app UI exists, recording output.
- [ ] **Step 3: Implement a minimal Compose app** with a high-contrast AMOLED theme, and minimum Android manifest permissions (none until network module is added).
- [ ] **Step 4: Run Gradle unit tests and debug assemble**, inspect output and debug manifest for correct application ID; provide instrumentation-run outcome separately (emulator/device).
- [ ] **Step 5: Commit** foundation code, CI and changelog. Verify workflow build before claiming APK.

### Task 2: Domain and deterministic capsule formatter

**Files:** `domain/ProjectModels.kt`, `domain/CapsuleComposer.kt`, `app/src/test/java/dev/lolokeksu/contextcapsule/domain/CapsuleComposerTest.kt`.

**Interfaces:**
- `enum class EvidenceKind { VERIFIED_REMOTE, OWNER_CONFIRMED, UNVERIFIED, UNKNOWN }`
- `data class CapsuleInput(val project: Project, val decisions: List<Decision>, val tasks: List<ProjectTask>, val snapshot: GithubSnapshot?)`
- `class CapsuleComposer { fun toMarkdown(input: CapsuleInput, compact: Boolean = false): String; fun toJson(input: CapsuleInput): String }`

- [ ] **Step 1: Test** that `toMarkdown` output has Goal, Approved decisions, GitHub status, Open tasks, Unknowns and Sources; repeated calls with same input yield identical bytes.
- [ ] **Step 2: Confirm the test fails** before implementing the formatter.
- [ ] **Step 3: Define immutable domain types and formatter**, preserving exact source URLs, SHAs and sync timestamps in both full and compact mode; never upgrade unverified decisions.
- [ ] **Step 4: Test** stale snapshot labeling and compact source preservation; run focused JUnit suite.
- [ ] **Step 5: Commit** domain code and test evidence with changelog.

### Task 3: Public GitHub read-only adapter

**Files:** `github/PublicGithubClient.kt`, `app/src/test/java/dev/lolokeksu/contextcapsule/github/PublicGithubClientTest.kt`.

**Interfaces:**
- `interface GithubSource { suspend fun fetchSnapshot(repository: String, branch: String?): GithubSyncResult }`
- `sealed interface GithubSyncResult { data class Success(val snapshot: GithubSnapshot): GithubSyncResult; data class Partial(val snapshot: GithubSnapshot, val reason: String): GithubSyncResult; data class Failure(val reason: String): GithubSyncResult }`

- [ ] **Step 1: Test** repository slug validation, known fixtures (commits/PRs/Actions), rate-limit 403/429, HTTP errors, timeouts and partial pagination.
- [ ] **Step 2: Confirm tests fail** without connector.
- [ ] **Step 3: Implement bounded, read-only HTTPS GitHub REST fetching** with explicit timeout and user-triggered refresh, recording `checkedAt`, HEAD SHA and sources; add only `INTERNET` permission.
- [ ] **Step 4: Run adapter tests** and verify no API token requirement or network POST/PUT/PATCH/DELETE.
- [ ] **Step 5: Commit** connector, tests and changelog.

### Task 4: Local persistence and reviewed decisions

**Files:** `data/ProjectDatabase.kt`, `data/ProjectRepository.kt`, `ui/ProjectScreens.kt`, storage and view-model tests.

**Interfaces:**
- `interface ProjectStore { fun observeProjects(): Flow<List<Project>>; suspend fun createProject(title: String, repository: String?): String; suspend fun saveDecision(projectId: String, text: String, evidence: EvidenceKind); suspend fun saveSnapshot(projectId: String, snapshot: GithubSnapshot) }`
- Projects and decisions use stable IDs; a decision added from imported text defaults to `UNVERIFIED` and requires explicit user confirmation to become `OWNER_CONFIRMED`.

- [ ] **Step 1: Test** project isolation, empty title rejection, imported-note trust default, offline read and transaction behavior.
- [ ] **Step 2: Confirm tests fail** before implementation.
- [ ] **Step 3: Implement Room schema, repositories, migrations strategy**, Compose project cards, project detail, decision review and explicit GitHub sync action.
- [ ] **Step 4: Run database and UI smoke tests**, document any unverified device-only paths.
- [ ] **Step 5: Commit** storage, UX, tests, changelog.

### Task 5: Capsule review, Android sharing, import/export

**Files:** `ui/CapsuleScreen.kt`, `io/BackupCodec.kt`, `io/ShareCoordinator.kt`, matching tests.

**Interfaces:**
- `object BackupCodec { const val SCHEMA_VERSION: Int = 1; fun encode(data: ProjectBackup): String; fun decode(raw: String): ProjectBackup }`
- `ShareCoordinator`: operations for user-initiated copy, ACTION_SEND text and SAF create/open document flows, avoiding direct writes outside user-selected URIs.

- [ ] **Step 1: Test** UTF-8/special characters, duplicate project IDs, unsupported version, corrupt data, schema migration rejection and atomic merge/replace.
- [ ] **Step 2: Confirm tests fail** before implementation.
- [ ] **Step 3: Implement import preview, user-selected merge/replace, export warnings and deterministic capsule preview with Copy/Share/Save actions.**
- [ ] **Step 4: Test** fallback when no share target exists, one-project round trip and no data mutation on invalid backup; validate Android 16 flow on device separately.
- [ ] **Step 5: Commit** export/import, UX, tests, changelog.

### Task 6: Release verification and handoff

**Files:** `.github/workflows/android.yml`, `README.md`, `docs/CHANGELOG.md`, test configuration.

- [ ] **Step 1: Run `./gradlew testDebugUnitTest lintDebug assembleDebug`**, recording actual results and failed task names.
- [ ] **Step 2: Verify GitHub Actions full run**, test count and named debug APK artifact, matching exact source SHA.
- [ ] **Step 3: Check security** (manifest permissions, no embedded tokens, no analytics/network exfiltration of notes), status labels, accessibility and data-loss warnings.
- [ ] **Step 4: Verify install, basic workflow, Sharesheet and SAF on Lenovo Legion Y70 Android 16**, or explicitly mark device QA pending.
- [ ] **Step 5: Update README and changelog** with exact build command, artifacts, verified CI link and known limits; open PR for review. Do not merge without review.

## Current execution gate

Plan published for user review. **No application implementation has started**. Review the architecture specification and this plan before executing Task 1. Build/plugin versions must be freshly checked and pinned in Task 1 rather than guessed.
