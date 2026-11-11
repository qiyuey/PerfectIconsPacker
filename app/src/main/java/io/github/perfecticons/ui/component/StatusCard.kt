package io.github.perfecticons.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class StatusTone { Ready, Warning, Error }

/** A semantic status within the same neutral card hierarchy as the other apps. */
@Composable
fun StatusCard(tone: StatusTone, title: String, detail: String, footer: String, modifier: Modifier = Modifier) {
    val colors = MiuixTheme.colorScheme
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(PageDesign.cardPadding), verticalArrangement = Arrangement.spacedBy(PageDesign.relatedGap)) {
            Text(title, style = MiuixTheme.textStyles.title3,
                color = when (tone) { StatusTone.Ready -> colors.primary; StatusTone.Warning -> colors.onSurface; StatusTone.Error -> colors.error })
            Text(detail, style = MiuixTheme.textStyles.body2, color = colors.onSurfaceVariantSummary)
            Text(footer, style = MiuixTheme.textStyles.footnote1, color = colors.onSurfaceVariantSummary)
        }
    }
}
