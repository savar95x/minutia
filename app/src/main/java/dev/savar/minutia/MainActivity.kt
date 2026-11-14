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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

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

private fun widgetLabel(ctx: Context, id: Int, index: Int): String {
    val s = WidgetUpdater.size(ctx, id)
    return "Widget ${index + 1}  ·  ${s.wDp}×${s.hDp}dp"
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

    // Google Fonts name list for type-ahead (shared by the clock and date font editors).
    var catalogueVersion by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        FontManager.loadCatalogue(ctx)
        catalogueVersion++
    }
    var showAppPicker by remember { mutableStateOf(false) }
    var nameHint by remember { mutableStateOf(false) }
    var resetCount by remember { mutableIntStateOf(0) }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
            Text(
                "Minutia",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp),
            )

            // ---------- live preview ----------
            val size = remember(selectedId, widgetIds) { WidgetUpdater.size(ctx, selectedId) }
            val preview = remember(cfg, size, minuteTick, fontVersion) {
                ClockRenderer.render(ctx, cfg, size.w, size.h, size.dp).asImageBitmap()
            }
            val scheme = MaterialTheme.colorScheme
            val bgBrush = if (lightPreview) {
                Brush.linearGradient(listOf(Color(0xFFE9E4DA), Color(0xFFC9D6E3), Color(0xFFF3E1D3)))
            } else {
                Brush.linearGradient(listOf(scheme.primaryContainer, Color(0xFF1A1C22), scheme.tertiaryContainer))
            }
            Box(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(bgBrush)
                    .clickable { lightPreview = !lightPreview },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    bitmap = preview,
                    contentDescription = "Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                )
                Text(
                    if (lightPreview) "light wallpaper · tap" else "dark wallpaper · tap",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (lightPreview) Color(0x99000000) else Color(0x99FFFFFF),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                )
            }

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ---------- which widget ----------
                val pinSupported = remember { AppWidgetManager.getInstance(ctx).isRequestPinAppWidgetSupported }
                if (widgetIds.isEmpty()) {
                    Section("Get started") {
                        Text(
                            "No clock on your home screen yet. Style it here, then add it — it'll use these settings.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        if (pinSupported) {
                            Button(onClick = {
                                AppWidgetManager.getInstance(ctx).requestPinAppWidget(WidgetUpdater.component(ctx), null, null)
                            }) { Text("Add to home screen") }
                        }
                        Text(
                            "Or: long-press your home screen → Widgets → Minutia.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                } else if (widgetIds.size > 1 || pinSupported) {
                    Section("Editing") {
                        ChipFlow {
                            widgetIds.forEachIndexed { i, id ->
                                FilterChip(
                                    selected = id == selectedId,
                                    onClick = { selectedId = id },
                                    label = { Text(widgetLabel(ctx, id, i)) },
                                )
                            }
                            if (pinSupported) {
                                TextButton(onClick = {
                                    AppWidgetManager.getInstance(ctx).requestPinAppWidget(WidgetUpdater.component(ctx), null, null)
                                }) { Text("+ Add another") }
                            }
                        }
                        if (pinSupported && selectedId >= 0) {
                            val labeled = remember(selectedId, widgetIds) { WidgetUpdater.isLabeled(ctx, selectedId) }
                            SwitchRow("Show name under widget", labeled) { want ->
                                // A widget's name is fixed when it's placed, so swap in the other variant.
                                ConfigStore.save(ctx, selectedId, cfg)
                                val target = if (want) WidgetUpdater.labeledComponent(ctx) else WidgetUpdater.component(ctx)
                                AppWidgetManager.getInstance(ctx).requestPinAppWidget(target, null, null)
                                nameHint = true
                            }
                            if (nameHint) {
                                Text(
                                    "Android fixes a widget's name when it's placed, so add the new one, then remove the old one. Your style carries over.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // ---------- style ----------
                Section("Style") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(ClockStyle.entries.toList()) { style ->
                            val thumb = remember(cfg, style, minuteTick, fontVersion) {
                                val d = ctx.resources.displayMetrics.density
                                ClockRenderer.render(
                                    ctx, cfg.copy(style = style, sizePercent = 100),
                                    (150 * d).toInt(), (84 * d).toInt(), d * 0.6f,
                                ).asImageBitmap()
                            }
                            val selected = style == cfg.style
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier
                                        .size(width = 150.dp, height = 84.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Brush.linearGradient(listOf(scheme.primaryContainer, Color(0xFF1A1C22))))
                                        .border(
                                            BorderStroke(if (selected) 3.dp else 0.dp, if (selected) scheme.primary else Color.Transparent),
                                            RoundedCornerShape(18.dp),
                                        )
                                        .clickable { update(cfg.copy(style = style)) },
                                ) {
                                    Image(thumb, style.label, Modifier.fillMaxSize().padding(6.dp), contentScale = ContentScale.Fit)
                                }
                                Text(
                                    style.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) scheme.primary else scheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }

                // ---------- fonts ----------
                val hasDateLine = cfg.showDate || (!cfg.use24h && cfg.showAmPm && cfg.style != ClockStyle.ANALOG)
                if (cfg.style != ClockStyle.ANALOG || hasDateLine) {
                    Section("Font") {
                        if (cfg.style != ClockStyle.ANALOG) {
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
                        if (hasDateLine) {
                            if (cfg.style != ClockStyle.ANALOG) {
                                HorizontalDivider(Modifier.padding(vertical = 14.dp), color = scheme.outlineVariant)
                            }
                            Text("Date font", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                                val modes = DateFontMode.entries.filter {
                                    it != DateFontMode.CLOCK || cfg.style != ClockStyle.ANALOG
                                }
                                modes.forEachIndexed { i, m ->
                                    SegmentedButton(
                                        selected = cfg.dateFontMode == m,
                                        onClick = { update(cfg.copy(dateFontMode = m)) },
                                        shape = SegmentedButtonDefaults.itemShape(index = i, count = modes.size),
                                        icon = {},
                                    ) { Text(m.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                }
                            }
                            if (cfg.dateFontMode == DateFontMode.CUSTOM) {
                                Spacer(Modifier.height(14.dp))
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
                }

                // ---------- colours ----------
                Section("Colours") {
                    val canDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    SwitchRow("Match wallpaper (Material You)", cfg.useWallpaperColors && canDynamic, enabled = canDynamic) {
                        update(cfg.copy(useWallpaperColors = it))
                    }
                    if (!cfg.useWallpaperColors || !canDynamic) {
                        ColorPicker("Main colour", cfg.primaryColor) { update(cfg.copy(primaryColor = it)) }
                        ColorPicker("Accent colour", cfg.accentColor) { update(cfg.copy(accentColor = it)) }
                    }
                    SwitchRow("Shadow (helps on bright wallpapers)", cfg.shadow) { update(cfg.copy(shadow = it)) }
                }

                // ---------- time & date ----------
                Section("Time & date") {
                    SwitchRow("24-hour time", cfg.use24h) { update(cfg.copy(use24h = it)) }
                    SwitchRow("Leading zero (09:05)", cfg.leadingZero) { update(cfg.copy(leadingZero = it)) }
                    if (!cfg.use24h) SwitchRow("Show AM / PM", cfg.showAmPm) { update(cfg.copy(showAmPm = it)) }
                    if (cfg.style == ClockStyle.WORDS) {
                        SwitchRow("All lowercase words", cfg.lowercaseWords) { update(cfg.copy(lowercaseWords = it)) }
                    }
                    SwitchRow("Show date", cfg.showDate) { update(cfg.copy(showDate = it)) }
                    if (cfg.showDate) {
                        val now = remember(minuteTick) { Date() }
                        val isPreset = cfg.dateFormat in DateFormats.PRESETS
                        var customOpen by remember(selectedId, resetCount) { mutableStateOf(!isPreset) }
                        var customText by remember(selectedId, resetCount) { mutableStateOf(if (isPreset) "" else cfg.dateFormat) }
                        ChipFlow {
                            DateFormats.PRESETS.forEach { f ->
                                val sample = remember(f, now) { SimpleDateFormat(f, Locale.getDefault()).format(now) }
                                FilterChip(
                                    selected = !customOpen && cfg.dateFormat == f,
                                    onClick = { customOpen = false; update(cfg.copy(dateFormat = f)) },
                                    label = { Text(sample) },
                                )
                            }
                            FilterChip(
                                selected = customOpen,
                                onClick = {
                                    customOpen = true
                                    if (customText.isBlank()) customText = cfg.dateFormat
                                },
                                label = { Text("Custom…") },
                            )
                        }
                        if (customOpen) {
                            val fmt = remember(customText) { DateFormats.formatter(customText) }
                            val shows = remember(fmt, now) { fmt?.format(now) }
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = customText,
                                onValueChange = {
                                    customText = it
                                    if (DateFormats.formatter(it) != null) update(cfg.copy(dateFormat = it))
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                label = { Text("Date pattern") },
                                placeholder = { Text("EEE, d MMM  or  %a, %d %b") },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                                isError = customText.isNotBlank() && fmt == null,
                                supportingText = {
                                    Text(
                                        when {
                                            customText.isBlank() -> "Type a pattern, e.g. EEEE d MMMM"
                                            fmt == null -> "Android doesn't know one of these codes"
                                            else -> "Shows: $shows"
                                        },
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            val cheats = remember(now) {
                                DateFormats.CHEATSHEET.joinToString("   ") { code ->
                                    "$code → ${SimpleDateFormat(code, Locale.getDefault()).format(now)}"
                                }
                            }
                            Text(
                                "$cheats\nWords go in 'quotes'. % codes like %a %d %b %Y work too.",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                            Row {
                                TextButton(
                                    onClick = { openUrl(ctx, DateFormats.PATTERN_GUIDE_URL) },
                                    contentPadding = PaddingValues(horizontal = 0.dp),
                                ) { Text("Pattern guide") }
                                Spacer(Modifier.width(20.dp))
                                TextButton(
                                    onClick = { openUrl(ctx, DateFormats.STRFTIME_GUIDE_URL) },
                                    contentPadding = PaddingValues(horizontal = 0.dp),
                                ) { Text("% code guide") }
                            }
                        }
                        SwitchRow("All caps", cfg.uppercaseDate) { update(cfg.copy(uppercaseDate = it)) }
                    }
                }

                // ---------- layout ----------
                Section("Size & layout") {
                    LabeledSlider("Size", cfg.sizePercent, 20..100, 0, "${cfg.sizePercent}%",
                        onChange = { setLocal(cfg.copy(sizePercent = it)) }, onDone = { commit() })
                    if (cfg.style != ClockStyle.ANALOG) {
                        LabeledSlider("Letter spacing", cfg.letterSpacing, -12..24, 0, "${cfg.letterSpacing}",
                            onChange = { setLocal(cfg.copy(letterSpacing = it)) }, onDone = { commit() })
                    }
                    Text("Alignment", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 6.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        val all = HAlign.entries
                        all.forEachIndexed { i, a ->
                            SegmentedButton(
                                selected = cfg.align == a,
                                onClick = { update(cfg.copy(align = a)) },
                                shape = SegmentedButtonDefaults.itemShape(index = i, count = all.size),
                                icon = {}, // selection shown by colour only, like the font rows
                            ) { Text(a.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    SwitchRow(if (cfg.style == ClockStyle.ANALOG) "Filled clock face" else "Background card", cfg.background) {
                        update(cfg.copy(background = it))
                    }
                    if (cfg.background) {
                        if (!cfg.useWallpaperColors || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                            ColorPicker("Background colour", cfg.bgColor) { update(cfg.copy(bgColor = it)) }
                        }
                        LabeledSlider("Background opacity", cfg.bgOpacity, 0..100, 0, "${cfg.bgOpacity}%",
                            onChange = { setLocal(cfg.copy(bgOpacity = it)) }, onDone = { commit() })
                        if (cfg.style != ClockStyle.ANALOG) {
                            LabeledSlider("Corner radius", cfg.cornerRadius, 0..48, 0, "${cfg.cornerRadius}dp",
                                onChange = { setLocal(cfg.copy(cornerRadius = it)) }, onDone = { commit() })
                        }
                    }
                }

                // ---------- tap ----------
                Section("When tapped") {
                    ChipFlow {
                        TapAction.entries.forEach { a ->
                            FilterChip(
                                selected = cfg.tapAction == a,
                                onClick = {
                                    if (a == TapAction.APP) showAppPicker = true else update(cfg.copy(tapAction = a))
                                },
                                label = { Text(a.label) },
                            )
                        }
                    }
                    if (cfg.tapAction == TapAction.APP && cfg.tapPackage.isNotEmpty()) {
                        val appName = remember(cfg.tapPackage) { appLabel(ctx, cfg.tapPackage) }
                        Text(
                            "Opens $appName",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
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

                // ---------- reliability ----------
                Section("Keep it on time") {
                    Text(
                        if (ignoringBattery) "Battery optimisation is off for this app, so the clock won't get frozen. ✓"
                        else "OnePlus phones sometimes freeze widgets to save battery. Allow this app to run in the background so the time never lags.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (!ignoringBattery) {
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            runCatching {
                                ctx.startActivity(
                                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}"))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                )
                            }
                        }) { Text("Allow background") }
                    }
                }

                TextButton(onClick = { update(WidgetConfig()); resetCount++ }) {
                    Text("Reset to defaults")
                }
                val version = remember {
                    runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: ""
                }
                Text(
                    "Minutia $version",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            content()
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        )
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
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
        title = { Text("Open which app?") },
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

    fun fetch(name: String, weight: Int = current.weight) {
        val typed = name.trim()
        if (typed.isEmpty() || downloading) return
        downloading = true
        status = "Downloading $typed…"
        scope.launch {
            val r = FontManager.downloadGoogleFont(ctx, typed, weight)
            downloading = false
            r.onSuccess { family ->
                FontManager.addRecent(ctx, family)
                applyFont(current.copy(source = FontSource.GOOGLE, googleFamily = family, weight = weight))
                tab = FontSource.GOOGLE
                query = family
                fontLoaded()
                status = "Using $family"
            }.onFailure {
                status = it.message ?: "Couldn't get “$typed”."
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
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            val all = FontSource.entries
            all.forEachIndexed { i, src ->
                SegmentedButton(
                    selected = tab == src,
                    onClick = {
                        tab = src
                        status = null
                        when (src) {
                            FontSource.SYSTEM -> applyFont(current.copy(source = src))
                            FontSource.GOOGLE -> {
                                val fam = current.googleFamily
                                if (fam.isBlank()) {
                                    // Nothing chosen yet: just show the search box.
                                } else if (FontManager.isGoogleDownloaded(ctx, fam, current.weight)) {
                                    applyFont(current.copy(source = src))
                                } else {
                                    fetch(fam)
                                }
                            }
                            FontSource.FILE ->
                                if (current.localFontFile.isNotEmpty()) applyFont(current.copy(source = src))
                                else pick.launch(arrayOf("*/*"))
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = i, count = all.size),
                    icon = {},
                ) { Text(src.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
        }
        Spacer(Modifier.height(14.dp))

        when (tab) {
            FontSource.SYSTEM -> ChipFlow {
                FontManager.systemFamilies.forEach { (fam, label) ->
                    FilterChip(
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
                    placeholder = { Text("Search any Google Font") },
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
                Text(
                    when {
                        typing && matches.isNotEmpty() -> "Matching fonts"
                        recents.isNotEmpty() -> "Your recent fonts, then popular picks"
                        else -> "Popular picks"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
                )
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    // Fresh scroll position whenever the list switches between matches and popular picks.
                    val rowState = remember(chips) { LazyListState() }
                    LazyRow(state = rowState, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(chips) { fam ->
                            FilterChip(
                                selected = spec.source == FontSource.GOOGLE && spec.googleFamily.equals(fam, ignoreCase = true),
                                onClick = { query = fam; fetch(fam) },
                                label = { Text(fam) },
                            )
                        }
                    }
                }
                TextButton(
                    onClick = { openUrl(ctx, "https://fonts.google.com/") },
                    contentPadding = PaddingValues(horizontal = 0.dp),
                ) { Text("Browse all fonts on fonts.google.com") }
            }

            FontSource.FILE -> Column {
                Text(
                    if (spec.source == FontSource.FILE && spec.localFontFile.isNotEmpty()) "Using your font file."
                    else "Load any .ttf or .otf font from your phone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                status?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = scheme.error)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { pick.launch(arrayOf("*/*")) }) {
                    Text(if (spec.localFontFile.isEmpty()) "Choose font file" else "Choose another file")
                }
            }
        }

        if (tab != FontSource.FILE) {
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
            Text(
                "Some fonts only come in one weight.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

private fun openUrl(ctx: Context, url: String) {
    runCatching {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
