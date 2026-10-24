# CONTEXT CAPSULE

**Android project memory · Offline-first · No paid AI API**

CONTEXT CAPSULE is a proposed native Android application that keeps explicit project decisions, tasks and source-backed GitHub state in a portable local knowledge base. It exports a reviewed Markdown/JSON **context capsule** for continuing work in a new ChatGPT conversation. ChatGPT receives the capsule via user-initiated Android sharing or copy/paste — there is no automatic access to private chats.

## 0.4.0 preview — умный обзор проекта

При нажатии «Обновить» приложение напрямую считывает из публичного GitHub:
- README / описание проекта, последние 6 коммитов (ссылки на исходные изменения).
- Последний запуск CI для default branch: `success`, `failure`, `in_progress` или `unknown` без выдуманных статусов.
- Открытые PR и Issues с первыми связанными ссылками (данные первых 100, предупреждение о неполноте).
- Краткий вывод по CI из проверяемых правил, а не платной или «волшебной» нейросети.
- Читаемую сводку в экспортируемой капсуле для продолжения чата.

Экраны теперь начинают проект с обзора репозитория, ниже остаются капсула, решения и задачи. Данные сохраняются в существующем поле `GithubSnapshot.summary`, миграция Room не требуется. Старые снимки не переименовываются в новые и требуют повторной синхронизации.

**Ограничения:** публичный GitHub API имеет лимит без авторизации; данные снимка могут устареть. Успех CI не доказывает готовность продукта. Приложение не читает приватные чаты или закрытые репозитории и не использует OpenAI API.

## 0.3.0 preview — проектная память без лишних действий

- **Системное «Поделиться» → CONTEXT CAPSULE:** выдели текст в ChatGPT, Telegram или браузере, открой меню Android «Поделиться», выбери CONTEXT CAPSULE, проверь текст и укажи проект. Сохранение происходит только после выбора; импортированные утверждения остаются `UNVERIFIED`.
- **Поиск проектов:** по названию и адресу GitHub, с понятным пустым результатом.
- **Поиск по решениям:** внутри проекта; фильтр «На проверку» показывает записи, которые ещё не подтверждены.
- **Компактный главный экран:** когда проекты существуют, исчезает большой рекламный блок, освобождая место рабочим карточкам.
- Защита: текст из других приложений ограничен 20 000 символами и не передаётся во внешние сервисы автоматически.
- Не требует доступа к приватной истории ChatGPT, OpenAI API, root или новых сетевых разрешений.

**Ограничения:** принимается только обычный текст `text/plain`, а не фотографии, файлы или весь архив чатов. Локальная база остаётся прежней (Room v1) и не мигрирует. Android-сборка и установка на реальном устройстве проверяются отдельно.

## 0.2.0 preview — visual identity

The Android feature branch now uses a refined, **Russian-localized AMOLED dashboard and project workspace**:
- Explicit WCAG-oriented text contrast; no black-on-black fallback.
- Product-specific glowing capsule geometry drawn in native Compose Canvas (no remote imagery/assets).
- A distinctive project overview with primary Create action, real empty-state onboarding, truthful local/GitHub status.
- Project detail with source verification, approved/unverified decisions, next tasks and a prominent ChatGPT handoff.
- No new permissions, backend or paid API.

**Build status:** GitHub Actions is the source of truth. A source commit is not a verified device installation. Debug signing may vary across GitHub runners; back up existing local projects before changing installed builds.

## Current status — 24 October 2026

| Item | Verified state |
| --- | --- |
| Repository | `lolokeksu09/NOX-LEGION` (legacy GitHub name) |
| Default branch | `main` |
| Tracked files | Android source under feature/context-capsule-v1; main remains docs-only |
| Old Android code, graphics, Gradle, APK workflow | Absent from `main` (new workflow on feature branch) |
| Active feature branches | `feature/context-capsule-v1` |
| Open pull requests | [PR #15](https://github.com/lolokeksu09/NOX-LEGION/pull/15) |
| CONTEXT CAPSULE Android implementation | Debug preview available in feature branch |
| APK / GitHub Actions build | See latest successful GitHub Actions artifact for the exact commit |

The repository's name is still `NOX-LEGION`. Renaming it to `CONTEXT-CAPSULE` requires a separate GitHub repository-settings operation. Historic commits may still contain the old app; branch deletion is not Git-history erasure.

## Planned v1 experience

1. Create local projects and store approved decisions, references and tasks.
2. Refresh public GitHub repository metadata, commits, PR and Actions results **read-only** on demand.
3. Keep provenance explicit: `verified_remote`, `owner_confirmed`, `unverified`, `unknown`.
4. Generate deterministic source-linked Markdown or JSON snapshots; display stale/partial data clearly.
5. Preview, copy, save and share a context capsule into ChatGPT manually.
6. Import/export a versioned local project backup, with validation and explicit data-replacement approval.

No root, no proprietary server, no hidden chat collection, no paid OpenAI API.

## Architecture (planned, not implemented)

- Device: Lenovo Legion Y70 2026, Android 16.
- Kotlin + Jetpack Compose + Material 3, Room, coroutines/Flow, Android Storage Access Framework.
- Target API 36; planned app ID `dev.lolokeksu.contextcapsule`.
- Read-only public GitHub REST API, a deterministic capsule composer, and local project database.
- Premium accessible AMOLED-black UI with cobalt accents and large project cards.

## Design and development

- [Architecture and product specification](docs/superpowers/specs/2026-10-24-context-capsule-design.md)
- [Implementation plan — review before coding](docs/superpowers/plans/2026-10-24-context-capsule-android-v1.md)
- [Developer rules](AGENTS.md)
- [Changelog](docs/CHANGELOG.md)

**Next milestone:** review the specification and plan, then create a feature branch, write tests before code, implement the Android client and verify its APK through GitHub Actions. A debug APK is only available after an Android Actions run succeeds; no build result has been validated yet.
