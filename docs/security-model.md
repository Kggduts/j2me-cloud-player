# Security model

## Untrusted MIDlets

Uploaded JAR files must be treated as untrusted input. Browser sandboxing reduces access to the user's local system, but it does not make a MIDlet harmless: a game may still consume CPU, memory, storage, network bandwidth, or abuse any network permissions exposed by the runtime.

The product must not claim that every JAR is safe.

## Network access

Network access is a security boundary, not merely an emulator feature.

- HTTP(S) is subject to browser origin, CORS, TLS, cookie, and mixed-content policies.
- TCP/UDP access through CheerpJ's Tailscale integration must be authenticated and scoped.
- Never embed a reusable Tailscale auth key, API token, or shared secret in public HTML, JavaScript, source maps, or a public repository.
- Prefer interactive user authentication or a short-lived, single-use enrollment flow issued by a trusted backend.
- Do not grant all uploaded games access to a privileged private network by default.
- Define per-user identity, device expiry, network ACLs, bandwidth limits, connection limits, and revocation before enabling generalized networking.

## Data handling

- Game files should remain in the user's browser storage unless the user explicitly exports or uploads them.
- Do not upload JARs to a server by default.
- If server-side storage is introduced, document retention, deletion, access control, and malware-handling behavior.
- Never log game contents, credentials, auth keys, or full sensitive URLs.

## Runtime isolation

- Apply resource limits where the runtime allows them.
- Provide a way to stop a running MIDlet and close its connections.
- Handle page refresh, tab closure, and runtime failure as connection teardown events.
- Do not claim CPU/memory isolation between games unless the implementation actually provides it.

## Release checklist

- [ ] No secrets in public assets or repository history.
- [ ] Network authentication is user-scoped and revocable.
- [ ] Tailscale ACLs prevent unintended access to private services.
- [ ] Connection and bandwidth limits are defined.
- [ ] MIDlet stop/close behavior is tested.
- [ ] Browser security and privacy behavior is documented.
- [ ] JAR handling and retention policy is documented.
