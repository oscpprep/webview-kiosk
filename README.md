# Webview Kiosk — SakinaForever Edition

[![Latest Debug APK](https://img.shields.io/badge/download-latest%20debug%20APK-blue)](https://github.com/oscpprep/webview-kiosk/releases/download/debug-latest/app-debug.apk)
[![Debug APK Release](https://github.com/oscpprep/webview-kiosk/actions/workflows/debug-release.yml/badge.svg?branch=main)](https://github.com/oscpprep/webview-kiosk/actions/workflows/debug-release.yml)
[![Tests](https://github.com/oscpprep/webview-kiosk/actions/workflows/tests.yaml/badge.svg?branch=main)](https://github.com/oscpprep/webview-kiosk/actions/workflows/tests.yaml)

**SakinaForever's managed Android kiosk client**, built on the open-source Webview Kiosk project.

## Download

**[Download the latest Debug APK](https://github.com/oscpprep/webview-kiosk/releases/download/debug-latest/app-debug.apk)**

The permanent download URL above is updated automatically after tests pass and a new debug APK is successfully built from `main`. No production signing keystore is required.

<div align="center">

[<img src="./docs/src/assets/badges/github.svg" alt="Get it on GitHub" width="260px" />](https://github.com/nktnet1/webview-kiosk/releases)
[<img src="./docs/src/assets/badges/obtainium.svg" alt="Get it on Obtainium" width="260px" />](https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/nktnet1/webview-kiosk)
[<img src="./docs/src/assets/badges/google-play.svg" alt="Get it on Google Play" width="260px" />](https://play.google.com/store/apps/details?id=com.nktnet.webview_kiosk)
[<img src="./docs/src/assets/badges/f-droid.svg" alt="Get it on F-Droid" width="260px" />](https://f-droid.org/packages/uk.nktnet.webviewkiosk)
[<img src="./docs/src/assets/badges/izzy-on-droid.svg" alt="Get it on IzzyOnDroid" width="260px" />](https://apt.izzysoft.de/fdroid/index/apk/uk.nktnet.webviewkiosk)

</div>

Webview Kiosk is a free and open-source Android application for secure,
kiosk-style web browsing.

It can function as digital signage, a kid-friendly restricted browser, an
immersive web reader, an interactive sign-up form, a home assistant dashboard,
or simply a wall clock.

Designed for small businesses and device owners who are looking for a simple
and standalone Kiosk solution.

## Key Features

### Core / Standalone

- <b>Lock Task Mode (Pin):</b> prevent access to your device's home screen, apps and status bar
- <b>Secure Settings:</b> configurations are protected by biometrics, device credentials or custom password
- <b>URL Filtering:</b> use regular expressions to control web access through a URL blacklist and whitelist
- <b>Export/Import:</b> backup and restore user settings in Base64 or JSON format
- <b>Local files:</b> display images, audio, video, HTML or PDF (using Mozilla's PDF.js) in kiosk mode
- <b>Default Launcher:</b> use as the home app and launch other apps in lock task mode (kiosk)
- <b>JS Scripts:</b> supports NFC (Near Field Communication), Eruda console, Dark Reader and custom scripts
- <b>Adblock:</b> blocks ads and trackers using rules from uBlock Origin, AdGuard, EasyList and HaGeZi

### Remote Management / Enterprise

1. MQTT (Message Queuing Telemetry Transport)
   - Monitor events, update settings, execute commands and build custom automations
   - Requires an MQTT broker (e.g. Mosquitto, EMQX, HiveMQ, or their HomeAssistant integrations)
   - Supports installing verified local web files remotely via `install_local_file` (see [docs/install-local-file.md](docs/install-local-file.md))

2. UnifiedPush
   - Send commands and update settings via a decentralised push notification system
   - Requires a distributor application (e.g. sunup, ntfy, gCompat-UP)

3. Managed Configurations (App Restrictions)
   - For fully-managed (company-owned) devices, settings can be configured via an MDM/EMM provider
   - Advanced users with a device policy controller (e.g. Test DPC or OwnDroid) can also enforce configurations locally

## Optional Server Setup Example

The Android kiosk client can be used independently. A separate server is optional and is only needed if you want to use the SakinaForever remote-management setup (including the MQTT broker, device-management API, and web dashboard).

A Docker Compose server example is provided separately as an optional setup reference. It is not required to build, install, or use the Android app. If you choose to deploy the server, follow the server example's README and environment setup instructions:

1. Copy `.env.example` to `.env` before starting the stack:
   ```bash
   cp .env.example .env
   ```
2. Open `.env.example` and copy/save the generated passwords securely before proceeding. Keep the values consistent with the corresponding entries in `.env`.
3. Keep `.env` private. Do not commit it, publish it, or share its passwords.
4. Start the services from the directory containing `docker-compose.yml`:
   ```bash
   docker compose up -d --build
   ```

The server is an independent project; its containers, configuration, and persistent data are separate from the Android app.

## Permissions

- <b>INTERNET</b>: for general web browsing
- <b>ACCESS_NETWORK_STATE</b>: to refresh web pages when network connectivity is restored
- <b>ACCESS_LOCAL_NETWORK</b>: (optional, Android 17+) for web apps that access the local network
- <b>CAMERA</b>: (optional) for use with web apps that requires photo/video capture
- <b>RECORD_AUDIO</b>: (optional) for use with web apps that requires audio capture
- <b>MODIFY_AUDIO_SETTINGS</b>: for routing audio (microphone will not work without this)
- <b>ACCESS_FINE_LOCATION</b>: (optional) for web apps that needs precise geolocation
- <b>ACCESS_COARSE_LOCATION</b>: (optional) for web apps that needs approximate geolocation
- <b>NFC</b>: for reading and writing NFC tags using the device's NFC hardware
- <b>VIBRATE</b>: (optional) for navigator.vibrate(pattern) when enabled
- <b>INSTALL_SHORTCUT</b>: for adding home screen launcher shortcuts on Android 7.1 and below
- <b>QUERY_ALL_PACKAGES</b>: to find launchable apps, device owners and lock task packages
- <b>POST_NOTIFICATIONS</b>: (optional) for lock task mode (kiosk) launches and MQTT
- <b>FOREGROUND_SERVICE</b>: for persistent notification and management with lock task and MQTT
- <b>FOREGROUND_SERVICE_SPECIAL_USE</b>: provides a return mechanism for opening apps in kiosk
- <b>USE_BIOMETRIC</b>: replaces USE_FINGERPRINT from Android 9.0 onwards
- <b>USE_FINGERPRINT</b>: use fingerprint hardware when accessing settings or unlocking kiosk
- <b>WAKE_LOCK</b>: to optionally wake the screen when receiving MQTT commands
- <b>WRITE_EXTERNAL_STORAGE</b>: for downloading files in Android 9.0 and below
- <b>API (Dhizuku)</b>: to request shared device owner privileges

## Installation Notes

From [v0.17.0](https://github.com/nktnet1/webview-kiosk/releases/tag/v0.17.0),
Google Play's [automatic protection](https://support.google.com/googleplay/android-developer/answer/10183279)
has been intentionally <b>disabled</b> to allow installations from the Aurora Store.

From [v0.15.7](https://github.com/nktnet1/webview-kiosk/releases/tag/v0.15.7),
the package name for all installation sources except the Google Play Store has
changed from `com.nktnet.webview_kiosk` to `uk.nktnet.webviewkiosk`.

## Contact

- support@webviewkiosk.nktnet.uk

## License

This project is licensed under the GNU Affero General Public License v3.0 or later.

See the [LICENSE](./LICENSE) file for details.

## Screenshots

<div align="center">
  <img src="./metadata/en-US/images/phoneScreenshots/001.phone-default.png" width="275px" alt="Phone Screenshot 1" />&nbsp;
  <img src="./metadata/en-US/images/phoneScreenshots/002.phone-locked.png" width="275px" alt="Phone Screenshot 2"/>&nbsp;
  <img src="./metadata/en-US/images/phoneScreenshots/003.phone-kiosk-control-panel.png" width="275px" alt="Phone Screenshot 3" />&nbsp;
  <img src="./metadata/en-US/images/phoneScreenshots/004.phone-page-blocked.png" width="275px" alt="Phone Screenshot 4" />&nbsp;
  <img src="./metadata/en-US/images/phoneScreenshots/005.phone-settings.png" width="275px" alt="Phone Screenshot 5" />&nbsp;
  <img src="./metadata/en-US/images/phoneScreenshots/006.phone-settings-mqtt.png" width="275px" alt="Phone Screenshot 6" />
</div>

## Alternatives

- [Fully Kiosk Browser Lockdown](https://www.fully-kiosk.com) (feature-rich, but not a free kiosk solution)
- [Web Kiosk](https://github.com/screenlite/web-kiosk) (by Screenlite)
- [FreeKiosk](https://github.com/RushB-fr/freekiosk) (by Rushb-fr)
- [Kite AOS](https://github.com/andrew-malitchuk/kite-aos) (by andrew-malitchuk)
