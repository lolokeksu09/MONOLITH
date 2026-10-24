# CONTEXT CAPSULE

**Android project memory · Offline-first · No paid AI API**

CONTEXT CAPSULE is a proposed native Android application that keeps explicit project decisions, tasks and source-backed GitHub state in a portable local knowledge base. It exports a reviewed Markdown/JSON **context capsule** for continuing work in a new ChatGPT conversation. ChatGPT receives the capsule via user-initiated Android sharing or copy/paste — there is no automatic access to private chats.

## Current status — 24 October 2026

| Item | Verified state |
| --- | --- |
| Repository | `lolokeksu09/NOX-LEGION` (legacy GitHub name) |
| Default branch | `main` |
| Tracked files | Android source under feature/context-capsule-v1; main remains docs-only |
| Old Android code, graphics, Gradle, APK workflow | Absent from `main` (new workflow on feature branch) |
| Active feature branches | None at the last verification |
| Open pull requests | None at the last verification |
| CONTEXT CAPSULE Android implementation | **Source code added on feature branch; build unverified** |
| APK / GitHub Actions build | **Workflow created; first build pending** |

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
