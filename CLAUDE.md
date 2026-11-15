# Minutia: handoff for AI agents

Native Android home-screen **clock widget** with a Jetpack Compose settings app. Kotlin, package
`dev.savar.minutia`, minSdk 29 · targetSdk 34 · compileSdk 37. Owner: Savar Verma (GitHub `savar95x`),
daily driver **OnePlus 13s, OxygenOS 16**. Public repo: https://github.com/savar95x/minutia

Read this file first. Design history and rejected ideas: [`docs/DECISIONS.md`](docs/DECISIONS.md).
User-facing changes per version: [`CHANGELOG.md`](CHANGELOG.md).

---

## Golden rules
1. **Never change the signing key (`app/release.jks`) or `applicationId`.** Installed copies could no
   longer update, and uninstalling wipes the owner's widget settings. `scripts/inspect-apk.py` checks the signer.
2. **Every shipped change = version bump + changelog entry in the same push** (see Versioning). CI publishes
   the release; then send the owner the APK.
3. **Commit as** `Savar Verma <102670483+savar95x@users.noreply.github.com>`. Never use his personal email:
   the repo is public. End commit messages with the co-author / session lines your harness asks for.
4. **Material 3, minimal copy.** Short labels, no explanatory paragraphs. Prefer stock Material components
   over custom ones (see UI conventions). Small, focused changes; don't redesign what the owner approved.
5. **Don't claim it works on a phone.** You can't run it. Say it built and the APK checks out, and ask for a
   screenshot or screen recording when visuals or motion matter.

## The working loop (no Android SDK needed locally)
GitHub Actions is the compiler. A typical change:
```bash
git pull --rebase
# edit code; for a release also bump version.properties + add a CHANGELOG.md section
git commit -am "…" && git push
scripts/ci-wait.sh                        # waits for CI; prints Kotlin errors on failure
scripts/fetch-apk.sh                      # latest release APK -> out/
python3 scripts/inspect-apk.py out/Minutia-X.Y.Z.apk "Some new label"   # pip install androguard
```
- A failed build publishes nothing, so it's safe to bump the version in the same push as risky code.
- To check something compiles before releasing, push **without** bumping the version: CI only build-checks.
- To try library or toolchain upgrades, push the dependency change alone first; fix, then add UI work.
- Owner feedback usually arrives as phone screenshots or `.mp4` screen recordings. Use
  `scripts/video-frames.sh rec.mp4 1` for an overview, then a higher fps and a crop to inspect motion.
- If `gh` can't reach the repo, the session's GitHub connection may need re-attaching. Creating repos,
  setting Actions secrets and deleting releases were **not** permitted through the original connection.

## Build & release
- Workflow: `.github/workflows/build.yml` runs `gradle :app:assembleRelease` on `ubuntu-latest`
  (`gradle-version: current`, Java 17). Pushes touching only `*.md`, `docs/**`, `scripts/**` skip CI.
- Toolchain: **AGP 9.5.1 with built-in Kotlin** (do *not* add `org.jetbrains.kotlin.android`), Compose
  compiler plugin `org.jetbrains.kotlin.plugin.compose` 2.4.21, **Compose BOM 2026.09.00**
  (needs AGP ≥ 9.1 and compileSdk ≥ 37), activity-compose 1.13.0, lifecycle 2.11.0.
- **Versioning**: `version.properties` → `VERSION_NAME=MAJOR.MINOR.PATCH` is the single source.
  `versionCode = major*10000 + minor*100 + patch`. Patch for fixes, minor for features. Never reuse or lower.
- **Release**: if tag `vX.Y.Z` doesn't exist yet, CI publishes a GitHub Release `Minutia X.Y.Z` with asset
  `Minutia-X.Y.Z.apk` and notes = that version's `## [X.Y.Z] - date` section of `CHANGELOG.md`.
  Otherwise it only build-checks and uploads an Actions artifact.
