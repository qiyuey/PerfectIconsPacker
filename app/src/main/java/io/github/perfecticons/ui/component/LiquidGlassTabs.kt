package io.github.perfecticons.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.perfecticons.ui.component.liquid.IosLiquidGlassNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.blur.LayerBackdrop

data class GlassTab(val label: String, val icon: ImageVector)

/** Floating Liquid Glass navigation with a single rendering path. */
@Composable
fun LiquidGlassTabs(tabs: List<GlassTab>, selected: Int, backdrop: LayerBackdrop?, onSelect: (Int) -> Unit) {
    if (tabs.isEmpty()) return
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        IosLiquidGlassNavigationBar(
            items = tabs.map { NavigationItem(it.label, it.icon) }, selectedIndex = selected.coerceIn(tabs.indices),
            onItemClick = onSelect, backdrop = backdrop, isBlurActive = backdrop != null,
        )
    }
}
