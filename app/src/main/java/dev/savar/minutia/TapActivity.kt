package dev.savar.minutia

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Toast

/**
 * Invisible hand-off screen for widget taps. Resolving the clock app at tap time (instead of
 * when the widget is drawn) lets us try several ways in turn: OEM clocks differ a lot
 * (OnePlus/OPPO renamed theirs across OxygenOS/ColorOS versions).
 */
class TapActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val cfg = ConfigStore.load(this, id)
        val ok = when (cfg.tapAction) {
            TapAction.APP -> launchPackage(cfg.tapPackage) || launchClock()
            else -> launchClock()
        }
        if (!ok) {
            Toast.makeText(this, "Couldn't find a clock app. Pick one under \"When tapped\".", Toast.LENGTH_LONG).show()
            tryStart(Intent(this, MainActivity::class.java).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
        }
        finish()
    }

    private fun tryStart(i: Intent): Boolean = try {
        startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: Exception) {
        false
    }

    private fun launchPackage(pkg: String): Boolean {
        if (pkg.isBlank() || pkg == packageName) return false
        val i = packageManager.getLaunchIntentForPackage(pkg) ?: return false
        return tryStart(i)
    }

    private fun launchClock(): Boolean {
        // 1. The standard "show alarms" action most clock apps handle.
        val show = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        if (show.resolveActivity(packageManager) != null && tryStart(show)) return true
        // 2. Known clock packages.
        for (p in KNOWN_CLOCKS) if (launchPackage(p)) return true
        // 3. Any installed app that looks like a clock.
        @Suppress("DEPRECATION")
        val apps = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0,
        )
        val guess = apps.map { it.activityInfo.packageName }
            .filter { it != packageName }
            .firstOrNull { p -> CLOCK_HINTS.any { p.contains(it, ignoreCase = true) } }
        return guess != null && launchPackage(guess)
    }

    companion object {
        val KNOWN_CLOCKS = listOf(
            "com.coloros.alarmclock",
            "com.oplus.alarmclock",
            "com.oneplus.deskclock",
            "com.google.android.deskclock",
            "com.android.deskclock",
            "com.sec.android.app.clockpackage",
            "com.miui.clock",
            "com.android.alarmclock",
        )
        private val CLOCK_HINTS = listOf("deskclock", "alarmclock", "clock")
    }
}
