package io.github.perfecticons.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/** Appearance always follows the system; legacy per-app overrides are no longer read. */
@Composable
fun PerfectIconsApp() {
    val controller = remember { ThemeController(colorSchemeMode = ColorSchemeMode.System) }
    MiuixTheme(controller = controller) { PerfectIconsScaffold() }
}
