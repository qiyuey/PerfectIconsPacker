package io.github.perfecticons.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.perfecticons.core.util.runCatchingCancellable
import io.github.perfecticons.data.repo.IconRepository
import io.github.perfecticons.data.repo.RepoLock
import io.github.perfecticons.data.repo.RepoPaths
import io.github.perfecticons.data.repo.RepoSnapshot
import io.github.perfecticons.data.repo.SyncProgress
import io.github.perfecticons.domain.model.IconTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class ResourcesUiState(
    val snapshot: RepoSnapshot? = null,
    val isSyncing: Boolean = false,
    val progress: SyncProgress? = null,
    val error: String? = null,
) {
    val isSynced get() = snapshot != null
}

/** Resource actions are shown inside the icon page; there is no resource tab. */
class ResourcesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = IconRepository(RepoPaths.root(application))
    private val mutableState = MutableStateFlow(ResourcesUiState())
    val state = mutableState.asStateFlow()
    private var revision = 0

    init {
        viewModelScope.launch {
            val snapshot = withContext(Dispatchers.IO) { repository.snapshot() }
            // A user action may have started while this read was in flight.
            if (revision == 0) mutableState.value = mutableState.value.copy(snapshot = snapshot)
        }
    }

    fun sync() = operate(clear = false)
    fun clear() = operate(clear = true)
    private fun operate(clear: Boolean) {
        if (mutableState.value.isSyncing) return
        revision++
        mutableState.value = mutableState.value.copy(isSyncing = true, progress = null, error = null)
        viewModelScope.launch {
            runCatchingCancellable {
                if (clear) {
                    withContext(Dispatchers.IO) { RepoLock.snapshot.withLock { repository.clear() } }
                    null
                } else repository.sync(IconTheme.styles) { progress ->
                    mutableState.value = mutableState.value.copy(progress = progress)
                }
            }.onSuccess { snapshot ->
                mutableState.value = ResourcesUiState(snapshot = snapshot)
            }.onFailure { failure ->
                mutableState.value = mutableState.value.copy(isSyncing = false, error = failure.message ?: "资源操作失败")
            }
        }
    }
}
