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
- Static quality workflow: `.github/workflows/quality.yml`.
- FreeJ2ME emulator integration: not yet present in this repository.
- MIDP GCF networking: not implemented.
- TCP/UDP: not enabled; CheerpJ/Tailscale integration remains under evaluation.

The browser diagnostic measures Fetch behavior only. It is not a J2ME networking test and does not demonstrate raw TCP/UDP support.

## Development

Run the static checks with Node.js installed:

```sh
bash scripts/check-project.sh
```

Serve the static project locally from the `site/` directory using an HTTP server. The emulator itself is not included yet.

See:

- [Network compatibility design](docs/network-compatibility.md)
- [Runtime evaluation](docs/runtime-evaluation.md)
- [Security model](docs/security-model.md)
- [MIDlet network probe specification](web/netprobe/README.md)
- [Project landing page](site/index.html)
- [Browser HTTP(S) diagnostic](site/netprobe/index.html)

## Upstream

The emulator baseline is [zb3/freej2me-web](https://github.com/zb3/freej2me-web). Review its license and preserve required notices when importing or modifying upstream code.

## License

Project-specific licensing and upstream license notices must be finalized before distribution.
