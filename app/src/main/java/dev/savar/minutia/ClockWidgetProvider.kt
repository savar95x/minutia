package dev.savar.minutia

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.RemoteViews
import kotlin.math.sqrt

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                WidgetUpdater.updateAll(context)
                TickScheduler.schedule(context)
            }
            else -> super.onReceive(context, intent)
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { WidgetUpdater.update(context, manager, it) }
        TickScheduler.schedule(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle) {
        WidgetUpdater.update(context, manager, id)
    }

    override fun onEnabled(context: Context) = TickScheduler.schedule(context)

    override fun onDisabled(context: Context) = TickScheduler.cancel(context)

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { ConfigStore.delete(context, it) }
    }

    companion object {
        const val ACTION_TICK = "dev.savar.minutia.TICK"
    }
}

/** Wakes the widget at the start of every minute. */
object TickScheduler {
    private fun pending(ctx: Context): PendingIntent = PendingIntent.getBroadcast(
        ctx, 0,
        Intent(ctx, ClockWidgetProvider::class.java).setAction(ClockWidgetProvider.ACTION_TICK),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    fun schedule(ctx: Context) {
        if (WidgetUpdater.ids(ctx).isEmpty()) {
            cancel(ctx); return
        }
        val am = ctx.getSystemService(AlarmManager::class.java) ?: return
        val now = System.currentTimeMillis()
        val next = (now / 60_000L + 1) * 60_000L + 150L
        val pi = pending(ctx)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        try {
            if (canExact) am.setExact(AlarmManager.RTC, next, pi)
            else am.setWindow(AlarmManager.RTC, next, 5_000L, pi)
        } catch (e: SecurityException) {
            am.setWindow(AlarmManager.RTC, next, 5_000L, pi)
        }
    }

    fun cancel(ctx: Context) {
        ctx.getSystemService(AlarmManager::class.java)?.cancel(pending(ctx))
    }
}

object WidgetUpdater {
    data class SizePx(val w: Int, val h: Int, val dp: Float, val wDp: Int, val hDp: Int)

    fun component(ctx: Context) = ComponentName(ctx, ClockWidgetProvider::class.java)

    fun ids(ctx: Context): IntArray =
        AppWidgetManager.getInstance(ctx).getAppWidgetIds(component(ctx)) ?: IntArray(0)

    fun updateAll(ctx: Context) {
        val m = AppWidgetManager.getInstance(ctx)
        ids(ctx).forEach { update(ctx, m, it) }
    }

    fun update(ctx: Context, id: Int) = update(ctx, AppWidgetManager.getInstance(ctx), id)

    fun size(ctx: Context, id: Int): SizePx {
        val m = AppWidgetManager.getInstance(ctx)
        val o = if (id >= 0) m.getAppWidgetOptions(id) else Bundle()
        val portrait = ctx.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        var wDp = o.getInt(if (portrait) AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        var hDp = o.getInt(if (portrait) AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        if (wDp <= 0) wDp = 300
        if (hDp <= 0) hDp = 140
        val density = ctx.resources.displayMetrics.density
        var w = wDp * density
        var h = hDp * density
        var k = 1f
        val maxPx = 1_200_000f
        if (w * h > maxPx) {
            k = sqrt(maxPx / (w * h)); w *= k; h *= k
        }
        return SizePx(w.toInt(), h.toInt(), density * k, wDp, hDp)
    }

    fun update(ctx: Context, manager: AppWidgetManager, id: Int) {
        try {
            val cfg = ConfigStore.load(ctx, id)
            val sz = size(ctx, id)
            val bmp = ClockRenderer.render(ctx, cfg, sz.w, sz.h, sz.dp)
            val rv = RemoteViews(ctx.packageName, R.layout.widget_root)
            rv.setImageViewBitmap(R.id.clock, bmp)
            rv.setContentDescription(R.id.clock, ClockRenderer.timeDescription(cfg))
            tapIntent(ctx, cfg, id)?.let { rv.setOnClickPendingIntent(R.id.root, it) }
            manager.updateAppWidget(id, rv)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val CLOCK_PACKAGES = listOf(
        "com.coloros.alarmclock",
        "com.oneplus.deskclock",
        "com.google.android.deskclock",
        "com.android.deskclock",
        "com.sec.android.app.clockpackage",
    )

    private fun clockIntent(ctx: Context): Intent? {
        val show = Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (show.resolveActivity(ctx.packageManager) != null) return show
        for (p in CLOCK_PACKAGES) {
            ctx.packageManager.getLaunchIntentForPackage(p)?.let { return it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        }
        return null
    }

    private fun tapIntent(ctx: Context, cfg: WidgetConfig, id: Int): PendingIntent? {
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return when (cfg.tapAction) {
            TapAction.NONE -> null
            TapAction.CLOCK -> clockIntent(ctx)?.let { PendingIntent.getActivity(ctx, id * 4 + 1, it, flags) }
            TapAction.SETTINGS -> PendingIntent.getActivity(
                ctx, id * 4 + 2,
                Intent(ctx, MainActivity::class.java)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                flags,
            )
        }
    }
}
