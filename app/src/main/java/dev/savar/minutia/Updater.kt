package dev.savar.minutia

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * In-app updates from GitHub Releases.
 *
 * - [check] asks the GitHub API for the latest release (with an ETag, so repeat checks are free 304s).
 * - [download] saves the APK to `cacheDir/updates/Minutia-X.Y.Z.apk`: resumable (`.part` + Range), verified
 *   against GitHub's size and sha256 digest, and reused if it's already there. Old APKs are deleted as soon as
 *   they're not the newest release, and all of them once the app is up to date, so nothing is downloaded twice
 *   and nothing piles up.
 * - [install] streams the file into a [PackageInstaller] session (same signing key, so settings are kept).
 *   After Minutia has installed itself once it is the installer of record, and later updates can go through
 *   without a confirmation prompt (`UPDATE_PACKAGES_WITHOUT_USER_ACTION`).
 */
object Updater {
    private const val API = "https://api.github.com/repos/savar95x/minutia/releases/latest"
    private const val PREFS = "updater"

    data class Release(val version: String, val notes: String, val url: String, val size: Long, val sha256: String?)

    sealed interface State {
        data object Idle : State
        data class Available(val release: Release) : State
        data class Downloading(val release: Release, val progress: Float) : State
        data class Ready(val release: Release, val file: File) : State
        data class Installing(val release: Release) : State
        data class Failed(val release: Release, val message: String) : State
    }

    var state by mutableStateOf<State>(State.Idle)
        internal set

    private var checked = false

    fun currentVersion(ctx: Context): String =
        runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: "0"

