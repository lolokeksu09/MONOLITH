# NOX — журнал изменений

> Каждый следующий патч обязан обновлять этот файл и публиковаться коммитом в GitHub. См. [AGENTS.md](../AGENTS.md) и [DEVELOPMENT_WORKFLOW.md](DEVELOPMENT_WORKFLOW.md).

## [1.7.1 / Patch 2] — Preview packaging and regression hardening (2026-10-23)

- Updated versionName to `1.7.1`, versionCode to `11`, independent debug application ID `studio.nox.light.lumina`, launcher `NOX 1.7.1 Light Dynamics`; CI uploads `NOX-1.7.1-light-dynamics-APK`.
- Updated structural regression tests for package, version and newly described COMET branch. Build commits: `081f2678`, `f86c0923`, `8ffcdc35`, `59ec7a87`; regression correction: `52d29d96`.
- Source shader changes from Patch 1 persist; FLUX/HALO/NEBULA remain unchanged. README now documents user-visible improvements and preserving previous APKs.
- **Verification:** full NOX 1.7.1 CI pending at time of this note; Android/GL runtime QA on Lenovo unverified.

## [1.7.1 / Patch 1] — Light Dynamics shader refinement (2026-10-23)

- **AURORA:** five multi-scale drifting light curtains with broad color veils, luminous cores and delicate filaments, preserving pure-black regions.
- **PULSE:** six distinct outward-traveling waves; life-cycle edge fade avoids hard ring restarts, and active touch shifts the wave center.
- **COMET:** seven deterministic particles with depth-based brightness, per-comet direction/speed and longer variable tails; density controls visibility, phase fade prevents hard reset.
- Preserve existing FLUX, HALO, NEBULA shader branches, six scene IDs, Room schema and pinned Studio preview.
- Code commit: `d93c8b8225e99792014b971de4e20f5a916f81ba`. Regression contract: `4e38fb65732875b8f79c67870730021826c37988` (expected red before implementation).
- CI/build results: **not yet confirmed** for updated shader at this documentation commit. New shaders still require on-device GLSL, performance and visual validation.

## [1.7.0 / Patch 5] — Release candidate APK (2026-10-23)

