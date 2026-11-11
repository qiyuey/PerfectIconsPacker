package io.github.perfecticons.domain

import io.github.perfecticons.domain.build.ThemeZipWriter
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ThemeZipWriterTest {

    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun `directory entries with traversal are rejected`() {
        val out = File(temp.root, "out.zip")
        ThemeZipWriter(out.outputStream()).use { writer ->
            var thrown: Throwable? = null
            try {
                writer.addDirectory("layer_animating_icons/com.foo/../../evil")
            } catch (t: IOException) {
                thrown = t
            }
            assertEquals("traversal must not reach the archive", true, thrown is IOException)
        }
    }

    @Test
    fun `png layers are stored without reencoding`() {
        val inner = File(temp.root, "0.png").apply { writeBytes(ByteArray(4096) { 7 }) }
        val out = File(temp.root, "out.zip")
        ThemeZipWriter(out.outputStream()).use { writer ->
            writer.addFile("res/drawable-xxhdpi/com.example/0.png", inner)
        }
        ZipFile(out).use { zip ->
            assertEquals(
                ZipEntry.STORED,
                zip.getEntry("res/drawable-xxhdpi/com.example/0.png").method,
            )
            assertArrayEquals(inner.readBytes(), zip.getInputStream(zip.getEntry("res/drawable-xxhdpi/com.example/0.png")).readBytes())
        }
    }
}
