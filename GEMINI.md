# Agent instructions

All project instructions for AI coding agents live in **[CLAUDE.md](CLAUDE.md)**: read it before changing
anything. Design history and rejected ideas are in [docs/DECISIONS.md](docs/DECISIONS.md).

Short version: no local Android SDK is needed. Push to `main`, let GitHub Actions build, then check the APK
with the scripts in `scripts/`. Never change `app/release.jks` or the `applicationId`. Every release needs a
`version.properties` bump plus a `CHANGELOG.md` entry.
