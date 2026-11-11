pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    // Lint's ASM 9.9 cannot read JVM 27 (major 71). Scope the reader upgrade to lint,
    // including detached tool configurations, without changing APK dependencies.
    components {
        for (module in listOf("lint-api", "lint-checks", "lint-typedef-remover")) {
            withModule("com.android.tools.lint:$module") {
                allVariants {
                    withDependencies {
                        filter { it.group == "org.ow2.asm" }
                            .forEach { it.version { strictly("9.11.1") } }
                    }
                }
            }
        }
    }
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "PerfectIconsPacker"

include(":app")
