#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

command -v node >/dev/null 2>&1 || {
  echo "Node.js is required for JavaScript syntax checks." >&2
  exit 1
}

test -s site/index.html
test -s site/styles.css
test -s site/netprobe/index.html
test -s site/netprobe/netprobe.js
test -s docs/network-compatibility.md
test -s docs/runtime-evaluation.md

node --check site/netprobe/netprobe.js
bash -n scripts/build-emulator.sh
bash -n tools/run-java-probe-tests.sh

if grep -R -nE '(tailscaleAuthKey|AuthKey[a-zA-Z0-9_-]{10,})' site; then
  echo "Possible Tailscale auth key found in public site files." >&2
  exit 1
fi

echo "Project sanity checks passed."
echo "Note: these checks do not build or execute the FreeJ2ME emulator."
