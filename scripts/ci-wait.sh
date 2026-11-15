#!/usr/bin/env bash
# Wait for the GitHub Actions run of the current HEAD and report the result.
# On failure, prints the Kotlin / Gradle errors so you can fix them without opening the web UI.
# Usage: scripts/ci-wait.sh            (run after `git push`)
# Needs: gh (authenticated for this repo). Docs-only pushes (*.md, docs/**, scripts/**) don't run CI.
set -uo pipefail
sha=$(git rev-parse HEAD)
echo "Waiting for CI on ${sha:0:7}…"
id=""
for i in $(seq 1 90); do
  row=$(gh run list --commit "$sha" --limit 1 --json databaseId,status,conclusion \
        --jq '.[0] | "\(.databaseId) \(.status) \(.conclusion)"' 2>/dev/null)
  if [ -z "$row" ] || [ "$row" = "null null null" ]; then
    if [ "$i" -ge 6 ] && [ -z "$id" ]; then echo "No run found for this commit (docs-only push?)"; exit 0; fi
    sleep 10; continue
  fi
  read -r id status conclusion <<<"$row"
  if [ "$status" = "completed" ]; then
    echo "Run $id: $conclusion"
    if [ "$conclusion" != "success" ]; then
      gh run view "$id" --log-failed 2>/dev/null | sed 's/^.*Z //' \
        | grep -E '^e: |^w: .*error|What went wrong|Could not|requires|error:' | head -40
      exit 1
    fi
    v=$(grep -E '^VERSION_NAME=' version.properties | cut -d= -f2)
    if gh release view "v$v" >/dev/null 2>&1; then echo "Release v$v is published."; fi
    exit 0
  fi
  sleep 15
done
echo "Timed out waiting for run $id"; exit 1
