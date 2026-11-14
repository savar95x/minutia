# Changelog

Versions follow [semantic versioning](https://semver.org): MAJOR.MINOR.PATCH.
Each `## [x.y.z]` section becomes the release notes on GitHub when that version ships.

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
