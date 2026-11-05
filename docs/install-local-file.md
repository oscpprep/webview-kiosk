# `install_local_file` MQTT command

Downloads a file over HTTPS, verifies its SHA-256, installs it into the app's
local web content directory, and (optionally) sets it as the home page and
navigates to it. Requires **Allow Local Files** to be enabled so the file can be served.

## Minimal example

```json
{
  "commandType": "install_local_file",
  "data": {
    "url": "https://example.com/kiosk/page.html",
    "sha256": "<64-hex-char sha256>"
  }
}
```

## Full example

```json
{
  "commandType": "install_local_file",
  "messageId": "install-001",
  "data": {
    "url": "https://example.com/kiosk/page.html",
    "sha256": "<64-hex-char sha256>",
    "filename": "page.html",
    "setAsHome": true,
    "navigate": true,
    "headers": { "Authorization": "Bearer <token>" }
  }
}
```

Defaults: `filename` = last URL path segment, `setAsHome` = `true`, `navigate` = `true`, `headers` = `{}`.
When `navigate` is true the WebView cache is cleared before navigating.

## Response

Published to the configured **Response** topic (`responseType` = `install_local_file`):

```json
{"responseType":"install_local_file","messageId":"...","username":"...","appInstanceId":"...","requestMessageId":"install-001","correlationData":null,
 "data":{"type":"install_local_file","ok":true,"filename":"page.html","url":"https://appassets.androidplatform.net/web-content-files/remote%7Cpage.html","setAsHome":true}}
```

Failure:

```json
{"responseType":"install_local_file", "...": "...", "data":{"type":"install_local_file","ok":false,"error":"sha256 mismatch"}}
```

Possible errors: `busy`, `invalid url`, `https required`, `invalid sha256`, `invalid filename`, `http <code>`,
`file too large`, `sha256 mismatch`, `backup failed`, `rename failed`, `download failed: <Exception>`, `install failed`.

## `local_file_info` request

```json
{"requestType":"local_file_info","messageId":"info-1"}
```

Response (`responseType` = `local_file_info`):

```json
{"responseType":"local_file_info", "...": "...",
 "data":{"type":"local_file_info",
   "files":[{"filename":"page.html","size":12345,"modified":1700000000000,
             "url":"https://appassets.androidplatform.net/web-content-files/remote%7Cpage.html"}],
   "homeUrl":"https://appassets.androidplatform.net/web-content-files/remote%7Cpage.html"}}
```

`.bak` backups are not listed.

## File location

Files are stored flat in `filesDir/web-content-files/` with a `remote|` prefix:
`filesDir/web-content-files/remote|<filename>`, served at
`https://appassets.androidplatform.net/web-content-files/remote%7C<filename>`.
(A prefix rather than a `remote/` subdirectory is used because the app's local-file
link handling only accepts files directly in `web-content-files/`.) The previous version
is kept as `remote|<filename>.bak`. Downloads stage in `cacheDir/remote-tmp`.

Only one install runs at a time; a concurrent command is rejected with `"error":"busy"`.

## Security notes

- HTTPS is required (plain HTTP only for `localhost` / `127.0.0.1`).
- `sha256` is mandatory; the file is only installed if it matches.
- Maximum size is 512 MB.
- Redirects are not followed.
- `headers` are never logged.

## Testing with `mosquitto_pub`

```sh
SHA=$(sha256sum page.html | cut -d' ' -f1)
mosquitto_pub -h broker.local -t 'wk/<username>/command' \
  -m "{\"commandType\":\"install_local_file\",\"data\":{\"url\":\"https://example.com/page.html\",\"sha256\":\"$SHA\"}}"
mosquitto_sub -h broker.local -t 'wk/<username>/response/#' -v
```

Adjust topics to match your Command / Response topic settings.
