# CONTEXT CAPSULE 1.0 — Android design specification

**Status:** architecture draft, pending owner review; no Android implementation yet.
**Date:** 2026-10-24
**Device:** Lenovo Legion Y70 2026, Android 16, no root
**Application ID (planned):** `dev.lolokeksu.contextcapsule`
**Repository:** existing connected repository; name change is a separate GitHub setting.

## Goal

Build a native, offline-first, user-controlled project memory application. Preserve explicit decisions and verifiable project status across long ChatGPT conversations, and create portable handoff packets for new conversations. No paid AI API, subscription key, custom cloud server, or background scraping of ChatGPT.

## Acceptance criteria

1. Maintain multiple completely isolated local projects.
2. Add a public GitHub repository to a project and synchronize selected commits, pull requests, and available CI outcomes on demand.
3. Paste or import project decisions and tasks, preview and explicitly approve their meaning before marking them confirmed.
4. Distinguish confirmed user decisions, verified remote data, imported unverified claims, and unknowns.
5. Build structured, reproducible Markdown and JSON capsules with source links, timestamps and clear stale/incomplete labels.
6. Share or copy a reviewed handoff packet to ChatGPT manually; do not claim the message was sent until Android reports a user action, and do not promise chat integration without an API.
7. Backup, restore, and merge project data through Android's document picker with validation and explicit overwrite confirmation.
8. Work with previously cached project content when offline.

## Evidence model

`verified_remote`: public data fetched from GitHub with repository, URL, timestamp and commit SHA where available.
`owner_confirmed`: a decision specifically confirmed by the user, with time and optional source.
`unverified`: imported notes or claims without authoritative verification.
`unknown`: missing or inconsistent data, never silently guessed.

Remote CI success does not prove an unrelated feature is implemented. Conflicting statements are surfaced for review. Every GitHub snapshot retains its retrieval time and becomes stale when it cannot be refreshed.

## Main flows

**Projects:** Create/open a project, optionally attach `owner/name` GitHub repository. Projects without a repository work locally.

**Decisions:** Paste text or import selected files; user reviews and classifies each extracted statement. Do not auto-approve generated interpretations.

**Sync:** User initiates read-only GitHub fetching. Include bounded pagination, caching, rate-limit errors, partial-result warnings, retries and remote revision references.

**Capsule review:** Goal, confirmed decisions, verified repository state, open/blocked tasks, unresolved conflicts, suggested next actions, and sources. Full and compact modes clearly disclose any truncation.

**Handoff:** Use Android's standard text share sheet, copy-to-clipboard and save-file actions. The recipient app may require manual paste; never silently send chat messages.

**Backup:** Save schema-versioned JSON via SAF; provide import preview, validation, conflict review, merge/replace options and an explicit privacy warning for unencrypted exports.

## Engineering architecture

- Kotlin, Jetpack Compose, Material 3, Coroutines/Flow, Room, native Android Storage Access Framework.
- Target Android API 36; minSdk at least 26, finalized in implementation plan.
- Modules: app/UI, domain models, persistence, GitHub read-only adapter, deterministic capsule compiler; isolate boundaries with repositories/interfaces.
- Prefer app-private storage, no ads/analytics SDK, no broad filesystem or accessibility permissions.
- No OpenAI API, on-device model requirement, root, unattended GitHub writes, or automatic access to private ChatGPT history.
- Future official ChatGPT export import requires explicit opt-in and robust format validation.

## Visual experience

A minimal premium AMOLED interface with black surfaces, restrained cobalt accent and editorial typography. Large project cards, clear status, readable source provenance and easy switching between projects. Navigation: Projects / Overview / Decisions / Tasks / Sources / Capsule preview / Import-Export / Settings. Handle empty, error, stale, partial, loading and done states distinctly.

## Security

- Never upload private decisions or chat text to a public GitHub repository.
- Imported text is untrusted data, not executable instructions.
- Validate repository identifiers, URLs and JSON schemas.
- Do not persist provider tokens in source code or Android package.
- Preserve existing data when import fails; confirm before replacing, sharing or deleting.
- A separate Android app ID avoids silently overwriting previously installed applications.

## Tests and delivery

Unit tests for capsule determinism and provenance, conflicts, migrations and import integrity; integration tests for GitHub success, rate limits and offline behavior; UI smoke for projects, decisions, capsule review and export. CI must build an Android debug APK and make it available only after verification.

## Stage

The repository's default-branch working tree has been reset to documentation only. No Android code, tests, workflow, release APK, or device validation exists for CONTEXT CAPSULE yet. Historical Git commits may still contain the previous application, unless history is deliberately rewritten in a separate high-risk operation. Application implementation requires a reviewed design and implementation plan.
