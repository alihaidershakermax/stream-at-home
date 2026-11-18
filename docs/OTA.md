# Over-the-air updates

The app's stable update feed is a public file in this repository, so a separate server or repository is not needed. The release workflow builds the manifest from the exact release files, records each file's SHA-256 and size, checks the download URLs, and only then publishes the new feed to the repository.

## How installed copies find updates

The app reads:

`https://raw.githubusercontent.com/alihaidershakermax/stream-at-home/main/updates/latest.json`

If that file cannot be reached, the app falls back to this repository's GitHub Releases API. The manifest has distinct `android-tv` and `android-universal` entries, so a TV receives the TV APK and a phone receives the phone APK. Download URLs point to this repository's GitHub Release assets, not the original TMPlayer mirror. Preview versions are offered only when **Get test versions** is enabled; they do not replace the stable feed.

`updates/latest.json` starts as an empty placeholder until this repository has a stable release whose downloads pass the workflow's link check. The existing `site/public/latest.json` remains separate and is not used by this build's updater.

## Building and releasing

CI runs the shared code and UI tests, Android phone and TV unit tests, and builds both debug APK variants. A version-tagged stable release creates the OTA manifest; after its release assets are reachable, the workflow commits the manifest to `updates/latest.json`. Preview tags never replace the stable feed. For public OTA updates, the repository and release assets must be publicly readable.
