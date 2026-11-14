# Minutia: notes for AI/dev sessions

Native Android home-screen clock widget (Kotlin, Jetpack Compose settings UI). Package `dev.savar.minutia`, minSdk 29, targetSdk 34.

## Build & release
- No local Android SDK is assumed. **Push to `main`**: `.github/workflows/build.yml` runs `gradle :app:assembleRelease` (Gradle 8.9, AGP 8.5.2, Kotlin 2.0.20, Compose BOM 2024.09.00). Pushes touching only `*.md` / `docs/**` skip CI.
- **Versioning**: `version.properties` (`VERSION_NAME=MAJOR.MINOR.PATCH`) is the single source. `versionCode = major*10000 + minor*100 + patch`. CI publishes a GitHub Release (tag `vX.Y.Z`, asset `Minutia-X.Y.Z.apk`, notes = that version's section of `CHANGELOG.md`) only when that tag doesn't exist yet; other pushes just build-check and upload an Actions artifact.
- **To ship a change**: bump `VERSION_NAME` (patch for fixes, minor for features) and add a `## [X.Y.Z] - date` section to `CHANGELOG.md` in the same push. Never reuse or lower a version.
- Old `build-1`…`build-6` releases (versionCode 1–6) predate this scheme.
- Signed with `app/release.jks` (committed). **Never change the key or `applicationId`**, or installed copies can't update (uninstall wipes widget settings).
- R8 minify + resource shrinking are on. Config enums are stored by `name` and parsed with `entries.firstOrNull`, so they're safe under R8.

## Architecture (app/src/main/java/dev/savar/minutia)
- `ClockRenderer.kt`: draws the whole widget into a Bitmap (Canvas). Each style returns a `Block` laid out at unit size 100, then scaled to fit the widget area × `sizePercent`. Styles: BOLD, STACKED, PILLS, ANALOG, WORDS. Add a style = add enum in `WidgetConfig.kt` + a block function + a `when` branch.
- `ClockWidgetProvider.kt`: `AppWidgetProvider` + `TickScheduler` (exact alarm at each minute boundary, `USE_EXACT_ALARM`) + `WidgetUpdater` (RemoteViews with one ImageView; size from widget options, capped at ~1.2 MP). Also re-renders on TIME_SET / TIMEZONE / LOCALE / BOOT / package replaced.
- `WidgetConfig.kt`: per-widget settings as JSON in SharedPreferences (`w_<id>`); new widgets inherit the last edited config.
- `FontManager.kt`: system fonts, Google Fonts download, local .ttf/.otf import. Fonts are stored in `filesDir/fonts`.
- `MainActivity.kt`: Compose settings screen with live preview; also the widget configure/reconfigure activity.

## Gotchas
- **Google Fonts format depends on User-Agent.** `curl/8.5.0` and an Android-4 UA get TTF; IE6 gets EOT and Safari 5 gets WOFF, which Android can't load. Files are validated by magic bytes (`00010000`/`OTTO`/`true`/`ttcf`). A CI step logs what each UA receives.
- **Two widget providers**: `ClockWidgetProvider` (label U+2800, blank, so launchers like OxygenOS print no name under it) and `LabeledClockWidgetProvider` (label "Minutia"). A placed widget's label can't change at runtime, so the "Show name under widget" switch pins the other variant (new widget inherits the last-saved config). `WidgetUpdater.ids()` covers both; only the main provider receives TICK/system broadcasts and it updates all ids.
- **Widget taps go through `TapActivity`** (Theme.NoDisplay trampoline), which resolves the clock at tap time: SHOW_ALARMS → known OEM clock packages → any launchable app whose package contains "clock". `TapAction.APP` launches a user-picked package. Needs the MAIN/LAUNCHER `<queries>` entry.
- Owner's phone: OnePlus 13s, OxygenOS 16. Aggressive battery management, so the app offers a "disable battery optimisation" button.
- Commit as `Savar Verma <102670483+savar95x@users.noreply.github.com>` (public repo, keep personal email out).