- R8 minify + resource shrinking are on. Config enums are stored by `name` and parsed with
  `entries.firstOrNull`, so they survive R8. Lint is not run for release builds.
- A CI step logs what Google Fonts serves per User-Agent (see Gotchas).

## Architecture (`app/src/main/java/dev/savar/minutia`)
| File | What it does |
|---|---|
| `ClockRenderer.kt` | Draws the whole widget into a Bitmap (Canvas). Each style returns a `Block` laid out at unit size 100, then scaled to fit the area × `sizePercent`. Styles: BOLD, STACKED, PILLS, ANALOG, WORDS. New style = enum in `WidgetConfig.kt` + block function + `when` branch. |
| `ClockWidgetProvider.kt` | `AppWidgetProvider`s (see two providers below), `TickScheduler` (exact alarm each minute, `USE_EXACT_ALARM`), `WidgetUpdater` (RemoteViews with one ImageView; size from widget options, capped ~1.2 MP). Re-renders on TIME_SET / TIMEZONE / LOCALE / BOOT / package replaced. |
| `WidgetConfig.kt` | Per-widget settings, JSON in SharedPreferences `widget_configs` (`w_<id>`); new widgets inherit the last edited config (`last`). `FontSpec`, `DateFontMode`, enums. |
| `FontManager.kt` | System fonts, Google Fonts download + catalogue + type-ahead, local .ttf/.otf import (`filesDir/fonts`), recent fonts. `typeface(ctx, FontSpec)` resolves any font. |
| `DateFormats.kt` | Date presets and custom patterns: SimpleDateFormat letters or strftime `%` codes (`fromStrftime`). Invalid patterns aren't saved; renderer falls back to the first preset. |
| `AppFont.kt` | Font of the app's own UI (not the widget): `FontSpec` in prefs `ui`/`app_font`, observable state read by `AppTheme`; Google fonts get 400/500/700 downloaded so the type hierarchy survives. Default = system font. |
| `FontBrowser.kt` | `FontBrowserSheet`: full-height sheet of all Google Fonts (popularity order, search, category chips), rows preview name + "12:45" via `FontManager.previewTypeface` (Google `text=` subset, few KB, cached in `filesDir/fonts/preview`, max 4 concurrent). Opened from "Browse all fonts" in `FontEditor`. |
| `TapActivity.kt` | Invisible trampoline for widget taps (see Gotchas). |
| `MainActivity.kt` | Compose settings screen + widget configure/reconfigure activity. Composables: `SettingsScreen`, `PreviewCard`, `FontEditor` (reusable System/Google/File picker + weight, used for clock and date), `Section`, `SwitchRow`, `LabeledSlider`, `ColorPicker`, `Segmented`, `Chip`, `ChipFlow`, `Reveal`, `AppPickerDialog`. |

Fonts are `FontSpec`s: `cfg.clockFont`/`withClockFont()` and `cfg.dateFont`/`withDateFont()`; the date
font is used only when `dateFontMode == CUSTOM` (`DEFAULT` = sans-serif 500, `CLOCK` = clock font).
Old configs' `dateUsesClockFont` migrates to `CLOCK`.

