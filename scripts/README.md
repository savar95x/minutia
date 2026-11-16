# Scripts

| Script | Use | Needs |
|---|---|---|
| `ci-wait.sh` | After `git push`: waits for the Actions run of HEAD; prints Kotlin/Gradle errors on failure | `gh` authenticated for the repo |
| `fetch-apk.sh [vX.Y.Z]` | Downloads the latest (or given) release APK to `out/` | `gh` |
| `inspect-apk.py APK ["text" …]` | Prints version, versionCode, signer (checked against `app/release.jks`), components; checks given strings are in the APK | `pip install -r scripts/requirements.txt` |
| `video-frames.sh rec.mp4 [fps] [crop] [out]` | Contact sheet of a screen recording, to inspect motion | `ffmpeg` |

The icon generator lives in `docs/icon/make.py` (needs `fonttools`, `cairosvg`, also in requirements).

First-time setup in a fresh sandbox:
```bash
pip install -r scripts/requirements.txt   # add --break-system-packages if pip refuses
gh auth status                            # must reach savar95x/minutia
```
