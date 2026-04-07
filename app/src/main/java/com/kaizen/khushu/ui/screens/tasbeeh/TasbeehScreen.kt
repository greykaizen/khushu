package com.kaizen.khushu.ui.screens.tasbeeh

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaizen.khushu.data.TasbeehCollection
import com.kaizen.khushu.ui.components.KhushuAppBar
import com.kaizen.khushu.ui.navigation.AppDestinations
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

private const val PREFS_NAME = "tasbeeh_prefs"
private const val KEY_SKIP_CONFIRM = "skip_start_confirm"

@Composable
fun TasbeehScreen(
    viewModel: TasbeehViewModel,
    onCollectionTap: (TasbeehCollection) -> Unit,
    onSettingsClick: () -> Unit,
    hazeState: HazeState,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    val collections by viewModel.collections.collectAsStateWithLifecycle(initialValue = emptyList())
    var query by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<TasbeehCollection?>(null) }
    var pendingStart by remember { mutableStateOf<TasbeehCollection?>(null) }
    var dontAskAgain by remember { mutableStateOf(false) }
    val skipConfirm = remember { prefs.getBoolean(KEY_SKIP_CONFIRM, false) }

    val filtered = remember(collections, query) {
        if (query.isBlank()) collections
        else collections.filter {
            it.title?.contains(query, ignoreCase = true) == true ||
                it.items.any { item -> item.name.contains(query, ignoreCase = true) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = contentPadding.calculateTopPadding(),
                end = 20.dp,
                bottom = contentPadding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().haze(state = hazeState),
        ) {
            // Search bar — spans both columns, stays at top of scroll content
            item(key = "search", span = { GridItemSpan(2) }) {
                SearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    modifier = Modifier.padding(top = 32.dp, bottom = 8.dp),
                )
            }

            items(
                filtered,
                key = { "real_${it.id}" },
            ) { collection ->
                CollectionCard(
                    collection = collection,
                    onTap = {
                        if (skipConfirm) {
                            onCollectionTap(collection)
                        } else {
                            dontAskAgain = false
                            pendingStart = collection
                        }
                    },
                    onLongPress = { pendingDelete = collection },
                )
            }
        }

        KhushuAppBar(
            title = AppDestinations.TASBEEH.label,
            onSettingsClick = onSettingsClick,
//            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.TopCenter),
        )

    }

    // Start confirmation dialog
    pendingStart?.let { collection ->
        AlertDialog(
            onDismissRequest = { pendingStart = null },
            title = { Text("Start Tasbih?") },
            text = {
                Column {
                    val name = collection.title?.takeIf { it.isNotBlank() }
                    if (name != null) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        text = "${collection.items.size} dhikr item${if (collection.items.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = dontAskAgain,
                            onCheckedChange = { dontAskAgain = it },
                        )
                        Text(
                            text = "Don't ask again",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (dontAskAgain) prefs.edit().putBoolean(KEY_SKIP_CONFIRM, true).apply()
                    onCollectionTap(collection)
                    pendingStart = null
                }) { Text("Start") }
            },
            dismissButton = {
                TextButton(onClick = { pendingStart = null }) { Text("Cancel") }
            },
        )
    }

    // Delete confirmation dialog
    pendingDelete?.let { collection ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete collection?") },
            text = {
                val name = collection.title?.takeIf { it.isNotBlank() } ?: "This collection"
                Text("\"$name\" will be permanently deleted.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(collection)
                    pendingDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Search Tasbih",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        },
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollectionCard(
    collection: TasbeehCollection,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(collection.colorInt))
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
            .padding(12.dp),
    ) {
        Column {
            if (!collection.title.isNullOrBlank()) {
                Text(
                    text = collection.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
            }

            val displayItems = collection.items.take(3)
            displayItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                    )
                    Text(
                        text = item.targetCount.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            if (collection.items.size > 3) {
                Text(
                    text = "...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
    }
}
