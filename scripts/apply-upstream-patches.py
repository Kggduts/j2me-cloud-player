#!/usr/bin/env python3
"""Apply the small, pinned FreeJ2ME Web source overlay before its Ant build."""
from pathlib import Path
import shutil
import sys

root = Path(__file__).resolve().parents[1]
upstream = Path(sys.argv[1]).resolve()
expected = "c19416e75cbc15f9a27f7e967ee81cb108761e30"

import subprocess
actual = subprocess.check_output(
    ["git", "-C", str(upstream), "rev-parse", "HEAD"], text=True
).strip()
if actual != expected:
    raise SystemExit("Refusing to patch unexpected upstream revision: " + actual)

overlay = root / "patches" / "src"
for source in overlay.rglob("*.java"):
    relative = source.relative_to(overlay)
    target = upstream / "src" / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target)

main_js = upstream / "web" / "src" / "main.js"
js = main_js.read_text(encoding="utf-8")
marker = "// J2ME Cloud Player: optional user-authenticated Tailscale networking."
if marker not in js:
    needle = "    await cheerpjInit({\n        enableDebug: false,"
    replacement = """    await cheerpjInit({
        // J2ME Cloud Player: optional user-authenticated Tailscale networking.
        // No reusable auth key is embedded in the public client.
        tailscaleLoginUrlCb(url) {
            const login = document.getElementById('tailscale-login');
            if (login) {
                login.href = url;
                login.hidden = false;
                login.textContent = 'Sign in to enable TCP/UDP networking';
            }
        },
        tailscaleIpCb(ip) {
            const status = document.getElementById('tailscale-status');
            if (status) status.textContent = 'Private network connected: ' + ip;
        },
        enableDebug: false,"""
    if needle not in js:
        raise SystemExit("Expected cheerpjInit anchor not found in upstream main.js")
    js = js.replace(needle, replacement, 1)
    main_js.write_text(js, encoding="utf-8")

run_html = upstream / "web" / "run.html"
html = run_html.read_text(encoding="utf-8")
html_marker = '<!-- J2ME Cloud Player: optional private-network sign-in -->'
if html_marker not in html:
    needle = '    <div id="screen-area">'
    replacement = """    <!-- J2ME Cloud Player: optional private-network sign-in -->
    <div id="network-tools" style="position:fixed;z-index:20;top:8px;left:8px;max-width:min(90vw,420px);font:13px sans-serif;background:rgba(255,255,255,.94);padding:7px 10px;border-radius:6px;box-shadow:0 1px 5px #0003">
        <a id="tailscale-login" href="#" target="_blank" rel="noopener noreferrer" hidden>Sign in to enable TCP/UDP networking</a>
        <span id="tailscale-status" aria-live="polite"></span>
    </div>
    <div id="screen-area">"""
    if needle not in html:
        raise SystemExit("Expected screen-area anchor not found in upstream run.html")
    html = html.replace(needle, replacement, 1)
    run_html.write_text(html, encoding="utf-8")

print("Applied GCF transport and optional Tailscale login overlays.")
