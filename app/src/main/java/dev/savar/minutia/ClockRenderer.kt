package dev.savar.minutia

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Draws the clock into a bitmap. Every style is laid out at a unit size, then scaled to fit. */
object ClockRenderer {

    data class Palette(val primary: Int, val accent: Int, val bg: Int)

    private class Block(val w: Float, val h: Float, val draw: (Canvas, Float, Float) -> Unit)

    private class TimeParts(
        val hour24: Int,
        val hour12: Int,
        val minute: Int,
        val hours: String,
        val minutes: String,
        val ampm: String,
    )

    fun palette(ctx: Context, cfg: WidgetConfig): Palette {
        val bg = cfg.bgColor
        if (cfg.useWallpaperColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return Palette(
                primary = ctx.getColor(android.R.color.system_accent1_50),
                accent = ctx.getColor(android.R.color.system_accent1_200),
                bg = ctx.getColor(android.R.color.system_neutral1_900),
            )
        }
        return Palette(cfg.primaryColor, cfg.accentColor, bg)
    }

    private fun timeParts(cfg: WidgetConfig, cal: Calendar): TimeParts {
        val h24 = cal.get(Calendar.HOUR_OF_DAY)
        val h12 = (h24 % 12).let { if (it == 0) 12 else it }
        val m = cal.get(Calendar.MINUTE)
        val h = if (cfg.use24h) h24 else h12
        val hs = if (cfg.leadingZero) String.format(Locale.US, "%02d", h) else h.toString()
        return TimeParts(h24, h12, m, hs, String.format(Locale.US, "%02d", m), if (h24 < 12) "AM" else "PM")
    }

    fun timeDescription(cfg: WidgetConfig, cal: Calendar = Calendar.getInstance()): String {
        val t = timeParts(cfg, cal)
        return "${t.hours}:${t.minutes}" + if (!cfg.use24h) " ${t.ampm}" else ""
    }

    private fun dateText(cfg: WidgetConfig, cal: Calendar): String {
        val fmt = DateFormats.formatter(cfg.dateFormat)
            ?: SimpleDateFormat(DateFormats.PRESETS.first(), Locale.getDefault())
        val s = fmt.format(cal.time)
        return if (cfg.uppercaseDate) s.uppercase(Locale.getDefault()) else s
    }

    /** Date line, with AM/PM appended when the style has nowhere else to show it. */
    private fun dateLine(cfg: WidgetConfig, cal: Calendar, t: TimeParts, withAmPm: Boolean): String? {
        val am = if (withAmPm && !cfg.use24h && cfg.showAmPm) t.ampm else null
        val d = if (cfg.showDate) dateText(cfg, cal) else null
        return when {
            d != null && am != null -> "$d  ·  $am"
            d != null -> d
            am != null -> am
            else -> null
        }
    }

