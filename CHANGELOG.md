# Changelog

Versions follow [semantic versioning](https://semver.org): MAJOR.MINOR.PATCH.
Each `## [x.y.z]` section becomes the release notes on GitHub when that version ships.

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
