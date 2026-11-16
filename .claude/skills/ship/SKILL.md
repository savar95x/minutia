---
name: ship
description: Release a new Minutia version - bump version.properties, add the CHANGELOG section, push, wait for CI, fetch and inspect the APK, send it to the owner. Use for any change the owner should get on his phone.
---

Follow `docs/RECIPES.md` → "Ship a release". In short:

1. Pick the bump: patch = fix, minor = feature. Edit `VERSION_NAME` in `version.properties`.
2. Add `## [X.Y.Z] - <today>` at the top of `CHANGELOG.md` with terse `### Added/Changed/Fixed` lines.
3. Commit as `Savar Verma <102670483+savar95x@users.noreply.github.com>` (never another email), with the
   co-author/session trailer lines your harness asks for. `git pull --rebase && git push`.
4. `scripts/ci-wait.sh`. On failure fix the printed errors and push again (a failed build publishes nothing).
5. `scripts/fetch-apk.sh` then `python3 scripts/inspect-apk.py out/Minutia-X.Y.Z.apk "<a new UI string>"`.
   Require the right version and `signer … OK`.
6. Send `out/Minutia-X.Y.Z.apk` to the owner with one or two lines on what changed. Don't claim it was tested
   on a device; ask for a screenshot/recording if visuals or motion changed.
