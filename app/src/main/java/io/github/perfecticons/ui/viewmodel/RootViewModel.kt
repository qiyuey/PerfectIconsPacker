package io.github.perfecticons.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.perfecticons.data.install.RootDetector
import io.github.perfecticons.data.install.RootStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

data class RootUiState(val rootStatus: RootStatus? = null, val isDetecting: Boolean = false, val error: String? = null)

class RootViewModel(application: Application) : AndroidViewModel(application) {
    private val root = RootDetector()
    private val inFlight = AtomicBoolean(false)
    private val mutableState = MutableStateFlow(RootUiState())
    val state = mutableState.asStateFlow()

    fun detect(recheck: Boolean = false) {
        if (!inFlight.compareAndSet(false, true)) return
        mutableState.value = mutableState.value.copy(isDetecting = true, error = null)
        viewModelScope.launch {
            try {
                val result = withTimeoutOrNull(20_000L) { root.detect(recheck) }
                mutableState.value = mutableState.value.copy(rootStatus = result, error = if (result == null) "Root 检测超时，请重新检测。" else null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                mutableState.value = mutableState.value.copy(error = failure.message)
            } finally {
                mutableState.value = mutableState.value.copy(isDetecting = false)
                inFlight.set(false)
            }
        }
    }
}
