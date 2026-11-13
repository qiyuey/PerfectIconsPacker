package io.github.perfecticons.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.perfecticons.domain.assemble.ThemeAssembler
import io.github.perfecticons.data.repo.IconRepository
import io.github.perfecticons.data.repo.RepoPaths
import io.github.perfecticons.data.repo.RepoSnapshot
import io.github.perfecticons.domain.model.IconTheme
import io.github.perfecticons.core.util.runCatchingCancellable
import io.github.perfecticons.data.repo.RepoLock
import io.github.perfecticons.data.install.RootDeployer
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Result of checking the upstream icon repository for a newer snapshot. */
sealed interface UpstreamStatus {
    data object Checking : UpstreamStatus
    data object UpToDate : UpstreamStatus
    data class UpdateAvailable(val commit: String) : UpstreamStatus
    data object Unavailable : UpstreamStatus
}

data class HomeUiState(
    val snapshot: RepoSnapshot? = null,
    val upstream: UpstreamStatus = UpstreamStatus.Checking,
    val isBuilding: Boolean = false,
    val applied: Boolean = false,
    val error: String? = null,

    val buildLog: List<String> = emptyList(),
    val showLogSheet: Boolean = false,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IconRepository(
        root = RepoPaths.root(application),
    )

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    /** Snapshot commit the upstream check was last run for. */
    private var checkedCommit: String? = null


    fun refresh() {
        viewModelScope.launch {
            val snapshot = withContext(Dispatchers.IO) { repository.snapshot() }
            _state.value = _state.value.copy(
                snapshot = snapshot,
                upstream = if (snapshot == null) UpstreamStatus.Unavailable else _state.value.upstream,
            )
            if (snapshot == null) checkedCommit = null
            if (snapshot != null && snapshot.commit != checkedCommit) {
                checkUpstream(snapshot)
            }
        }
    }

    /** Asks upstream for its current head and reports whether the snapshot is stale. */
    private fun checkUpstream(snapshot: RepoSnapshot) {
        checkedCommit = snapshot.commit
        viewModelScope.launch {
            val head = repository.remoteHead()
            if (_state.value.snapshot?.commit != snapshot.commit) return@launch
            _state.value = _state.value.copy(
                upstream = when {
                    head == null -> UpstreamStatus.Unavailable
                    head == snapshot.commit -> UpstreamStatus.UpToDate
                    else -> UpstreamStatus.UpdateAvailable(head)
                },
            )
        }
    }

    fun applyIcons() = operate("apply")
    fun reapplyIcons() = operate("reapply")
    fun restoreIcons() = operate("restore")

    private fun operate(mode: String) {
        if (_state.value.isBuilding) return
        _state.value = _state.value.copy(isBuilding = true, applied = false, error = null, buildLog = emptyList(), showLogSheet = true)
        viewModelScope.launch {
            fun log(message: String) {
                launch(Dispatchers.Main.immediate) {
                    _state.value = _state.value.copy(buildLog = (_state.value.buildLog + message).takeLast(300))
                }
            }
            runCatchingCancellable {
                withContext(Dispatchers.IO) {
                    val app = getApplication<Application>()
                    val workspace = if (mode == "apply") File(app.filesDir, "deployments/${UUID.randomUUID()}") else null
                    try {
                        val source = workspace?.let {
                            val styles = IconTheme.styles
                            val snapshot = repository.snapshot()
                            check(snapshot != null && snapshot.styles.toSet() == styles) { "请先检查更新，同步 HyperOS4 图标资源。" }
                            val release = File(it, "theme")
                            val bundledIcons = RepoPaths.copyBundledDynamicIcons(app, File(it, "bundled-icons"))
                            RepoLock.snapshot.withLock {
                                log("准备官方图标资源")
                                val theme = ThemeAssembler(repository.snapshotDir, RepoPaths.defaultTransformConfig(app), bundledIcons).assemble(styles)
                                io.github.perfecticons.domain.build.DeploymentResources(theme).write(release) { written, total ->
                                    if (written % 500 == 0) log("生成图标 $written/$total")
                                }
                                log("资源就绪：静态 ${theme.staticCount} · 动态 ${theme.dynamicCount}")
                            }
                            release
                        }
                        RootDeployer(app).run(mode, source, ::log)
                    } finally {
                        workspace?.deleteRecursively()
                    }
                }
            }.onSuccess {
                _state.value = _state.value.copy(isBuilding = false, applied = true)
            }.onFailure {
                _state.value = _state.value.copy(isBuilding = false, error = it.message ?: "应用失败")
                log(it.message ?: "应用失败")
            }
        }
    }

    fun dismissLogSheet() { _state.value = _state.value.copy(showLogSheet = false) }
    fun showLog() { _state.value = _state.value.copy(showLogSheet = true) }
}
