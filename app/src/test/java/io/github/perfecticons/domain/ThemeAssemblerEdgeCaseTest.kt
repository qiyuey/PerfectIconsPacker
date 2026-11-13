package io.github.perfecticons.domain

import io.github.perfecticons.domain.assemble.ThemeAssembler
import io.github.perfecticons.domain.build.DeploymentResources
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipFile

class ThemeAssemblerEdgeCaseTest {
    @get:Rule val temp = TemporaryFolder()
    @Test fun `upstream layers mask and dynamic entries reach separate archives`() {
        val snapshot = temp.newFolder("snapshot")
        fun resource(path: String) = File(snapshot, path).apply { parentFile?.mkdirs(); writeText(path) }
        resource("icons/com.base/0.png")
        resource("icons/com.base/1.png")
        resource("icons/com.invalid/0.png")
        resource("themes/HyperOS4/icons/com.flat.png")
        resource("themes/common/layer_animating_icons/com.clock/config.xml")
        resource("themes/common/layer_animating_icons/com.clock/0.png")
        val mask = temp.newFile("mask.xml").apply { writeText("bundled mask") }
        val assembler = ThemeAssembler(snapshot, mask)
        val theme = assembler.assemble(setOf("HyperOS4"))
        assertEquals(listOf("com.base"), theme.staticIcons.map { it.id })
        val output = File(temp.root, "theme")
        DeploymentResources(theme).write(output)
        ZipFile(File(output, "default/icons")).use {
            assertEquals("bundled mask", it.getInputStream(it.getEntry("transform_config.xml")).reader().readText())
            assertNotNull(it.getEntry("res/drawable-xxhdpi/com.base/1.png"))
            assertNull(it.getEntry("layer_animating_icons/com.clock/config.xml"))
        }
        ZipFile(File(output, "default/dynamicicons")).use { assertNotNull(it.getEntry("layer_animating_icons/com.clock/config.xml")) }
        assertTrue(File(output, "miui_mod_icons/com.flat.png").isFile)
    }

    @Test fun `Telegram overrides are excluded from cached upstream resources`() {
        val snapshot = temp.newFolder("snapshot")
        val ids = listOf("org.telegram.messenger", "org.telegram.messenger.web", "com.other")
        for (id in ids) {
            val resources = listOf(
                "icons/$id/0.png",
                "icons/$id/1.png",
                "themes/HyperOS4/icons/$id/0.png",
                "themes/HyperOS4/icons/$id/1.png",
                "themes/HyperOS4/icons/$id.png",
                "themes/HyperOS4/layer_animating_icons/$id/config.xml",
                "themes/common/layer_animating_icons/$id/config.xml",
            )
            for (path in resources) {
                File(snapshot, path).apply {
                    parentFile?.mkdirs()
                    writeText(path)
                }
            }
        }
        val assembler = ThemeAssembler(snapshot, temp.newFile("mask.xml"))
        for (styles in listOf(setOf("HyperOS4"), emptySet())) {
            val theme = assembler.assemble(styles)
            assertEquals(listOf("com.other"), theme.staticIcons.map { it.id })
            assertEquals(listOf("com.other"), theme.dynamicIcons.map { it.id })
            assertEquals(if (styles.isEmpty()) emptySet() else setOf("com.other"), theme.flatIcons.keys)
        }

        val bundled = temp.newFolder("telegram")
        File(bundled, "config.xml").writeText("bundled Telegram")
        val theme = ThemeAssembler(snapshot, temp.newFile("bundled-mask.xml"), bundled)
            .assemble(setOf("HyperOS4"))
        val output = File(temp.root, "theme")
        DeploymentResources(theme).write(output)
        ZipFile(File(output, "default/dynamicicons")).use { archive ->
            for (id in ids.take(2)) {
                val config = archive.getEntry("layer_animating_icons/$id/config.xml")
                assertEquals("bundled Telegram", archive.getInputStream(config).reader().use { it.readText() })
            }
        }
    }
}
