# NOX 1.7.1 — Light Dynamics

Date: 2026-10-23. Scope: targeted quality pass on existing AURORA, PULSE and COMET procedural wallpapers. The user approved continued live-wallpaper improvements after 1.7.

## Design intent and constraints
- Preserve AMOLED pure-black backdrop, cyan/graphite UI, 6 scene IDs, nine variants, current settings, pinned studio preview, Room schema and .nox compatibility.
- Increase visual sophistication **in the render itself**, not by hiding controls or adding unused presets.
- Avoid flashing/strobing or abrupt looping; preserve safe highp GLES2-compatible GLSL and bounded loops. No additional EGL contexts, Android permissions or network.
- Other approaches considered: (a) more shader modes — increases QA/performance and obscures current new scenes; (b) new user-facing sliders requiring persisted schema or overloaded controls — risk of compatibility; (c) refine three existing modes under existing parameters — selected.

## Visual/functional refinements
1. AURORA: multi-scale drifting ribbons; inner luminous filaments, broader color-blended curtains with restrained brightness and tapered vertical envelope. More visible motion using time, seed and configurable density, glow.
2. PULSE: genuinely outward-propagating concentric waves with phase-separated rings and fade-in/out at both ends to avoid popping. Responsive focus center on active touch; maintain soft core and balanced glow. Frequency follows existing speed time input, number/thickness follows density.
3. COMET: depth-layered comet trails with deterministic individual directions, lengths and velocities. Culling based on density and lifecycle fade around phase wrap; tail/head intensity responds to glow. No new particle engine.
4. All three: no global color grading that changes legacy FLUX/HALO/NEBULA, no use of high-frequency flicker, and no runtime allocation outside existing shader uniforms. UI remains identical to avoid interfering with proven pinned preview.

## Verification and user testing
- TDD structural tests for all three new signatures, six stable uMode assignments, original legacy shader text and capped for loops.
- GitHub Actions structural checks, Kotlin tests, assembleDebug, verified APK from exact commit.
- On Lenovo Legion Y70 hardware, user tests 3 modes for visual transitions, touch, orientation, live wallpaper install, screenshot artifacts, 60/120 FPS and thermals. CI cannot prove runtime shader compilation; disclose clearly.
- Distinct debug APK studio.nox.light.lumina and Android version 1.7.1/code 11; previous 1.7 app and presets unaffected.
- Every patch: GitHub commit and docs/CHANGELOG.md update. Never merge into main without approval.
