# SakinaForever Kiosk

Android kiosk client for SakinaForever passenger tablets. The app opens the SakinaForever experience in a locked-down WebView and supports remote device management through MQTT.

[![Latest Debug APK](https://img.shields.io/badge/APK-download%20latest-blue)](https://github.com/oscpprep/webview-kiosk/releases/download/debug-latest/app-debug.apk)
[![Debug APK Release](https://github.com/oscpprep/webview-kiosk/actions/workflows/debug-release.yml/badge.svg?branch=main)](https://github.com/oscpprep/webview-kiosk/actions/workflows/debug-release.yml)
[![Tests](https://github.com/oscpprep/webview-kiosk/actions/workflows/tests.yaml/badge.svg?branch=main)](https://github.com/oscpprep/webview-kiosk/actions/workflows/tests.yaml)

## Download

**[Download the latest SakinaForever APK](https://github.com/oscpprep/webview-kiosk/releases/download/debug-latest/app-debug.apk)**

This permanent link points to the APK attached to the `debug-latest` GitHub Release. The release is refreshed automatically from `main` after the required tests and APK build succeed.

This is a debug build. It does not require a production signing keystore.

## SakinaForever defaults

| Setting | Default |
|---|---|
| Start page | `https://sakinaforever.com` |
| MQTT host | `mqtt.sakinaforever.com` |
| MQTT port | `443` |
| MQTT transport | Secure WebSockets (TLS) |
| MQTT path | `/mqtt` |
| Initial device username | `kios-client-01` |

Each tablet must use its own MQTT password. Do not reuse credentials between devices or commit passwords, tokens, or private configuration files to this repository.

## Features

- Full-screen Android kiosk browsing.
- Lock-task / kiosk operation for managed tablets.
- MQTT connectivity for remote status, settings, and commands.
- Device-scoped MQTT topics and retained commands for reconnecting devices.
- QoS 1 delivery for commands and responses.
- Offline local content installation through the `install_local_file` command.
- SHA-256 verification before an installed file is accepted.
- Local file information reporting through `local_file_info`.

See [Offline file delivery](docs/install-local-file.md) for the command format, request/response behavior, and checksum requirements.

## Server

The Android app can be installed and used without deploying a server. A server is optional and is only needed for remote management, MQTT messaging, and the related administration experience.

The server is maintained separately from this Android repository. Follow the server project's own setup guide if you choose to deploy it. Keep server environment files and generated credentials private.

## Build from source

Requirements:
- Android Studio or a compatible Android SDK installation.
- JDK 21.

Build the debug APK:

```bash
./gradlew :app:assembleDebug
```

The APK is generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

## GitHub Actions

Only two workflows are used:

- **Debug APK Release** — runs on updates to `main`, executes the required tests, builds the debug APK, and refreshes the `debug-latest` release.
- **Tests** — validates pull requests and can also be run manually.

## License

See [LICENSE](LICENSE) for the license governing this repository.
