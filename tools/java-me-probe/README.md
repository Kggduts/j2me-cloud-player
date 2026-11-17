# MIDlet GCF network probe

This MIDlet tests the actual `javax.microedition.io` API exposed by the
patched emulator. Enter the host/IP of the machine running
`tools/netprobe-server.py` and its HTTP, TCP and UDP ports, then run the
three checks.

Build output: `dist/netprobe-midlet.jar`, assembled by
`scripts/build-emulator.sh`.

The project overlays the pinned upstream `Connector` implementation with
HTTP(S), TCP socket and UDP datagram adapters. The build also runs
`GcfAdapterSmokeTest` on a normal JVM against the same controlled server.
That JVM test checks adapter mapping, but does not verify CheerpJ's browser
networking path. Run this MIDlet in the browser emulator to perform that
separate check.

For TCP/UDP in CheerpJ, use the emulator's interactive Tailscale sign-in link
and a tailnet ACL that allows only the intended test destination. The pinned
loader's login flow still requires browser verification. Use a controlled
private test network; never expose the unauthenticated echo server to the
public internet.
