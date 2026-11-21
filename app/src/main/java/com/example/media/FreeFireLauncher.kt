package com.example.media

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

data class FreeFireInstallStatus(
    val isStandardInstalled: Boolean,
    val isMaxInstalled: Boolean,
) {
    val isAnyInstalled: Boolean get() = isStandardInstalled || isMaxInstalled
    val preferredPackage: String?
        get() = when {
            isMaxInstalled -> FreeFireLauncher.PKG_FREE_FIRE_MAX
            isStandardInstalled -> FreeFireLauncher.PKG_FREE_FIRE
            else -> null
        }
}

object FreeFireLauncher {
    const val PKG_FREE_FIRE = "com.dts.freefireth"
    const val PKG_FREE_FIRE_MAX = "com.dts.freefiremax"

    fun checkInstalled(context: Context): FreeFireInstallStatus {
        val pm = context.packageManager
        return FreeFireInstallStatus(
            isStandardInstalled = isPackageInstalled(pm, PKG_FREE_FIRE),
            isMaxInstalled = isPackageInstalled(pm, PKG_FREE_FIRE_MAX),
        )
    }

    private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun launchFreeFire(context: Context, packageName: String?): Boolean {
        val pkg = packageName ?: checkInstalled(context).preferredPackage ?: return false
        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true
    }

    fun openPlayStore(context: Context, packageName: String = PKG_FREE_FIRE) {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val fallbackIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { context.startActivity(marketIntent) }
            .onFailure { runCatching { context.startActivity(fallbackIntent) } }
    }

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
