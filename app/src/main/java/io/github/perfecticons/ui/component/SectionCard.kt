package io.github.perfecticons.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle

/**
 * Section header label. The list's horizontal margin is cancelled so the label
 * keeps only SmallTitle's own indent instead of stacking both margins.
 */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    PageSectionTitle(text, modifier, insidePage = true)
}

private val SectionContentMargin = PaddingValues(PageDesign.cardPadding)

/**
 * One section of a page: an optional [SmallTitle] label above a [Card] grouping
 * related content, following the miuix Example's `SmallTitle + Card` pattern.
 *
 * Miuix [Card] has no default inner margin. Free-form content (texts, buttons)
 * should use the default [padded]; preference rows carry their own padding and
 * need `padded = false`.
 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    padded: Boolean = true,
    contentSpacing: Dp = PageDesign.relatedGap,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            SectionLabel(text = title)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = if (padded) SectionContentMargin else PaddingValues(0.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(contentSpacing),
                content = content,
            )
        }
    }
}
