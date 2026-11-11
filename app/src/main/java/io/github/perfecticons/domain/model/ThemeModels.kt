package io.github.perfecticons.domain.model

/** An upstream static icon always contains both 0.png and 1.png. */
data class StaticIcon(
    val id: String,
    val background: String,
    val foreground: String,
)

/**
 * A dynamic layered icon directory. Layer count is unbounded; the directory is
 * copied verbatim because every file may be referenced by manifests.
 */
data class DynamicIcon(
    val id: String,
    val directory: String,
)

data class AssembledTheme(
    val staticIcons: List<StaticIcon>,
    val flatIcons: Map<String, String>,
    val dynamicIcons: List<DynamicIcon>,
    val transformConfig: String?,
) {
    val staticCount: Int get() = staticIcons.size + flatIcons.size
    val dynamicCount: Int get() = dynamicIcons.size

    /** Files the static write emits: one per set layer, plus the mask config. */
    val staticWriteCount: Int
        get() = staticIcons.size * 2 + (if (transformConfig != null) 1 else 0)
}
