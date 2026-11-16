---
name: owner-feedback
description: Turn the owner's phone screenshots or .mp4 screen recordings of Minutia into concrete findings (padding, alignment, jitter, transitions). Use whenever he sends an image or video with feedback.
---

1. Images: open them and compare against the relevant composable in `MainActivity.kt`. Name the exact element
   and the likely cause (padding values, `Modifier` order, fixed sizes) before editing.
2. Videos: `scripts/video-frames.sh rec.mp4 1` → find the moment and region; then
   `scripts/video-frames.sh rec.mp4 15 W:H:X:Y` on that crop to see motion frame by frame.
3. Check `docs/DECISIONS.md` so you don't re-propose a rejected direction (e.g. shrinking preview, Material
   carousel, custom segmented control).
4. If the fix is ambiguous and costly, ask one short question first. Otherwise fix, then use the `ship` skill.
5. Log the outcome (approved/rejected) in `docs/DECISIONS.md`.
