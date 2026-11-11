package io.github.perfecticons

import android.app.Application

class PerfectIconsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.topjohnwu.superuser.Shell.setDefaultBuilder(
            com.topjohnwu.superuser.Shell.Builder.create()
                .setFlags(com.topjohnwu.superuser.Shell.FLAG_MOUNT_MASTER)
                .setTimeout(10),
        )
    }
}
