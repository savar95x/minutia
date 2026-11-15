package dev.savar.minutia

import android.content.Context
import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.json.JSONObject
import java.io.File

/**
 * The font of Minutia's own UI (not the widget). App-wide preference, default = the phone's system font.
 * Observable Compose state so the theme updates as soon as it changes.
 */
object AppFont {
    private const val PREFS = "ui"
    private const val KEY = "app_font"

    /** Weights downloaded for Google fonts so titles, labels and body text keep their hierarchy. */
    val UI_WEIGHTS = listOf(400, 500, 700)

    var spec by mutableStateOf(FontSpec(source = FontSource.SYSTEM, systemFamily = "sans-serif", weight = 400))
        private set
    var version by mutableIntStateOf(0)
        private set
    private var loaded = false

    fun load(ctx: Context) {
        if (loaded) return
        loaded = true
        val s = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null) ?: return
        runCatching {
            val o = JSONObject(s)
            spec = FontSpec(
                source = FontSource.entries.firstOrNull { it.name == o.optString("source") } ?: FontSource.SYSTEM,
                systemFamily = o.optString("systemFamily", "sans-serif"),
                weight = o.optInt("weight", 400),
                googleFamily = o.optString("googleFamily", ""),
                localFontFile = o.optString("localFontFile", ""),
            )
        }
    }

    fun set(ctx: Context, f: FontSpec) {
        spec = f
        val o = JSONObject()
            .put("source", f.source.name).put("systemFamily", f.systemFamily).put("weight", f.weight)
            .put("googleFamily", f.googleFamily).put("localFontFile", f.localFontFile)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, o.toString()).apply()
        version++
    }

    /** Download the extra weights of a Google font, then refresh the theme. */
    suspend fun ensureWeights(ctx: Context) {
        val f = spec
        if (f.source != FontSource.GOOGLE || f.googleFamily.isBlank()) return
        for (w in UI_WEIGHTS) {
            if (!FontManager.isGoogleDownloaded(ctx, f.googleFamily, w)) FontManager.downloadGoogleFont(ctx, f.googleFamily, w)
        }
        version++
    }

    fun family(ctx: Context): FontFamily {
        val f = spec
        return when (f.source) {
            FontSource.SYSTEM -> when (f.systemFamily) {
                "sans-serif" -> FontFamily.Default
                "serif" -> FontFamily.Serif
                "monospace" -> FontFamily.Monospace
                "cursive" -> FontFamily.Cursive
                else -> FontFamily(Typeface.create(f.systemFamily, Typeface.NORMAL))
            }
            FontSource.GOOGLE -> {
                val fonts = UI_WEIGHTS.mapNotNull { w ->
                    FontManager.googleFile(ctx, f.googleFamily, w).takeIf { it.exists() && it.length() > 0 }
                        ?.let { Font(it, FontWeight(w)) }
                }.ifEmpty {
                    listOfNotNull(FontManager.googleFileAnyWeight(ctx, f.googleFamily)?.let { Font(it) })
                }
                if (fonts.isEmpty()) FontFamily.Default else FontFamily(fonts)
            }
            FontSource.FILE -> {
                val file = File(File(ctx.filesDir, "fonts"), f.localFontFile)
                if (f.localFontFile.isNotEmpty() && file.exists()) FontFamily(Font(file)) else FontFamily.Default
            }
        }
    }
}

/** Material typography with every style switched to [family]. */
fun Typography.withFamily(family: FontFamily): Typography {
    fun TextStyle.f() = copy(fontFamily = family)
    return Typography(
        displayLarge = displayLarge.f(), displayMedium = displayMedium.f(), displaySmall = displaySmall.f(),
        headlineLarge = headlineLarge.f(), headlineMedium = headlineMedium.f(), headlineSmall = headlineSmall.f(),
        titleLarge = titleLarge.f(), titleMedium = titleMedium.f(), titleSmall = titleSmall.f(),
        bodyLarge = bodyLarge.f(), bodyMedium = bodyMedium.f(), bodySmall = bodySmall.f(),
        labelLarge = labelLarge.f(), labelMedium = labelMedium.f(), labelSmall = labelSmall.f(),
    )
}
