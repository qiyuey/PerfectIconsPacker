package io.github.perfecticons.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.perfecticons.R
import io.github.perfecticons.ui.component.*
import io.github.perfecticons.ui.viewmodel.HomeViewModel
import io.github.perfecticons.ui.viewmodel.ResourcesViewModel
import io.github.perfecticons.domain.model.IconTheme
import io.github.perfecticons.ui.viewmodel.UpstreamStatus
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HomeScreen(
    padding: PaddingValues,
    viewModel: HomeViewModel = viewModel(),
    rootGranted: Boolean,
    resourcesViewModel: ResourcesViewModel = viewModel(),
    isActive: Boolean = true,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resourcesState by resourcesViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(resourcesState.snapshot, resourcesState.isSyncing) {
        if (!resourcesState.isSyncing) viewModel.refresh()
    }
    LaunchedEffect(isActive) {
        if (isActive) { viewModel.refresh() }
    }
    PageScaffold(title = stringResource(R.string.home_title), padding = padding) {
        item {
            val snapshot = state.snapshot
            val upstream = state.upstream
            val theme = (snapshot?.styles ?: IconTheme.styles.toList())
                .joinToString(", ")
            val themeFooter = stringResource(R.string.home_status_detail_theme, theme)
            when {
                snapshot == null -> StatusCard(
                    tone = StatusTone.Error,
                    title = stringResource(R.string.home_status_unsynced),
                    detail = stringResource(R.string.home_status_hint),
                    footer = themeFooter,
                )

                snapshot.styles.toSet() != IconTheme.styles -> StatusCard(
                    tone = StatusTone.Warning,
                    title = stringResource(R.string.home_theme_changed),
                    detail = stringResource(R.string.home_theme_changed_hint),
                    footer = themeFooter,
                )

                upstream is UpstreamStatus.UpdateAvailable -> StatusCard(
                    tone = StatusTone.Warning,
                    title = stringResource(R.string.home_status_update),
                    detail = stringResource(
                        R.string.home_status_update_detail,
                        snapshot.commit.take(8),
                        upstream.commit.take(8),
                    ),
                    footer = themeFooter,
                )

                upstream is UpstreamStatus.UpToDate -> StatusCard(
                    tone = StatusTone.Ready,
                    title = stringResource(R.string.home_status_ready),
                    detail = stringResource(
                        R.string.home_status_latest_detail,
                        snapshot.commit.take(8),
                    ),
                    footer = themeFooter,
                )

                else -> StatusCard(
                    tone = StatusTone.Ready,
                    title = stringResource(R.string.home_status_ready),
                    detail = stringResource(
                        R.string.home_status_commit_detail,
                        snapshot.commit.take(8),
                    ),
                    footer = themeFooter,
                )
            }
        }
        item {
            SectionCard(title = "应用图标", contentSpacing = 12.dp) {
                Text("将已同步的图标资源直接应用到本机，无需导出或安装模块。", style = MiuixTheme.textStyles.footnote2)
                ActionButton(
                    text = if (state.isBuilding) "正在处理…" else "应用图标",
                    onClick = viewModel::applyIcons,
                    primary = true,
                    enabled = !state.isBuilding && !resourcesState.isSyncing && state.snapshot?.styles?.toSet() == IconTheme.styles && rootGranted,
                    modifier = Modifier.fillMaxWidth(),
                )
                ActionRow {
                    ActionButton("重新应用", viewModel::reapplyIcons, enabled = !state.isBuilding && !resourcesState.isSyncing && rootGranted, modifier = Modifier.weight(1f))
                    ActionButton("恢复系统图标", viewModel::restoreIcons, enabled = !state.isBuilding && !resourcesState.isSyncing && rootGranted, modifier = Modifier.weight(1f))
                }
                if (state.isBuilding) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                if (state.applied) Text("操作完成，桌面布局已保留。", color = MiuixTheme.colorScheme.primary)
                Text("重启后，重新取得临时 root，再点击“重新应用”即可恢复。", style = MiuixTheme.textStyles.footnote2)
            }
        }
        iconResourceItems(resourcesState, enabled = !state.isBuilding,
            onSync = resourcesViewModel::sync, onClear = resourcesViewModel::clear)
        state.error?.let { message -> item { SectionCard { Text(message, color = MiuixTheme.colorScheme.error) } } }
    }
}
