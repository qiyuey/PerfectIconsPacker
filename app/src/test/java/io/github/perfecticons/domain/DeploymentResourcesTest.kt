package io.github.perfecticons.domain

import io.github.perfecticons.domain.build.DeploymentResources
import io.github.perfecticons.domain.model.AssembledTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipFile

class DeploymentResourcesTest {
    @get:Rule val temp = TemporaryFolder()
    @Test fun `writes separate archives and preserves flat image bytes`() {
        val image = temp.newFile().apply { writeBytes(byteArrayOf(0, 1, 2, 127)) }
        val theme = AssembledTheme(emptyList(), mapOf("com.example.app" to image.path), emptyList(), null)
        val output = File(temp.root, "theme")
        DeploymentResources(theme).write(output)
        ZipFile(File(output, "default/icons")).use { assertEquals(0, it.size()) }
        ZipFile(File(output, "default/dynamicicons")).use { assertEquals(0, it.size()) }
        assertArrayEquals(image.readBytes(), File(output, "miui_mod_icons/com.example.app.png").readBytes())
        assertFalse(File(output, "module.prop").exists())
    }
    @Test fun `rejects escaping flat image id`() {
        val image = temp.newFile().apply { writeText("bytes") }
        assertThrows(IllegalArgumentException::class.java) {
            DeploymentResources(AssembledTheme(emptyList(), mapOf("../escape" to image.path), emptyList(), null)).write(File(temp.root, "bad"))
        }
        assertFalse(File(temp.root, "escape.png").exists())
    }
}
