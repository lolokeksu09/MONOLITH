# CONTEXT CAPSULE 0.9 — Workspace Overhaul Design

**Date:** 2026-10-25  
**Existing package:** `dev.lolokeksu.contextcapsule`  
**Base:** feature/context-capsule-v1, Room v2.

## Intent

Make context maintenance useful daily: immediately see what requires action, manage a task plan separately from enduring decisions, and pass a short or full evidence-labelled project brief to ChatGPT without a paid API.

## Scope and boundaries

- Keep the AMOLED cobalt visual identity; no remote assets or generated decoration in UI.
- Offline-first Room remains authoritative for projects, tasks and notes.
- Public GitHub sync is manual, read-only, and explicitly labelled as a historical snapshot.
- No private ChatGPT scraping, analytics, server, root, new Android permissions, or implicit exports.
- Existing notes and tasks are preserved; no destructive Room migration.
- All user-facing navigation stays Russian; ≥48 dp critical touch targets.

## Design

**Home:** app wordmark, restrained introduction, top-line local project pulse, functional New/Import actions, search, project cards with open tasks, pending notes and completed-task progress. Values come from scoped SQL counts, not inferred GitHub readiness.

**Project:** a 3-part section selector under the title: Overview (GitHub), Memory (decisions, pinned/unverified labels, export), Plan (tasks). Each section has its own visual hierarchy and clear empty states. Handoff is accessible from a persistent bottom area across all three sections.

**Plan:** explicit quick task entry, progress indicator and OPEN/ALL/COMPLETED filters, checkboxes, edit, confirmation-gated delete. No automatic addition of tasks except the existing user-controlled GitHub suggestion button.

**Handoff:** Copy, Share, preview, and Markdown save honor one compact/full selection. Compact includes the repository, source URL/SHA/timestamp, confirmed decisions, unverified material with labels, open tasks and instruction/data boundary. Full retains all current sections. JSON backup always exports the complete project and shows a plaintext warning.

**Trust and safety:** changes to confirmed note text demote the evidence label; pinning does not promote evidence. Existing project-scoped editing/delete operations remain; confirming decisions and completing tasks are also scoped to the active project.

## Verification / acceptance

- Empty home and populated home both render; metrics are measured from two independent projects without cross-contamination.
- Project navigation allows Overview → Memory → Plan and back; persistent handoff available on all sections.
- Plan filters operate without editing stored rows; task creation/checkboxes persist via Room.
- Compact brief materially differs from full and preserves source/provenance/uncertainty and unfinished work.
- Android JUnit and instrumented emulator jobs green for same commit; APK exists in that workflow.
- Physical Android 16 Lenovo verification still distinct from CI.
