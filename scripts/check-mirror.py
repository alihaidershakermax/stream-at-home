#!/usr/bin/env python3
"""Fails when a download URL in the update feed cannot serve its release file.

The release job runs it on the latest.json it just wrote, before the feed goes live: every URL
under "assets" is asked for its first byte (a Range request, so nothing big moves). This covers
both the original project's download mirror and direct GitHub Release URLs used by forks.

    scripts/check-mirror.py updates/latest.json
"""

import json
import sys
import time
import urllib.error
import urllib.request

TRIES = 4
WAIT = 15


def probe(url: str) -> str | None:
    """None when the file is served, else a short reason."""
    req = urllib.request.Request(url, headers={"Range": "bytes=0-0", "User-Agent": "tmplayer-release-check"})
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            return None if r.status in (200, 206) else f"HTTP {r.status}"
    except urllib.error.HTTPError as e:
        return f"HTTP {e.code} (upstream {e.headers.get('X-Upstream-Status', '?')})"
    except Exception as e:  # network trouble is a reason too, not a crash
        return repr(e)


def main() -> int:
    feed = json.load(open(sys.argv[1]))
    bad = []
    for key, asset in feed.get("assets", {}).items():
        url = asset["url"]
        reason = None
        for attempt in range(TRIES):
            reason = probe(url)
            if reason is None:
                break
            if attempt < TRIES - 1:
                time.sleep(WAIT)
        print(f"{'ok  ' if reason is None else 'FAIL'} {key}: {url}" + (f" -> {reason}" if reason else ""))
        if reason:
            bad.append(key)
    if bad:
        print(f"::error::The update feed has unreachable downloads: {', '.join(bad)}")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
