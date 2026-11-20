@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package dev.savar.minutia

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Gradient
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Typography
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Checkbox
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.lerp
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val launchId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (launchId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            // Accept straight away so backing out never removes the widget.
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, launchId))
            WidgetUpdater.update(this, launchId)
            TickScheduler.schedule(this)
        }
        setContent {
            AppTheme { SettingsScreen(launchId) }
        }
    }
}

@Composable
private fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val base = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    ThemePrefs.load(ctx)
    // Pure black (AMOLED): only the page goes black; section cards keep their tonal containers.
    val scheme = if (dark && ThemePrefs.pureBlack) base.pureBlack() else base
    val activity = ctx as? android.app.Activity
    SideEffect {
        // Window behind Compose (shown while launching and during transitions) matches the page.
        activity?.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(scheme.background.toArgb()))
    }
    AppFont.load(ctx)
    val spec = AppFont.spec
    val version = AppFont.version
    val typography = remember(spec, version) {
        if (spec.source == FontSource.SYSTEM && spec.systemFamily == "sans-serif") Typography()
        else Typography().withFamily(AppFont.family(ctx))
    }
    MaterialTheme(colorScheme = scheme, typography = typography, content = content)
}

/**
 * AMOLED variant of a dark scheme, identical to Aria's: page #000, raised containers pulled most of the way
 * to black but keeping a hint of the wallpaper hue.
 */
private fun ColorScheme.pureBlack(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = lerp(Color.Black, surfaceContainerLow, 0.45f),
    surfaceContainer = lerp(Color.Black, surfaceContainer, 0.55f),
    surfaceContainerHigh = lerp(Color.Black, surfaceContainerHigh, 0.65f),
    surfaceContainerHighest = lerp(Color.Black, surfaceContainerHighest, 0.75f),
    surfaceVariant = lerp(Color.Black, surfaceVariant, 0.7f),
)

/** App-wide theme preferences (prefs `ui`), observable so the theme updates immediately. */
object ThemePrefs {
    private const val KEY_PURE_BLACK = "pure_black"
    var pureBlack by mutableStateOf(false)
        private set
    private var loaded = false

    fun load(ctx: Context) {
        if (loaded) return
        loaded = true
        pureBlack = ctx.getSharedPreferences("ui", Context.MODE_PRIVATE).getBoolean(KEY_PURE_BLACK, false)
    }

    fun setPureBlack(ctx: Context, on: Boolean) {
        pureBlack = on
        ctx.getSharedPreferences("ui", Context.MODE_PRIVATE).edit().putBoolean(KEY_PURE_BLACK, on).apply()
    }
}

const val SOURCE_URL = "https://github.com/savar95x/minutia"

private val SWATCHES = listOf(
    0xFFFFFFFF, 0xFFFFF4E0, 0xFFFFB59E, 0xFFFF7A59, 0xFFFF8FAB, 0xFFC9B6FF, 0xFF9AD0FF,
    0xFF9EF0C8, 0xFFD4F57A, 0xFFFFD95A, 0xFFC7CBD3, 0xFF5A6070, 0xFF1B1D22, 0xFF000000,
).map { it.toInt() }

private fun hex(c: Int) = String.format("#%06X", 0xFFFFFF and c)

private fun parseHex(s: String): Int? {
    val t = s.trim().removePrefix("#")
    if (t.length != 6 && t.length != 8) return null
    return try {
        android.graphics.Color.parseColor("#$t")
    } catch (e: Exception) {
        null
    }
}

/** The widget's size on the home screen in dp, e.g. "300×140". */
private fun widgetLabel(ctx: Context, id: Int, index: Int): String {
    val s = WidgetUpdater.size(ctx, id)
    return "${s.wDp}×${s.hDp}"
}

