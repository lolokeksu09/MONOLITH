# MONOLITH / Reference visual system

Source direction: the user-provided @ithemedesign TikTok video, not a copied paid theme.

## Design tokens

- **Ink** `#000000` — AMOLED landing and drawer.
- **Paper** `#F4F3F0` — typography and active controls.
- **Soft** `#A7A6A4` — secondary editorial details.
- **Dim** `#555557` — quiet rails and instructions.
- **Accent** `#C9C5BB` — rare emphasis, never generic bright neon.

The two scenes intentionally have different density: minimal = mostly empty black; editorial = artwork on the right, typography and monochrome application glyphs left. The generated `editorial_rock.png` is original, offline and independently usable; it is not copied from the clip.

## Rhythm and type

- Screen content obeys Android safeDrawing insets.
- App-menu and media tap targets are at least 48dp; minimum orbit icon-center spacing is 58dp before device scaling.
- One oversized day abbreviation; supporting labels 10–12sp; top media controls keep distinct hierarchy.
- Avoid card kits, filler labels, decorative neon and fake music metadata.

## Motion contract

- Tap/hold opens orbit. Icons spread spatially from the trigger; close collapses.
- Swipe left/right changes scene, up opens All Apps; Android Back dismisses orbit first.
- Animations derive from system animator settings; turning off device animations removes delays.
- Hidden apps do not surface in orbit; offline or empty app catalogs are recoverable.

## Quality and limits

This is a native Compose launcher on Android 26–36. CI is a build/correctness gate, not a visual device review. Visual fidelity, gestures and motion must be inspected on the Lenovo Legion Y70 and cannot be confirmed by a screenshot of a mockup.
