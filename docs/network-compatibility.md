# Network compatibility contract

## Why this exists

The Java ME Generic Connection Framework (GCF) exposes synchronous-looking Java interfaces. Browser networking APIs are asynchronous and constrained by the browser security model. A working browser fetch call alone does not establish GCF compatibility.

This document defines the implementation boundary and the evidence required before a transport is marked supported.

## Upstream baseline findings

The inspected upstream source currently declares several GCF interfaces, but declarations are not equivalent to functioning transports:

- `Connector.open()` handles the SMS scheme; other schemes currently fail with `ConnectionNotFoundException`.
- HTTP, HTTPS, socket, datagram, and stream connection types exist as API surfaces, but the inspected tree does not provide a complete working transport implementation for them.
- `PushRegistry` methods are stubs.
- The inspected SMS implementation is a placeholder rather than a real SMS transport.
- The web bootstrap initializes CheerpJ and registers JavaScript native bridges in `web/src/main.js`.

These are source-inspection findings, not runtime test results. Re-check them against the exact upstream revision imported into this repository.

## Transport boundaries

### HTTP and HTTPS

Potential implementation: a Java-to-JavaScript bridge backed by browser `fetch()`.

Constraints:

- Cross-origin requests require the remote server to permit the browser origin through CORS.
- Browser-managed redirects, credentials, cookies, forbidden headers, and response visibility differ from a native Java ME implementation.
- HTTPS certificate validation is controlled by the browser; MIDlets cannot be given arbitrary certificate-store or trust-manager behavior without a separately designed mechanism.
- Streaming request/response behavior and blocking Java stream semantics require explicit design and testing.

### TCP

Browser JavaScript does not expose arbitrary TCP sockets. A WebSocket endpoint is not a transparent TCP socket: it has a different handshake, framing, endpoint requirements, and protocol semantics.

A TCP-compatible GCF adapter therefore needs a separately deployed gateway or a runtime/backend with a documented raw-socket capability. The gateway protocol must define connection identity, binary framing, backpressure, close/error propagation, timeout behavior, and resource limits.

### UDP

Browser JavaScript does not expose arbitrary UDP datagrams. A UDP adapter requires a separately deployed gateway or a runtime/backend with a documented datagram capability. Datagram boundaries, packet size, address semantics, and timeout behavior must be preserved.

### Other GCF schemes

Server sockets, connection notifiers, push registry, SMS, Bluetooth, multicast/broadcast, and vendor-specific APIs are separate compatibility items. Do not imply support because a related interface compiles.

## Java/JavaScript bridge requirements

Before implementing a transport, prove the selected runtime's bridge behavior for:

1. Calling JavaScript from Java and returning asynchronous results.
2. Waiting without blocking the browser's main thread.
3. Interrupting or timing out pending operations.
4. Delivering binary data without text corruption.
5. Preserving stream ordering and partial reads/writes.
6. Closing resources after exceptions, page lifecycle changes, or game exit.
7. Handling concurrent MIDlet connections safely.

Do not implement a blocking Java API by synchronously waiting on the browser main thread.

## Compatibility test matrix

| Area | Required checks |
| --- | --- |
| HTTP | GET, POST, headers, status codes, redirects, binary body, CORS denial |
| HTTPS | valid TLS, invalid certificate behavior, redirects, CORS |
| TCP gateway | connect, bidirectional binary stream, ordering, timeout, remote close, reconnect, concurrent clients |
| UDP gateway | datagram boundaries, binary payload, packet limits, timeout, unreachable destination |
| Lifecycle | explicit close, MIDlet exit, page refresh, network loss, server restart |
| Load | 1 B, 64 B, 1 KiB, 8 KiB and larger payloads; sustained connection; concurrent connections |

## Release gates

A transport may be marked **implemented** only when code exists and automated tests pass.

A transport may be marked **browser-verified** only when tests have been run in supported real browsers.

A transport may be marked **game-verified** only when at least one real MIDlet using that transport succeeds against a controlled endpoint.

A transport may be marked **production-ready** only after security, abuse limits, deployment, observability, and lifecycle behavior are reviewed.

Until then, label it experimental or unsupported.
