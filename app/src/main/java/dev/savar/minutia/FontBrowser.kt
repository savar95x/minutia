@file:OptIn(ExperimentalMaterial3Api::class)

package dev.savar.minutia

import android.graphics.Typeface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val CATEGORIES = listOf(
    "All" to null,
    "Sans" to "Sans Serif",
    "Serif" to "Serif",
    "Display" to "Display",
    "Mono" to "Monospace",
    "Handwriting" to "Handwriting",
)

/**
 * Full-height sheet listing every Google Fonts family (most popular first), searchable and filterable by
 * category. Each row previews the family in its own typeface via a few-KB subset font.
 */
@Composable
fun FontBrowserSheet(selected: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val families by produceState(FontManager.cachedFamilies(ctx)) {
        FontManager.loadCatalogue(ctx)
        value = FontManager.cachedFamilies(ctx)
    }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }
    val shown = remember(families, query, category) {
        val q = query.trim().lowercase().replace(" ", "")
        families.filter { f ->
            (category == null || f.category == category) &&
                (q.isEmpty() || f.name.lowercase().replace(" ", "").contains(q))
        }
    }
    val listState = rememberLazyListState()
    LaunchedEffect(query, category) { listState.scrollToItem(0) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxHeight(0.92f)) {
            Text(
                "Google Fonts",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("Search ${if (families.isEmpty()) "" else "${families.size} "}fonts") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(CATEGORIES) { (label, value) ->
                    val sel = category == value
                    FilterChip(
                        selected = sel,
                        onClick = { category = value },
                        label = { Text(label) },
                        leadingIcon = if (sel) {
                            { Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null,
                    )
                }
            }
            HorizontalDivider()
            if (families.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Loading fonts…", style = MaterialTheme.typography.bodyMedium)
                }
            }
            LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
                items(shown, key = { it.name }) { fam ->
                    FontRow(fam, fam.name.equals(selected, ignoreCase = true)) { onPick(fam.name) }
                }
            }
        }
    }
}

@Composable
private fun FontRow(fam: FontManager.Family, isSelected: Boolean, onClick: () -> Unit) {
    val ctx = LocalContext.current
    val tf by produceState<Typeface?>(null, fam.name) { value = FontManager.previewTypeface(ctx, fam.name) }
    val family = remember(tf) { tf?.let { FontFamily(it) } }
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                fam.name,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = family,
                color = if (family == null) scheme.onSurfaceVariant else scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (fam.category.isNotEmpty()) {
                Text(fam.category, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            FontManager.PREVIEW_TEXT,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = family,
            color = if (isSelected) scheme.primary else scheme.onSurface,
        )
        if (isSelected) {
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Check, contentDescription = "Selected", tint = scheme.primary)
        }
    }
}
