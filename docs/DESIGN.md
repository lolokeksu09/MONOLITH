# MONOLITH / Reference visual system

Source direction: the user-provided @ithemedesign TikTok video, not a copied paid theme.

## Design tokens

- **Ink** `#000000` — AMOLED landing and drawer.
- **Paper** `#F4F3F0` — typography and active controls.
- **Soft** `#A7A6A4` — secondary editorial details.
- **Dim** `#555557` — decorative rails and dividers.
- **Hint** `#767678` — actionable instructions on black, 4.63:1 normal-text contrast.
- **Accent** `#C9C5BB` — rare emphasis, never generic bright neon.

The two scenes intentionally have different density: minimal = mostly empty black; editorial = artwork on the right, typography and application glyphs left. The generated `editorial_rock.png` is original, offline and independently usable; it is not copied from the clip.

## 0.16 / INK implementation — verification pending

The [0.16 requirement registry](PLAN_0.16.md) owns scope, status and verification. PR #35 implements this direction on a feature branch; physical visual acceptance is not claimed:

- Existing Ink / Paper / Soft / Hint tokens and true-black identity remain. No neon, glass, heavy cards or dynamic Material You default is added.
- Original preserves brand art; Mono uses grayscale; INK uses a native monochrome adaptive-icon layer on Android 13+ when available and original art otherwise. Upgrades and the current clean-install candidate remain Original. Generic Material icons never replace third-party brands.
- Launcher-owned controls use local rounded vector assets with consistent optical sizing and accessible 48dp targets. Application icons use density-aware, style/size-keyed bitmap caching bounded at 8 MiB.
- Orbit density is selectable at 8 / 12 / 15 while preserving favorite order and hidden-app exclusion. Empty space remains intentional; permanent labels around orbit icons are not added.
- The editorial scene keeps its rock and one oversized localized day abbreviation without repeated day labels. The drawer adopts the same editorial rhythm, a slim labeled search field and two-line application names.
- Media chrome identifies system media keys and unknown status rather than inventing an active player or metadata. HOME setup still disappears when the role is held; stock-HOME recovery remains available.
- Motion stays restrained. Existing reduced motion, stable press feedback, TalkBack semantics and Russian/English layouts must be rechecked on the installed candidate.

## Rhythm and type

- Screen content obeys Android safeDrawing insets. Rotated rails use measured post-rotation bounds and never use negative screen-edge offsets.
- App-menu and media tap targets are at least 48dp; minimum orbit icon-center spacing is greater than 51dp before device scaling at all designed densities.
- One oversized day abbreviation; supporting labels 10–12sp; top media controls keep distinct hierarchy and may wrap to two lines.
- Avoid card kits, heavy orbit backplates, filler labels, decorative neon and fake music metadata.

## Motion contract

- Tap/hold opens orbit. Icons spread together from the trigger with one gentle no-bounce spring for both opening and closing; there is no per-index stagger or animation-spec switch. The center trigger and close label crossfade with the same state change.
- Swipe left/right changes scene, up opens All Apps; Android Back dismisses orbit first. Incoming and outgoing scenes use equal fade durations, reduced travel and a no-bounce spatial transition. The persistent page indicator crossfades.
- Animations derive from system animator settings; turning off device animations removes delays, and the setting is re-read on subsequent recompositions.
- Selected orbit icons are decoded off the main thread before first expansion. Moving icons cannot receive taps until their 48dp targets are separated; hidden apps never surface in orbit, and empty/error catalogs are recoverable.
- Orbit expanded/collapsed state and app open/action labels are exposed through localized semantics. The clock refreshes at minute boundaries.

## Quality and limits

This is a native Compose launcher on Android 26–36. CI is a build/correctness gate, not a visual device review. Visual fidelity, gestures, motion, icon fallback and accessibility must be inspected on Lenovo Legion Y70 / Motorola Android 16 and cannot be confirmed by source code or a mockup.
