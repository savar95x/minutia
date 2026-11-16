<p align="center"><img src="docs/icon/icon.png" width="96" alt="Minutia icon" /></p>

<h1 align="center">Minutia</h1>

<p align="center"><em>Sweat the small stuff.</em> A home-screen clock widget for Android where every detail is yours to tweak.</p>

<p align="center">
  <img src="docs/screenshots/styles.webp" width="760" alt="Four Minutia styles: Bold, Pills, Words centred and Words left-aligned in a thin font" />
</p>
<p align="center"><sub>Bold · Pills · Words · Words, left-aligned in a thin Google Font. Colours come from the wallpaper.</sub></p>

<br />

<table align="center"><tr>
<td width="240"><img src="docs/screenshots/home.webp" width="240" alt="Minutia on a OnePlus 13s home screen" /></td>
<td width="380">

**On the home screen**

Just the time, in the font and colours you choose. No box around it and no name underneath.

Tap it to open your clock app.

<sub>OnePlus 13s · OxygenOS 16</sub>

</td>
</tr></table>

- **5 styles**: Bold, Stacked, Pills, Analog, Words
- **Any Google Font**, typed in any capitalisation with suggestions as you type, or your own `.ttf`/`.otf`. Separate font for the date
- **Material You** colours from your wallpaper, or pick your own
- Size, weight, letter spacing, alignment, shadow, background, 12/24h, date presets or your own pattern (`EEE, d MMM` or `%a %d %b`)
- Per-widget settings with a live preview
- Tap to open your clock app (or any app you pick)
- No name under the widget by default; *Show name* adds a named one

## Install
Grab `Minutia-x.y.z.apk` from the [latest release](../../releases/latest), open it on your phone and allow "Install unknown apps" when asked. What changed in each version is in [CHANGELOG.md](CHANGELOG.md).
Then long-press your home screen → Widgets → Minutia.

On OnePlus/OxygenOS, if the app's **Battery** row says *Restricted*, tap **Fix** so the clock never lags.

## Build
Every push to `main` builds a signed APK with GitHub Actions (`.github/workflows/build.yml`).
To release, bump `VERSION_NAME` in `version.properties` and add a matching section to `CHANGELOG.md`; CI then publishes `vX.Y.Z` on Releases.
Locally: `gradle :app:assembleRelease` with the Android SDK installed.

**Working on this with an AI agent?** Point it at [CLAUDE.md](CLAUDE.md) (also linked from `AGENTS.md` / `GEMINI.md`): build loop, architecture, conventions and gotchas. How-tos in [docs/RECIPES.md](docs/RECIPES.md), design history in [docs/DECISIONS.md](docs/DECISIONS.md).
