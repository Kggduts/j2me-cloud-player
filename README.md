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

## Development

See:

- [Network compatibility design](docs/network-compatibility.md)
- [Browser network probe](web/netprobe/README.md)

## Upstream

The emulator baseline is [zb3/freej2me-web](https://github.com/zb3/freej2me-web). Review its license and preserve required notices when importing or modifying upstream code.

## License

Project-specific licensing and upstream license notices must be finalized before distribution.
