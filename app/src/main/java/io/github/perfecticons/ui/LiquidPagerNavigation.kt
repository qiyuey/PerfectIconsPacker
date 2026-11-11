package io.github.perfecticons.ui

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.utils.springAnimateToPage

/** Keep a requested tab stable while the pager crosses intermediate pages (KernelSU). */
internal class LiquidPagerNavigation(
    private val currentPage: () -> Int,
    private val pageCount: () -> Int,
    private val animatePage: suspend (Int) -> Unit,
    private val scope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(currentPage())
        private set
    var isNavigating by mutableStateOf(false)
        private set
    private var navigationJob: Job? = null

    fun animateToPage(target: Int) {
        if (target !in 0 until pageCount() || target == selectedPage) return
        val previous = navigationJob
        selectedPage = target
        isNavigating = true
        // Publish ownership before the old job's finally can run.
        val next = scope.launch(start = CoroutineStart.LAZY) {
            val ownJob = coroutineContext.job
            try { animatePage(target) }
            finally {
                if (navigationJob == ownJob) {
                    isNavigating = false
                    selectedPage = currentPage()
                }
            }
        }
        navigationJob = next
        previous?.cancel()
        next.start()
    }

    fun syncPage() {
        if (!isNavigating) selectedPage = currentPage()
    }
}

@Composable
internal fun rememberLiquidPagerNavigation(pager: PagerState): LiquidPagerNavigation {
    val scope = rememberCoroutineScope()
    val navigation = remember(pager, scope) { LiquidPagerNavigation({ pager.currentPage }, { pager.pageCount }, { pager.springAnimateToPage(it) }, scope) }
    LaunchedEffect(pager.currentPage, navigation.isNavigating) { navigation.syncPage() }
    return navigation
}
