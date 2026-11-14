package dev.savar.minutia

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Date line formats. Users can type Android/Java pattern letters ("EEE, d MMM") or
 * strftime-style % codes ("%a, %d %b"); % codes are translated to pattern letters.
 */
object DateFormats {

    /** The short list of one-tap presets. Anything else is a custom pattern. */
    val PRESETS = listOf("EEE, d MMM", "EEEE, d MMMM", "EEEE", "d MMMM", "dd/MM/yyyy")

    const val PATTERN_GUIDE_URL = "https://developer.android.com/reference/java/text/SimpleDateFormat"
    const val STRFTIME_GUIDE_URL = "https://strftime.org/"

    /** Letters shown in the in-app cheat sheet, with what each prints today. */
    val CHEATSHEET = listOf("EEE", "EEEE", "d", "dd", "MMM", "MMMM", "MM", "yyyy", "D")

    fun toPattern(input: String): String = if ('%' in input) fromStrftime(input) else input

    /** A formatter for [input], or null if it contains codes Android doesn't understand. */
    fun formatter(input: String, locale: Locale = Locale.getDefault()): SimpleDateFormat? =
        if (input.isBlank()) null
        else try {
            SimpleDateFormat(toPattern(input), locale)
        } catch (e: IllegalArgumentException) {
            null
        }

    private val CODES = mapOf(
        'a' to "EEE", 'A' to "EEEE", 'b' to "MMM", 'h' to "MMM", 'B' to "MMMM",
        'd' to "dd", 'e' to "d", 'm' to "MM", 'y' to "yy", 'Y' to "yyyy", 'j' to "DDD",
        'H' to "HH", 'I' to "hh", 'M' to "mm", 'S' to "ss", 'p' to "a",
        'Z' to "zzz", 'z' to "Z", 'u' to "u", 'U' to "ww", 'W' to "ww", 'V' to "ww", 'G' to "YYYY",
    )

    /** "%-d" style (no zero padding). */
    private val NO_PAD = mapOf(
        'd' to "d", 'e' to "d", 'm' to "M", 'H' to "H", 'I' to "h", 'M' to "m", 'S' to "s", 'j' to "D",
    )

    fun fromStrftime(s: String): String {
        val out = StringBuilder()
        val literal = StringBuilder()
        fun flush() {
            if (literal.isEmpty()) return
            val t = literal.toString()
            // Letters (and quotes) are special in patterns, so quote literal text that has any.
            if (t.any { it.isLetter() || it == '\'' }) out.append('\'').append(t.replace("'", "''")).append('\'')
            else out.append(t)
            literal.clear()
        }
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '%' && i + 1 < s.length) {
                var j = i + 1
                var noPad = false
                if (s[j] == '-' && j + 1 < s.length) {
                    noPad = true
                    j++
                }
                val code = s[j]
                val mapped = if (noPad) NO_PAD[code] ?: CODES[code] else CODES[code]
                when {
                    code == '%' -> literal.append('%')
                    mapped != null -> { flush(); out.append(mapped) }
                    else -> literal.append(s, i, j + 1) // unknown code: show it as typed
                }
                i = j + 1
            } else {
                literal.append(c)
                i++
            }
        }
        flush()
        return out.toString()
    }
}
