# MONOLITH / Reference visual system

Source direction: the user-provided @ithemedesign TikTok video, not a copied paid theme.

## Design tokens

- **Ink** `#000000` — AMOLED landing and drawer.
- **Paper** `#F4F3F0` — typography and active controls.
- **Soft** `#A7A6A4` — secondary editorial details.
- **Dim** `#555557` — decorative rails and dividers.
- **Hint** `#767678` — actionable instructions on black, 4.63:1 normal-text contrast.
- **Accent** `#C9C5BB` — rare emphasis, never generic bright neon.

The two scenes intentionally have different density: minimal = mostly empty black; editorial = artwork on the right, typography and monochrome application glyphs left. The generated `editorial_rock.png` is original, offline and independently usable; it is not copied from the clip.

## Documented 0.16 / INK direction — not implemented

The [0.16 requirement registry](PLAN_0.16.md) owns scope, status and verification. This section records only the visual direction:

- Keep the existing Ink / Paper / Soft / Hint tokens and true-black identity. Do not add neon, glass, gradients, heavy cards or dynamic Material You color as the default.
- Application artwork gains Original / Mono / INK modes. Original preserves current brand art; Mono uses a tested grayscale treatment; INK uses a native monochrome adaptive-icon layer on Android 13+ when available and visibly falls back to original art otherwise. Upgrades remain on Original. Never substitute generic Material icons for third-party brands or force one destructive mask.
- Launcher-owned controls move to one Material Symbols Rounded family with consistent optical sizing and accessible 48dp targets. Application icons receive density-aware, optically consistent bounds without unbounded cache growth.
- Orbit density becomes selectable at 8 / 12 / 15 while preserving favorite order and hidden-app exclusion. Empty space is intentional; permanent labels around every orbit icon are not.
- The editorial scene keeps its rock and one oversized day abbreviation but removes repeated day labels. The drawer adopts the same editorial rhythm, a slim labeled search field and less premature name truncation.
- Media and HOME-setup chrome become contextual and honest. No fabricated track/artist metadata or new media permission is part of this direction; stock-HOME recovery remains available.
- Motion stays restrained. New work must preserve reduced motion, stable press feedback, TalkBack semantics and Russian/English layouts rather than adding continuous decorative animation.

## Rhythm and type

- Screen content obeys Android safeDrawing insets. Rotated rails use measured post-rotation bounds and must never use negative screen-edge offsets.
- App-menu and media tap targets are at least 48dp; minimum orbit icon-center spacing is 58dp before device scaling.
- One oversized day abbreviation; supporting labels 10–12sp; top media controls keep distinct hierarchy and may wrap to two lines.
- Avoid card kits, heavy orbit backplates, filler labels, decorative neon and fake music metadata.

## Motion contract

- Tap/hold opens orbit. Icons spread together from the trigger with one gentle no-bounce spring for both opening and closing; there is no per-index stagger or animation-spec switch. The center trigger and close label crossfade with the same state change.
- Swipe left/right changes scene, up opens All Apps; Android Back dismisses orbit first. Incoming and outgoing scenes use equal fade durations, reduced travel and a no-bounce spatial transition. The persistent page indicator crossfades instead of switching abruptly.
- Animations derive from system animator settings; turning off device animations removes delays, and the setting is re-read on subsequent recompositions rather than cached permanently.
- Selected orbit icons are decoded off the main thread before the first expansion. Moving icons cannot receive taps until their 48dp targets are separated; hidden apps never surface in orbit, and offline or empty app catalogs are recoverable.
- Orbit expanded/collapsed state and app open/action labels are exposed through localized semantics. The clock refreshes at minute boundaries rather than forcing HOME recompositions every 15 seconds.

## Quality and limits

This is a native Compose launcher on Android 26–36. CI is a build/correctness gate, not a visual device review. Visual fidelity, gestures and motion must be inspected on the Lenovo Legion Y70 and cannot be confirmed by a screenshot of a mockup.
