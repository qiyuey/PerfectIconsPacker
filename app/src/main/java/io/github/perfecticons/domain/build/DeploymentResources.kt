package io.github.perfecticons.domain.build

import io.github.perfecticons.domain.model.AssembledTheme
import io.github.perfecticons.domain.model.IconId
import java.io.File

/** Creates deployable HyperOS 4 resources, without an outer export archive. */
class DeploymentResources(private val theme: AssembledTheme) {
    fun write(directory: File, onProgress: (Int, Int) -> Unit = { _, _ -> }): File {
        require(!directory.exists()) { "部署目录已经存在" }
        val defaults = File(directory, "default")
        IconArchives(theme, defaults, onProgress).build()
        val flat = File(directory, "miui_mod_icons").apply { mkdirs() }
        theme.flatIcons.forEach { (id, source) ->
            require(IconId.isValid(id)) { "单张图标名称无效：$id" }
            File(source).copyTo(File(flat, "$id.png"))
        }
        return directory
    }
}
