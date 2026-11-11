package io.github.perfecticons.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.perfecticons.R
import io.github.perfecticons.core.util.formatBytes
import io.github.perfecticons.data.repo.SyncProgress
import io.github.perfecticons.ui.component.ActionButton
import io.github.perfecticons.ui.component.ActionRow
import io.github.perfecticons.ui.component.SectionCard
import io.github.perfecticons.ui.viewmodel.ResourcesUiState
import java.text.DateFormat
import java.util.Date
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal fun LazyListScope.iconResourceItems(
    state: ResourcesUiState,
    enabled: Boolean,
    onSync: () -> Unit,
    onClear: () -> Unit,
) {
    item {
        SectionCard(title = stringResource(R.string.resources_section_snapshot)) {
            Text("图标主题：HyperOS 4", style = MiuixTheme.textStyles.body2)
            SyncStateBody(state)
            ActionRow {
                ActionButton(stringResource(R.string.resources_sync), onSync, primary = true,
                    enabled = enabled && !state.isSyncing, modifier = Modifier.weight(1f))
                ActionButton(stringResource(R.string.resources_clear), onClear,
                    enabled = enabled && !state.isSyncing && state.isSynced, modifier = Modifier.weight(1f))
            }
        }
    }
    state.error?.let { message ->
        item { SectionCard { Text(stringResource(R.string.resources_error, message), color = MiuixTheme.colorScheme.error) } }
    }
}

@Composable
private fun ColumnScope.SyncStateBody(state: ResourcesUiState) {
    val snapshot = state.snapshot
    when {
        state.isSyncing -> {
            Column {
                Text(text = progressText(state.progress), style = MiuixTheme.textStyles.body2)
                LinearProgressIndicator(progress = progressFraction(state.progress), modifier = Modifier.fillMaxWidth())
            }
        }


        snapshot == null -> {
            Text(
                text = stringResource(R.string.resources_empty),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }

        else -> {
            Text(
                text = stringResource(R.string.resources_commit, snapshot.commit.take(8)),
                style = MiuixTheme.textStyles.body2,
            )
            Text(
                text = stringResource(
                    R.string.resources_stats,
                    snapshot.files.size,
                    formatBytes(snapshot.totalBytes),
                ),
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Text(
                text = stringResource(
                    R.string.resources_styles,
                    snapshot.styles.joinToString(", "),
                ),
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Text(
                text = stringResource(
                    R.string.resources_synced_at,
                    DateFormat.getDateTimeInstance().format(Date(snapshot.syncedAt)),
                ),
                style = MiuixTheme.textStyles.footnote2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun progressText(progress: SyncProgress?): String = when (progress) {
    SyncProgress.FetchingTrees -> stringResource(R.string.resources_progress_trees)
    SyncProgress.Resolving -> stringResource(R.string.resources_progress_resolving)
    is SyncProgress.FetchingBlobs -> stringResource(
        R.string.resources_progress_blobs,
        progress.fetched,
        progress.total,
        progress.batch,
        progress.batches,
    )
    is SyncProgress.Materializing -> stringResource(
        R.string.resources_progress_materializing,
        progress.written,
        progress.total,
    )
    SyncProgress.Done -> stringResource(R.string.resources_progress_done)
    is SyncProgress.Failed -> progress.message
    null -> stringResource(R.string.resources_progress_starting)
}

private fun progressFraction(progress: SyncProgress?): Float = when (progress) {
    is SyncProgress.FetchingBlobs -> progress.fetched.toFloat() / progress.total.coerceAtLeast(1)
    is SyncProgress.Materializing -> progress.written.toFloat() / progress.total.coerceAtLeast(1)
    SyncProgress.Done -> 1f
    else -> 0f
}
