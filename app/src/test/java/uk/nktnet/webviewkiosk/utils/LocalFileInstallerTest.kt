package uk.nktnet.webviewkiosk.utils

import java.io.File
import java.net.URI
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalFileInstallerTest {
    private val sha = "a".repeat(64)

    @Test
    fun validatesUrlAndSha() {
        LocalFileInstaller.validateUrl("https://example.com/a.html")
        LocalFileInstaller.validateUrl("http://127.0.0.1/a.html")
        assertThrows(LocalFileInstaller.InstallException::class.java) {
            LocalFileInstaller.validateUrl("http://example.com/a.html")
        }
        assertEquals(sha, LocalFileInstaller.validateSha256(sha.uppercase()))
        assertThrows(LocalFileInstaller.InstallException::class.java) {
            LocalFileInstaller.validateSha256("xyz")
        }
    }

    @Test
    fun sanitizesFilename() {
        val uri = URI("https://example.com/dir/page.html")
        assertEquals("page.html", LocalFileInstaller.sanitizeFilename(null, uri))
        // A blank filename falls back to the last URL path segment.
        assertEquals("page.html", LocalFileInstaller.sanitizeFilename(" ", uri))
        for (bad in listOf("../x", "a/b", "a\\b", "..", "a|b")) {
            assertThrows(LocalFileInstaller.InstallException::class.java) {
                LocalFileInstaller.sanitizeFilename(bad, uri)
            }
        }
    }

    @Test
    fun shaMismatchKeepsPreviousFile() {
        val dir = Files.createTempDirectory("lfi").toFile()
        val existing = File(dir, LocalFileInstaller.FILE_PREFIX + "p.html")
        existing.writeText("old")
        val temp = File(dir, "t.part")
        val (actual, bytes) =
            LocalFileInstaller.streamToFile("new".byteInputStream(), temp)
        val mismatch =
            assertThrows(LocalFileInstaller.InstallException::class.java) {
                LocalFileInstaller.commit(temp, actual, sha, bytes, dir, "p.html")
            }
        assertTrue(mismatch.message.orEmpty().contains("expected"))
        assertTrue(mismatch.message.orEmpty().contains("received"))
        assertTrue(mismatch.message.orEmpty().contains("$bytes bytes"))
        assertEquals("old", existing.readText())
        assertFalse(temp.exists())

        val temp2 = File(dir, "t2.part")
        val (actual2, bytes2) =
            LocalFileInstaller.streamToFile("new".byteInputStream(), temp2)
        LocalFileInstaller.commit(temp2, actual2, actual2, bytes2, dir, "p.html")
        assertEquals("new", existing.readText())
        assertTrue(File(dir, existing.name + ".bak").exists())
    }
}
