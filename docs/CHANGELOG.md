# NOX — журнал изменений

> Каждый следующий патч обязан обновлять этот файл и публиковаться коммитом в GitHub. См. [AGENTS.md](../AGENTS.md) и [DEVELOPMENT_WORKFLOW.md](DEVELOPMENT_WORKFLOW.md).

## [1.7.0 / Patch 3] — Nine scene variants (2026-10-23)

- Created `ScenePresets.kt` with 3 named variations each for AURORA (Polar, Emerald, Violet Mist), PULSE (Cyan Beat, Crimson Core, Neon Echo), COMET (Deep Space, White Tail, Blue Drift).
- Reuses the existing `SceneConfig` fields with deterministic seed values; no Room migration. Added JUnit tests for names, exact colors, ranges and `.nox` round-trip.
- Commit series: `0b270e1` (red contracts), `3c047d3` (implementation).
- **Verification:** Android CI pending; no device validation or shader screenshots at this stage.

## [1.7.0 / Patch 2] — Procedural renderer (2026-10-23)

- Added GPU modes 3=AURORA, 4=PULSE, 5=COMET in the shared `GlBackend.kt`, maintaining existing modes 0=FLUX, 1=HALO, 2=NEBULA.
- New scenes use bounded iteration and existing colors, glow, motion and seed; old three shader branches retain their math.
- Added structural tests checking distinct mode dispatch and legacy NEBULA branch.
- Commit series: `34ab1c1` (red contracts), `6eb6297` (shader implementation).
- **Verification:** Android shader compilation and hardware appearance are pending; GLES shader correctness must be verified on Lenovo Legion Y70.

## [1.7.0 / Patch 1] — Catalog (2026-10-23)

- Added `SceneRegistry` / `SceneCategory` and registered AURORA, PULSE, COMET without changing the defaults of FLUX, HALO or NEBULA.
- Added `SceneRegistryTest` covering ordered IDs, categories, defaults and normalization.
- Changes: `SceneRegistry.kt`, `SceneConfig.kt`, `SceneRegistryTest.kt`; commit series `91ef2e0`, `8b663b9`, `42e3871`.
- **Verification:** GitHub Actions test run queued/in progress when this entry was written; no device tests. Shaders and UI not yet implemented.

## [1.7.0] — PLANNED / спецификация подготовлена (2026-10-23)

- **Запланировано:** AURORA, PULSE, COMET; всего 6 сцен.
- **Запланировано:** 9 стартовых вариаций, категории сцен, горизонтальный переключатель с миниатюрами и отметками NEW.
- **Архитектура:** текущий SceneConfig без изменения Room; общий OpenGL рендерер с режимами 0–5; обратная совместимость FLUX/HALO/NEBULA.
- **Документы:** [спецификация](superpowers/specs/2026-10-23-nox-1-7-living-scenes-design.md) и [план из 5 патчей](superpowers/plans/2026-10-23-nox-1-7-living-scenes.md).
- **Ветка:** `feature/nox-1-7-living-scenes` от `feature/nox-1-6-curated`.
- **Текущий статус:** только документы; реализация, CI для новых шейдеров и APK **ещё не выполнены**. Записи последующих патчей добавлять отдельно, с SHA и проверками.

## [1.6.0] — исходная основа новой ветки (2026-10-23)

- **Содержится в базе:** три сцены FLUX, HALO, NEBULA; избранное в коллекции и быстрые цветовые стили на главном экране.
- **Ветка:** `feature/nox-1-6-curated`; существующий PR #7 остаётся отдельным.
- **Статус:** известна успешная предыдущая CI-сборка; подтверждение работы 1.6 непосредственно на телефоне в эту запись не вносилось.

## Правила ведения

Для каждого реализованного этапа 1.7 добавлять новую запись с конкретной датой, SHA коммита, изменёнными файлами/функциями, результатами тестов, CI, ограничениями и статусом аппаратной проверки. Не подменять «запланировано» утверждением «сделано».
