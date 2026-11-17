# MIDlet GCF network probe

This MIDlet tests the actual `javax.microedition.io` API exposed by the
emulator. Enter the host/IP of the machine running `tools/netprobe-server.py`
and its HTTP, TCP and UDP ports, then run the three checks.

Build output: `dist/netprobe-midlet.jar` (assembled by
`scripts/build-emulator.sh` after the pinned FreeJ2ME Web jar is built).

The current upstream baseline does not implement HTTP, TCP or UDP GCF
transports. The probe is expected to report failures until the corresponding
adapters are implemented. It is intentionally a diagnostic, not a claim that
networking already works. Use a controlled private test network; never expose
the unauthenticated echo server to the public internet.
