# Changelog

Versions follow [semantic versioning](https://semver.org): MAJOR.MINOR.PATCH.
Each `## [x.y.z]` section becomes the release notes on GitHub when that version ships.

## [1.10.0] - 2026-11-15

### Added
- Font browser: "Browse all fonts" opens a full list of Google Fonts, most popular first, with search and category filters (Sans, Serif, Display, Mono, Handwriting). Each font previews its name and "12:45" in its own typeface. Works for the clock, date and app fonts.

## [1.9.0] - 2026-11-15

### Added
- App font: choose the font of Minutia's own screens from the ⋮ menu (system fonts, any Google Font, or a font file). Defaults to the phone's system font.

## [1.8.3] - 2026-11-15

### Changed
- App icon: bolder Inter "m" (ExtraBold), back to the original cream and coral on deep ink.

## [1.8.2] - 2026-11-15

### Changed
- App icon redrawn in Inter with a round colon and Material You tonal colours (sage on muted green, peach colon).

## [1.8.1] - 2026-11-15

### Changed
- App icon redrawn in Fragment Mono: its "m" and square colon, in place of the rounded custom letter.

## [1.8.0] - 2026-11-15

### Changed
- New app icon: a geometric "m" with a coral clock colon. Adapts to round/squircle launcher shapes and supports Android's themed (monochrome) icons.

### Added
- Repo handoff for AI agents and contributors: restructured `CLAUDE.md`, `AGENTS.md`/`GEMINI.md` pointers, design history in `docs/DECISIONS.md`, helper scripts in `scripts/` (wait for CI, fetch and inspect the APK, turn screen recordings into frames).

## [1.7.3] - 2026-11-15

### Fixed
- Style row no longer jitters at its ends: it's now a standard horizontal list (the Material carousel component itself was wobbling). The next style still peeks in at the edge, and the row opens scrolled to your current style.

## [1.7.2] - 2026-11-15

### Fixed
- Style carousel no longer wobbles when you keep dragging at the end of the row.

## [1.7.1] - 2026-11-15

### Changed
- Style picker is now a Material carousel. The next style always peeks in at the edge, so it's clear there are more than two.

## [1.7.0] - 2026-11-15

### Changed
- The full preview scrolls away with the settings (like Google's Wallpaper & style); the mini clock in the top bar is gone.

### Added
- "Pin preview" in the ⋮ menu keeps the full preview fixed at the top while settings scroll underneath (like widget editors).

## [1.6.4] - 2026-11-15

### Fixed
- The mini clock in the top bar now lines up with the title: it's just the time, cropped tight, at title height.
- No more glitch when the title swaps to the mini clock: fixed-size slot, a short slide + fade, and it only switches at clearly different scroll points so it can't flicker.

## [1.6.3] - 2026-11-15

### Changed
- The preview now scrolls with the settings like the rest of the page. Once it's out of view, a small live copy of the clock appears in the top bar in place of the title, so you always see the clock without a big card taking up space.

## [1.6.2] - 2026-11-15

### Fixed
- Preview card had no gap above it and sat under a differently tinted bar after scrolling. It now has even spacing, and the top bar keeps the page colour so the title and preview form one header.

## [1.6.1] - 2026-11-15

### Fixed
- Smoother collapsing preview: it now shrinks as part of the scroll (content moves with your finger instead of twice as fast) and no longer redraws the whole screen while scrolling.

### Changed
- Widget chips show the widget's size again, small and muted (e.g. "Widget 1  300×140").

## [1.6.0] - 2026-11-14

### Changed
- More Material 3: large app bar that collapses on scroll, grouped settings sections, standard segmented buttons (animated check + fill), filter chips with a check, switches with a check icon.
- The clock preview shrinks as you scroll, leaving more room for settings.
- Battery status is a proper list row with an icon and a Fix button.
- Reset moved to the ⋮ menu and now asks for confirmation. Version is shown there too.
- Built with the latest Material 3 / Compose (BOM 2026.09.00), Android Gradle Plugin 9.5 and Kotlin 2.4.

## [1.5.1] - 2026-11-14

### Changed
- Much less text in settings: shorter labels and section names, no hint paragraphs, quieter font picker and date-pattern help. Every feature is still there.

## [1.5.0] - 2026-11-14

### Added
- Motion throughout settings:
  - Option rows (font source, date font, alignment) have a highlight that glides to your choice, with a light haptic tick.
  - Settings that appear or disappear (custom date, background options, AM/PM, custom date font, colour pickers) expand and fade smoothly.
  - Switching between System / Google / File fonts crossfades.
  - Style cards spring up when chosen; the preview crossfades between styles and blends when switching light/dark wallpaper.

## [1.4.3] - 2026-11-14

### Fixed
- Alignment buttons no longer show a checkmark, matching the Font and Date font buttons (selection is shown by colour).

## [1.4.2] - 2026-11-14

### Changed
- Fonts you've searched for now sit at the start of the font row, newest first, followed by the fixed curated picks.

## [1.4.1] - 2026-11-14

### Changed
- The curated font picks are now fixed: always first, always in the same order, never replaced by recent fonts. Fragment Mono joins them as the third pick.
- Fonts you search for are added after the curated picks, each new one at the end of the row (up to 50 kept).

## [1.4.0] - 2026-11-14

### Added
- Custom date format: tap "Custom…" and type any pattern (`EEE, d MMM`) or % codes (`%a, %d %b`), with a live preview, a cheat sheet of today's values, and links to the full pattern and % code guides.
- Recently used Google Fonts now appear first in the font row, newest first (up to 50).

### Changed
- Shorter list of date presets.
- "UPPERCASE date" switch is now called "All caps".

## [1.3.1] - 2026-11-14

### Fixed
- "Same as clock" date-font option was cut off; it's now labelled "Clock". Labels that don't fit their button end in "…" instead of being clipped.

## [1.3.0] - 2026-11-14

### Added
- Separate font for the date: Default, Same as clock, or Custom. Custom has its own System / Google Fonts / File picker and weight.
- Date font can be changed on the Analog style too.

### Changed
- The old "Use this font for the date too" switch is now the "Clock" option under Date font; existing widgets keep their look.
- Weight slider is hidden for fonts loaded from a file (their weight is fixed).

## [1.2.0] - 2026-11-14

### Added
- Font suggestions while you type: start typing a name and matching Google Fonts appear as chips.

### Fixed
- Google Fonts typed in any capitalisation now work ("fragment mono" → Fragment Mono). Google's font service is case-sensitive, so the app now looks up the official name.
- Fonts that only come in one weight download fine whatever the weight slider says.
- Clearer errors: "No Google Font called …, did you mean …?" instead of raw HTTP codes.

## [1.1.0] - 2026-11-14

### Added
- "Show name under widget" switch, plus a second widget variant with the name visible.
- Tap action "Pick an app…" to open any app from the widget.
- App version shown at the bottom of settings.

### Changed
- Proper version numbers: releases are published only when the version is bumped, not on every push.
- Cleaner font settings: search field with icons, compact tabs, one-row suggestions.
- Even spacing between rows of option buttons.

### Fixed
- Tapping the clock now opens the clock app on OnePlus / OxygenOS 16.
- No app name printed under the widget by default.
- Google Fonts download now gets real TTF files Android can load.

## [1.0.0] - 2026-11-14

First version: five styles (Bold, Stacked, Pills, Analog, Words), Google Fonts and custom font files, Material You colours, per-widget settings with a live preview.
