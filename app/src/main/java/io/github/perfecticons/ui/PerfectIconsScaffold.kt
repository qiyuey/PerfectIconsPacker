package io.github.perfecticons.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.perfecticons.R
import io.github.perfecticons.ui.component.*
import io.github.perfecticons.ui.screen.*
import io.github.perfecticons.ui.viewmodel.HomeViewModel
import io.github.perfecticons.ui.viewmodel.RootViewModel
import io.github.perfecticons.ui.viewmodel.ResourcesViewModel
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Settings

@Composable
fun PerfectIconsScaffold() {
    val tabs = listOf(GlassTab(stringResource(R.string.nav_home), MiuixIcons.Regular.GridView),
        GlassTab(stringResource(R.string.nav_settings), MiuixIcons.Regular.Settings))
    val pager = rememberPagerState(pageCount = { tabs.size })
    val navigation = rememberLiquidPagerNavigation(pager)
    val home: HomeViewModel = viewModel()
    val root: RootViewModel = viewModel()
    val resources: ResourcesViewModel = viewModel()
    val homeState by home.state.collectAsStateWithLifecycle()
    val rootState by root.state.collectAsStateWithLifecycle()
    LaunchedEffect(pager.currentPage) {
        if (!homeState.isBuilding) root.detect()
    }
    val context = LocalContext.current
    val backdrop = rememberBlurBackdrop()
    val onSelect: (Int) -> Unit = navigation::animateToPage
    Scaffold(bottomBar = {
        // Requested selection stays stable during animated page navigation.
        LiquidGlassTabs(tabs, navigation.selectedPage, backdrop, onSelect)
    }) { padding ->
        Box(if (backdrop != null) Modifier.fillMaxSize().layerBackdrop(backdrop) else Modifier.fillMaxSize()) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 1, overscrollEffect = null) { page ->
                when (page) {
                    0 -> HomeScreen(padding = padding, viewModel = home, rootGranted = rootState.rootStatus?.granted == true,
                        resourcesViewModel = resources, isActive = pager.currentPage == 0)
                    1 -> SettingsScreen(padding = padding, rootViewModel = root, busy = homeState.isBuilding,
                        hasIconLogs = homeState.buildLog.isNotEmpty(), onIconLogs = home::showLog)
                }
            }
            LogSheet(show = homeState.showLogSheet, title = stringResource(R.string.home_apply_log),
                lines = homeState.buildLog, onDismiss = home::dismissLogSheet,
                onCopy = { copyToClipboard(context, homeState.buildLog.joinToString("\n")) })
        }
    }
}

private fun copyToClipboard(context: android.content.Context, text: String) {
    val manager = context.getSystemService(android.content.ClipboardManager::class.java)
    manager?.setPrimaryClip(android.content.ClipData.newPlainText("log", text))
}
