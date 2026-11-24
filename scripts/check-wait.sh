#!/usr/bin/env bash
# Wait for the "Branch check" workflow on HEAD (a non-main branch) to publish branch check-log, then extract it
# into out/check/ (gradle.log, app.apk). Uses only git. On failure prints the Kotlin errors.
set -uo pipefail
sha=$(git rev-parse HEAD)
echo "Waiting for the build of ${sha:0:7}…"
for i in $(seq 1 90); do
  if git fetch -q --depth 1 origin check-log 2>/dev/null && [ "$(git show FETCH_HEAD:SHA 2>/dev/null)" = "$sha" ]; then
    rm -rf out/check && mkdir -p out/check
    git archive FETCH_HEAD | tar -x -C out/check
    if grep -q "BUILD SUCCESSFUL" out/check/gradle.log 2>/dev/null; then echo "BUILD SUCCESSFUL"; ls -la out/check/app.apk; exit 0; fi
    echo "BUILD FAILED"
    grep -E '^e: |What went wrong|Could not|FAILED|Caused by' out/check/gradle.log | head -60
    exit 1
  fi
  sleep 20
done
echo "Timed out"; exit 1
