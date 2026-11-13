package io.github.perfecticons.data.repo

import android.content.Context
import java.io.File

object RepoPaths {
    fun root(context: Context): File = File(context.filesDir, "repo")

    fun copyTelegramDynamicIcon(context: Context, directory: File): File {
        val files = listOf(
            "config.xml",
            "0/quiet/quietImage.png",
            "1/quiet/quietImage.png",
            "1/fancy/icon.png",
            "1/fancy/manifest.xml",
        )
        for (path in files) {
            val target = File(directory, path)
            target.parentFile?.mkdirs()
            context.assets.open("telegram-dynamic-icon/$path").use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return directory
    }

    /** Returns the bundled HyperOS 4 mask in app-private storage. */
    fun defaultTransformConfig(context: Context): File {
        val target = File(context.filesDir, "defaults/transform_config.xml")
        if (target.isFile) return target
        target.parentFile?.mkdirs()
        context.assets.open("mask/hyperos4_transform_config.xml").use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return target
    }
}
