package io.github.perfecticons.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.perfecticons.ui.component.ActionButton
import io.github.perfecticons.ui.component.PageScaffold
import io.github.perfecticons.ui.component.SectionCard
import io.github.perfecticons.ui.component.StatusCard
import io.github.perfecticons.ui.component.StatusTone
import io.github.perfecticons.ui.viewmodel.RootViewModel

/** One shared root permission entry for all features. */
@Composable
fun SettingsScreen(
    padding: PaddingValues,
    rootViewModel: RootViewModel,
    busy: Boolean,
    hasIconLogs: Boolean,
    onIconLogs: () -> Unit,
) {
    val rootState by rootViewModel.state.collectAsStateWithLifecycle()
    val ready = rootState.rootStatus?.granted == true && rootState.error == null
    PageScaffold(title = "设置", padding = padding) {
        item {
            StatusCard(
                tone = when {
                    rootState.isDetecting -> StatusTone.Warning
                    ready -> StatusTone.Ready
                    else -> StatusTone.Error
                },
                title = when {
                    rootState.isDetecting -> "正在检测 Root"
                    rootState.error != null -> "Root 检测失败"
                    ready -> "Root 已就绪"
                    else -> "Root 未授权"
                },
                detail = rootState.error ?: when {
                    rootState.isDetecting -> "正在确认本机 Root 权限。"
                    ready -> "已获得 Root 权限，可用于应用与恢复图标。"
                    else -> "请在 Root 管理器中允许完美图标使用 Root。"
                },
                footer = "应用图标需要 Root 权限。",
            )
        }
        item {
            SectionCard {
                ActionButton("重新检测", { rootViewModel.detect(recheck = true) },
                    enabled = !rootState.isDetecting && !busy, modifier = Modifier.fillMaxWidth())
            }
        }
        item {
            SectionCard(title = "日志") {
                ActionButton("查看图标日志", onIconLogs, enabled = hasIconLogs, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