- Version `1.7.0` (`versionCode=10`), separate debug app ID `studio.nox.light.livingscenes`, launcher label `NOX 1.7 Living Scenes`.
- CI output: [GitHub Actions run #40004992732](https://github.com/lolokeksu09/NOX-LEGION/actions/runs/40004992732), **success** for structural tests, Kotlin unit tests, `:app:assembleDebug`, and upload-artifact, on code commit `0446ab267d63d664d7091f6007d121acadcb498d`.
- Artifact: `NOX-1.7-living-scenes-APK`, GitHub artifact ID `12524570195`. Extracted `app-debug.apk`: 19,478,319 bytes (19.5 MB decimal); SHA-256 `bd69e94cbe6185baf5bfe2fcb31474a40b2866f767f2e382a56c4c645cfe5072`. ZIP integrity checked, `AndroidManifest.xml`, DEX and resources present.
- Code fixes during CI: corrected SceneLook/SceneLooks/NamedScenePreset Kotlin imports; added import regression test, prevented Home control overflow; avoided shadowing built-in `cross` in COMET shader.
- **Limits:** Kotlin/Android compilation does not execute GLES shader compilation on the target device. Actual visuals, stable performance, thermal behavior, touchscreen gestures, lock/unlock, live wallpaper apply and PNG export for new scenes remain **NOT TESTED ON DEVICE**.
- Docs: `README.md`, `docs/CHANGELOG.md`, `docs/QA_STATUS.md`, spec, plan. No automatic merge to `main`; preview install is isolated from earlier NOX packages.

## [1.7.0 / Patch 4] — Gallery, category filters and Home variants (2026-10-23)

- Home now has an accessible, horizontally scrollable selection of six scenes with `NEW` labels for AURORA, PULSE and COMET, plus a modal listing three curated variants for the selected new scene. Selecting a variant edits only an unsaved draft.
- Added lightweight Canvas representations of AURORA, PULSE and COMET to avoid six GL rendering surfaces.
- The gallery retains scene, name, favorites, sort and two-column art grid while adding Signature/Ambient/Tech/Cosmic category filtering.
- Added structural regression tests for all three new thumbnails, category filtering, current-configuration export and pinned live preview.
- Commit series: `722ccb8` tests, `00ba447` Home, `f22a0ce` Canvas/gallery; baseline renderer unchanged since Patch 2.
- **Verification:** passed structural tests, Kotlin tests and Android compilation in CI run #40004992732; hardware appearance and touch handling remain unverified.

## [1.7.0 / Patch 3] — Nine scene variants (2026-10-23)

- Created `ScenePresets.kt` with 3 named variations each for AURORA (Polar, Emerald, Violet Mist), PULSE (Cyan Beat, Crimson Core, Neon Echo), COMET (Deep Space, White Tail, Blue Drift).
- Reuses the existing `SceneConfig` fields with deterministic seed values; no Room migration. Added JUnit tests for names, exact colors, ranges and `.nox` round-trip.
- Commit series: `0b270e1` (red contracts), `3c047d3` (implementation).
- **Verification:** passed in CI run #40004992732; no on-device visual QA yet.

## [1.7.0 / Patch 2] — Procedural renderer (2026-10-23)

- Added GPU modes 3=AURORA, 4=PULSE, 5=COMET in the shared `GlBackend.kt`, maintaining existing modes 0=FLUX, 1=HALO, 2=NEBULA.
- New scenes use bounded iteration and existing colors, glow, motion and seed; old three shader branches retain their math.
- Added structural tests checking distinct mode dispatch and legacy NEBULA branch.
- Commit series: `34ab1c1` (red contracts), `6eb6297` (shader implementation).
- **Verification:** Kotlin/Android CI passed in run #40004992732; GLES shader compilation on Lenovo and actual appearance still need device checks.

## [1.7.0 / Patch 1] — Catalog (2026-10-23)

- Added `SceneRegistry` / `SceneCategory` and registered AURORA, PULSE, COMET without changing the defaults of FLUX, HALO or NEBULA.
- Added `SceneRegistryTest` covering ordered IDs, categories, defaults and normalization.
- Changes: `SceneRegistry.kt`, `SceneConfig.kt`, `SceneRegistryTest.kt`; commit series `91ef2e0`, `8b663b9`, `42e3871`.
- **Verification:** catalogue/legacy model tests and full CI passed (run #40004992732); no device tests.

## [1.7.0] — IMPLEMENTED PREVIEW / аппаратная QA ожидается (2026-10-23)

- **Запланировано:** AURORA, PULSE, COMET; всего 6 сцен.
- **Запланировано:** 9 стартовых вариаций, категории сцен, горизонтальный переключатель с миниатюрами и отметками NEW.
- **Архитектура:** текущий SceneConfig без изменения Room; общий OpenGL рендерер с режимами 0–5; обратная совместимость FLUX/HALO/NEBULA.
- **Документы:** [спецификация](superpowers/specs/2026-10-23-nox-1-7-living-scenes-design.md) и [план из 5 патчей](superpowers/plans/2026-10-23-nox-1-7-living-scenes.md).
- **Ветка:** `feature/nox-1-7-living-scenes` от `feature/nox-1-6-curated`.
- **Текущий статус:** исходники реализованы, Android debug APK собран и проверен в CI; аппаратная проверка GLES и автономности ещё не выполнялась.

## [1.6.0] — исходная основа новой ветки (2026-10-23)

- **Содержится в базе:** три сцены FLUX, HALO, NEBULA; избранное в коллекции и быстрые цветовые стили на главном экране.
- **Ветка:** `feature/nox-1-6-curated`; существующий PR #7 остаётся отдельным.
- **Статус:** известна успешная предыдущая CI-сборка; подтверждение работы 1.6 непосредственно на телефоне в эту запись не вносилось.

## Правила ведения

Для каждого реализованного этапа 1.7 добавлять новую запись с конкретной датой, SHA коммита, изменёнными файлами/функциями, результатами тестов, CI, ограничениями и статусом аппаратной проверки. Не подменять «запланировано» утверждением «сделано».