    private fun parts(v: String) = v.trim().removePrefix("v").split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }

    fun isNewer(candidate: String, current: String): Boolean {
        val a = parts(candidate)
        val b = parts(current)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    private fun dir(ctx: Context) = File(ctx.cacheDir, "updates").apply { mkdirs() }
    private fun apk(ctx: Context, r: Release) = File(dir(ctx), "Minutia-${r.version}.apk")

    /** Delete every cached APK except [keep]'s (or all of them). */
    private fun prune(ctx: Context, keep: Release?) {
        val keepName = keep?.let { "Minutia-${it.version}.apk" }
        dir(ctx).listFiles()?.forEach { f ->
            if (keepName == null || (f.name != keepName && f.name != "$keepName.part")) f.delete()
        }
    }

    /** Once per app launch. Silent on any failure (offline, rate-limited…). */
    suspend fun check(ctx: Context, force: Boolean = false) {
        if (checked && !force) return
        checked = true
        val app = ctx.applicationContext
        val release = withContext(Dispatchers.IO) { runCatching { fetchLatest(app) }.getOrNull() } ?: return
        val current = currentVersion(app)
        if (!isNewer(release.version, current)) {
            prune(app, null)
            state = State.Idle
            return
        }
        prune(app, release)
        val file = apk(app, release)
        state = if (file.exists() && file.length() == release.size) State.Ready(release, file) else State.Available(release)
    }

    private fun fetchLatest(ctx: Context): Release? {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val conn = (URL(API).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Minutia")
            prefs.getString("etag", null)?.let { setRequestProperty("If-None-Match", it) }
        }
        try {
            val body = when (conn.responseCode) {
                200 -> conn.inputStream.bufferedReader().readText().also { text ->
                    prefs.edit().putString("etag", conn.getHeaderField("ETag")).putString("body", text).apply()
                }
                304 -> prefs.getString("body", null) ?: return null
                else -> return null
            }
            val o = JSONObject(body)
            val version = o.optString("tag_name").removePrefix("v")
            val assets = o.optJSONArray("assets") ?: return null
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (!a.optString("name").endsWith(".apk")) continue
                val digest = a.optString("digest", "")
                return Release(
                    version = version,
                    notes = o.optString("body", "").trim(),
                    url = a.getString("browser_download_url"),
                    size = a.optLong("size", -1L),
                    sha256 = digest.removePrefix("sha256:").takeIf { digest.startsWith("sha256:") },
                )
            }
            return null
        } finally {
            conn.disconnect()
        }
    }

    /** Download (or resume) the APK, verify it, then mark it ready to install. */
    suspend fun download(ctx: Context, r: Release) {
        val app = ctx.applicationContext
        val file = apk(app, r)
        if (file.exists() && file.length() == r.size) {
            state = State.Ready(r, file); return
        }
        state = State.Downloading(r, 0f)
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val part = File(file.path + ".part")
                var have = if (part.exists()) part.length() else 0L
                if (r.size > 0 && have > r.size) { part.delete(); have = 0L }
                val conn = (URL(r.url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Minutia")
                    if (have > 0) setRequestProperty("Range", "bytes=$have-")
                }
                try {
                    val code = conn.responseCode
                    if (code != 200 && code != 206) error("Download failed ($code)")
                    if (code == 200) have = 0L // server ignored the range: start over
                    val total = if (r.size > 0) r.size else have + conn.contentLengthLong
                    FileOutputStream(part, code == 206).use { out ->
                        conn.inputStream.use { input ->
                            val buf = ByteArray(64 * 1024)
                            var done = have
                            var lastShown = 0f
                            while (true) {
                                val n = input.read(buf)
                                if (n < 0) break
                                out.write(buf, 0, n)
                                done += n
                                val p = if (total > 0) done.toFloat() / total else 0f
                                if (p - lastShown >= 0.01f) {
                                    lastShown = p
                                    withContext(Dispatchers.Main) { state = State.Downloading(r, p) }
                                }
                            }
                        }
                    }
                } finally {
                    conn.disconnect()
                }
                if (r.size > 0 && part.length() != r.size) error("Incomplete download")
                if (r.sha256 != null && sha256(part) != r.sha256.lowercase()) {
                    part.delete(); error("Download corrupted")
                }
                if (file.exists()) file.delete()
                if (!part.renameTo(file)) error("Couldn't save update")
                file
            }
        }
        state = result.fold({ State.Ready(r, it) }, { State.Failed(r, it.message ?: "Download failed") })
    }

    private fun sha256(f: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        RandomAccessFile(f, "r").use { raf ->
            val buf = ByteArray(64 * 1024)
            while (true) {
                val n = raf.read(buf)
                if (n < 0) break
                md.update(buf, 0, n)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    /** True if Minutia may install packages; otherwise opens the "Install unknown apps" switch for it. */
    fun ensureInstallAllowed(ctx: Context): Boolean {
        if (ctx.packageManager.canRequestPackageInstalls()) return true
        runCatching {
            ctx.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
        return false
    }

    suspend fun install(ctx: Context, r: Release, file: File) {
        val app = ctx.applicationContext
        if (!ensureInstallAllowed(ctx)) return
        state = State.Installing(r)
        val ok = withContext(Dispatchers.IO) {
            runCatching {
                val installer = app.packageManager.packageInstaller
                val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                    setAppPackageName(app.packageName)
                    setSize(file.length())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                    }
                }
                val id = installer.createSession(params)
                installer.openSession(id).use { session ->
                    session.openWrite("minutia.apk", 0, file.length()).use { out ->
                        file.inputStream().use { it.copyTo(out, 64 * 1024) }
                        session.fsync(out)
                    }
                    val status = PendingIntent.getBroadcast(
                        app, id,
                        Intent(app, InstallResultReceiver::class.java).putExtra(EXTRA_VERSION, r.version),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
                    )
                    session.commit(status.intentSender)
                }
            }
        }
        ok.onFailure { state = State.Failed(r, it.message ?: "Install failed") }
    }

    internal const val EXTRA_VERSION = "dev.savar.minutia.UPDATE_VERSION"
}

/** Receives the PackageInstaller session result: shows the system confirmation, or reports failure. */
class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val current = Updater.state
        val release = when (current) {
            is Updater.State.Installing -> current.release
            is Updater.State.Ready -> current.release
            else -> null
        }
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                @Suppress("DEPRECATION")
                val confirm = if (Build.VERSION.SDK_INT >= 33) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }
                confirm?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let { runCatching { ctx.startActivity(it) } }
            }
            PackageInstaller.STATUS_SUCCESS -> Unit // the process is replaced; the next launch prunes the APK
            else -> {
                val msg = if (status == PackageInstaller.STATUS_FAILURE_ABORTED) "Update cancelled"
                else intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "Install failed"
                if (release != null) {
                    // Keep the downloaded file: retrying installs it again without downloading.
                    val file = java.io.File(ctx.cacheDir, "updates/Minutia-${release.version}.apk")
                    Updater.state = if (file.exists()) Updater.State.Ready(release, file) else Updater.State.Available(release)
                }
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
