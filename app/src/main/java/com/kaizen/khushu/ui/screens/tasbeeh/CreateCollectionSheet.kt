package com.kaizen.khushu.ui.screens.tasbeeh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.kaizen.khushu.ui.theme.KhushuColors
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kaizen.khushu.data.model.DhikrItem
import com.kaizen.khushu.data.model.TasbeehCollection
import com.kaizen.khushu.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateCollectionSheet(
    viewModel: TasbeehViewModel,
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    // skipPartiallyExpanded = true prevents the 'half-open' state from expanding weirdly on focus
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settings by settingsViewModel.settings.collectAsState()
    
    val dhikrRows = viewModel.createDhikrRows
    val selectedColor = KhushuColors.Palette[viewModel.createColorIndex]
    val lazyListState = rememberLazyListState()

    val canSave = dhikrRows.any { it.name.trim().isNotBlank() && it.count.toIntOrNull() != null }

    val transparentColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
    )

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetCreateState()
            onDismiss()
        },
        sheetState = sheetState,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header (Sticky)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Tasbih",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            scope.launch {
                                sheetState.hide()
                                viewModel.resetCreateState()
                                onDismiss()
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 28.dp),
            ) {
                item {
                    OutlinedTextField(
                        value = viewModel.createTitle,
                        onValueChange = { viewModel.updateCreateTitle(it) },
                        label = { Text("Tasbih name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = selectedColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Spacer(Modifier.height(24.dp))
                }

                itemsIndexed(dhikrRows) { index, row ->
                    DhikrRowItem(
                        row = row,
                        index = index,
                        color = selectedColor,
                        autoFocus = viewModel.pendingFocusId == row.id,
                        transparentColors = transparentColors,
                        onNameChange = { viewModel.updateDhikrName(row.id, it) },
                        onCountChange = { viewModel.updateDhikrCount(row.id, it) },
                        onFocusConsumed = viewModel::clearPendingFocus,
                        onRemove = { viewModel.removeDhikrRow(row.id) }
                    )
                }

                item {
                    TextButton(
                        onClick = { 
                            viewModel.addDhikrRow()
                            scope.launch {
                                // Delay slightly to allow the item to be added to the list
                                kotlinx.coroutines.delay(100)
                                lazyListState.animateScrollToItem(dhikrRows.size)
                            }
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Dhikr Item")
                    }
                    Spacer(Modifier.height(100.dp)) // Buffer for keyboard
                }
            }

            // Footer (Sticky Save Button)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp,
                shadowElevation = 0.dp
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
                )
                Button(
                    onClick = {
                        val items = dhikrRows
                            .filter { it.name.isNotBlank() && it.count.toIntOrNull() != null }
                            .map { DhikrItem(name = it.name.trim(), targetCount = it.count.toInt()) }
                        val collection = TasbeehCollection(
                            title = viewModel.createTitle.trim().ifBlank { null },
                            colorInt = selectedColor.toArgb(),
                            items = items,
                        )
                        scope.launch {
                            viewModel.insert(collection)
                            viewModel.resetCreateState()
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    enabled = canSave,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 16.dp)
                        .height(52.dp),
                ) { Text("Save Tasbih") }
            }
        }
    }
}

@Composable
private fun DhikrRowItem(
    row: CreateDhikrRow,
    index: Int,
    color: Color,
    autoFocus: Boolean,
    transparentColors: androidx.compose.material3.TextFieldColors,
    onNameChange: (String) -> Unit,
    onCountChange: (String) -> Unit,
    onFocusConsumed: () -> Unit,
    onRemove: () -> Unit
) {
    val nameFocusRequester = remember { FocusRequester() }
    val latestOnFocusConsumed by rememberUpdatedState(onFocusConsumed)

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            nameFocusRequester.requestFocus()
            latestOnFocusConsumed()
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = row.name,
                onValueChange = onNameChange,
                placeholder = { Text("Dhikr name") },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(nameFocusRequester),
                colors = transparentColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                )
            )
            Spacer(Modifier.width(4.dp))
            TextField(
                value = row.count,
                onValueChange = onCountChange,
                placeholder = { Text("33") },
                modifier = Modifier.width(72.dp),
                colors = transparentColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                )
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
