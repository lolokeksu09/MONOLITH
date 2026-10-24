# NOX 1.9.1 — Cinematic Depth, visual-composition pass

**Status:** design refinement of the owner-approved Graphics Overdrive scope; implementation and validation pending. **Date:** 2026-10-24. **Base:** NOX 1.9.0 `feature/nox-1-9-graphics-overdrive`.

## Problem and outcome

NOX's nine GLSL live wallpapers can be sparse even with the 1.9 Rich/Ultra light boost. The user explicitly wants more **content, visual density, composition and graphics**, not more scene IDs or decorative UI. Success means the screen shows recognizable broad/mid/fine layers of light, distinct silhouettes and depth while maintaining true AMOLED black between forms. Runtime quality on the Lenovo device must still be verified.

## Alternatives and decision

1. Real-time full-resolution convolution bloom: strong visual potential but high GPU cost; reject on phone.
2. Network or video wallpapers: storage, permissions and battery, not offline-native; reject.
3. **Selected:** quality-gated procedural cinematic composition inside existing shader — no Room/data model changes, no new services or GPU textures.

This is a **bounded continuation** of the previously specified 1.9 graphics upgrade; no new user-facing functionality or UX restructuring.

## Visual blueprint (9 distinct art directions)

| Scene | New foreground/secondary content beyond 1.9 | Depth and composition |
|---|---|---|
| FLUX | ribbon wings, nested luminous filaments and a few energy knots | intersecting strands spanning screen height |
| HALO | secondary elliptical arcs, orbit beacons and low-frequency corona | massive central ring system |
| NEBULA | continuous multi-octave gas field and two broad cloud lobes | large nebula spanning vertical screen, not rectangular grid |
| AURORA | extra drifting curtain bands and vertical hairline texture | tall cinematic curtains across nearly full height |
| PULSE | a dense central disc and a soft orbiting halo | readable energy reactor with concentric field |
| COMET | deep star haze and moving secondary hero streak | foreground/background scale separation |
| VEIL | smoke folds and inner luminous veins with smooth noise | broad glowing cloudlike curtains |
| GRID | distant skyline beams, horizon arcs and convergence | illuminated ground plane + overhead geometry |
| SHARDS | several large rotating diamond silhouettes and light edges | central crystal plus lateral faceted satellites |

## Engineering

- Preserve existing 9 modes 0–8 and the previously approved shader formulas; append a quality-gated **CINEMATIC** pass inside `if(uQuality>0.5)` after prior per-mode premiumDetail, with `vec3 cinematicDetail`.
- Lightweight `smoothNoise` / `cloudField` shader helpers for clouds only. No image textures, readback or CPU scene engine.
- `Rich` mode enables large shapes, `Ultra` further adds restrained microscopic energy specks localized to existing light with a **second quality threshold**; Balanced retains existing baseline.
- Avoid flashes, gray backdrop lift, hard tile boundaries, unbounded loops, `active` GLSL identifier, more than 9 additional fixed-iteration elements per scene, and uniform spam.
- Maintain `uQuality`, power/thermal downgrade, wallpapers/preview, settings, import/export. Save format unchanged.
- Preview version `1.9.1` code `14`, debug suffix `.cinematic`, launcher `NOX 1.9.1 Cinematic`. Independent preview package prevents accidental replacement of 1.9.0 debug data.

## Mandatory QA

- Write red structural tests for quality-gated cinematic path and all nine named scene branches; run existing structure/GLSL/JUnit/assembleDebug in GitHub Actions.
- Verify final run head SHA and ZIP/APK CRC and embedded AndroidManifest.xml/classes.dex/resources.arsc.
- **Device QA required:** compare Balanced/Rich/Ultra at 60/120FPS, visual fullness of NEBULA/VEIL/AURORA/SHARDS, actual effects, lock/unlock, wallpaper and thermals on Lenovo Legion Y70. No claim of hardware-proven quality before evidence.
- Every source patch gets GitHub commit and CHANGELOG note, per AGENTS.md. No automatic main merge.
