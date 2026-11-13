package io.github.perfecticons.data.repo

import android.content.Context
import java.io.File

object RepoPaths {
    fun root(context: Context): File = File(context.filesDir, "repo")

    fun copyBundledDynamicIcons(context: Context, directory: File): Map<String, File> {
        val assets = mapOf(
            "org.telegram.messenger" to "telegram",
            "org.telegram.messenger.web" to "telegram",
            "com.tencent.wework" to "wecom",
            "com.tencent.wework.launch.LaunchSplashActivity" to "wecom",
            "cn.futu.trader.universal" to "futu",
            "cn.futu.trader.launch.activity.LaunchActivity" to "futu",
            "com.xingin.xhs" to "red",
            "com.xingin.xhs.index.v2.IndexActivityV2" to "red",
            "com.ss.android.ugc.aweme" to "douyin",
            "com.ss.android.ugc.aweme.splash.SplashActivity" to "douyin",
            "com.sankuai.meituan" to "meituan",
            "com.meituan.android.pt.homepage.activity.MainActivity" to "meituan",
            "com.xunmeng.pinduoduo" to "pinduoduo",
            "com.xunmeng.pinduoduo.ui.activity.MainFrameActivity" to "pinduoduo",
            "com.twitter.android" to "twitter",
            "com.x.android.main.MainActivity" to "twitter",
            "com.ruanmei.ithome" to "ithome",
            "com.ruanmei.ithome.ui.MainActivity" to "ithome",
            "com.jingdong.app.mall" to "jd",
            "com.jingdong.app.mall.MainFrameActivity" to "jd",
            "com.sdu.didi.psnger" to "didi",
            "com.didi.sdk.app.launch.splash.SplashActivity" to "didi",
            "com.xiaomi.scanner" to "scanner",
            "com.xiaomi.scanner.app.ScanActivity" to "scanner",
            "com.tencent.qqmusic" to "qqmusic",
            "com.tencent.qqmusic.activity.AppStarterActivity" to "qqmusic",
            "com.tencent.weread" to "weread",
            "com.tencent.weread.LauncherActivity" to "weread",
            "com.xiaomi.smarthome" to "mihome",
            "com.xiaomi.smarthome.SmartHomeMainActivity" to "mihome",
            "com.miui.gallery" to "gallery",
            "com.miui.gallery.MainActivity" to "gallery",
            "com.android.contacts.activities.TwelveKeyDialer" to "phone",
            "com.android.mms" to "messages",
            "com.android.mms.ui.MmsTabActivity" to "messages",
            "com.android.settings" to "settings",
            "com.android.settings.MainSettings" to "settings",
            "com.android.camera" to "camera",
            "com.android.camera.Camera" to "camera",
            "com.microsoft.emmx" to "edge",
            "com.microsoft.ruby.Main" to "edge",
            "com.miui.calculator" to "calculator",
            "com.miui.calculator.cal.CalculatorActivity" to "calculator",
            "com.android.calendar" to "calendar",
            "com.android.calendar.homepage.AllInOneActivity" to "calendar",
            "com.xiaomi.market" to "getapps",
            "com.xiaomi.market.ui.DefaultLauncherIcon" to "getapps",
            "com.ugreen.pro" to "ugreen",
            "com.ugreen.pro.ui.Splash" to "ugreen",
            "com.azure.authenticator" to "authenticator",
            "com.azure.authenticator.ui.LaunchActivity" to "authenticator",
            "tv.danmaku.bili" to "bilibili",
            "tv.danmaku.bili.MainActivityV2" to "bilibili",
            "com.coolapk.market" to "coolapk",
            "com.coolapk.market.view.main.MainActivity" to "coolapk",
            "cn.ninebot.ninebot" to "ninebot",
            "cn.ninebot.ninebot.mainshell.SplashActivity" to "ninebot",
            "com.github.android" to "github",
            "com.github.android.main.MainActivity" to "github",
            "com.binance.dev" to "binance",
            "com.eaas.launcher.activities.main.MainActivity" to "binance",
        )
        val directories = assets.values.toSet().associateWith { name ->
            File(directory, name).also { copyAsset(context, "$name-dynamic-icon", it) }
        }
        return assets.mapValues { (_, name) -> directories.getValue(name) }
    }

    private fun copyAsset(context: Context, path: String, target: File) {
        val children = context.assets.list(path).orEmpty()
        if (children.isEmpty()) {
            target.parentFile?.mkdirs()
            context.assets.open(path).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        } else {
            for (child in children) {
                copyAsset(context, "$path/$child", File(target, child))
            }
        }
    }

    /** Returns the bundled HyperOS 4 mask in app-private storage. */
    fun defaultTransformConfig(context: Context): File {
        val target = File(context.filesDir, "defaults/transform_config.xml")
        if (target.isFile) return target
        target.parentFile?.mkdirs()
        context.assets.open("mask/hyperos4_transform_config.xml").use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return target
    }
}
