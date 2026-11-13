package io.github.perfecticons.domain.assemble

import io.github.perfecticons.data.repo.Paths
import io.github.perfecticons.domain.model.AssembledTheme
import io.github.perfecticons.domain.model.DynamicIcon
import io.github.perfecticons.domain.model.IconId
import io.github.perfecticons.domain.model.StaticIcon
import java.io.File

/** Assembles HyperOS 4 resources with the bundled mask and Telegram override. */
class ThemeAssembler(
    private val snapshotDir: File,
    private val defaultTransformConfig: File,
    private val telegramDynamicIcon: File? = null,
) {
    fun assemble(styles: Set<String>): AssembledTheme {
        val staticIcons = linkedMapOf<String, StaticIcon>()
        val flatIcons = linkedMapOf<String, String>()
        val dynamicIcons = linkedMapOf<String, DynamicIcon>()
        collectUpstream(styles, staticIcons, flatIcons, dynamicIcons)
        telegramDynamicIcon?.let { directory ->
            for (id in telegramIds) {
                dynamicIcons[id] = DynamicIcon(id, directory.path)
            }
        }

        return AssembledTheme(
            staticIcons = staticIcons.values.toList(),
            flatIcons = flatIcons,
            dynamicIcons = dynamicIcons.values.toList(),
            transformConfig = defaultTransformConfig.absolutePath,
        )
    }

    private fun collectUpstream(
        styles: Set<String>,
        staticIcons: MutableMap<String, StaticIcon>,
        flatIcons: MutableMap<String, String>,
        dynamicIcons: MutableMap<String, DynamicIcon>,
    ) {
        val base = File(snapshotDir, Paths.BASE_STATIC_DIR)
        if (base.isDirectory) {
            base.listFiles().orEmpty().filter { it.isDirectory }.forEach { dir ->
                readStatic(dir)?.let { staticIcons[dir.name] = it }
            }
        }

        styles.forEach { style ->
            val themeRoot = File(snapshotDir, "${Paths.THEMES_DIR}/$style")
            val themeStatic = File(themeRoot, Paths.STATIC_DIR)
            if (themeStatic.isDirectory) {
                themeStatic.listFiles().orEmpty().forEach { entry ->
                    when {
                        entry.isDirectory -> readStatic(entry)?.let { staticIcons[entry.name] = it }
                        entry.isFile && entry.extension.equals("png", ignoreCase = true) -> {
                            val id = entry.nameWithoutExtension
                            if (shouldAssemble(id)) flatIcons[id] = entry.path
                        }
                    }
                }
            }

            val themeDynamic = File(themeRoot, Paths.DYNAMIC_DIR)
            if (themeDynamic.isDirectory) {
                themeDynamic.listFiles().orEmpty().filter { it.isDirectory }.forEach { dir ->
                    if (shouldAssemble(dir.name) && File(dir, "config.xml").isFile) {
                        dynamicIcons[dir.name] = DynamicIcon(dir.name, dir.path)
                    }
                }
            }
        }

        val commonDynamic = File(
            snapshotDir,
            "${Paths.THEMES_DIR}/${Paths.COMMON_STYLE}/${Paths.DYNAMIC_DIR}",
        )
        if (commonDynamic.isDirectory) {
            commonDynamic.listFiles().orEmpty().filter { it.isDirectory }.forEach { dir ->
                if (shouldAssemble(dir.name) && File(dir, "config.xml").isFile) {
                    dynamicIcons.putIfAbsent(dir.name, DynamicIcon(dir.name, dir.path))
                }
            }
        }
    }

    private fun readStatic(dir: File): StaticIcon? {
        if (!shouldAssemble(dir.name)) return null
        val background = File(dir, "0.png")
        val foreground = File(dir, "1.png")
        if (!background.isFile || !foreground.isFile) return null
        return StaticIcon(dir.name, background.path, foreground.path)
    }

    private fun shouldAssemble(id: String): Boolean =
        id !in telegramIds && IconId.isValid(id)

    private companion object {
        val telegramIds = setOf("org.telegram.messenger", "org.telegram.messenger.web")
    }
}
