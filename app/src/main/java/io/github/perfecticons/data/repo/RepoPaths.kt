package io.github.perfecticons.data.repo

import android.content.Context
import java.io.File

object RepoPaths {
    fun root(context: Context): File = File(context.filesDir, "repo")

    fun copyBundledDynamicIcons(context: Context, directory: File): Map<String, File> {
        val telegram = File(directory, "telegram")
        val wecom = File(directory, "wecom")
        val futu = File(directory, "futu")
        copyAsset(context, "telegram-dynamic-icon", telegram)
        copyAsset(context, "wecom-dynamic-icon", wecom)
        copyAsset(context, "futu-dynamic-icon", futu)
        return mapOf(
            "org.telegram.messenger" to telegram,
            "org.telegram.messenger.web" to telegram,
            "com.tencent.wework" to wecom,
            "com.tencent.wework.launch.LaunchSplashActivity" to wecom,
            "cn.futu.trader.universal" to futu,
            "cn.futu.trader.launch.activity.LaunchActivity" to futu,
        )
    }

    private fun copyAsset(context: Context, path: String, target: File) {
        val children = context.assets.list(path).orEmpty()
        if (children.isEmpty()) {
            target.parentFile?.mkdirs()
            context.assets.open(path).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        } else {
            for (child in children) {
                copyAsset(context, "$path/$child", File(target, child))
            }
        }
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
