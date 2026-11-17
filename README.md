# J2ME Cloud Player

A browser-based J2ME (MIDP/CLDC) player built around the FreeJ2ME Web emulator.

## Project status

Early development. The repository is being initialized around a compatibility-first approach. Network support is not yet implemented or certified.

## Goals

- Run MIDP games in a modern browser without a native Android/desktop installation.
- Preserve the upstream emulator core where practical instead of rewriting it.
- Build a J2ME Generic Connection Framework (GCF) compatibility layer.
- Validate HTTP, HTTPS, TCP, and UDP independently before presenting them as supported.
- Keep game files local to the browser session unless the user explicitly exports or stores them.

## Compatibility policy

A game is not considered network-compatible merely because the browser can make a request. J2ME games expect Java ME connection semantics, including stream behavior, binary payloads, status codes, timeouts, and connection lifecycle.

Browser security limits matter:

- HTTP(S) requests are subject to CORS, mixed-content, and browser-origin rules.
- Browser JavaScript does not expose arbitrary raw TCP or UDP sockets.
- WebSocket is not a transparent replacement for arbitrary TCP or UDP.
- TCP/UDP support therefore requires a separately designed and deployed gateway/runtime backend. It must be tested against real MIDlets before being advertised.

No universal compatibility claim is made. Vendor APIs, device-specific extensions, SMS, Bluetooth, push registry, multicast, and server-side listening may require separate implementations or remain unsupported.

## Current implementation status

- Project landing page: available under `site/`.
- Browser HTTP(S) diagnostic: available under `site/netprobe/`.
- Local HTTP/TCP/UDP echo server and automated tests: available under `tools/`.
- Java SE HTTP/TCP/UDP smoke-test client: `tools/java-probe/` (not a MIDlet/GCF test).
- Static quality workflow: `.github/workflows/quality.yml`.
- Pinned FreeJ2ME Web source and reproducible build workflow: configured; first successful CI build pending.
- MIDP GCF networking: not implemented.
- TCP/UDP: not enabled; CheerpJ/Tailscale integration remains under evaluation.

The browser diagnostic measures Fetch behavior only. It is not a J2ME networking test and does not demonstrate raw TCP/UDP support.

## Development

Run static checks with Node.js installed:

```sh
bash scripts/check-project.sh
```

Run Java SE transport smoke tests with a JDK and Python 3:

```sh
bash tools/run-java-probe-tests.sh
```

Serve the static project locally from the `site/` directory using an HTTP server. The emulator source is fetched at a pinned upstream revision during CI; the resulting deployable site is published as a build artifact, not committed into this repository.

See:

- [Network compatibility design](docs/network-compatibility.md)
- [Runtime evaluation](docs/runtime-evaluation.md)
- [Security model](docs/security-model.md)
- [MIDlet network probe specification](web/netprobe/README.md)
- [Java SE transport probe](tools/java-probe/README.md)
- [Project landing page](site/index.html)
- [Browser HTTP(S) diagnostic](site/netprobe/index.html)

## Upstream

The emulator baseline is [zb3/freej2me-web](https://github.com/zb3/freej2me-web). Review its license and preserve required notices when importing or modifying upstream code.

## License

Project-specific licensing and upstream license notices must be finalized before distribution.
