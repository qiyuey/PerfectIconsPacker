package io.github.perfecticons.ui.component.liquid

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Shared visual/touch geometry for all three apps; dimensions are project design tokens. */
internal object NavigationLayout {
    val panelPadding = 4.dp
    val tabHeight = 56.dp
    val minimumTabWidth = 76.dp
    val minimumTouchWidth = 48.dp
    val horizontalMargin = 24.dp
    val iconSize = 24.dp
    val labelSpacing = 1.dp
    val contentGap = 12.dp
}

@Composable
internal fun navigationTabWidth(labels: List<String>, icons: List<ImageVector>): Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val windowWidth = LocalWindowInfo.current.containerSize.width
    val style = MiuixTheme.textStyles.footnote2
    val textWidth = labels.maxOfOrNull { measurer.measure(it, style = style, maxLines = 1).size.width } ?: 0
    val preferred = maxOf(NavigationLayout.minimumTabWidth, with(density) { textWidth.toDp() } + 16.dp,
        icons.maxOfOrNull { it.defaultWidth } ?: NavigationLayout.iconSize)
    val available = (with(density) { windowWidth.toDp() } - NavigationLayout.horizontalMargin * 2 - NavigationLayout.panelPadding * 2) / labels.size.coerceAtLeast(1)
    return preferred.coerceAtMost(available.coerceAtLeast(NavigationLayout.minimumTouchWidth))
}

@Composable
internal fun navigationTabHeight(labels: List<String>): Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val style = MiuixTheme.textStyles.footnote2
    val height = labels.maxOfOrNull { measurer.measure(it, style = style, maxLines = 1).size.height } ?: 0
    return maxOf(NavigationLayout.tabHeight,
        NavigationLayout.iconSize + NavigationLayout.labelSpacing + with(density) { height.toDp() } + 12.dp)
}

@Composable
internal fun navigationBottomPadding(): Dp {
    val inset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return if (inset != 0.dp) inset + 8.dp else 36.dp
}

@Composable
internal fun navigationBarOccupiedHeight(labels: List<String>): Dp =
    navigationTabHeight(labels) + NavigationLayout.panelPadding * 2 + navigationBottomPadding()
