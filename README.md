# OnnTube4K

An Android TV / Google TV YouTube browser prototype for the Onn 4K Plus.

## Configure the YouTube API key

1. In GitHub, open **Settings → Secrets and variables → Actions**.
2. Create a repository secret named `YOUTUBE_API_KEY`.
3. Paste the YouTube Data API v3 key as the secret value. Do not commit the key to source control.
4. Open **Actions → Build OnnTube4K APK → Run workflow** (or push a code change).

The key is packaged into the APK at build time. APKs can be reverse-engineered, so restrict the key in Google Cloud Console and monitor its quota. A repository secret protects it from being committed to Git, but does not make a client-side key confidential.

## Build

The GitHub Actions workflow builds `app/build/outputs/apk/debug/app-debug.apk` and uploads it as the `OnnTube4K-debug` artifact.

## Current prototype scope

- YouTube Data API v3 video search and thumbnails.
- Android TV launcher entry and basic D-pad focusable results.
- Embedded YouTube IFrame player for playback.

This is an early prototype, not a finished YouTube TV replacement. Playback compatibility, remote-control behavior, account features, and YouTube player requirements need testing on the target TV device. The official embedded player controls playback; the Data API itself does not supply video streams.