    fun render(
        ctx: Context,
        cfg: WidgetConfig,
        width: Int,
        height: Int,
        dp: Float,
        cal: Calendar = Calendar.getInstance(),
    ): Bitmap {
        val w = width.coerceAtLeast(1)
        val h = height.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val pal = palette(ctx, cfg)
        val clockTf = FontManager.clockTypeface(ctx, cfg)
        val dateTf = FontManager.dateTypeface(ctx, cfg)
        val t = timeParts(cfg, cal)

        val cardOn = cfg.background && cfg.style != ClockStyle.ANALOG
        if (cardOn) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = withAlpha(pal.bg, cfg.bgOpacity) }
            val r = cfg.cornerRadius * dp
            c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), r, r, p)
        }

        val minSide = min(w, h).toFloat()
        val pad = if (cardOn) max(minSide * 0.14f, 12 * dp) else minSide * 0.06f
        val area = RectF(pad, pad, w - pad, h - pad)
        if (area.width() <= 1f || area.height() <= 1f) return bmp

        val builder: (Float) -> Block = { s ->
            when (cfg.style) {
                ClockStyle.BOLD -> bold(s, cfg, pal, clockTf, dateTf, t, dateLine(cfg, cal, t, false), dp)
                ClockStyle.STACKED -> stacked(s, cfg, pal, clockTf, dateTf, t, dateLine(cfg, cal, t, true), dp)
                ClockStyle.PILLS -> pills(s, cfg, pal, clockTf, dateTf, t, dateLine(cfg, cal, t, true), dp)
                ClockStyle.ANALOG -> analog(s, cfg, pal, dateTf, t, if (cfg.showDate) dateText(cfg, cal) else null, dp)
                ClockStyle.WORDS -> words(s, cfg, pal, clockTf, dateTf, t, dateLine(cfg, cal, t, true), dp)
            }
        }

        val unit = builder(100f)
        val fit = min(area.width() / max(unit.w, 1f), area.height() / max(unit.h, 1f))
        val s = 100f * fit * (cfg.sizePercent.coerceIn(20, 100) / 100f)
        val block = builder(s)
        val x = when (cfg.align) {
            HAlign.START -> area.left
            HAlign.CENTER -> area.centerX() - block.w / 2f
            HAlign.END -> area.right - block.w
        }
        val y = area.centerY() - block.h / 2f
        block.draw(c, x, y)
        return bmp
    }

    /**
     * Just the time (no date, background, margins or shadow), cropped tight to the ink.
     * Used in the app bar so it lines up with the title.
     */
    fun renderMini(ctx: Context, cfg: WidgetConfig, heightPx: Int, dp: Float): Bitmap {
        val c = cfg.copy(
            showDate = false, background = false, shadow = false,
            align = HAlign.START, sizePercent = 100,
        )
        val w = if (cfg.style == ClockStyle.ANALOG) heightPx else heightPx * 6
        return trimTransparent(render(ctx, c, w, heightPx, dp))
    }

    private fun trimTransparent(b: Bitmap): Bitmap {
        val w = b.width
        val h = b.height
        val px = IntArray(w * h)
        b.getPixels(px, 0, w, 0, 0, w, h)
        var minX = w; var minY = h; var maxX = -1; var maxY = -1
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                if ((px[row + x] ushr 24) > 12) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }
        if (maxX < 0) return b
        return Bitmap.createBitmap(b, minX, minY, maxX - minX + 1, maxY - minY + 1)
    }

    // ---------- helpers ----------

    private fun withAlpha(color: Int, percent: Int): Int =
        Color.argb((percent.coerceIn(0, 100) * 255 / 100), Color.red(color), Color.green(color), Color.blue(color))

    private fun withAlpha255(color: Int, a: Int): Int =
        Color.argb(a.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))

    private fun onColor(bg: Int): Int = if (Color.luminance(bg) > 0.45f) 0xFF15171C.toInt() else Color.WHITE

    private fun textPaint(tf: Typeface, color: Int, size: Float, cfg: WidgetConfig, dp: Float, spacing: Float = cfg.letterSpacing / 100f, shadow: Boolean = cfg.shadow) =
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = tf
            this.color = color
            textSize = size
            letterSpacing = spacing
            if (shadow) setShadowLayer(max(2f * dp, size * 0.04f), 0f, max(1f * dp, size * 0.012f), 0x55000000)
        }

    private fun datePaint(tf: Typeface, color: Int, size: Float, cfg: WidgetConfig, dp: Float) =
        textPaint(tf, color, size, cfg, dp, spacing = if (cfg.uppercaseDate) 0.12f else 0.02f)

    private fun bounds(p: Paint, s: String): Rect = Rect().also { p.getTextBounds(s, 0, s.length, it) }

    private fun within(align: HAlign, x: Float, outer: Float, inner: Float) = when (align) {
        HAlign.START -> x
        HAlign.CENTER -> x + (outer - inner) / 2f
        HAlign.END -> x + outer - inner
    }

    private const val DIGITS = "0123456789"

    // ---------- styles ----------

    private fun bold(s: Float, cfg: WidgetConfig, pal: Palette, tf: Typeface, dtf: Typeface, t: TimeParts, date: String?, dp: Float): Block {
        val tp = textPaint(tf, pal.primary, s, cfg, dp)
        val cp = textPaint(tf, pal.accent, s, cfg, dp)
        val db = bounds(tp, DIGITS)
        val capH = -db.top.toFloat()
        val hw = tp.measureText(t.hours)
        val cw = cp.measureText(":")
        val mw = tp.measureText(t.minutes)

        val am = if (!cfg.use24h && cfg.showAmPm) t.ampm else null
        val ap = textPaint(tf, pal.accent, s * 0.22f, cfg, dp, spacing = 0.04f)
        val amW = am?.let { ap.measureText(it) } ?: 0f
        val amGap = if (am != null) s * 0.07f else 0f
        val amCap = -bounds(ap, "AMP").top.toFloat()
        val timeW = hw + cw + mw + amGap + amW

        val dp2 = datePaint(dtf, pal.accent, s * 0.17f, cfg, dp)
        val dB = date?.let { bounds(dp2, it) }
        val dW = date?.let { dp2.measureText(it) } ?: 0f
        val gap = s * 0.15f
        val timeH = capH + db.bottom
        val h = timeH + if (dB != null) gap + dB.height() else 0f
        val w = max(timeW, dW)

        return Block(w, h) { c, x, y ->
            val tx = within(cfg.align, x, w, timeW)
            val base = y + capH
            c.drawText(t.hours, tx, base, tp)
            c.drawText(":", tx + hw, base, cp)
            c.drawText(t.minutes, tx + hw + cw, base, tp)
            if (am != null) c.drawText(am, tx + hw + cw + mw + amGap, base - capH + amCap, ap)
            if (date != null && dB != null) {
                c.drawText(date, within(cfg.align, x, w, dW), y + timeH + gap - dB.top, dp2)
            }
        }
    }

    private fun stacked(s: Float, cfg: WidgetConfig, pal: Palette, tf: Typeface, dtf: Typeface, t: TimeParts, date: String?, dp: Float): Block {
        val hp = textPaint(tf, pal.primary, s, cfg, dp)
        val mp = textPaint(tf, pal.accent, s, cfg, dp)
        val db = bounds(hp, DIGITS)
        val capH = -db.top.toFloat()
        val lineGap = s * 0.1f
        val hw = hp.measureText(t.hours)
        val mw = mp.measureText(t.minutes)
        val dp2 = datePaint(dtf, pal.primary, s * 0.16f, cfg, dp)
        val dB = date?.let { bounds(dp2, it) }
        val dW = date?.let { dp2.measureText(it) } ?: 0f
        val gap = s * 0.16f
        val timeH = capH * 2 + lineGap + db.bottom
        val h = timeH + if (dB != null) gap + dB.height() else 0f
        val w = maxOf(hw, mw, dW)
        return Block(w, h) { c, x, y ->
            c.drawText(t.hours, within(cfg.align, x, w, hw), y + capH, hp)
            c.drawText(t.minutes, within(cfg.align, x, w, mw), y + capH * 2 + lineGap, mp)
            if (date != null && dB != null) {
                c.drawText(date, within(cfg.align, x, w, dW), y + timeH + gap - dB.top, dp2)
            }
        }
    }

    private fun pills(s: Float, cfg: WidgetConfig, pal: Palette, tf: Typeface, dtf: Typeface, t: TimeParts, date: String?, dp: Float): Block {
        val pillH = s
        val textSize = s * 0.6f
        val hourFill = pal.accent
        val hp = textPaint(tf, onColor(hourFill), textSize, cfg, dp, shadow = false)
        val mp = textPaint(tf, pal.primary, textSize, cfg, dp, shadow = false)
        val capH = -bounds(hp, DIGITS).top.toFloat()
        val hw = hp.measureText(t.hours)
        val mw = mp.measureText(t.minutes)
        val pw1 = max(hw + s * 0.42f, s * 0.95f)
        val pw2 = max(mw + s * 0.42f, s * 0.95f)
        val between = s * 0.09f
        val radius = s * 0.32f
        val timeW = pw1 + between + pw2

        val dp2 = datePaint(dtf, pal.primary, s * 0.18f, cfg, dp)
        val dB = date?.let { bounds(dp2, it) }
        val dW = date?.let { dp2.measureText(it) } ?: 0f
        val gap = s * 0.16f
        val h = pillH + if (dB != null) gap + dB.height() else 0f
        val w = max(timeW, dW)

        val fill1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = hourFill
            if (cfg.shadow) setShadowLayer(s * 0.06f, 0f, s * 0.02f, 0x40000000)
        }
        val fill2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha255(pal.primary, 0x3A)
            if (cfg.shadow) setShadowLayer(s * 0.06f, 0f, s * 0.02f, 0x26000000)
        }
        return Block(w, h) { c, x, y ->
            val px = within(cfg.align, x, w, timeW)
            val r1 = RectF(px, y, px + pw1, y + pillH)
            val r2 = RectF(r1.right + between, y, r1.right + between + pw2, y + pillH)
            c.drawRoundRect(r1, radius, radius, fill1)
            c.drawRoundRect(r2, radius, radius, fill2)
            val base = y + pillH / 2f + capH / 2f
            c.drawText(t.hours, r1.centerX() - hw / 2f, base, hp)
            c.drawText(t.minutes, r2.centerX() - mw / 2f, base, mp)
            if (date != null && dB != null) {
                c.drawText(date, within(cfg.align, x, w, dW), y + pillH + gap - dB.top, dp2)
            }
        }
    }

    private fun analog(s: Float, cfg: WidgetConfig, pal: Palette, dtf: Typeface, t: TimeParts, date: String?, dp: Float): Block {
        return Block(s, s) { c, x, y ->
            val r = s / 2f
            val cx = x + r
            val cy = y + r
            val shadowR = max(2f * dp, r * 0.05f)

            if (cfg.background) {
                val face = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = withAlpha(pal.bg, cfg.bgOpacity)
                    if (cfg.shadow) setShadowLayer(r * 0.06f, 0f, r * 0.02f, 0x33000000)
                }
                c.drawCircle(cx, cy, r * 0.98f, face)
            } else {
                val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = r * 0.025f
                    color = withAlpha255(pal.primary, 0x55)
                }
                c.drawCircle(cx, cy, r * 0.97f, ring)
            }

            val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = pal.primary
                strokeCap = Paint.Cap.ROUND
                strokeWidth = r * 0.045f
                if (cfg.shadow) setShadowLayer(shadowR, 0f, shadowR * 0.3f, 0x44000000)
            }
            val dot = Paint(tick).apply { color = withAlpha255(pal.primary, 0xB0) }
            for (i in 0 until 12) {
                val a = Math.toRadians(i * 30.0 - 90.0)
                val ca = cos(a).toFloat()
                val sa = sin(a).toFloat()
                if (i % 3 == 0) {
                    c.drawLine(cx + ca * r * 0.74f, cy + sa * r * 0.74f, cx + ca * r * 0.84f, cy + sa * r * 0.84f, tick)
                } else {
                    c.drawCircle(cx + ca * r * 0.82f, cy + sa * r * 0.82f, r * 0.022f, dot)
                }
            }

            if (date != null) {
                val dpaint = datePaint(dtf, withAlpha255(pal.primary, 0xD0), r * 0.15f, cfg, dp).apply {
                    textAlign = Paint.Align.CENTER
                }
                c.drawText(date, cx, cy + r * 0.5f, dpaint)
            }

            fun hand(angleDeg: Double, len: Float, width: Float, color: Int) {
                val a = Math.toRadians(angleDeg - 90.0)
                val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = color
                    strokeWidth = width
                    strokeCap = Paint.Cap.ROUND
                    if (cfg.shadow) setShadowLayer(shadowR, 0f, shadowR * 0.4f, 0x55000000)
                }
                val ca = cos(a).toFloat()
                val sa = sin(a).toFloat()
                c.drawLine(cx - ca * r * 0.08f, cy - sa * r * 0.08f, cx + ca * len, cy + sa * len, p)
            }
            val hourAngle = (t.hour24 % 12 + t.minute / 60.0) * 30.0
            val minuteAngle = t.minute * 6.0
            hand(hourAngle, r * 0.46f, r * 0.11f, pal.primary)
            hand(minuteAngle, r * 0.7f, r * 0.07f, pal.accent)
            c.drawCircle(cx, cy, r * 0.075f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pal.accent })
            c.drawCircle(cx, cy, r * 0.03f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pal.bg })
        }
    }

    private val ONES = listOf(
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen",
    )
    private val TENS = listOf("", "", "twenty", "thirty", "forty", "fifty")

    private fun minuteWords(m: Int): String = when {
        m == 0 -> "o'clock"
        m < 10 -> "oh ${ONES[m]}"
        m < 20 -> ONES[m]
        m % 10 == 0 -> TENS[m / 10]
        else -> "${TENS[m / 10]}-${ONES[m % 10]}"
    }

    private fun words(s: Float, cfg: WidgetConfig, pal: Palette, tf: Typeface, dtf: Typeface, t: TimeParts, date: String?, dp: Float): Block {
        fun cased(x: String) = if (cfg.lowercaseWords) x else x.replaceFirstChar { it.uppercase() }
        val l1 = cased(ONES[t.hour12])
        val l2 = cased(minuteWords(t.minute))
        val p1 = textPaint(tf, pal.primary, s, cfg, dp)
        val p2 = textPaint(tf, pal.accent, s, cfg, dp)
        val b1 = bounds(p1, l1)
        val b2 = bounds(p2, l2)
        val w1 = p1.measureText(l1)
        val w2 = p2.measureText(l2)
        val lineGap = s * 0.12f
        val dp2 = datePaint(dtf, pal.primary, s * 0.22f, cfg, dp)
        val dB = date?.let { bounds(dp2, it) }
        val dW = date?.let { dp2.measureText(it) } ?: 0f
        val gap = s * 0.2f
        val textH = b1.height() + lineGap + b2.height()
        val h = textH + if (dB != null) gap + dB.height() else 0f
        val w = maxOf(w1, w2, dW)
        return Block(w, h) { c, x, y ->
            val base1 = y - b1.top
            val base2 = y + b1.height() + lineGap - b2.top
            c.drawText(l1, within(cfg.align, x, w, w1), base1, p1)
            c.drawText(l2, within(cfg.align, x, w, w2), base2, p2)
            if (date != null && dB != null) {
                c.drawText(date, within(cfg.align, x, w, dW), y + textH + gap - dB.top, dp2)
            }
        }
    }
}
