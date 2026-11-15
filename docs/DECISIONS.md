# Design decisions and history

Why things are the way they are, including what was tried and rejected. Newest first.
Versions refer to [`CHANGELOG.md`](../CHANGELOG.md).

## Visual identity
- **App icon (1.8.0)**: a geometric lowercase "m" made of rounded strokes, plus a coral clock colon ("m:"),
  cream on deep ink-green. Five concepts were mocked up (abstract pills, pills with digits, "m:", a colon
  whose lower dot is a clock, cropped "9:4" numerals); "m:" was chosen because it ties to the name, stays
  legible at launcher size and works as a single-colour themed icon. Built from strokes, not font outlines,
  so it's a clean vector. Regenerate previews with `docs/icon/make.py`.
- **Name (1.1.0)**: "Minutia" (minute + minutiae, tagline *Sweat the small stuff*). Other candidates were
  Kern, Fontime, Halfpast and Dialtone. Glyph was avoided (Nothing's trademark), Tempo/Chrono/Epoch are crowded.

## Settings screen layout
- **Preview behaviour (1.7.0)**: default = the full preview scrolls away with the page (like Google's
  Wallpaper & style); optional **Pin preview** in the ⋮ menu keeps it fixed (like widget editors such as KWGT).
  Rejected along the way, as not production-like:
  - 1.6.0–1.6.2: a pinned preview card that shrank from 200dp to 108dp while scrolling (looked odd, even
    after moving it to a nested-scroll header and removing the app bar tint).
  - 1.6.3–1.6.4: a mini clock that replaced the app bar title once the preview scrolled away (misaligned,
    then judged non-standard even when aligned and smooth).
- **Style picker (1.7.1–1.7.3)**: two 150dp cards fit the width exactly, hiding the other three styles.
  The Material 3 uncontained carousel fixed discoverability but jittered at the ends on the owner's phone,
  even with overscroll off. Final: `LazyRow` with 132dp cards so the next one peeks.
- **Material 3 (1.6.0)**: the owner asked for "as Material as possible": large collapsing app bar, grouped
  sections, segmented buttons with the stock check, chips with checks, switch thumb icons, list-item battery
  row, Reset moved into the ⋮ menu behind a confirmation. Toolchain upgraded (AGP 9.5, Kotlin 2.4, BOM 2026.09)
  to get the newest Material components.
- **Motion (1.5.0 → 1.6.0)**: a custom sliding segmented selector with haptics was built to make the app feel
  alive. The owner preferred the stock Material segmented button's animated check, so it was reverted.
  `Reveal` (expand/fade) and the crossfades stayed.
- **Copy (1.5.1)**: the owner found the UI wordy. Labels were cut hard and hint paragraphs removed. Keep it terse.
- **Segment labels**: "Same as clock" was clipped and became "Clock". Labels must fit a third of the card.
- **Checkmarks**: removed from segments in 1.4.3 for uniformity, restored for all of them in 1.6.0 (stock Material).

## Fonts
- **Curated chips are fixed** (owner request, 1.4.1): never reordered or replaced; Fragment Mono is 3rd.
  **Recents go first, newest first** (1.4.2), up to 50.
- **Custom date font (1.3.0)**: Default / Clock / Custom, with the same picker as the clock.
- **Case-insensitive names (1.2.0)**: "fragment mono" failed because the CSS API is case-sensitive; the app
  downloads Google's family list to resolve official names and offer type-ahead.
- **TTF only (1.1.0)**: Google Fonts picks the format by User-Agent; CI verified which UAs get TTF.

## Widget behaviour
- **Rendered bitmap + exact minute alarm (1.0.0)**: needed for arbitrary/downloaded fonts and the custom styles.
  RemoteViews `TextClock` can't use downloaded fonts.
- **No name under the widget (1.1.0)**: OxygenOS prints the widget label under widgets; the label is U+2800.
  A second, labelled provider exists for anyone who wants the name ("Show name" switch pins it).
- **Tap trampoline (1.1.0)**: tapping did nothing on OxygenOS 16 (clock package not in the known list), so
  taps resolve the clock app at tap time with fallbacks; "Pick an app…" lets the user choose.

## Process
- **Build in CI (1.0.0)**: the agent sandbox can't download the Android SDK or Maven, so GitHub Actions builds
  and the agent inspects the APK. The repo had to be created by the owner; the agent pushes to it.
- **Semantic versioning (1.1.0)**: replaced the early `build-N` releases; a release is published only when
  `version.properties` changes, with notes from `CHANGELOG.md`.
- **Privacy**: commits use the GitHub no-reply address; history was rewritten after the first commit leaked
  the personal email (the old `build-1` tag still references it).
