package uk.nktnet.webviewkiosk.utils

import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Downloads a file over HTTPS into the app's web content directory (served by
 * WebViewAssetLoader), verifying its SHA-256. Pure JVM so it can be unit tested.
 */
object LocalFileInstaller {
    const val MAX_BYTES = 512L * 1024 * 1024
    const val FILE_PREFIX = "remote|"
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 60_000
    private val SHA256_REGEX = Regex("^[A-Fa-f0-9]{64}$")
    private val busy = AtomicBoolean(false)

    class InstallException(message: String) : Exception(message)

    data class Result(val file: File, val sha256: String, val bytes: Long)

    fun validateUrl(url: String): URI {
        val uri =
            try {
                URI(url)
            } catch (_: Exception) {
                throw InstallException("invalid url")
            }
        val host = uri.host?.lowercase() ?: throw InstallException("invalid url")
        val scheme = uri.scheme?.lowercase()
        val isLoopback = host == "localhost" || host == "127.0.0.1"
        if (scheme != "https" && !(scheme == "http" && isLoopback)) {
            throw InstallException("https required")
        }
        return uri
    }

    fun validateSha256(sha256: String): String {
        if (!SHA256_REGEX.matches(sha256)) {
            throw InstallException("invalid sha256")
        }
        return sha256.lowercase()
    }

    fun sanitizeFilename(filename: String?, url: URI): String {
        val raw =
            filename?.takeIf { it.isNotBlank() }
                ?: url.path.orEmpty().substringAfterLast('/')
        val name = raw.trim()
        if (
            name.isEmpty() ||
                name == "." ||
                name.contains("/") ||
                name.contains("\\") ||
                name.contains("..") ||
                name.contains("|") ||
                name.any { it.isISOControl() }
        ) {
            throw InstallException("invalid filename")
        }
        return name
    }

    fun tryAcquire(): Boolean = busy.compareAndSet(false, true)

    fun release() = busy.set(false)

    /** Streams [input] to [temp] computing SHA-256; returns (hex, bytes). */
    fun streamToFile(
        input: java.io.InputStream,
        temp: File,
        maxBytes: Long = MAX_BYTES,
    ): Pair<String, Long> {
        val digest = MessageDigest.getInstance("SHA-256")
        var total = 0L
        temp.outputStream().use { out ->
            val buf = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buf)
                if (n < 0) {
                    break
                }
                total += n
                if (total > maxBytes) {
                    throw InstallException("file too large")
                }
                digest.update(buf, 0, n)
                out.write(buf, 0, n)
            }
            out.fd.sync()
        }
        return digest.digest().joinToString("") { "%02x".format(it) } to total
    }

    /**
     * Verifies [temp] against [expectedSha], then atomically swaps it into
     * [targetDir]/remote|<filename>, keeping one .bak generation. On any failure the
     * temp file is deleted and the existing target is left unchanged.
     */
    fun commit(
        temp: File,
        actualSha: String,
        expectedSha: String,
        bytes: Long,
        targetDir: File,
        filename: String,
    ): Result {
        try {
            if (!actualSha.equals(expectedSha, ignoreCase = true)) {
                throw InstallException(
                    "sha256 mismatch (expected ${expectedSha.take(12)}, " +
                        "received ${actualSha.take(12)}; $bytes bytes)"
                )
            }
            targetDir.mkdirs()
            val target = File(targetDir, FILE_PREFIX + filename)
            val bak = File(targetDir, FILE_PREFIX + filename + ".bak")
            if (target.exists()) {
                bak.delete()
                if (!target.renameTo(bak)) {
                    throw InstallException("backup failed")
                }
            }
            if (!temp.renameTo(target)) {
                if (bak.exists()) {
                    bak.renameTo(target)
                }
                throw InstallException("rename failed")
            }
            return Result(target, actualSha.lowercase(), bytes)
        } finally {
            temp.delete()
        }
    }

    fun download(
        uri: URI,
        headers: Map<String, String>,
        expectedSha: String,
        tempDir: File,
        targetDir: File,
        filename: String,
    ): Result {
        tempDir.mkdirs()
        val temp = File.createTempFile("install", ".part", tempDir)
        try {
            val conn = URL(uri.toString()).openConnection() as HttpURLConnection
            // Never follow redirects automatically (could downgrade to http).
            conn.instanceFollowRedirects = false
            conn.useCaches = false
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            // Ask Cloudflare and other intermediaries for the original bytes.
            // The expected SHA-256 is calculated over the uploaded file itself.
            conn.setRequestProperty("Accept-Encoding", "identity")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            try {
                val code = conn.responseCode
                if (code !in 200..299) {
                    throw InstallException("http $code")
                }
                val declaredLength = conn.contentLengthLong
                if (declaredLength > MAX_BYTES) {
                    throw InstallException("file too large")
                }
                val (sha, bytes) = conn.inputStream.use { streamToFile(it, temp) }
                if (declaredLength >= 0L && declaredLength != bytes) {
                    throw InstallException(
                        "download size mismatch (server declared $declaredLength bytes, " +
                            "received $bytes bytes)"
                    )
                }
                return commit(temp, sha, expectedSha, bytes, targetDir, filename)
            } finally {
                conn.disconnect()
            }
        } catch (e: InstallException) {
            throw e
        } catch (e: IOException) {
            throw InstallException("download failed: ${e.javaClass.simpleName}")
        } finally {
            temp.delete()
        }
    }
}
