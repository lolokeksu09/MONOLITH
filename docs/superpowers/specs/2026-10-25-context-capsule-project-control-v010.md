# CONTEXT CAPSULE 0.10 — Project Control Center

**Date:** 2026-10-25  
**Existing app:** `dev.lolokeksu.contextcapsule`  
**Base:** `feature/context-capsule-v1`, Room 2.

## Goal

Make local project lifecycle controllable and reversible where possible. A user may rename a project, link/relink a public GitHub repository, archive projects and restore them, export the current project, or explicitly delete it. Nothing happens without a deliberate action.

## UX

**Home:** preserve the premium AMOLED layout and project counters. Distinguish Active and Archive with two visible filters. Archived projects appear only in the Archive filter, and retain their card, metrics and source info. They can be opened and restored.

**Project:** add a clear 'Настроить' control in the top bar. Settings provides editable title, optional GitHub repository link, JSON backup action, reversible archive action, and a separate dangerous 'Удалить проект навсегда' entry. Confirmation requires the exact case-sensitive project title plus acknowledgement. The dialog warns that all local notes, tasks and cached GitHub state will be removed, and advises prior backup.

**Data:** Room migration 2→3 adds `projects.archived` boolean default false. Existing 1→2 migration remains. Project title/repository update is atomic; only a genuinely changed repository invalidates its GitHub snapshot. Note evidence and tasks are never modified by relinking/archiving. Project deletion cascades through existing foreign keys, with exact-name validation again at persistence boundary.

**Backup:** optional `project.archived` is stored in existing schemaVersion 1 JSON and imported with default false. The user remains responsible for securing unencrypted exports.

## Acceptance

- Active and archived lists are disjoint; archive/restoration never modifies project notes or tasks.
- Changing repository removes its stale GitHub snapshot, not project data; changing title alone retains snapshot.
- UI will not allow project deletion without a case-exact name and acknowledgement; ViewModel enforces name even if UI bypassed.
- Migration preserves legacy rows, links and default active state; Android UI and unit tests pass.
- No automatic remote writes, new permissions, or background work. No changes merged into `main` before verification.
