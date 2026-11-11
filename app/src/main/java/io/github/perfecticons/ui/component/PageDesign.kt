package io.github.perfecticons.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.SmallTitle

/** Project page tokens, implemented with Miuix; not an official Xiaomi numeric specification. */
internal object PageDesign {
    val horizontalMargin = 12.dp
    val maximumContentWidth = 800.dp
    val sectionGap = 12.dp
    val cardPadding = 16.dp
    val relatedGap = 8.dp
    val minimumTouchHeight = 48.dp
}

@Composable
internal fun pageHorizontalMargin(): Dp {
    val width = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val direction = LocalLayoutDirection.current
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    val safe = maxOf(insets.calculateLeftPadding(direction), insets.calculateRightPadding(direction))
    return maxOf(PageDesign.horizontalMargin, (width - PageDesign.maximumContentWidth) / 2, safe)
}

@Composable
internal fun PageSectionTitle(text: String, modifier: Modifier = Modifier, insidePage: Boolean = false) {
    SmallTitle(text = text, modifier = modifier,
        insideMargin = PaddingValues(horizontal = PageDesign.cardPadding + if (insidePage) 0.dp else pageHorizontalMargin(),
            vertical = PageDesign.relatedGap))
}