@Composable
private fun SettingsScreen(launchId: Int) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var widgetIds by remember { mutableStateOf(WidgetUpdater.ids(ctx).toList()) }
    var selectedId by remember {
        mutableIntStateOf(if (launchId >= 0) launchId else widgetIds.firstOrNull() ?: -1)
    }
    var ignoringBattery by remember { mutableStateOf(true) }

    LifecycleResumeEffect(Unit) {
        widgetIds = WidgetUpdater.ids(ctx).toList()
        if (selectedId !in widgetIds) selectedId = widgetIds.firstOrNull() ?: -1
        val pm = ctx.getSystemService(PowerManager::class.java)
        ignoringBattery = pm?.isIgnoringBatteryOptimizations(ctx.packageName) ?: true
        onPauseOrDispose { }
    }

    var cfg by remember(selectedId) { mutableStateOf(ConfigStore.load(ctx, selectedId)) }
    var fontVersion by remember { mutableIntStateOf(0) }
    var lightPreview by remember { mutableStateOf(false) }

    fun commit() {
        ConfigStore.save(ctx, selectedId, cfg)
        if (selectedId >= 0) {
            WidgetUpdater.update(ctx, selectedId)
            TickScheduler.schedule(ctx)
        }
    }
    fun setLocal(c: WidgetConfig) { cfg = c }
    fun update(c: WidgetConfig) { cfg = c; commit() }

    val minuteTick by produceState(0) {
        while (true) {
            delay(60_000L - System.currentTimeMillis() % 60_000L + 100L)
            value++
        }
    }

    // New release on GitHub? Checked once per launch; silent when offline.
    LaunchedEffect(Unit) { Updater.check(ctx) }

    // Google Fonts name list for type-ahead (shared by the clock and date font editors).
    var catalogueVersion by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        FontManager.loadCatalogue(ctx)
        catalogueVersion++
    }
    var showAppPicker by remember { mutableStateOf(false) }
    var nameHint by remember { mutableStateOf(false) }
    var resetCount by remember { mutableIntStateOf(0) }
    val haptics = LocalHapticFeedback.current

    var menuOpen by remember { mutableStateOf(false) }
    val systemDark = isSystemInDarkTheme()
    var confirmReset by remember { mutableStateOf(false) }
    val version = remember {
        runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: ""
    }
    val scheme = MaterialTheme.colorScheme
    val size = remember(selectedId, widgetIds) { WidgetUpdater.size(ctx, selectedId) }
    // App preference (not per widget): on the home page a pinned preview stays at the top like a widget
    // editor; otherwise it scrolls away like Wallpaper & style. Sub-pages always pin it.
    val uiPrefs = remember { ctx.getSharedPreferences("ui", Context.MODE_PRIVATE) }
    var showAppFont by remember { mutableStateOf(false) }
    var pinPreview by remember { mutableStateOf(uiPrefs.getBoolean("pin_preview", false)) }
    val pinSupported = remember { AppWidgetManager.getInstance(ctx).isRequestPinAppWidgetSupported }

    var page by rememberSaveable { mutableStateOf<Page?>(null) }
    BackHandler(enabled = page != null) { page = null }

    AnimatedContent(
        targetState = page,
        transitionSpec = {
            // Material shared-axis X: forward slides in from the right, back from the left.
            val forward = targetState != null
            val dir = if (forward) 1 else -1
            (slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { w -> dir * w / 6 } + fadeIn(tween(220, delayMillis = 60)))
                .togetherWith(slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { w -> -dir * w / 6 } + fadeOut(tween(120)))
        },
        label = "page",
    ) { shown ->
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val pinned = shown != null || pinPreview
        Scaffold(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = { Text(shown?.title ?: "Minutia") },
                    navigationIcon = {
                        if (shown != null) {
                            IconButton(onClick = { page = null }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (shown == null) Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("App font") },
                                    leadingIcon = { Icon(Icons.Rounded.FontDownload, contentDescription = null) },
                                    onClick = { menuOpen = false; showAppFont = true },
                                )
                                DropdownMenuItem(
                                    text = { Text("Pin preview") },
                                    leadingIcon = { Icon(Icons.Rounded.PushPin, contentDescription = null) },
                                    trailingIcon = { Checkbox(checked = pinPreview, onCheckedChange = null) },
                                    onClick = {
                                        pinPreview = !pinPreview
                                        uiPrefs.edit().putBoolean("pin_preview", pinPreview).apply()
                                        menuOpen = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Pure black") },
                                    leadingIcon = { Icon(Icons.Rounded.Contrast, contentDescription = null) },
                                    trailingIcon = { Checkbox(checked = ThemePrefs.pureBlack, onCheckedChange = null) },
                                    onClick = { ThemePrefs.setPureBlack(ctx, !ThemePrefs.pureBlack); menuOpen = false },
                                    enabled = systemDark,
                                )
                                DropdownMenuItem(
                                    text = { Text("Reset widget") },
                                    leadingIcon = { Icon(Icons.Rounded.RestartAlt, contentDescription = null) },
                                    onClick = { menuOpen = false; confirmReset = true },
                                )
                                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("Source code")
                                            Text(
                                                "Version $version",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = scheme.onSurfaceVariant,
                                            )
                                        }
                                    },
                                    leadingIcon = { Icon(Icons.Rounded.Code, contentDescription = null) },
                                    trailingIcon = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, Modifier.size(18.dp)) },
                                    onClick = { menuOpen = false; openUrl(ctx, SOURCE_URL) },
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    // With a pinned preview nothing scrolls under the bar, so it shouldn't tint.
                    // Pure black keeps the bar black like Aria's; a pinned preview means nothing scrolls under it.
                    colors = if (pinned || (systemDark && ThemePrefs.pureBlack)) {
                        TopAppBarDefaults.topAppBarColors(scrolledContainerColor = scheme.surface)
                    } else {
                        TopAppBarDefaults.topAppBarColors()
                    },
                )
            },
        ) { innerPadding ->
            Column(Modifier.fillMaxSize().padding(innerPadding).imePadding()) {
                if (pinned) {
                    PreviewCard(
                        cfg, size, minuteTick, fontVersion, lightPreview, { lightPreview = !lightPreview },
                        Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
                        height = if (shown != null) 168.dp else 200.dp,
                    )
                }
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    when (shown) {
                        null -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (!pinned) {
                                    PreviewCard(cfg, size, minuteTick, fontVersion, lightPreview, { lightPreview = !lightPreview }, Modifier.padding(top = 4.dp))
                                }
                                // ---------- which widget ----------
                                if (widgetIds.isNotEmpty() && (widgetIds.size > 1 || pinSupported)) {
                                    ChipFlow {
                                        widgetIds.forEachIndexed { i, id ->
                                            Chip(
                                                selected = id == selectedId,
                                                onClick = { selectedId = id },
                                                label = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("Widget ${i + 1}")
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            widgetLabel(ctx, id, i),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = scheme.onSurfaceVariant,
                                                        )
                                                    }
                                                },
                                            )
                                        }
                                        if (pinSupported) {
                                            AssistChip(
                                                onClick = {
                                                    AppWidgetManager.getInstance(ctx).requestPinAppWidget(WidgetUpdater.component(ctx), null, null)
                                                },
                                                label = { Text("Add") },
                                                leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null, Modifier.size(AssistChipDefaults.IconSize)) },
                                            )
                                        }
                                    }
                                }
                            }
                            if (widgetIds.isEmpty()) {
                                Group {
                                    item {
                                        ListItem(
                                            leadingContent = { RowIcon(Icons.Rounded.Widgets) },
                                            headlineContent = { Text("No widget yet") },
                                            supportingContent = {
                                                Text(if (pinSupported) "Add one to your home screen" else "Long-press home screen → Widgets")
                                            },
                                            trailingContent = if (pinSupported) {
                                                {
                                                    FilledTonalButton(onClick = {
                                                        AppWidgetManager.getInstance(ctx).requestPinAppWidget(WidgetUpdater.component(ctx), null, null)
                                                    }) { Text("Add") }
                                                }
                                            } else null,
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                        )
                                    }
                                }
                            }
                            UpdateCard()
                            Group {
                                item { NavRow(Icons.Rounded.Widgets, "Style", cfg.style.label) { page = Page.STYLE } }
                                item { NavRow(Icons.Rounded.TextFields, "Font", fontName(cfg)) { page = Page.FONT } }
                                item {
                                    NavRow(
                                        Icons.Rounded.Palette, "Colours",
                                        if (cfg.useWallpaperColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "Wallpaper" else "Custom",
                                    ) { page = Page.COLOURS }
                                }
                            }
                            Group {
                                item {
                                    val summary = remember(cfg, minuteTick) {
                                        val t = if (cfg.use24h) "24-hour" else "12-hour"
                                        if (cfg.showDate) "$t · ${DateFormats.formatter(cfg.dateFormat)?.format(Date()) ?: ""}" else t
                                    }
                                    NavRow(Icons.Rounded.Schedule, "Time & date", summary) { page = Page.TIME }
                                }
                                item {
                                    NavRow(Icons.Rounded.AspectRatio, "Layout", "${cfg.sizePercent}% · ${cfg.align.label}") { page = Page.LAYOUT }
                                }
                                item {
                                    val tap = when {
                                        cfg.tapAction == TapAction.APP && cfg.tapPackage.isNotEmpty() -> remember(cfg.tapPackage) { appLabel(ctx, cfg.tapPackage) }
                                        cfg.tapAction == TapAction.NONE -> "Nothing"
                                        else -> "Open ${cfg.tapAction.label.lowercase().removeSuffix("…")}"
                                    }
                                    NavRow(Icons.Rounded.TouchApp, "On tap", tap) { page = Page.TAP }
                                }
                            }
                            // ---------- reliability ----------
                            Group {
                                item {
                                    val ok = ignoringBattery
                                    ListItem(
                                        modifier = Modifier.clickable {
                                            runCatching {
                                                val i = if (ok) {
                                                    // OxygenOS keeps auto-launch / background activity on the app info page.
                                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))
                                                } else {
                                                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}"))
                                                }
                                                ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                            }
                                        },
                                        leadingContent = {
                                            RowIcon(
                                                if (ok) Icons.Rounded.BatteryFull else Icons.Rounded.BatteryAlert,
                                                container = if (ok) scheme.primaryContainer else scheme.errorContainer,
                                                tint = if (ok) scheme.onPrimaryContainer else scheme.onErrorContainer,
                                            )
                                        },
                                        headlineContent = { Text("Battery") },
                                        supportingContent = { Text(if (ok) "Unrestricted · updates on time" else "Restricted · clock may lag") },
                                        trailingContent = if (ok) {
                                            { Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
                                        } else {
                                            { FilledTonalButton(onClick = {
                                                runCatching {
                                                    ctx.startActivity(
                                                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}"))
                                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                                    )
                                                }
                                            }) { Text("Fix") } }
                                        },
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    )
                                }
                            }
                        }

                        // ---------- style ----------
                        Page.STYLE -> {
                            val styles = remember { ClockStyle.entries.toList() }
                            styles.chunked(2).forEach { pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    pair.forEach { style ->
                                        StyleCard(style, cfg, style == cfg.style, minuteTick, fontVersion, Modifier.weight(1f)) {
                                            if (style != cfg.style) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            update(cfg.copy(style = style))
                                        }
                                    }
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        // ---------- fonts ----------
                        Page.FONT -> {
                            val hasDateLine = cfg.showDate || (!cfg.use24h && cfg.showAmPm && cfg.style != ClockStyle.ANALOG)
                            if (cfg.style != ClockStyle.ANALOG) {
                                Group("Clock") {
                                    item {
                                        Pad {
                                            FontEditor(
                                                spec = cfg.clockFont,
                                                resetKey = Triple(selectedId, resetCount, "clock"),
                                                catalogueVersion = catalogueVersion,
                                                recentKey = fontVersion,
                                                onPreview = { setLocal(cfg.withClockFont(it)) },
                                                onApply = { update(cfg.withClockFont(it)) },
                                                onFontLoaded = { fontVersion++ },
                                            )
                                        }
                                    }
                                }
                            }
                            if (hasDateLine) {
                                Group("Date") {
                                    item {
                                        Pad {
                                            val modes = DateFontMode.entries.filter {
                                                it != DateFontMode.CLOCK || cfg.style != ClockStyle.ANALOG
                                            }
                                            Segmented(modes, cfg.dateFontMode, { it.label }) { update(cfg.copy(dateFontMode = it)) }
                                        }
                                    }
                                    if (cfg.dateFontMode == DateFontMode.CUSTOM) item {
                                        Pad {
                                            FontEditor(
                                                spec = cfg.dateFont,
                                                resetKey = Triple(selectedId, resetCount, "date"),
                                                catalogueVersion = catalogueVersion,
                                                recentKey = fontVersion,
                                                onPreview = { setLocal(cfg.withDateFont(it)) },
                                                onApply = { update(cfg.withDateFont(it)) },
                                                onFontLoaded = { fontVersion++ },
                                            )
                                        }
                                    }
                                }
                            } else if (cfg.style == ClockStyle.ANALOG) {
                                Text(
                                    "Analog has no text",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }

                        // ---------- colours ----------
                        Page.COLOURS -> {
                            val canDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                            Group {
                                item {
                                    SwitchRow("Wallpaper colours", cfg.useWallpaperColors && canDynamic, enabled = canDynamic, icon = Icons.Rounded.Wallpaper) {
                                        update(cfg.copy(useWallpaperColors = it))
                                    }
                                }
                                if (!cfg.useWallpaperColors || !canDynamic) {
                                    item { Pad { ColorPicker("Main", cfg.primaryColor) { update(cfg.copy(primaryColor = it)) } } }
                                    item { Pad { ColorPicker("Accent", cfg.accentColor) { update(cfg.copy(accentColor = it)) } } }
                                }
                            }
                            Group {
                                item { SwitchRow("Shadow", cfg.shadow, icon = Icons.Rounded.Gradient) { update(cfg.copy(shadow = it)) } }
                            }
                        }

                        // ---------- time & date ----------
                        Page.TIME -> {
                            Group("Time") {
                                item { SwitchRow("24-hour time", cfg.use24h) { update(cfg.copy(use24h = it)) } }
                                item { SwitchRow("Leading zero", cfg.leadingZero) { update(cfg.copy(leadingZero = it)) } }
                                if (!cfg.use24h) item { SwitchRow("AM / PM", cfg.showAmPm) { update(cfg.copy(showAmPm = it)) } }
                                if (cfg.style == ClockStyle.WORDS) {
                                    item { SwitchRow("Lowercase", cfg.lowercaseWords) { update(cfg.copy(lowercaseWords = it)) } }
                                }
                            }
                            Group("Date") {
                                item { SwitchRow("Show date", cfg.showDate) { update(cfg.copy(showDate = it)) } }
                                if (cfg.showDate) {
                                    item { Pad { DateFormatPicker(cfg, selectedId, resetCount, minuteTick) { update(cfg.copy(dateFormat = it)) } } }
                                    item { SwitchRow("All caps", cfg.uppercaseDate) { update(cfg.copy(uppercaseDate = it)) } }
                                }
                            }
                        }

                        // ---------- layout ----------
                        Page.LAYOUT -> {
                            Group("Clock") {
                                item {
                                    Pad {
                                        LabeledSlider("Size", cfg.sizePercent, 20..100, 0, "${cfg.sizePercent}%",
                                            onChange = { setLocal(cfg.copy(sizePercent = it)) }, onDone = { commit() })
                                    }
                                }
                                if (cfg.style != ClockStyle.ANALOG) item {
                                    Pad {
                                        LabeledSlider("Spacing", cfg.letterSpacing, -12..24, 0, "${cfg.letterSpacing}",
                                            onChange = { setLocal(cfg.copy(letterSpacing = it)) }, onDone = { commit() })
                                    }
                                }
                                item {
                                    Pad {
                                        Text("Alignment", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 10.dp))
                                        Segmented(HAlign.entries, cfg.align, { it.label }) { update(cfg.copy(align = it)) }
                                    }
                                }
                            }
                            Group("Background") {
                                item {
                                    SwitchRow(if (cfg.style == ClockStyle.ANALOG) "Filled face" else "Background", cfg.background) {
                                        update(cfg.copy(background = it))
                                    }
                                }
                                if (cfg.background) {
                                    if (!cfg.useWallpaperColors || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                                        item { Pad { ColorPicker("Colour", cfg.bgColor) { update(cfg.copy(bgColor = it)) } } }
                                    }
                                    item {
                                        Pad {
                                            LabeledSlider("Opacity", cfg.bgOpacity, 0..100, 0, "${cfg.bgOpacity}%",
                                                onChange = { setLocal(cfg.copy(bgOpacity = it)) }, onDone = { commit() })
                                        }
                                    }
                                    if (cfg.style != ClockStyle.ANALOG) item {
                                        Pad {
                                            LabeledSlider("Corners", cfg.cornerRadius, 0..48, 0, "${cfg.cornerRadius}dp",
                                                onChange = { setLocal(cfg.copy(cornerRadius = it)) }, onDone = { commit() })
                                        }
                                    }
                                }
                            }
                            if (pinSupported && selectedId >= 0) {
                                Group("Home screen") {
                                    item {
                                        val labeled = remember(selectedId, widgetIds) { WidgetUpdater.isLabeled(ctx, selectedId) }
                                        SwitchRow("Show name", labeled, summary = if (nameHint) "Place the new one, then remove the old" else null) { want ->
                                            // A widget's name is fixed when it's placed, so swap in the other variant.
                                            ConfigStore.save(ctx, selectedId, cfg)
                                            val target = if (want) WidgetUpdater.labeledComponent(ctx) else WidgetUpdater.component(ctx)
                                            AppWidgetManager.getInstance(ctx).requestPinAppWidget(target, null, null)
                                            nameHint = true
                                        }
                                    }
                                }
                            }
                        }

                        // ---------- tap ----------
                        Page.TAP -> {
                            Group {
                                TapAction.entries.forEach { a ->
                                    item {
                                        val selected = cfg.tapAction == a
                                        ListItem(
                                            modifier = Modifier.selectable(selected = selected, role = Role.RadioButton) {
                                                if (a == TapAction.APP) showAppPicker = true else update(cfg.copy(tapAction = a))
                                            },
                                            leadingContent = { RadioButton(selected = selected, onClick = null) },
                                            headlineContent = {
                                                Text(
                                                    when (a) {
                                                        TapAction.CLOCK -> "Open clock"
                                                        TapAction.APP -> "Open an app"
                                                        TapAction.SETTINGS -> "Open Minutia"
                                                        TapAction.NONE -> "Do nothing"
                                                    },
                                                )
                                            },
                                            supportingContent = if (a == TapAction.APP && cfg.tapPackage.isNotEmpty()) {
                                                { Text(remember(cfg.tapPackage) { appLabel(ctx, cfg.tapPackage) }) }
                                            } else null,
                                            trailingContent = if (a == TapAction.APP) {
                                                { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null) }
                                            } else null,
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            onDismiss = { showAppPicker = false },
            onPick = { pkg ->
                showAppPicker = false
                update(cfg.copy(tapAction = TapAction.APP, tapPackage = pkg))
            },
        )
    }

    if (showAppFont) {
        val scope = rememberCoroutineScope()
        ModalBottomSheet(onDismissRequest = { showAppFont = false }) {
            Column(
                Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text("App font", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                FontEditor(
                    spec = AppFont.spec,
                    resetKey = "appfont",
                    catalogueVersion = catalogueVersion,
                    recentKey = fontVersion,
                    onPreview = {},
                    onApply = { f ->
                        AppFont.set(ctx, f)
                        scope.launch { AppFont.ensureWeights(ctx) }
                    },
                    onFontLoaded = { fontVersion++ },
                    showWeight = false,
                )
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
            title = { Text("Reset widget?") },
            text = { Text("All its settings go back to default.") },
            confirmButton = {
                TextButton(
                    onClick = { confirmReset = false; update(WidgetConfig()); resetCount++ },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }
}

/** Shown on the home page only when GitHub has a newer release. Tap for release notes. */
@Composable
private fun UpdateCard() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val st = Updater.state
    var notesOpen by remember { mutableStateOf(false) }
    val release = when (st) {
        Updater.State.Idle -> null
        is Updater.State.Available -> st.release
        is Updater.State.Downloading -> st.release
        is Updater.State.Ready -> st.release
        is Updater.State.Installing -> st.release
        is Updater.State.Failed -> st.release
    }
    AnimatedVisibility(
        visible = release != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        if (release == null) return@AnimatedVisibility
        val scheme = MaterialTheme.colorScheme
        fun act() {
            when (val s = Updater.state) {
                is Updater.State.Available, is Updater.State.Failed -> scope.launch { Updater.download(ctx, release) }
                is Updater.State.Ready -> scope.launch { Updater.install(ctx, s.release, s.file) }
                else -> Unit
            }
        }
        Surface(color = scheme.primaryContainer, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column {
                ListItem(
                    modifier = Modifier.clickable { notesOpen = true },
                    leadingContent = {
                        RowIcon(Icons.Rounded.SystemUpdate, container = scheme.primary, tint = scheme.onPrimary)
                    },
                    headlineContent = { Text("Update available") },
                    supportingContent = {
                        Text(
                            when (st) {
                                is Updater.State.Downloading -> "${release.version} · ${(st.progress * 100).roundToInt()}%"
                                is Updater.State.Ready -> "${release.version} · downloaded"
                                is Updater.State.Installing -> "${release.version} · installing"
                                is Updater.State.Failed -> st.message
                                else -> "${release.version} · ${"%.1f".format(release.size / 1_048_576f)} MB"
                            },
                        )
                    },
                    trailingContent = {
                        when (st) {
                            is Updater.State.Downloading, is Updater.State.Installing ->
                                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                            is Updater.State.Ready -> Button(onClick = ::act) { Text("Install") }
                            is Updater.State.Failed -> FilledTonalButton(onClick = ::act) { Text("Retry") }
                            else -> Button(onClick = ::act) { Text("Update") }
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                        headlineColor = scheme.onPrimaryContainer,
                        supportingColor = scheme.onPrimaryContainer.copy(alpha = 0.8f),
                    ),
                )
                if (st is Updater.State.Downloading) {
                    LinearProgressIndicator(
                        progress = { st.progress },
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    )
                }
            }
        }
        if (notesOpen) {
            ModalBottomSheet(onDismissRequest = { notesOpen = false }) {
                Column(
                    Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text("Minutia ${release.version}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "You have ${Updater.currentVersion(ctx)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp),
                    )
                    releaseNotes(release.notes).forEach { (heading, line) ->
                        if (heading) {
                            Text(
                                line,
                                style = MaterialTheme.typography.titleSmall,
                                color = scheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                            )
                        } else {
                            Row(Modifier.padding(vertical = 3.dp)) {
                                Text("•", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(16.dp))
                                Text(line, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { notesOpen = false; act() },
                        enabled = st !is Updater.State.Downloading && st !is Updater.State.Installing,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(if (st is Updater.State.Ready) "Install" else "Update") }
                }
            }
        }
    }
}

/** CHANGELOG markdown → (isHeading, text) lines: "### Added" headings and "- item" bullets. */
private fun releaseNotes(md: String): List<Pair<Boolean, String>> = md.lines().mapNotNull { raw ->
    val l = raw.trim()
    when {
        l.isEmpty() || l.startsWith("## ") -> null
        l.startsWith("#") -> true to l.trimStart('#').trim()
        l.startsWith("- ") || l.startsWith("* ") -> false to l.drop(2).replace("`", "")
        else -> false to l.replace("`", "")
    }
}

/** Sub-pages of the settings home list. */
private enum class Page(val title: String) {
    STYLE("Style"), FONT("Font"), COLOURS("Colours"), TIME("Time & date"), LAYOUT("Layout"), TAP("On tap"),
}

private fun fontName(cfg: WidgetConfig): String {
    if (cfg.style == ClockStyle.ANALOG) return "Analog"
    val f = cfg.clockFont
    return when (f.source) {
        FontSource.SYSTEM -> FontManager.systemFamilies.firstOrNull { it.first == f.systemFamily }?.second ?: "System"
        FontSource.GOOGLE -> f.googleFamily.ifBlank { "Google Fonts" }
        FontSource.FILE -> "Font file"
    }
}

/** Collects the rows of a [Group]; rows can be added conditionally. */
private class GroupScope {
    val items = mutableListOf<@Composable () -> Unit>()
    fun item(content: @Composable () -> Unit) { items += content }
}

/**
 * Material 3 Expressive list group (Android 16 Settings style): each row is its own tonal container,
 * 2dp apart, with large corners on the outside of the group and small corners between rows.
 */
@Composable
private fun Group(title: String? = null, build: GroupScope.() -> Unit) {
    val items = GroupScope().apply(build).items
    if (items.isEmpty()) return
    Column(Modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            )
        }
        Column(
            Modifier.fillMaxWidth().animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items.forEachIndexed { i, content ->
                val big = 24.dp
                val small = 6.dp
                val shape = RoundedCornerShape(
                    topStart = if (i == 0) big else small,
                    topEnd = if (i == 0) big else small,
                    bottomStart = if (i == items.lastIndex) big else small,
                    bottomEnd = if (i == items.lastIndex) big else small,
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = shape,
                    modifier = Modifier.fillMaxWidth(),
                ) { content() }
            }
        }
    }
}

/** Padding for free-form content (sliders, pickers) inside a group row. */
@Composable
private fun Pad(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), content = content)
}

/** Leading icon in a tonal circle, as in Android 16 Settings. */
@Composable
private fun RowIcon(
    icon: ImageVector,
    container: Color = MaterialTheme.colorScheme.secondaryContainer,
    tint: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(container),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp)) }
}

/** Home list row: icon, title, current value, chevron. Opens a sub-page. */
@Composable
private fun NavRow(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { RowIcon(icon) },
        headlineContent = { Text(title) },
        supportingContent = { Text(value, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

/** Clock style tile: live thumbnail on a wallpaper-like gradient, label below. */
@Composable
private fun StyleCard(
    style: ClockStyle,
    cfg: WidgetConfig,
    selected: Boolean,
    tick: Int,
    fontVersion: Int,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val ctx = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val thumb = remember(cfg, style, tick, fontVersion) {
        val d = ctx.resources.displayMetrics.density
        ClockRenderer.render(ctx, cfg.copy(style = style, sizePercent = 100), (168 * d).toInt(), (104 * d).toInt(), d * 0.7f)
            .asImageBitmap()
    }
    val outline by animateColorAsState(if (selected) scheme.primary else Color.Transparent, tween(250), label = "cardOutline")
    val corner by animateDpAsState(if (selected) 32.dp else 20.dp, spring(stiffness = Spring.StiffnessMediumLow), label = "cardCorner")
    Column(
        modifier
            .clip(RoundedCornerShape(corner))
            .background(if (selected) scheme.secondaryContainer else scheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(104.dp)
                .clip(RoundedCornerShape(corner - 8.dp))
                .background(Brush.linearGradient(listOf(scheme.primaryContainer, Color(0xFF1A1C22))))
                .border(BorderStroke(2.dp, outline), RoundedCornerShape(corner - 8.dp)),
        ) {
            Image(thumb, style.label, Modifier.fillMaxSize().padding(8.dp), contentScale = ContentScale.Fit)
        }
        Row(
            Modifier.padding(top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(selected) {
                Icon(
                    Icons.Rounded.Check, contentDescription = null,
                    tint = scheme.onSecondaryContainer,
                    modifier = Modifier.padding(end = 4.dp).size(18.dp),
                )
            }
            Text(
                style.label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

/** Date format: preset chips (shown as today's date) or a custom pattern with a live result. */
@Composable
private fun DateFormatPicker(cfg: WidgetConfig, selectedId: Int, resetCount: Int, tick: Int, onFormat: (String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val now = remember(tick) { Date() }
    val isPreset = cfg.dateFormat in DateFormats.PRESETS
    var customOpen by remember(selectedId, resetCount) { mutableStateOf(!isPreset) }
    var customText by remember(selectedId, resetCount) { mutableStateOf(if (isPreset) "" else cfg.dateFormat) }
    Text("Format", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 10.dp))
    ChipFlow {
        DateFormats.PRESETS.forEach { f ->
            val sample = remember(f, now) { SimpleDateFormat(f, Locale.getDefault()).format(now) }
            Chip(
                selected = !customOpen && cfg.dateFormat == f,
                onClick = { customOpen = false; onFormat(f) },
                label = { Text(sample) },
            )
        }
        Chip(
            selected = customOpen,
            onClick = {
                customOpen = true
                if (customText.isBlank()) customText = cfg.dateFormat
            },
            label = { Text("Custom") },
        )
    }
    Reveal(customOpen) {
        val fmt = remember(customText) { DateFormats.formatter(customText) }
        val shows = remember(fmt, now) { fmt?.format(now) }
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = customText,
            onValueChange = {
                customText = it
                if (DateFormats.formatter(it) != null) onFormat(it)
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("EEE, d MMM  ·  %a %d %b") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            isError = customText.isNotBlank() && fmt == null,
            supportingText = {
                Text(
                    when {
                        customText.isBlank() -> ""
                        fmt == null -> "Unknown code"
                        else -> "$shows"
                    },
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        val cheats = remember(now) {
            DateFormats.CHEATSHEET.joinToString("  ") { code ->
                "$code ${SimpleDateFormat(code, Locale.getDefault()).format(now)}"
            }
        }
        Text(
            cheats,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            AssistChip(onClick = { openUrl(ctx, DateFormats.PATTERN_GUIDE_URL) }, label = { Text("Codes") },
                trailingIcon = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, Modifier.size(AssistChipDefaults.IconSize)) })
            AssistChip(onClick = { openUrl(ctx, DateFormats.STRFTIME_GUIDE_URL) }, label = { Text("% codes") },
                trailingIcon = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, Modifier.size(AssistChipDefaults.IconSize)) })
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    summary: String? = null,
    onChange: (Boolean) -> Unit,
) {
    val content = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = content)
            if (summary != null) {
                Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null, // the whole row toggles
            enabled = enabled,
            thumbContent = if (checked) {
                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
            } else {
                null
            },
        )
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Int,
    range: IntRange,
    steps: Int,
    valueText: String,
    onChange: (Int) -> Unit,
    onDone: () -> Unit,
) {
    Column {
        Row {
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text(valueText, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value.toFloat().coerceIn(range.first.toFloat(), range.last.toFloat()),
            onValueChange = { onChange(it.roundToInt()) },
            onValueChangeFinished = onDone,
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = steps,
        )
    }
}

@Composable
private fun ColorPicker(label: String, color: Int, onPick: (Int) -> Unit) {
    var text by remember(color) { mutableStateOf(hex(color)) }
    Column(Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    parseHex(it)?.let { c -> onPick(c or 0xFF000000.toInt()) }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(120.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SWATCHES) { s ->
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(s))
                        .border(
                            if ((s or 0xFF000000.toInt()) == (color or 0xFF000000.toInt())) 3.dp else 1.dp,
                            if ((s or 0xFF000000.toInt()) == (color or 0xFF000000.toInt())) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            CircleShape,
                        )
                        .clickable { onPick(s) },
                )
            }
        }
    }
}

@Composable
private fun ChipFlow(content: @Composable () -> Unit) {
    // Chips keep their 32dp height instead of padding out to 48dp touch targets, so rows sit tight.
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { content() }
    }
}

private class AppEntry(val label: String, val pkg: String, val icon: ImageBitmap)

private fun appLabel(ctx: Context, pkg: String): String = try {
    val pm = ctx.packageManager
    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
} catch (e: Exception) {
    pkg
}

@Suppress("DEPRECATION")
private fun loadApps(ctx: Context): List<AppEntry> {
    val pm = ctx.packageManager
    val px = (40 * ctx.resources.displayMetrics.density).toInt()
    return pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        .filter { it.activityInfo.packageName != ctx.packageName }
        .distinctBy { it.activityInfo.packageName }
        .map { ri ->
            AppEntry(
                ri.loadLabel(pm).toString(),
                ri.activityInfo.packageName,
                ri.loadIcon(pm).toBitmap(px, px).asImageBitmap(),
            )
        }
        .sortedBy { it.label.lowercase() }
}

@Composable
private fun AppPickerDialog(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val ctx = LocalContext.current
    val apps by produceState<List<AppEntry>?>(null) {
        value = withContext(Dispatchers.IO) { loadApps(ctx) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Choose app") },
        text = {
            val list = apps
            if (list == null) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(Modifier.heightIn(max = 440.dp)) {
                    items(list) { a ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPick(a.pkg) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(a.icon, contentDescription = null, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(14.dp))
                            Text(a.label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
    )
}

/**
 * Pick a font from System / Google Fonts / a file, plus its weight. Used for both the clock
 * and the date. [onPreview] updates the preview only (while dragging); [onApply] saves.
 */
@Composable
private fun FontEditor(
    spec: FontSpec,
    resetKey: Any,
    catalogueVersion: Int,
    recentKey: Int,
    onPreview: (FontSpec) -> Unit,
    onApply: (FontSpec) -> Unit,
    onFontLoaded: () -> Unit,
    showWeight: Boolean = true,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    val current by rememberUpdatedState(spec)
    val applyFont by rememberUpdatedState(onApply)
    val fontLoaded by rememberUpdatedState(onFontLoaded)

    var tab by remember(resetKey) { mutableStateOf(spec.source) }
    var query by remember(resetKey) { mutableStateOf(spec.googleFamily) }
    var status by remember(resetKey) { mutableStateOf<String?>(null) }
    var downloading by remember { mutableStateOf(false) }
    var showBrowser by remember { mutableStateOf(false) }

    fun fetch(name: String, weight: Int = current.weight) {
        val typed = name.trim()
        if (typed.isEmpty() || downloading) return
        downloading = true
        status = null
        scope.launch {
            val r = FontManager.downloadGoogleFont(ctx, typed, weight)
            downloading = false
            r.onSuccess { family ->
                FontManager.addRecent(ctx, family)
                applyFont(current.copy(source = FontSource.GOOGLE, googleFamily = family, weight = weight))
                tab = FontSource.GOOGLE
                query = family
                fontLoaded()
                status = null
            }.onFailure {
                status = it.message ?: "Failed"
            }
        }
    }

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                FontManager.importLocalFont(ctx, uri)
                    .onSuccess {
                        applyFont(current.copy(source = FontSource.FILE, localFontFile = it))
                        tab = FontSource.FILE
                        fontLoaded()
                        status = null
                    }
                    .onFailure { status = it.message }
            }
        }
    }

    // Make sure a chosen Google font is on the phone (e.g. after reinstalling).
    LaunchedEffect(resetKey) {
        if (spec.source == FontSource.GOOGLE && spec.googleFamily.isNotBlank() &&
            !FontManager.isGoogleDownloaded(ctx, spec.googleFamily, spec.weight)
        ) fetch(spec.googleFamily)
    }

    Column {
        Segmented(FontSource.entries, tab, { it.label }) { choice ->
            tab = choice
            status = null
            when (choice) {
                FontSource.SYSTEM -> applyFont(current.copy(source = choice))
                FontSource.GOOGLE -> {
                    val fam = current.googleFamily
                    if (fam.isBlank()) {
                        // Nothing chosen yet: just show the search box.
                    } else if (FontManager.isGoogleDownloaded(ctx, fam, current.weight)) {
                        applyFont(current.copy(source = choice))
                    } else {
                        fetch(fam)
                    }
                }
                FontSource.FILE ->
                    if (current.localFontFile.isNotEmpty()) applyFont(current.copy(source = choice))
                    else pick.launch(arrayOf("*/*"))
            }
        }
        Spacer(Modifier.height(14.dp))

        AnimatedContent(
            targetState = tab,
            transitionSpec = { (fadeIn(tween(220, delayMillis = 60)) togetherWith fadeOut(tween(120))) using SizeTransform(clip = false) },
            label = "fontTab",
        ) { shown ->
        when (shown) {
            FontSource.SYSTEM -> ChipFlow {
                FontManager.systemFamilies.forEach { (fam, label) ->
                    Chip(
                        selected = spec.source == FontSource.SYSTEM && spec.systemFamily == fam,
                        onClick = { applyFont(current.copy(source = FontSource.SYSTEM, systemFamily = fam)) },
                        label = { Text(label) },
                    )
                }
            }

            FontSource.GOOGLE -> Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Search Google Fonts") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (downloading) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { fetch(query) }, enabled = query.isNotBlank()) {
                                Icon(Icons.Default.Check, contentDescription = "Use this font")
                            }
                        }
                    },
                    supportingText = if (status != null) { { Text(status ?: "") } } else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { fetch(query) }),
                    modifier = Modifier.fillMaxWidth(),
                )
                val matches = remember(query, catalogueVersion) { FontManager.suggest(ctx, query) }
                val typing = query.isNotBlank() && !query.trim().equals(spec.googleFamily, ignoreCase = true)
                // Fonts you've searched for come first (newest first, up to 50), then the curated
                // picks in their fixed order. Curated picks are never stored as recents, so never moved.
                val recents = remember(recentKey) {
                    FontManager.recentFonts(ctx)
                        .filterNot { r -> FontManager.googleSuggestions.any { it.equals(r, ignoreCase = true) } }
                }
                val everyday = remember(recents) { recents + FontManager.googleSuggestions }
                val chips = if (typing && matches.isNotEmpty()) matches else everyday
                Spacer(Modifier.height(8.dp))
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    // Fresh scroll position whenever the list switches between matches and popular picks.
                    val rowState = remember(chips) { LazyListState() }
                    LazyRow(state = rowState, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(chips) { fam ->
                            Chip(
                                selected = spec.source == FontSource.GOOGLE && spec.googleFamily.equals(fam, ignoreCase = true),
                                onClick = { query = fam; fetch(fam) },
                                label = { Text(fam) },
                            )
                        }
                    }
                }
                TextButton(
                    onClick = { showBrowser = true },
                    contentPadding = PaddingValues(horizontal = 0.dp),
                ) { Text("Browse all fonts") }
                if (showBrowser) {
                    FontBrowserSheet(
                        selected = if (spec.source == FontSource.GOOGLE) spec.googleFamily else "",
                        onPick = { fam -> showBrowser = false; query = fam; fetch(fam) },
                        onDismiss = { showBrowser = false },
                    )
                }
            }

            FontSource.FILE -> Column {
                OutlinedButton(onClick = { pick.launch(arrayOf("*/*")) }) {
                    Text(if (spec.localFontFile.isEmpty()) "Choose .ttf / .otf" else "Change file")
                }
                status?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = scheme.error)
                }
            }
        }
        }

        Reveal(tab != FontSource.FILE && showWeight) {
            Spacer(Modifier.height(10.dp))
            LabeledSlider(
                label = "Weight",
                value = spec.weight,
                range = 100..900,
                steps = 7,
                valueText = "${spec.weight}",
                onChange = { onPreview(current.copy(weight = (it / 100) * 100)) },
                onDone = {
                    val f = current
                    if (f.source == FontSource.GOOGLE && !FontManager.isGoogleDownloaded(ctx, f.googleFamily, f.weight)) {
                        fetch(f.googleFamily, f.weight)
                    } else {
                        applyFont(f)
                    }
                },
            )
        }
    }
}

private fun openUrl(ctx: Context, url: String) {
    runCatching {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/** Material 3 single-choice segmented buttons (selected segment gets the animated check + fill). */
@Composable
private fun <T> Segmented(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { i, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = i, count = options.size),
            ) { Text(label(option), maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

/** Settings that come and go expand + fade instead of popping in. */
@Composable
private fun Reveal(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220, delayMillis = 40)) + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
        exit = fadeOut(tween(140)) + shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)),
    ) {
        Column { content() }
    }
}

/** Material filter chip: selected chips show a leading check. */
@Composable
private fun Chip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else {
            null
        },
    )
}

