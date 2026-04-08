package com.kaizen.khushu.ui.screens.tasbeeh

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaizen.khushu.R
import com.kaizen.khushu.data.TasbeehCollection
import com.kaizen.khushu.ui.components.KhushuAppBar
import com.kaizen.khushu.ui.navigation.AppDestinations
import com.kaizen.khushu.ui.screens.settings.SettingsViewModel
import com.kaizen.khushu.ui.theme.BeVietnamPro
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun TasbeehScreen(
    viewModel: TasbeehViewModel,
    settingsViewModel: SettingsViewModel,
    onCollectionTap: (TasbeehCollection) -> Unit,
    onSettingsClick: () -> Unit,
    hazeState: HazeState,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    val collections by viewModel.collections.collectAsStateWithLifecycle(initialValue = emptyList())
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val isListMode = settings.tasbeehListMode
    var query by remember { mutableStateOf("") }
    var selectedCollection by remember { mutableStateOf<TasbeehCollection?>(null) }

    val filtered = remember(collections, query) {
        if (query.isBlank()) collections
        else collections.filter {
            it.title?.contains(query, ignoreCase = true) == true ||
                    it.items.any { item -> item.name.contains(query, ignoreCase = true) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isListMode) 1 else 2),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = contentPadding.calculateTopPadding(),
                end = 20.dp,
                bottom = contentPadding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().haze(state = hazeState),
        ) {
            item(key = "search", span = { GridItemSpan(maxLineSpan) }) {
                SearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    modifier = Modifier.padding(top = 32.dp, bottom = 4.dp),
                )
            }

            item(key = "view_toggle", span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_toggle),
                        contentDescription = "Grid view",
                        tint = if (!isListMode) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { settingsViewModel.toggleTasbeehListMode(false) }
                            .padding(10.dp),
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_list_view),
                        contentDescription = "List view",
                        tint = if (isListMode) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { settingsViewModel.toggleTasbeehListMode(true) }
                            .padding(10.dp),
                    )
                }
            }

            itemsIndexed(
                items = filtered,
                key = { _, it -> "real_${it.id}" }
            ) { index, collection ->
                var isVisible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(150L + (minOf(index, 8) * 40L))
                    isVisible = true
                }

                val alpha by animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0f,
                    animationSpec = tween(400),
                    label = "card_alpha"
                )

                val scale by animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0.9f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    label = "card_scale"
                )

                CollectionCard(
                    collection = collection,
                    isListMode = isListMode,
                    onTap = { selectedCollection = collection },
                    modifier = Modifier.graphicsLayer {
                        this.alpha = alpha
                        this.scaleX = scale
                        this.scaleY = scale
                    }
                )
            }
        }

        KhushuAppBar(
            title = AppDestinations.TASBEEH.label,
            onSettingsClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }

    // Safely unwrap to prevent NullPointerExceptions on Dismiss/Delete
    selectedCollection?.let { collection ->
        CollectionDetailSheet(
            collection = collection,
            onDismiss = { selectedCollection = null },
            onStart = {
                onCollectionTap(collection)
                selectedCollection = null
            },
            onDelete = {
                viewModel.delete(collection)
                selectedCollection = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionDetailSheet(
    collection: TasbeehCollection,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.63f)
                .navigationBarsPadding(),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f).padding(top = 24.dp,end = 12.dp)) {
                    Text(
                        text = collection.title?.takeIf { it.isNotBlank() } ?: "Collection",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "${collection.items.size} dhikr items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(
                    onClick = onStart,
//                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
//                    contentPadding = PaddingValues(end = 18.dp, top = 12.dp),
                    modifier = Modifier.padding(end = 12.dp, top = 12.dp),
                ) {
                    Text(
//                        text = "بسم الله",
                        text = "Start",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(collection.items) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "×${item.targetCount}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Sticky delete footer
            if (showDeleteConfirm) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = false },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("Cancel") }
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("Confirm Delete") }
                }
            } else {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Delete Collection", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
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
    isListMode: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isListMode) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(collection.colorInt))
                .clickable(onClick = onTap)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (!collection.title.isNullOrBlank()) {
                    Text(
                        text = collection.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = BeVietnamPro,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                }
                Text(
                    text = "${collection.items.size} dhikr",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
                if (collection.items.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    collection.items.take(2).forEach { item ->
                        Text(
                            text = "· ${item.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (collection.items.isNotEmpty()) {
                Text(
                    text = collection.items.first().targetCount.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.18f),
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(collection.colorInt))
                .clickable(onClick = onTap)
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
}