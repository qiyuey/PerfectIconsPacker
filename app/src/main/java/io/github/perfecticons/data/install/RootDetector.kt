package io.github.perfecticons.data.install

import android.util.Log
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Verifies the app shell's UID; no module, root-brand or metamodule discovery. */
class RootDetector {

    private companion object {
        const val TAG = "RootDetector"

        /** libsu 缓存字段名，见 [dropCachedVerdict]。 */
        const val LIBSU_UTILS_CLASS = "com.topjohnwu.superuser.internal.Utils"
        const val LIBSU_ROOT_STATE_FIELD = "currentRootState"
        const val LIBSU_ROOT_STATE_UNKNOWN = -1
    }

    /**
     * 重新检测前清掉 libsu 的两层陈旧状态，否则运行期间新授予的 root 不会生效。
     *
     * 第一层是结论缓存：`Utils.currentRootState`（-1 未知 / 0 无 su / 1 有 su 未验证
     * / 2 已确认），首次判定后不再重算。
     *
     * 第二层是主 shell 本身。`BuilderImpl.start()` 在拿不到 root 时会退化成
     * `exec("sh")`，而这个非 root shell 会被 `MainShell` 一直接着用（`getCached()`
     * 只看 status 是否有效），用户后来授权也不会换掉它，于是 `id` 永远不是 root。
     * 因此仅在**当前主 shell 不是 root** 时才关闭它，逼下一次访问重建；
     * 已经是 root 的 shell 不动，避免无谓地打断正常工作的会话。
     */
    private fun dropCachedVerdict() {
        runCatching { Shell.getCachedShell() }
            .getOrNull()
            ?.takeIf { !it.isRoot }
            ?.let { stale ->
                Log.i(TAG, "丢弃非 root 主 shell，下次访问重建")
                runCatching { stale.close() }
                    .onFailure { Log.w(TAG, "关闭缓存的 shell 失败", it) }
            }

        runCatching {
            val field = Class.forName(LIBSU_UTILS_CLASS)
                .getDeclaredField(LIBSU_ROOT_STATE_FIELD)
                .apply { isAccessible = true }
            field.setInt(null, LIBSU_ROOT_STATE_UNKNOWN)
        }.onFailure { Log.w(TAG, "重置 libsu root 缓存失败", it) }
    }

    /**
     * @param recheck 用户主动“重新检测”时为 true：先清掉 libsu 的陈旧状态，
     *   再用一次现场命令重新判定。自动检测沿用现有 shell，避免每次进入首页
     *   都重建会话、反复触发授权弹窗。
     */
    suspend fun detect(recheck: Boolean = false): RootStatus = withContext(Dispatchers.IO) {
        if (recheck) dropCachedVerdict()

        val probe = runCatching { Shell.cmd("id -u").exec() }.getOrNull()
        val granted = rootGranted(probe?.code ?: -1, probe?.out.orEmpty())
        Log.i(TAG, "probe recheck=$recheck root=$granted")
        RootStatus(granted)
    }
}
