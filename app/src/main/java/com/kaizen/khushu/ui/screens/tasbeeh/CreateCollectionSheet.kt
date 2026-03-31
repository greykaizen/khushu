package com.kaizen.khushu.ui.screens.tasbeeh

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.data.DhikrItem
import com.kaizen.khushu.data.TasbeehCollection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableColumn

// Immutable fields — all mutations go through .copy() on the snapshot list.
// 'var' on data class fields causes missed recompositions; 'val' + list write is correct.
private data class DhikrRow(
    val id: Int,
    val name: String = "",
    val count: String = "",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateCollectionSheet(
    viewModel: TasbeehViewModel,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    // Single BackHandler owns the full back-press hierarchy for this sheet.
    // The sheet's own back handling is disabled (shouldDismissOnBackPress = false) so
    // the predictive-back system animation never races against this handler.
    //   • IME visible → collapse keyboard only; sheet stays open.
    //   • IME gone   → animate sheet closed then invoke onDismiss.
    BackHandler {
        if (isImeVisible) {
            focusManager.clearFocus()
        } else {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        }
    }

    var title by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    val dhikrRows = remember { mutableStateListOf(DhikrRow(id = 0)) }
    var nextId by remember { mutableIntStateOf(1) }
    // ID of the row that should steal focus after being added; null when no pending focus.
    var pendingFocusId by remember { mutableStateOf<Int?>(null) }

    val selectedColor = TasbeehPastelColors[selectedColorIndex]
    val canSave = dhikrRows.any { it.name.isNotBlank() && it.count.toIntOrNull() != null }

    // ── Hoisted out of the list scope ─────────────────────────────────────────
    // TextFieldDefaults.colors is a composable call — hoisting prevents a fresh
    // allocation on every recomposition of every row.
    val transparentColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
    ) {
        // ── Layout ────────────────────────────────────────────────────────────
        // fillMaxHeight(0.95f) artificially props the container open so the sheet
        // has the runway to snap fully to the top — without it the sheet stalls at
        // whatever the natural content height happens to be.
        // verticalScroll handles content overflow at any expansion level.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {

            // ── Header ────────────────────────────────────────────────────────
            Text(
                text = "New Tasbih",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(20.dp))

            // ── Title field ───────────────────────────────────────────────────
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tasbih name") },
                placeholder = { Text("Optional") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            // ── Color picker ──────────────────────────────────────────────────
            Text(
                text = "Color",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(TasbeehPastelColors) { index, color ->
                    val isSelected = index == selectedColorIndex
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 2.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape,
                                ) else Modifier
                            )
                            .clickable { selectedColorIndex = index },
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Dhikr section ─────────────────────────────────────────────────
            Text(
                text = "Dhikr Items",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))

            // Premium inset grouped list:
            //  • Rounded corners + surface background live on the ReorderableColumn wrapper.
            //  • Individual rows are flat / transparent.
            //  • Rows separated by a 1dp HorizontalDivider (inset-style).
            //  • Dragging a row applies shadow + tonal surface shift for a "lift" effect.
            // key(dhikrRows.size) forces the ReorderableColumn to fully dispose and
            // recreate whenever the list grows or shrinks. Without this, the library's
            // internal index-based position tracking goes stale on add/remove, causing
            // an out-of-bounds crash during the recomposition. Reordering (same size)
            // never triggers recreation, so drag-and-drop is unaffected.
            key(dhikrRows.size) {
            ReorderableColumn(
                list = dhikrRows,
                onSettle = { from, to ->
                    dhikrRows.add(to, dhikrRows.removeAt(from))
                },
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) { index, row, isDragging ->
                key(row.id) {
                    // Each row gets its own FocusRequester. When pendingFocusId matches,
                    // LaunchedEffect fires once and requests focus on this row's name field.
                    val focusRequester = remember { FocusRequester() }
                    if (pendingFocusId == row.id) {
                        LaunchedEffect(row.id) {
                            // Delay until the TextField has completed layout and attached
                            // itself to the focus tree. Calling requestFocus() before layout
                            // throws IllegalStateException (FocusRequester not initialized).
                            delay(50)
                            try { focusRequester.requestFocus() } catch (_: IllegalStateException) {}
                            pendingFocusId = null
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isDragging)
                                        Modifier.shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(12.dp),
                                            ambientColor = MaterialTheme.colorScheme.primary
                                                .copy(alpha = 0.15f),
                                            spotColor = MaterialTheme.colorScheme.primary
                                                .copy(alpha = 0.25f),
                                        )
                                    else Modifier
                                )
                                .background(
                                    if (isDragging)
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    else
                                        Color.Transparent,
                                )
                                .padding(horizontal = 16.dp),
                        ) {
                            // Name field — focusRequester applied so auto-focus works on add.
                            // indexOfFirst locates the row by stable ID before mutating,
                            // preventing the stale-index crash when items have been reordered.
                            TextField(
                                value = row.name,
                                onValueChange = { new ->
                                    val i = dhikrRows.indexOfFirst { it.id == row.id }
                                    if (i != -1) dhikrRows[i] = dhikrRows[i].copy(name = new)
                                },
                                placeholder = { Text("Dhikr name") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next,
                                ),
                                colors = transparentColors,
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                            )
                            Spacer(Modifier.width(4.dp))
                            // Count field — same safe mutation pattern.
                            TextField(
                                value = row.count,
                                onValueChange = { new ->
                                    if (new.length <= 4) {
                                        val i = dhikrRows.indexOfFirst { it.id == row.id }
                                        if (i != -1) dhikrRows[i] = dhikrRows[i].copy(count = new)
                                    }
                                },
                                placeholder = { Text("33") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                ),
                                colors = transparentColors,
                                modifier = Modifier.width(72.dp),
                            )
                            // Drag handle
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Drag to reorder",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(20.dp)
                                    .draggableHandle(),
                            )
                        }

                        if (index < dhikrRows.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }
            }
            } // end key(dhikrRows.size)

            Spacer(Modifier.height(4.dp))

            // ── Add Dhikr ─────────────────────────────────────────────────────
            // Setting pendingFocusId triggers LaunchedEffect in the new row's key block,
            // requesting keyboard focus immediately without the user having to tap.
            TextButton(
                onClick = {
                    val newId = nextId++
                    dhikrRows.add(DhikrRow(id = newId))
                    pendingFocusId = newId
                },
                modifier = Modifier.align(Alignment.Start),
            ) { Text("+ Add Dhikr") }

            Spacer(Modifier.height(20.dp))

            // ── Save ──────────────────────────────────────────────────────────
            Button(
                onClick = {
                    val items = dhikrRows
                        .filter { it.name.isNotBlank() && it.count.toIntOrNull() != null }
                        .map { DhikrItem(name = it.name.trim(), targetCount = it.count.toInt()) }
                    val collection = TasbeehCollection(
                        title = title.trim().ifBlank { null },
                        colorInt = selectedColor.toArgb(),
                        items = items,
                    )
                    scope.launch {
                        viewModel.insert(collection)
                        sheetState.hide()
                        onDismiss()
                    }
                },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) { Text("Save") }
        }
    }
}
