#!/usr/bin/env bash
# Turn a screen recording from the owner into a contact sheet you can look at.
# Usage: scripts/video-frames.sh rec.mp4 [fps=1] [crop=W:H:X:Y] [out=frames.png]
#   1) Run with fps=1 and no crop to find the area of interest (frames are tiled 7 across).
#   2) Re-run with a higher fps and a crop (full-resolution pixels) to see motion/jitter frame by frame.
set -euo pipefail
in=$1; fps=${2:-1}; crop=${3:-}; out=${4:-frames.png}
vf="fps=$fps"
[ -n "$crop" ] && vf="$vf,crop=$crop"
vf="$vf,scale=300:-1,tile=7x6"
ffmpeg -v error -y -i "$in" -vf "$vf" -frames:v 1 "$out"
echo "wrote $out"