App icon: Inter Display **ExtraBold** "m:", cream `#F0ECDE` "m" + coral `#FF8A65` round colon on
`@color/ic_launcher_bg` (#121614 deep ink). Adaptive vector `res/drawable/ic_launcher_foreground.xml`
+ `ic_launcher_monochrome.xml` (themed icons), **generated** by
`docs/icon/make.py --font InterDisplay-ExtraBold.otf --write` from real glyph outlines.
Don't hand-edit the path data; change the script's constants and regenerate. Full spec: `docs/icon/DESIGN.md`.

## UI conventions and owner preferences
- Scaffold + collapsing `LargeTopAppBar` (default colours). ⋮ menu: **App font…** (bottom sheet reusing `FontEditor` with `showWeight = false`), **Pin preview** (checkbox, app pref
  `ui`/`pin_preview`), **Reset widget…** (confirm dialog, destructive), version.
- `PreviewCard` (200dp) is the first item of the scrolling settings (scrolls away, like Wallpaper & style),
  or pinned above the list when Pin preview is on (then the app bar has no scrolled tint).
- `Section(title)` = primary-coloured label above a `surfaceContainer`, `extraLarge`-shape group.
- Option rows: `Segmented` = Material `SingleChoiceSegmentedButtonRow` with the default animated check.
  Keep labels short enough for a third of the card ("Default · Clock · Custom").
- Chips via `Chip` (FilterChip + check when selected). `SwitchRow` = whole row toggles, Switch has a check thumb.
- Style picker: plain `LazyRow` of **132dp** cards so the next one peeks; never size cards so a whole
  number fit exactly. Opens scrolled to the selected style.
- Font chips row: **recents first (newest first), then the curated list** `FontManager.googleSuggestions`
  in fixed order. The owner wants **Fragment Mono 3rd** in the curated list. Never reorder/replace curated
  picks; curated names are never stored as recents (max 50 recents).
- Widget chips: "Widget N" + muted `W×H` (dp).
- Conditional settings appear via `Reveal(visible) { }` (expand + fade).
- Settings copy is deliberately terse ("Wallpaper colours", "Shadow", "On tap", "Battery").

## Gotchas
- **RemoteViews can't use downloaded fonts**, so the widget is a rendered bitmap updated every minute by an
  exact alarm, not a `TextClock`.
- **Google Fonts names are case-sensitive** in the CSS API (`fragment mono` → 400, `Fragment Mono` → 200).
  `FontManager` downloads `fonts.google.com/metadata/fonts` (~2.7 MB, ~1,960 families, cached weekly in
  `filesDir/fonts/catalogue2.tsv` as `name<TAB>category`, popularity order, brand fonts excluded) to map typed names to official ones and for type-ahead, then falls back
  to Title Case + known acronyms (IBM, DM, EB, PT…).
- **Google Fonts format depends on User-Agent.** `curl/8.5.0` and an Android 4 UA get TTF; IE6 gets EOT and
  Safari 5 gets WOFF, which Android can't load. Files are validated by magic bytes
  (`00010000`/`OTTO`/`true`/`ttcf`). Single-weight families (e.g. Fragment Mono) fall back to the plain URL.
- **Two widget providers**: `ClockWidgetProvider` (label U+2800, blank, so launchers like OxygenOS print no
  name under it) and `LabeledClockWidgetProvider` (label "Minutia"). A placed widget's label can't change at
  runtime, so "Show name" pins the other variant (it inherits the last-saved config). `WidgetUpdater.ids()`
  covers both; only the main provider receives TICK/system broadcasts and it updates all ids.
- **Widget taps go through `TapActivity`** (Theme.NoDisplay trampoline), which resolves the clock at tap time:
  SHOW_ALARMS → known OEM clock packages → any launchable app whose package contains "clock".
  `TapAction.APP` launches a user-picked package. Needs the MAIN/LAUNCHER `<queries>` entry.
- OxygenOS kills background work aggressively. The Battery row offers "Fix"
  (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`).
- Material 3 `HorizontalUncontainedCarousel` jitters at its ends on device, even with overscroll disabled.
  Don't use it.

## Known issues / open items
- **Signing key is committed to a public repo**, so anyone could sign an "update". Fix: make the repo private,
  or move the keystore to Actions secrets (needs the owner, or a connection allowed to set secrets).
- Old releases `build-1`…`build-6` (pre-semver) are still on GitHub. `build-1`'s tag points at a commit that
  contains the owner's personal email. The owner should delete them (the agent connection couldn't).
- No automated tests and no emulator in the loop; verification = CI build + APK inspection + owner's device.
- `targetSdk` is still 34. Bumping it would need a behaviour review (edge-to-edge is already on).
