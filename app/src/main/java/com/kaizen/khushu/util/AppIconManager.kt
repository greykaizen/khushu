package com.kaizen.khushu.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Switches the active launcher alias to match the selected [logoStyle].
 * Disables all other aliases with DONT_KILL_APP, then enables the target
 * alias with no flag — allowing Android to restart the process cleanly.
 */
object AppIconManager {

    private val styleToAlias = mapOf(
        "DYNAMIC" to "MainActivityAliasDynamic",
        "DARK"    to "MainActivityAliasDark",
        "LIGHT"   to "MainActivityAliasLight",
        "GREEN"   to "MainActivityAliasGreen",
    )

    fun apply(context: Context, logoStyle: String) {
        val pm = context.packageManager
        val pkg = context.packageName

        styleToAlias.forEach { (style, aliasShortName) ->
            val isTarget = style == logoStyle
            pm.setComponentEnabledSetting(
                ComponentName(pkg, "$pkg.$aliasShortName"),
                if (isTarget) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                else          PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                if (isTarget) 0                              // no flag → allows process restart
                else          PackageManager.DONT_KILL_APP, // keep running while disabling others
            )
        }
    }
}
