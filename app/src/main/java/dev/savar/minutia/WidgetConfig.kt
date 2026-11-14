package dev.savar.minutia

import android.content.Context
import org.json.JSONObject

enum class ClockStyle(val label: String) {
    BOLD("Bold"),
    STACKED("Stacked"),
    PILLS("Pills"),
    ANALOG("Analog"),
    WORDS("Words"),
}

enum class FontSource(val label: String) {
    SYSTEM("System"),
    GOOGLE("Google"),
    FILE("File"),
}

enum class DateFontMode(val label: String) {
    DEFAULT("Default"),
    CLOCK("Same as clock"),
    CUSTOM("Custom"),
}

/** One font choice: where it comes from plus the details for that source. */
data class FontSpec(
    val source: FontSource = FontSource.SYSTEM,
    val systemFamily: String = "sans-serif",
    val weight: Int = 500,
    val googleFamily: String = "",
    val localFontFile: String = "",
)

enum class HAlign(val label: String) {
    START("Left"),
    CENTER("Centre"),
    END("Right"),
}

enum class TapAction(val label: String) {
    CLOCK("Clock app"),
    APP("Pick an app…"),
    SETTINGS("Minutia settings"),
    NONE("Nothing"),
}

data class WidgetConfig(
    val style: ClockStyle = ClockStyle.BOLD,
    val fontSource: FontSource = FontSource.SYSTEM,
    val systemFamily: String = "sans-serif",
    val fontWeight: Int = 800,
    val googleFamily: String = "Space Grotesk",
    val localFontFile: String = "",
    val dateFontMode: DateFontMode = DateFontMode.DEFAULT,
    val dateFontSource: FontSource = FontSource.SYSTEM,
    val dateSystemFamily: String = "sans-serif",
    val dateFontWeight: Int = 500,
    val dateGoogleFamily: String = "",
    val dateLocalFontFile: String = "",
    val useWallpaperColors: Boolean = true,
    val primaryColor: Int = 0xFFFFFFFF.toInt(),
    val accentColor: Int = 0xFFFFB59E.toInt(),
    val sizePercent: Int = 90,
    val letterSpacing: Int = -3, // hundredths of an em
    val align: HAlign = HAlign.CENTER,
    val use24h: Boolean = true,
    val leadingZero: Boolean = true,
    val showAmPm: Boolean = true,
    val showDate: Boolean = true,
    val dateFormat: String = "EEE, d MMM",
    val uppercaseDate: Boolean = false,
    val lowercaseWords: Boolean = true,
    val shadow: Boolean = true,
    val background: Boolean = false,
    val bgColor: Int = 0xFF101216.toInt(),
    val bgOpacity: Int = 45,
    val cornerRadius: Int = 28,
    val tapAction: TapAction = TapAction.CLOCK,
    val tapPackage: String = "",
) {
    val clockFont: FontSpec
        get() = FontSpec(fontSource, systemFamily, fontWeight, googleFamily, localFontFile)

    fun withClockFont(f: FontSpec) = copy(
        fontSource = f.source, systemFamily = f.systemFamily, fontWeight = f.weight,
        googleFamily = f.googleFamily, localFontFile = f.localFontFile,
    )

    val dateFont: FontSpec
        get() = FontSpec(dateFontSource, dateSystemFamily, dateFontWeight, dateGoogleFamily, dateLocalFontFile)

    fun withDateFont(f: FontSpec) = copy(
        dateFontSource = f.source, dateSystemFamily = f.systemFamily, dateFontWeight = f.weight,
        dateGoogleFamily = f.googleFamily, dateLocalFontFile = f.localFontFile,
    )

    fun toJson(): String = JSONObject().apply {
        put("style", style.name)
        put("fontSource", fontSource.name)
        put("systemFamily", systemFamily)
        put("fontWeight", fontWeight)
        put("googleFamily", googleFamily)
        put("localFontFile", localFontFile)
        put("dateFontMode", dateFontMode.name)
        put("dateFontSource", dateFontSource.name)
        put("dateSystemFamily", dateSystemFamily)
        put("dateFontWeight", dateFontWeight)
        put("dateGoogleFamily", dateGoogleFamily)
        put("dateLocalFontFile", dateLocalFontFile)
        put("useWallpaperColors", useWallpaperColors)
        put("primaryColor", primaryColor)
        put("accentColor", accentColor)
        put("sizePercent", sizePercent)
        put("letterSpacing", letterSpacing)
        put("align", align.name)
        put("use24h", use24h)
        put("leadingZero", leadingZero)
        put("showAmPm", showAmPm)
        put("showDate", showDate)
        put("dateFormat", dateFormat)
        put("uppercaseDate", uppercaseDate)
        put("lowercaseWords", lowercaseWords)
        put("shadow", shadow)
        put("background", background)
        put("bgColor", bgColor)
        put("bgOpacity", bgOpacity)
        put("cornerRadius", cornerRadius)
        put("tapAction", tapAction.name)
        put("tapPackage", tapPackage)
    }.toString()

    companion object {
        private inline fun <reified T : Enum<T>> enumOr(name: String?, def: T): T =
            enumValues<T>().firstOrNull { it.name == name } ?: def

        fun fromJson(s: String): WidgetConfig {
            val d = WidgetConfig()
            return try {
                val o = JSONObject(s)
                WidgetConfig(
                    style = enumOr(o.optString("style"), d.style),
                    fontSource = enumOr(o.optString("fontSource"), d.fontSource),
                    systemFamily = o.optString("systemFamily", d.systemFamily),
                    fontWeight = o.optInt("fontWeight", d.fontWeight),
                    googleFamily = o.optString("googleFamily", d.googleFamily),
                    localFontFile = o.optString("localFontFile", d.localFontFile),
                    // Before 1.3.0 this was a single "dateUsesClockFont" switch.
                    dateFontMode = if (o.has("dateFontMode")) enumOr(o.optString("dateFontMode"), d.dateFontMode)
                    else if (o.optBoolean("dateUsesClockFont", false)) DateFontMode.CLOCK else DateFontMode.DEFAULT,
                    dateFontSource = enumOr(o.optString("dateFontSource"), d.dateFontSource),
                    dateSystemFamily = o.optString("dateSystemFamily", d.dateSystemFamily),
                    dateFontWeight = o.optInt("dateFontWeight", d.dateFontWeight),
                    dateGoogleFamily = o.optString("dateGoogleFamily", d.dateGoogleFamily),
                    dateLocalFontFile = o.optString("dateLocalFontFile", d.dateLocalFontFile),
                    useWallpaperColors = o.optBoolean("useWallpaperColors", d.useWallpaperColors),
                    primaryColor = o.optInt("primaryColor", d.primaryColor),
                    accentColor = o.optInt("accentColor", d.accentColor),
                    sizePercent = o.optInt("sizePercent", d.sizePercent),
                    letterSpacing = o.optInt("letterSpacing", d.letterSpacing),
                    align = enumOr(o.optString("align"), d.align),
                    use24h = o.optBoolean("use24h", d.use24h),
                    leadingZero = o.optBoolean("leadingZero", d.leadingZero),
                    showAmPm = o.optBoolean("showAmPm", d.showAmPm),
                    showDate = o.optBoolean("showDate", d.showDate),
                    dateFormat = o.optString("dateFormat", d.dateFormat),
                    uppercaseDate = o.optBoolean("uppercaseDate", d.uppercaseDate),
                    lowercaseWords = o.optBoolean("lowercaseWords", d.lowercaseWords),
                    shadow = o.optBoolean("shadow", d.shadow),
                    background = o.optBoolean("background", d.background),
                    bgColor = o.optInt("bgColor", d.bgColor),
                    bgOpacity = o.optInt("bgOpacity", d.bgOpacity),
                    cornerRadius = o.optInt("cornerRadius", d.cornerRadius),
                    tapAction = enumOr(o.optString("tapAction"), d.tapAction),
                    tapPackage = o.optString("tapPackage", d.tapPackage),
                )
            } catch (e: Exception) {
                d
            }
        }
    }
}

/** Per-widget settings. New widgets start from whatever was edited last. */
object ConfigStore {
    private const val PREFS = "widget_configs"
    private const val LAST = "last"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(ctx: Context, widgetId: Int): WidgetConfig {
        val p = prefs(ctx)
        val s = p.getString("w_$widgetId", null) ?: p.getString(LAST, null)
        return s?.let { WidgetConfig.fromJson(it) } ?: WidgetConfig()
    }

    fun save(ctx: Context, widgetId: Int, cfg: WidgetConfig) {
        val json = cfg.toJson()
        prefs(ctx).edit().apply {
            if (widgetId >= 0) putString("w_$widgetId", json)
            putString(LAST, json)
        }.apply()
    }

    fun delete(ctx: Context, widgetId: Int) {
        prefs(ctx).edit().remove("w_$widgetId").apply()
    }
}
