#!/usr/bin/env python3
"""CI guard for MONOLITH's user-locked, unmodified installed-app icons.

This intentionally checks the established source-level presentation contract.
It is not a substitute for visual device acceptance. If the user explicitly
approves a different icon treatment, update AGENTS.md, DESIGN.md and this guard
in the same independently reviewed PR.
"""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
UI = ROOT / "app/src/main/java/dev/monolith/launcher/ui"
ERRORS: list[str] = []


def content(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        ERRORS.append(f"Could not read {path.relative_to(ROOT)}: {exc}")
        return ""


def require(haystack: str, snippet: str, context: str) -> None:
    if snippet not in haystack:
        ERRORS.append(f"{context}: missing approved native-icon baseline: {snippet!r}")


def forbid(haystack: str, snippet: str, context: str) -> None:
    if snippet in haystack:
        ERRORS.append(f"{context}: forbidden icon decoration/transformation: {snippet!r}")


def section(source: str, start: str, end: str, context: str) -> str:
    a = source.find(start)
    b = source.find(end, a + len(start)) if a >= 0 else -1
    if a < 0 or b < 0:
        ERRORS.append(f"{context}: icon composable boundaries changed; review icon fidelity explicitly")
        return ""
    return source[a:b]


def main() -> int:
    cache = content(UI / "AppIconCache.kt")
    drawer = content(UI / "MonolithScreen.kt")
    home = content(UI / "ReferenceHome.kt")

    for snippet in (
        "context.packageManager.getActivityIcon(",
        "AppIconStyle.ORIGINAL -> drawable.toBitmap(",
        "style: AppIconStyle = AppIconStyle.ORIGINAL",
    ):
        require(cache, snippet, "Android package icon source")
    require(drawer, "rememberAppIcon(app, iconStyle)", "All Apps source artwork")
    require(home, "rememberAppIcon(apps[index], iconStyle)", "Orbit source artwork")

    tile = section(
        drawer, "private fun DrawerAppTile(", "\n@Composable\nprivate fun SortPicker(",
        "All Apps app icon tile"
    )
    orbit = section(
        home, "private fun OrbitCluster(", "\n@Composable\nprivate fun MediaControl(",
        "Orbit app icons"
    )

    for snippet in (
        "Box(Modifier.size(51.dp), contentAlignment = Alignment.Center)",
        "modifier = Modifier.size(44.dp)",
    ):
        require(tile, snippet, "All Apps — approved unframed 0.21 icon sizes")
    for snippet in (
        ".size(48.dp)",
        "modifier = Modifier.size(38.dp)",
    ):
        require(orbit, snippet, "Orbit — approved 0.21 icon sizes/touch targets")

    # Only inspect the app-image composables, not the design surrounding them.
    for context, block in (("All Apps icon", tile), ("Orbit icon", orbit)):
        for forbidden in (
            ".background(", ".border(", ".clip(", "colorFilter",
            "ColorFilter", "shadow(", "blur(", "Brush.",
        ):
            forbid(block, forbidden, context)

    if ERRORS:
        print("FAILED: MONOLITH native icon artwork is a locked user requirement.", file=sys.stderr)
        for issue in ERRORS:
            print(f"  - {issue}", file=sys.stderr)
        print(
            "Do not modify app icons during general UI work. A change requires "
            "separate explicit user approval and a reviewed contract update.",
            file=sys.stderr,
        )
        return 1

    print(
        "PASS: original Android app icon source, unframed All Apps icons, "
        "and 0.21 Orbit icon presentation are preserved."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
