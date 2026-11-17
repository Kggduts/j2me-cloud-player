# Runtime evaluation: CheerpJ and network access

## Current findings from official documentation

The current CheerpJ documentation describes:

- A full OpenJDK class library for supported Java versions.
- HTTP and HTTPS through the browser networking stack, subject to same-origin and CORS rules.
- TCP and UDP through a Tailscale-based networking path, or a custom networking backend in enterprise deployments.
- Tailscale traffic carried over WebSockets, with a relay and control server that can be self-hosted; Headscale is mentioned as a self-hostable control-server option.

Sources:

- https://cheerpj.com/docs/explanation/architecture
- https://cheerpj.com/docs/faq
- https://cheerpj.com/docs/tutorials/serverclient
- https://cheerpj.com/compatibility/

## Important distinction

The pinned upstream revision uses the loader URL `https://cjrtnc.leaningtech.com/20260317_2978/loader.js`. The current CheerpJ documentation describes TCP/UDP through Tailscale, but I cannot verify from the loader URL alone that every documented networking option is available and supported in this exact runtime build. The runtime/networking API must be tested in-browser before integration.

The upstream web bootstrap currently uses a versioned CheerpJ loader URL. Its exact version and supported options must be checked before selecting a migration path.

## Evaluation plan

1. Identify the exact CheerpJ version used by the upstream project.
2. Identify the current supported CheerpJ release and its initialization/networking API.
3. Build a minimal Java SE socket probe independent of FreeJ2ME.
4. Verify HTTP(S) behavior and CORS constraints.
5. Verify TCP and UDP through the documented Tailscale path using a controlled test network.
6. Test whether the selected runtime can execute the FreeJ2ME build and required Java ME APIs.
7. Test Java ME GCF behavior separately; Java SE socket support does not automatically implement `javax.microedition.io.Connector`.
8. Review authentication, ephemeral credentials, network isolation, relay costs, and abuse limits before exposing network access to arbitrary uploaded JARs.

## Provisional direction

Prefer the official CheerpJ networking path over inventing a raw-socket bridge, if the required runtime version, deployment model, terms, and compatibility tests permit it.

Do not embed a long-lived Tailscale auth key in public client-side JavaScript. Authentication and device enrollment must be designed as a security boundary, not treated as a configuration detail.

## Decision status

**Not selected yet.** Runtime compatibility with the existing FreeJ2ME Web code and the exact availability/terms of networking features remain unverified by execution.
