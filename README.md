# Minutia

*Sweat the small stuff.* A home-screen clock widget for Android where every detail is yours to tweak.

<p align="center">
  <img src="docs/screenshots/bold.jpg" width="24%" alt="Bold style on the home screen" />
  <img src="docs/screenshots/pills.jpg" width="24%" alt="Pills style on the home screen" />
  <img src="docs/screenshots/words.jpg" width="24%" alt="Words style on the home screen" />
  <img src="docs/screenshots/settings.jpg" width="24%" alt="Settings screen with live preview" />
</p>
<p align="center"><sub>Bold · Pills · Words · Settings with live preview (OnePlus 13s, colours from the wallpaper)</sub></p>

- **5 styles**: Bold, Stacked, Pills, Analog, Words
- **Any Google Font**: type a family name and it downloads on the phone; or load your own `.ttf`/`.otf`
- **Material You** colours from your wallpaper, or pick your own
- Size, weight, letter spacing, alignment, shadow, background card, date formats, 12/24h
- Per-widget settings with a live preview
- Tap to open your clock app (or any app you pick)
- Optional name under the widget: add the "named" variant from the widget picker, or flip *Show name under widget* in settings

## Install
Grab the latest APK from [Releases](../../releases), open it on your phone and allow "Install unknown apps" when asked.
Then long-press your home screen → Widgets → Minutia.

On OnePlus/OxygenOS, open the app and tap **Allow background** so the clock never lags.

## Build
Every push to `main` builds a signed APK with GitHub Actions (`.github/workflows/build.yml`).
Locally: `gradle :app:assembleRelease` with the Android SDK installed.
