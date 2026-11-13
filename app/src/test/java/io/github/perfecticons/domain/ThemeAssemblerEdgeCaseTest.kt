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

    @Test fun `bundled dynamic icons replace cached upstream resources`() {
        val snapshot = temp.newFolder("snapshot")
        val telegram = temp.newFolder("telegram")
        val wecom = temp.newFolder("wecom")
        val bundled = mapOf(
            "org.telegram.messenger" to telegram,
            "org.telegram.messenger.web" to telegram,
            "com.tencent.wework" to wecom,
        )
        for (directory in bundled.values.toSet()) {
            File(directory, "config.xml").writeText(directory.name)
        }
        val ids = bundled.keys + "com.other"
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
        val assembler = ThemeAssembler(snapshot, temp.newFile("mask.xml"), bundled)
        for (styles in listOf(setOf("HyperOS4"), emptySet())) {
            val theme = assembler.assemble(styles)
            assertEquals(listOf("com.other"), theme.staticIcons.map { it.id })
            assertEquals(ids, theme.dynamicIcons.map { it.id }.toSet())
            assertEquals(if (styles.isEmpty()) emptySet() else setOf("com.other"), theme.flatIcons.keys)
        }

        val theme = assembler.assemble(setOf("HyperOS4"))
        val output = File(temp.root, "theme")
        DeploymentResources(theme).write(output)
        ZipFile(File(output, "default/dynamicicons")).use { archive ->
            for ((id, directory) in bundled) {
                val config = archive.getEntry("layer_animating_icons/$id/config.xml")
                assertEquals(directory.name, archive.getInputStream(config).reader().use { it.readText() })
            }
        }
    }
}
