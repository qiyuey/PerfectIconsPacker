package io.github.perfecticons.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * Standard page shell: a collapsing [TopAppBar] over a scrolling [LazyColumn],
 * composed the way the miuix Example builds its pages.
 *
 * [padding] is the outer scaffold's [PaddingValues] (the navigation bar); each
 * page owns its own top bar, so the outer shell only hosts navigation chrome.
 */
@Composable
fun PageScaffold(
    title: String,
    padding: PaddingValues,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            TopAppBar(title = title, color = MiuixTheme.colorScheme.surface,
                scrollBehavior = scrollBehavior)
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.pageScrollModifiers(scrollBehavior),
            contentPadding = pageContentPadding(innerPadding, padding),
            verticalArrangement = Arrangement.spacedBy(PageDesign.sectionGap),
            overscrollEffect = null,
            content = content,
        )
    }
}

/** Overscroll glow, scroll-end haptics and top-app-bar collapse wired together. */
private fun Modifier.pageScrollModifiers(scrollBehavior: ScrollBehavior): Modifier = this
    .scrollEndHaptic()
    .overScrollVertical()
    .nestedScroll(scrollBehavior.nestedScrollConnection)
    .fillMaxSize()

/**
 * Content margins for a page: below the top bar, above whichever bottom bar is
 * taller (the page's own or the shell's navigation bar), and horizontally
 * centered with a 800.dp cap on wide screens (ghostlock pattern).
 */
@Composable
private fun pageContentPadding(
    innerPadding: PaddingValues,
    outerPadding: PaddingValues,
): PaddingValues {
    val horizontal = pageHorizontalMargin()
    // A page that owns a bottomBar (e.g. the pinned confirm button) draws it over
    // its content, so the list has to clear the bar and not just the shell's
    // navigation bar. Whichever is taller wins; pages without a bar keep the
    // shell padding.
    val bottom = maxOf(
        innerPadding.calculateBottomPadding(),
        outerPadding.calculateBottomPadding(),
    )
    return PaddingValues(
        start = horizontal,
        end = horizontal,
        top = innerPadding.calculateTopPadding() + PageDesign.sectionGap,
        bottom = bottom + PageDesign.sectionGap,
    )
}