/** The live widget preview on a wallpaper-like gradient; tap toggles a light/dark backdrop. */
@Composable
private fun PreviewCard(
    cfg: WidgetConfig,
    size: WidgetUpdater.SizePx,
    tick: Int,
    fontVersion: Int,
    light: Boolean,
    onToggleLight: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
) {
    val ctx = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    // Wallpaper gradient blends between dark and light when tapped.
    val g1 by animateColorAsState(if (light) Color(0xFFE9E4DA) else scheme.primaryContainer, tween(450), label = "g1")
    val g2 by animateColorAsState(if (light) Color(0xFFC9D6E3) else Color(0xFF1A1C22), tween(450), label = "g2")
    val g3 by animateColorAsState(if (light) Color(0xFFF3E1D3) else scheme.tertiaryContainer, tween(450), label = "g3")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(g1, g2, g3)))
            .clickable(onClick = onToggleLight),
        contentAlignment = Alignment.Center,
    ) {
        // Crossfade when the style changes; other edits update instantly.
        Crossfade(targetState = cfg.style, animationSpec = tween(320), label = "previewStyle") { st ->
            val preview = remember(cfg, st, size, tick, fontVersion) {
                ClockRenderer.render(ctx, cfg.copy(style = st), size.w, size.h, size.dp).asImageBitmap()
            }
            Image(
                bitmap = preview,
                contentDescription = "Preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(14.dp),
            )
        }
        Icon(
            if (light) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
            contentDescription = if (light) "Light backdrop" else "Dark backdrop",
            tint = if (light) Color(0x99000000) else Color(0xB3FFFFFF),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .clip(CircleShape)
                .background(if (light) Color(0x1A000000) else Color(0x26FFFFFF))
                .padding(6.dp)
                .size(16.dp),
        )
    }
}
