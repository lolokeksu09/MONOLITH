# NOX 1.9.1 Cinematic Depth — implementation plan

**Base:** `feature/nox-1-9-graphics-overdrive`. **Branch:** `feature/nox-1-9-1-cinematic-depth`. **Spec:** `docs/superpowers/specs/2026-10-24-nox-1-9-1-cinematic-depth-design.md`.

## Patch A — tests, design and source-level scene contracts
- [ ] Add Python red tests validating all nine `CINEMATIC` branches, `smoothNoise`, `cloudField`, quality gating, localized Ultra dust and original 1.9 renderer/overlay preservation.
- [ ] Verify failure with GitHub Actions structural test. Ensure no unintended changes to settings/preset code.
- [ ] Commit red tests and CHANGELOG entry.

## Patch B — richer procedural scenes
- [ ] Implement smooth, interpolated cloud noise functions and nine cinematic scene branches in existing fragment shader with bounded loops and existing colors/time/density/glow/seed.
- [ ] Keep Balanced not materially affected: added contributions conditional on Rich and Ultra. Avoid uniform gray and strobe.
- [ ] Check red->green structural, GLSL 100 validation, Kotlin tests and Android build. Document code and limitations per commit.

## Patch C — preview package and release
- [ ] Version 1.9.1/code14, new independent debug application ID `.cinematic`, title, CI artifact `NOX-1.9.1-cinematic-APK`; update README/CHANGELOG/QA docs.
- [ ] Confirm exact HEAD successful CI, original uploaded artifact, and APK ZIP validity, hash, manifest and DEX. Deliver ZIP and APK (ZIP user preference).
- [ ] PR for review targeting 1.9.0 branch, not main. Never claim Lenovo validation unless performed.
