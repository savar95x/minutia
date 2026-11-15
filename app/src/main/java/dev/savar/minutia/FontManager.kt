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
    // Fixed order, never reordered or replaced by recents. Fragment Mono stays third.
    val googleSuggestions = listOf(
        "Space Grotesk", "Bebas Neue", "Fragment Mono", "Anton", "Outfit", "Unbounded", "Syne",
        "Archivo Black", "Big Shoulders Display", "Michroma", "Orbitron", "Righteous",
        "Bungee", "Monoton", "Rubik Mono One", "Major Mono Display", "JetBrains Mono",
        "Space Mono", "DM Serif Display", "Instrument Serif", "Playfair Display",
        "Fraunces", "Abril Fatface", "Poppins", "Inter", "Manrope", "Sora",
        "Lexend", "Teko", "Oswald", "Fjalla One", "Pacifico", "Caveat",
    )

    private val memCache = HashMap<String, Typeface>()

    // ---------- recently used Google Fonts (newest first) ----------

    private const val RECENT_KEY = "recent_google_fonts"
    private const val MAX_RECENT = 50

    private fun prefs(ctx: Context) = ctx.getSharedPreferences("fonts", Context.MODE_PRIVATE)

    fun recentFonts(ctx: Context): List<String> =
        (prefs(ctx).getString(RECENT_KEY, "") ?: "").split('\n').filter { it.isNotBlank() }

    /** Remembers a searched font. Curated picks are always shown already, so they're not stored. */
    fun addRecent(ctx: Context, family: String) {
        if (googleSuggestions.any { it.equals(family, ignoreCase = true) }) return
        val list = (listOf(family) + recentFonts(ctx).filterNot { it.equals(family, ignoreCase = true) })
            .take(MAX_RECENT)
        prefs(ctx).edit().putString(RECENT_KEY, list.joinToString("\n")).apply()
    }

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

    fun typeface(ctx: Context, f: FontSpec): Typeface = when (f.source) {
        FontSource.SYSTEM -> system(f.systemFamily, f.weight)
        FontSource.GOOGLE -> fromFile(googleFile(ctx, f.googleFamily, f.weight))
            ?: googleFileAnyWeight(ctx, f.googleFamily)?.let { fromFile(it) }
            ?: system("sans-serif", f.weight)
        FontSource.FILE -> (if (f.localFontFile.isNotEmpty()) fromFile(File(fontsDir(ctx), f.localFontFile)) else null)
            ?: system("sans-serif", f.weight)
    }

    fun clockTypeface(ctx: Context, cfg: WidgetConfig): Typeface = typeface(ctx, cfg.clockFont)

    fun dateTypeface(ctx: Context, cfg: WidgetConfig): Typeface = when (cfg.dateFontMode) {
        DateFontMode.DEFAULT -> system("sans-serif", 500)
        DateFontMode.CLOCK -> clockTypeface(ctx, cfg)
        DateFontMode.CUSTOM -> typeface(ctx, cfg.dateFont)
    }

    // ---------- Google Fonts catalogue (exact names, categories, type-ahead, browser) ----------

    private const val CATALOGUE_URL = "https://fonts.google.com/metadata/fonts"
    private const val CATALOGUE_MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000

    /** One Google Fonts family. [category] is Google's ("Sans Serif", "Serif", "Display", "Handwriting", "Monospace"). */
    data class Family(val name: String, val category: String)

    @Volatile private var entries: List<Family>? = null

    // TSV "name<TAB>category", most popular first. (v1 was catalogue.txt: names only.)
    private fun catalogueFile(ctx: Context) = File(fontsDir(ctx), "catalogue2.tsv")

    /** Families already on disk, most popular first (empty until [loadCatalogue] has run once). */
    fun cachedFamilies(ctx: Context): List<Family> {
        entries?.let { return it }
        val f = catalogueFile(ctx)
        if (!f.exists()) return emptyList()
        return f.readLines().mapNotNull { line ->
            val parts = line.split('\t')
            if (parts[0].isBlank()) null else Family(parts[0], parts.getOrElse(1) { "" })
        }.also { entries = it }
    }

    fun cachedCatalogue(ctx: Context): List<String> = cachedFamilies(ctx).map { it.name }

    /** Downloads Google's family list (~1,900) at most once a week. Never throws. */
    suspend fun loadCatalogue(ctx: Context): List<String> = withContext(Dispatchers.IO) {
        val f = catalogueFile(ctx)
        val fresh = f.exists() && System.currentTimeMillis() - f.lastModified() < CATALOGUE_MAX_AGE_MS
        if (!fresh) {
            try {
                val text = httpGetText(CATALOGUE_URL, USER_AGENTS[0])
                val list = parseCatalogue(text)
                if (list.size > 500) {
                    f.writeText(list.joinToString("\n") { "${it.name}\t${it.category}" })
                    entries = list
                }
            } catch (e: Exception) {
                // Offline or endpoint changed: fall back to the cached list / name guessing.
            }
        }
        cachedCatalogue(ctx)
    }

    private fun parseCatalogue(text: String): List<Family> {
        // The response starts with an anti-JSON-hijacking prefix like ")]}'".
        return try {
            val root = org.json.JSONObject(text.substring(text.indexOf('{')))
            val arr = root.getJSONArray("familyMetadataList")
            (0 until arr.length()).map { arr.getJSONObject(it) }
                .filterNot { it.optBoolean("isBrandFont", false) } // e.g. Google Sans: not downloadable
                .map { Triple(it.optString("family"), it.optString("category"), it.optInt("popularity", Int.MAX_VALUE)) }
                .filter { it.first.isNotBlank() }
                .sortedBy { it.third }
                .map { Family(it.first, it.second) }
        } catch (e: Exception) {
            Regex(""""family"\s*:\s*"([^"]+)"""").findAll(text)
                .map { Family(it.groupValues[1], "") }.distinctBy { it.name }.toList()
        }
    }

    // ---------- tiny preview fonts for the browser ----------

    private val previewGate = kotlinx.coroutines.sync.Semaphore(4)
    private val previewCache = HashMap<String, Typeface?>()
    const val PREVIEW_TEXT = "12:45"

    /**
     * A few-KB subset of [family] containing just its name and the sample digits
     * (Google's `text=` parameter), cached on disk. Null if it can't be fetched.
     */
    suspend fun previewTypeface(ctx: Context, family: String): Typeface? = withContext(Dispatchers.IO) {
        synchronized(previewCache) { if (previewCache.containsKey(family)) return@withContext previewCache[family] }
        val dir = File(fontsDir(ctx), "preview").apply { mkdirs() }
        val file = File(dir, "${safe(family)}.ttf")
        if (!looksLikeFont(file)) {
            previewGate.acquire()
            try {
                val chars = (PREVIEW_TEXT + family).toSet().joinToString("")
                val url = "https://fonts.googleapis.com/css2?family=${URLEncoder.encode(family, "UTF-8")}" +
                    "&text=${URLEncoder.encode(chars, "UTF-8")}"
                for (ua in USER_AGENTS) {
                    try {
                        val css = httpGetText(url, ua)
                        val src = Regex("""src:\s*url\(['"]?([^)'"\s]+)['"]?\)""").find(css)?.groupValues?.get(1) ?: continue
                        httpDownload(src, file, ua)
                        if (looksLikeFont(file)) break else file.delete()
                    } catch (e: Exception) {
                        file.delete()
                    }
                }
            } finally {
                previewGate.release()
            }
        }
        val tf = if (looksLikeFont(file)) runCatching { Typeface.createFromFile(file) }.getOrNull() else null
        synchronized(previewCache) { previewCache[family] = tf }
        tf
    }

    private fun squash(s: String) = s.lowercase().replace(Regex("[^a-z0-9]"), "")

    /** Type-ahead: names starting with the query first, then names containing it. Case and spaces ignored. */
    fun suggest(ctx: Context, query: String, limit: Int = 10): List<String> {
        val q = squash(query)
        if (q.length < 2) return emptyList()
        val all = cachedCatalogue(ctx)
        val starts = all.filter { squash(it).startsWith(q) }
        val contains = all.filter { val n = squash(it); !n.startsWith(q) && n.contains(q) }
        return (starts + contains).take(limit)
    }

    private val UPPER_WORDS = setOf("IBM", "DM", "EB", "PT", "BIZ", "UD", "IM", "SC", "JP", "KR", "TC", "HK", "SIL", "VT323")

    /** Names to try for what the user typed: the catalogue's exact spelling first, then sensible capitalisations. */
    private fun nameCandidates(ctx: Context, typed: String): List<String> {
        val t = typed.trim().replace(Regex("\\s+"), " ")
        val all = cachedCatalogue(ctx)
        val exact = all.firstOrNull { it.equals(t, ignoreCase = true) }
            ?: all.firstOrNull { squash(it) == squash(t) }
        val title = t.split(" ").joinToString(" ") { w ->
            if (w.uppercase() in UPPER_WORDS) w.uppercase()
            else w.lowercase().replaceFirstChar { it.uppercase() }
        }
        return listOfNotNull(exact, t, title).distinct()
    }

    /**
     * Downloads a Google Fonts family at the given weight and returns its official name
     * (e.g. "fragment mono" -> "Fragment Mono"; the CSS API is case-sensitive).
     * Google serves plain TTF to simple user agents, which Android can load directly.
     */
    suspend fun downloadGoogleFont(ctx: Context, family: String, weight: Int): Result<String> =
        withContext(Dispatchers.IO) {
            val typed = family.trim()
            if (typed.isEmpty()) return@withContext Result.failure(Exception("Type a name"))
            val catalogue = loadCatalogue(ctx)
            val names = nameCandidates(ctx, typed)
            val target = googleFile(ctx, names.first(), weight)
            if (target.exists() && target.length() > 0) return@withContext Result.success(names.first())

            var networkError: Exception? = null
            for (name in names) {
                val enc = URLEncoder.encode(name, "UTF-8")
                val cssUrls = listOf(
                    "https://fonts.googleapis.com/css2?family=$enc:wght@$weight",
                    "https://fonts.googleapis.com/css2?family=$enc",
                    "https://fonts.googleapis.com/css?family=$enc:$weight",
                )
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
                        return@withContext Result.success(name)
                    } catch (e: HttpStatusException) {
                        // 400 = no such family / weight: try the next URL or spelling.
                    } catch (e: Exception) {
                        networkError = e
                    }
                }
            }
            val msg = when {
                networkError != null && catalogue.isEmpty() ->
                    "No connection"
                else -> {
                    val close = suggest(ctx, typed, 3)
                    if (close.isNotEmpty()) "Not found. Try ${close.joinToString(", ")}"
                    else "Not found"
                }
            }
            Result.failure(Exception(msg))
        }

    /** Copies a user-picked .ttf/.otf into app storage. Returns the stored file name. */
    suspend fun importLocalFont(ctx: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val name = "local_${System.currentTimeMillis()}.ttf"
            val out = File(fontsDir(ctx), name)
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                out.outputStream().use { input.copyTo(it) }
            } ?: return@withContext Result.failure(Exception("Can't open file"))
            if (!looksLikeFont(out)) {
                out.delete()
                Result.failure(Exception("Not a .ttf / .otf font"))
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

    // Verified in CI: both of these get plain TrueType from fonts.googleapis.com (newer UAs get WOFF/WOFF2).
    private val USER_AGENTS = listOf(
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
            if (c.responseCode != 200) throw HttpStatusException(c.responseCode)
            return c.inputStream.bufferedReader().readText()
        } finally {
            c.disconnect()
        }
    }

    private fun httpDownload(url: String, dest: File, ua: String) {
        val c = open(url, ua)
        try {
            if (c.responseCode != 200) throw HttpStatusException(c.responseCode)
            c.inputStream.use { input -> dest.outputStream().use { input.copyTo(it) } }
        } finally {
            c.disconnect()
        }
    }
}

class HttpStatusException(val code: Int) : Exception("HTTP $code")
