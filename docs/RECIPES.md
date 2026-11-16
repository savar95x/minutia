# Recipes

Step-by-step for the changes that come up most. Read [`CLAUDE.md`](../CLAUDE.md) first.
Every recipe ends the same way: push, `scripts/ci-wait.sh`, and for a release, `scripts/fetch-apk.sh` +
`scripts/inspect-apk.py` (see [Ship a release](#ship-a-release)).

## Add a widget setting
1. `WidgetConfig.kt`: add the field with a default to the data class.
2. Same file: `toJson()` → `put("name", value)`; `fromJson()` → `o.optX("name", d.name)`. Enums: store
   `.name`, read with `enumOr(...)`. Old configs lack the key, so the default must look like today's behaviour.
3. `ClockRenderer.kt`: use it in the style block(s) it affects.
4. `MainActivity.kt` `SettingsScreen`: add a control inside the right `Section`. Reuse `SwitchRow`,
   `Segmented`, `LabeledSlider`, `ChipFlow`/`Chip`, `ColorPicker`. Hide dependent rows with `Reveal(visible)`.
   Keep the label to one or two words.
5. Settings are saved per widget and new widgets inherit `last`, so nothing else is needed.
6. Minor version bump + `### Added` changelog line.

## Add a clock style
1. `WidgetConfig.kt`: new `ClockStyle` entry with a short `label`. Never rename existing entries (they're
   stored by name).
2. `ClockRenderer.kt`: a `private fun yourStyle(...): Block` laid out at unit size `s` (100), then a branch in
   `when (cfg.style)`. Copy the closest existing style (`bold`, `stacked`, `pills`, `analog`, `words`).
3. The style picker (`LazyRow` of 132dp cards) picks it up from `ClockStyle.entries` automatically. Check the
   cards still don't fit a whole number across a ~400dp screen.
4. Update the style list in `README.md`. Ask the owner for a screenshot before replacing `docs/screenshots/`.

## Change a font list
- Curated chips: `FontManager.googleSuggestions`. Owner rule: fixed order, Fragment Mono 3rd. Append new
  picks only when asked.
- Names must be the official Google family names (case-sensitive). Check one with
  `curl -s -A curl/8.5.0 "https://fonts.googleapis.com/css2?family=Name+Here"` (HTTP 200 = valid).

## Change the app icon
Edit constants in `docs/icon/make.py` (palette is the owner's: don't change colours unasked), then
`pip install fonttools cairosvg` and
`python3 docs/icon/make.py --font InterDisplay-ExtraBold.otf --write` (Inter from rsms/inter releases, or
`/usr/share/fonts/opentype/inter/` if `fonts-inter` is installed). Never hand-edit the vector path data.
Update `docs/icon/DESIGN.md` history and send the owner `docs/icon/icon.png`.

## Upgrade a dependency
Push the version change alone, without a version bump (CI build-checks only). Fix errors, then layer UI work.
Compose BOM, AGP and compileSdk are coupled (see CLAUDE.md Build & release).

## Ship a release
1. `version.properties`: patch for fixes, minor for features.
2. `CHANGELOG.md`: new `## [X.Y.Z] - YYYY-MM-DD` section at the top (`### Added/Changed/Fixed`). Its text
   becomes the GitHub release notes, so write it for the owner, tersely.
3. Commit as the owner's no-reply identity (CLAUDE.md golden rule 3), push.
4. `scripts/ci-wait.sh` → `scripts/fetch-apk.sh` → `python3 scripts/inspect-apk.py out/Minutia-X.Y.Z.apk "New label"`.
   It must print the new version and `signer … OK`; passing a string checks it's in the APK.
5. Send the owner the APK. Say it built and checks out; don't say it works on the phone.

## Work from owner feedback
- Screenshots: look closely at padding, alignment and clipping; the owner notices small misalignments.
- Recordings: `scripts/video-frames.sh rec.mp4 1` for an overview, then e.g. `… 15 1080:600:0:300` to inspect
  jitter or a transition frame by frame.
- If a request could mean two things and the work is costly, ask first.
- Record approved or rejected design directions in `docs/DECISIONS.md` so the next agent doesn't retry them.
