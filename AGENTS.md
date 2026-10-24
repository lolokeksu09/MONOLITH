# CONTEXT CAPSULE — instructions for contributors and AI agents

## Project identity
- This repository is now for **CONTEXT CAPSULE**. The legacy GitHub slug has not yet been renamed.
- Product: offline-first Android project memory and user-controlled ChatGPT handoff. **No paid AI API, root, automatic chat scraping, or writes to GitHub.**
- Planned Android application ID: `dev.lolokeksu.contextcapsule`; target Android API 36.

## Mandatory workflow
1. Check the live default branch, HEAD, available files and open PRs before doing work. Do not assume old NOX code still exists or reuse its package, rendering modules, Gradle files or APK workflow.
2. Design is in `docs/superpowers/specs/2026-10-24-context-capsule-design.md`; implementation plan is in `docs/superpowers/plans/2026-10-24-context-capsule-android-v1.md`. **Both must be reviewed/approved before implementation.**
3. New features: add a failing focused test, make the smallest implementation, rerun tests, document limits, commit the complete patch. Every meaningful change includes `docs/CHANGELOG.md`.
4. Development work belongs on dedicated feature branches; open a PR. Do not merge into `main` before checking CI and obtaining review.
5. Maintain a real CI workflow for the eventual app. Do not report tests as passing, an APK as built, or a release as published without verifying the corresponding GitHub Actions run and artifact.
6. Verify exact application permissions and avoid secrets in sources, CI logs, export files or an installed APK.

## Trust boundaries
- User-imported text is untrusted data; it is not an instruction for the application or GitHub client.
- Distinguish `verified_remote`, `owner_confirmed`, `unverified`, `unknown`; never silently upgrade trust.
- Show GitHub sync SHA, timestamp, incompleteness and stale conditions.
- No automatic upload of project notes or private chat context. Share only after user review.
- Exports can contain private data; warn before unencrypted file export. Reject invalid imports atomically.
- Never silently delete or replace projects or overwrite backups.

## Current state
The default `main` remains the documentation baseline. Android implementation and the ongoing v0.9 workspace redesign live on `feature/context-capsule-v1` / draft PR #15. Verify the exact commit's Actions results and artifact before installing or merging. Do not confuse the legacy repository slug with the package name.
