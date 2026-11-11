package io.github.perfecticons.data.install

/** Live permission verdict, independent of the root manager brand. */
data class RootStatus(val granted: Boolean)

/** Only a successful, unambiguous UID-0 reply proves root permission. */
internal fun rootGranted(exitCode: Int, output: List<String>): Boolean =
    exitCode == 0 && output.map(String::trim).filter(String::isNotEmpty) == listOf("0")
