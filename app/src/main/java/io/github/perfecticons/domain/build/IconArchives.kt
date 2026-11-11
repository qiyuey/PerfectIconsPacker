package io.github.perfecticons.domain.build

import io.github.perfecticons.domain.model.AssembledTheme
import java.io.File
import java.nio.file.Files

/** Writes separate static and dynamic archives for direct deployment. */
class IconArchives(
    private val theme: AssembledTheme,
    private val workDir: File,
    private val onProgress: (written: Int, total: Int) -> Unit = { _, _ -> },
) {
    fun build() {
        workDir.mkdirs()
        val iconsZip = File(workDir, "icons")
        val dynamicZip = File(workDir, "dynamicicons")

        val totalStatic = theme.staticWriteCount
        var written = 0
        val report = {
            written++
            onProgress(written, totalStatic)
        }

        ThemeZipWriter(iconsZip.outputStream()).use { writer ->
            writeStatic(writer, report)
        }
        ThemeZipWriter(dynamicZip.outputStream()).use { writer -> writeDynamic(writer) }
    }

    private fun writeStatic(writer: ThemeZipWriter, report: () -> Unit) {
        theme.staticIcons.forEach { icon ->
            writer.addFile("res/drawable-xxhdpi/${icon.id}/0.png", File(icon.background))
            report()
            writer.addFile("res/drawable-xxhdpi/${icon.id}/1.png", File(icon.foreground))
            report()
        }

        theme.transformConfig?.let {
            writer.addFile("transform_config.xml", File(it))
            report()
        }
    }

    private fun writeDynamic(writer: ThemeZipWriter) {
        theme.dynamicIcons.forEach { icon ->
            val root = File(icon.directory)
            if (!root.isDirectory) return@forEach
            root.walkTopDown()
                .onEnter { !Files.isSymbolicLink(it.toPath()) }
                .forEach { file ->
                if (Files.isSymbolicLink(file.toPath())) return@forEach
                val relative = file.relativeTo(root).invariantSeparatorsPath
                val entry = "layer_animating_icons/${icon.id}/$relative"
                if (file.isDirectory) {
                    writer.addDirectory(entry)
                } else {
                    writer.addFile(entry, file)
                }
            }
        }
    }

}
