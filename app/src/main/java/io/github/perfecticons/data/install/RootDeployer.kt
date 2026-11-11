package io.github.perfecticons.data.install

import android.content.Context
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Only filesystem/mount operations cross the root shell boundary. */
class RootDeployer(private val context: Context) {
    suspend fun run(mode: String, source: File? = null, onLine: (String) -> Unit = {}) = withContext(Dispatchers.IO) {
        require(mode in setOf("apply", "reapply", "restore"))
        val script = File(context.filesDir, "root/live-deploy.sh")
        script.parentFile!!.mkdirs()
        context.assets.open("root/live-deploy.sh").use { input -> script.outputStream().use(input::copyTo) }
        fun quote(value: String) = "'" + value.replace("'", "'\\''") + "'"
        val command = "sh ${quote(script.absolutePath)} $mode" + (source?.let { " ${quote(it.absolutePath)}" } ?: "")
        val output = mutableListOf<String>()
        val errors = mutableListOf<String>()
        val result = Shell.getShell().newJob().add(command).to(output, errors).exec()
        (output + errors).forEach(onLine)
        check(result.isSuccess) { (errors + output).takeLast(12).joinToString("\n").ifBlank { "应用失败：${result.code}" } }
    }
}
