# NetProbe

NetProbe is the planned MIDlet/browser harness for measuring Java ME networking compatibility. This directory currently contains the test specification only; no executable MIDlet or endpoint has been implemented yet.

## Test principles

- Run the same probe JAR, endpoint, browser, and device across runtime variants.
- Record browser/runtime version, emulator revision, origin, network type, and timestamp with each run.
- Keep expected results separate from observed results.
- Never mark a transport as supported based only on compilation or a successful JavaScript fetch.

## Planned cases

### HTTP
- GET and POST
- Request and response headers
- Status codes including 2xx, 3xx, 4xx, and 5xx
- Redirect behavior
- Text and binary payloads
- Same-origin success and cross-origin CORS denial

### HTTPS
- Valid certificate
- Invalid/expired certificate behavior
- Redirects and CORS
- Binary request/response

### TCP gateway
- Connect and report connection failure
- Bidirectional stream echo
- Partial reads and writes
- Binary payload integrity
- Long-lived connection
- Timeout and remote close
- Disconnect/reconnect
- Multiple simultaneous connections

### UDP gateway
- Datagram send/receive
- Datagram boundary preservation
- Binary payload integrity
- Packet-size limits
- Timeout and unreachable destination

### Lifecycle and robustness
- Explicit close
- MIDlet termination while I/O is pending
- Browser refresh/navigation
- Network loss and recovery
- Server restart
- Repeated open/close cycles

## Payload sizes

At minimum: 1 byte, 64 bytes, 1 KiB, 8 KiB, and a larger configurable payload. Record exact bytes sent and received; compare hashes for larger binary payloads.

## Runtime comparison

Evaluate independently:

1. Current upstream FreeJ2ME Web runtime without a new network adapter.
2. A newer supported CheerpJ runtime with its native networking behavior, if applicable.
3. The selected runtime with the proposed GCF bridge/gateway.

Do not assume Java SE networking support automatically means Java ME GCF compatibility.

## Result format

Each case should record:

- Case ID
- Pass/fail/blocked
- Runtime and emulator revision
- Browser and OS/device
- Endpoint and origin
- Start/end time
- Expected behavior
- Observed behavior
- Error/exception
- Payload length and integrity result
- Notes and reproducible steps
