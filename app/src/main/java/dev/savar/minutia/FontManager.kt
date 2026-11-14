package dev.savar.minutia

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object FontManager {

    val systemFamilies = listOf(
        "sans-serif" to "Sans",
        "sans-serif-condensed" to "Condensed",
        "serif" to "Serif",
        "monospace" to "Mono",
        "cursive" to "Script",
        "casual" to "Casual",
    )

    /** A curated set that look great as clocks. Any Google Fonts family name works. */
    val googleSuggestions = listOf(
        "Space Grotesk", "Bebas Neue", "Anton", "Outfit", "Unbounded", "Syne",
        "Archivo Black", "Big Shoulders Display", "Michroma", "Orbitron", "Righteous",
        "Bungee", "Monoton", "Rubik Mono One", "Major Mono Display", "JetBrains Mono",
        "Space Mono", "DM Serif Display", "Instrument Serif", "Playfair Display",
        "Fraunces", "Abril Fatface", "Poppins", "Inter", "Manrope", "Sora",
        "Lexend", "Teko", "Oswald", "Fjalla One", "Pacifico", "Caveat",
    )

    private val memCache = HashMap<String, Typeface>()

    private fun fontsDir(ctx: Context) = File(ctx.filesDir, "fonts").apply { mkdirs() }

    private fun safe(s: String) = s.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_")

    fun googleFile(ctx: Context, family: String, weight: Int) =
        File(fontsDir(ctx), "g_${safe(family)}_$weight.ttf")

    fun googleFileAnyWeight(ctx: Context, family: String): File? =
        fontsDir(ctx).listFiles()
            ?.filter { it.name.startsWith("g_${safe(family)}_") && it.length() > 0 }
            ?.minByOrNull { it.name }

    fun isGoogleDownloaded(ctx: Context, family: String, weight: Int): Boolean {
        val f = googleFile(ctx, family, weight)
        return f.exists() && f.length() > 0
    }

    private fun fromFile(f: File): Typeface? {
        if (!f.exists() || f.length() == 0L) return null
        memCache[f.absolutePath]?.let { return it }
        if (!looksLikeFont(f)) return null
        return try {
            Typeface.createFromFile(f).also { memCache[f.absolutePath] = it }
        } catch (e: Exception) {
            null
        }
    }

    private fun system(family: String, weight: Int): Typeface =
        Typeface.create(Typeface.create(family, Typeface.NORMAL), weight.coerceIn(100, 900), false)

    fun clockTypeface(ctx: Context, cfg: WidgetConfig): Typeface = when (cfg.fontSource) {
        FontSource.SYSTEM -> system(cfg.systemFamily, cfg.fontWeight)
        FontSource.GOOGLE -> fromFile(googleFile(ctx, cfg.googleFamily, cfg.fontWeight))
            ?: googleFileAnyWeight(ctx, cfg.googleFamily)?.let { fromFile(it) }
            ?: system("sans-serif", cfg.fontWeight)
        FontSource.FILE -> fromFile(File(fontsDir(ctx), cfg.localFontFile).takeIf { cfg.localFontFile.isNotEmpty() } ?: File(""))
            ?: system("sans-serif", cfg.fontWeight)
    }

    fun dateTypeface(ctx: Context, cfg: WidgetConfig): Typeface =
        if (cfg.dateUsesClockFont) clockTypeface(ctx, cfg) else system("sans-serif", 500)

    /**
     * Downloads a Google Fonts family at the given weight. Google serves plain TTF files
     * to simple user agents, which Android can load directly.
     */
    suspend fun downloadGoogleFont(ctx: Context, family: String, weight: Int): Result<File> =
        withContext(Dispatchers.IO) {
            val name = family.trim()
            if (name.isEmpty()) return@withContext Result.failure(Exception("Type a font name"))
            val target = googleFile(ctx, name, weight)
            if (target.exists() && target.length() > 0) return@withContext Result.success(target)

            val enc = URLEncoder.encode(name, "UTF-8")
            val cssUrls = listOf(
                "https://fonts.googleapis.com/css2?family=$enc:wght@$weight",
                "https://fonts.googleapis.com/css?family=$enc:$weight",
                "https://fonts.googleapis.com/css2?family=$enc",
            )
            var lastError: Exception = Exception("Font \"$name\" not found on Google Fonts")
            // Google picks the font format from the User-Agent. These get plain TTF.
            for (ua in USER_AGENTS) for (cssUrl in cssUrls) {
                try {
                    val css = httpGetText(cssUrl, ua)
                    val fontUrl = Regex("""src:\s*url\(['"]?([^)'"\s]+)['"]?\)""").find(css)?.groupValues?.get(1)
                        ?: continue
                    val tmp = File(target.parentFile, target.name + ".part")
                    httpDownload(fontUrl, tmp, ua)
                    if (!looksLikeFont(tmp)) {
                        tmp.delete(); continue
                    }
                    target.delete()
                    tmp.renameTo(target)
                    memCache.remove(target.absolutePath)
                    return@withContext Result.success(target)
                } catch (e: Exception) {
                    lastError = e
                }
            }
            Result.failure(lastError)
        }

    /** Copies a user-picked .ttf/.otf into app storage. Returns the stored file name. */
    suspend fun importLocalFont(ctx: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val name = "local_${System.currentTimeMillis()}.ttf"
            val out = File(fontsDir(ctx), name)
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                out.outputStream().use { input.copyTo(it) }
            } ?: return@withContext Result.failure(Exception("Couldn't open that file"))
            if (!looksLikeFont(out)) {
                out.delete()
                Result.failure(Exception("That file isn't a font Android can read (use .ttf or .otf)"))
            } else Result.success(name)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** TrueType / OpenType / TrueType-collection magic numbers. WOFF/WOFF2 aren't supported. */
    private fun looksLikeFont(f: File): Boolean {
        if (!f.exists() || f.length() < 12) return false
        val head = ByteArray(4)
        f.inputStream().use { if (it.read(head) != 4) return false }
        val tag = String(head, Charsets.ISO_8859_1)
        val isTrueType = head[0] == 0.toByte() && head[1] == 1.toByte() && head[2] == 0.toByte() && head[3] == 0.toByte()
        return isTrueType || tag == "OTTO" || tag == "true" || tag == "ttcf"
    }

    private val USER_AGENTS = listOf(
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.59.8 (KHTML, like Gecko) Version/5.1.9 Safari/534.59.8",
        "curl/8.5.0",
        "Mozilla/5.0 (Linux; U; Android 4.0.3; en-us) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
    )

    private fun open(url: String, ua: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 20000
            setRequestProperty("User-Agent", ua)
        }

    private fun httpGetText(url: String, ua: String): String {
        val c = open(url, ua)
        try {
            if (c.responseCode != 200) throw Exception("Font not found (HTTP ${c.responseCode})")
            return c.inputStream.bufferedReader().readText()
        } finally {
            c.disconnect()
        }
    }

    private fun httpDownload(url: String, dest: File, ua: String) {
        val c = open(url, ua)
        try {
            if (c.responseCode != 200) throw Exception("Download failed (HTTP ${c.responseCode})")
            c.inputStream.use { input -> dest.outputStream().use { input.copyTo(it) } }
        } finally {
            c.disconnect()
        }
    }
}
