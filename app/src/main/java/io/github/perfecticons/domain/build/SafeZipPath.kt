package io.github.perfecticons.domain.build

import java.io.IOException

/**
 * Writes zip entries while rejecting anything that could escape the extraction
 * directory. Every path is normalized and validated before it reaches the archive.
 */
object SafeZipPath {

    private const val MAX_PATH_LENGTH = 512

    fun requireSafe(path: String): String {
        // Raw checks run before normalization so a converted path can never
        // sneak past them (normalize folds backslashes into separators).
        if (path.contains('\u0000')) throw IOException("非法字符：$path")
        if (path.contains('\\')) throw IOException("反斜杠路径：$path")
        if (path.startsWith("/")) throw IOException("绝对路径：$path")
        val normalized = normalize(path)
        if (normalized.isEmpty()) throw IOException("空路径")
        if (normalized.length > MAX_PATH_LENGTH) throw IOException("路径过长：$path")
        if (normalized.split('/').any { it == ".." }) throw IOException("路径穿越：$path")
        return normalized
    }

    private fun normalize(path: String): String = path
        .replace('\\', '/')
        .split('/')
        .filter { it.isNotEmpty() && it != "." }
        .joinToString("/")


}
