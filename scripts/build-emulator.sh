#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
UPSTREAM="$ROOT/vendor/freej2me-web"
PINNED_COMMIT="c19416e75cbc15f9a27f7e967ee81cb108761e30"
IMAGE="j2me-cloud-player/freej2me-builder"

cd "$ROOT"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is required. See the upstream FreeJ2ME Web build instructions." >&2
  exit 1
fi

if [ ! -d "$UPSTREAM/.git" ]; then
  echo "Missing upstream checkout at $UPSTREAM." >&2
  exit 1
fi

ACTUAL_COMMIT="$(git -C "$UPSTREAM" rev-parse HEAD)"
if [ "$ACTUAL_COMMIT" != "$PINNED_COMMIT" ]; then
  echo "Upstream revision mismatch." >&2
  echo "Expected: $PINNED_COMMIT" >&2
  echo "Actual:   $ACTUAL_COMMIT" >&2
  exit 1
fi

docker build --build-arg UID=1000 -t "$IMAGE" "$UPSTREAM/builder_image"
docker run --rm -w /app -v "$UPSTREAM:/app" "$IMAGE" ant

test -s "$UPSTREAM/web/freej2me-web.jar"

rm -rf "$ROOT/dist"
mkdir -p "$ROOT/dist"
cp -a "$ROOT/site/." "$ROOT/dist/"
mkdir -p "$ROOT/dist/emulator"
cp -a "$UPSTREAM/web/." "$ROOT/dist/emulator/"
cp "$UPSTREAM/LICENSE" "$ROOT/dist/emulator/UPSTREAM-LICENSE"
cp "$UPSTREAM/README.md" "$ROOT/dist/emulator/UPSTREAM-README.md"

cat > "$ROOT/dist/BUILD-INFO.txt" <<EOF
J2ME Cloud Player build
FreeJ2ME Web commit: $PINNED_COMMIT
The emulator is provided under its upstream license.
Network support is experimental/not implemented by this project.
EOF

echo "Built deployable site into $ROOT/dist"
echo "This build has not been browser-tested; verify HTTP Range support on the target host."
