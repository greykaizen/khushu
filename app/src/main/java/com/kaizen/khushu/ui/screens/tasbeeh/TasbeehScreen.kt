package com.kaizen.khushu.ui.screens.tasbeeh
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.kaizen.khushu.data.model.CustomBeadStyle
import com.kaizen.khushu.data.model.TasbeehCanvasPresetDomain
import com.kaizen.khushu.data.repository.UserSettings
import com.kaizen.khushu.data.model.TasbeehCollection



import androidx.graphics.shapes.RoundedPolygon
import com.kaizen.khushu.R
import com.kaizen.khushu.ui.components.KhushuAppBar
import com.kaizen.khushu.ui.navigation.AppDestinations
import com.kaizen.khushu.ui.screens.settings.SettingsViewModel
import com.kaizen.khushu.ui.theme.BeVietnamPro
import com.kaizen.khushu.ui.util.add
import com.kaizen.khushu.ui.util.rememberMorphShape
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TasbeehScreen(
    viewModel: TasbeehViewModel,
    settingsViewModel: SettingsViewModel,
    canvasViewModel: TasbeehCanvasViewModel,
    onCollectionTap: (TasbeehCollection) -> Unit,
    onEditCollection: () -> Unit,
    onCustomizeCanvas: () -> Unit,
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
    
    val expressiveShape = MaterialShapes.Cookie9Sided.toShape()

    val filtered = remember(collections, query) {
        if (query.isBlank()) collections
        else collections.filter {
            it.title?.contains(query, ignoreCase = true) == true ||
                    it.items.any { item -> item.name.contains(query, ignoreCase = true) }
        }
    }
    
    // Shape morphing for view toggles
    val gridProgress by animateFloatAsState(
        targetValue = if (!isListMode) 1f else 0f,
        label = "grid_morph_progress"
    )
    val listProgress by animateFloatAsState(
        targetValue = if (isListMode) 1f else 0f,
        label = "list_morph_progress"
    )
    
    val gridMorphShape = rememberMorphShape(
        start = MaterialShapes.Circle,
        end = MaterialShapes.Cookie9Sided,
        progress = gridProgress
    )
    val listMorphShape = rememberMorphShape(
        start = MaterialShapes.Circle,
        end = MaterialShapes.Cookie9Sided,
        progress = listProgress
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isListMode) 1 else 2),
            contentPadding = contentPadding.add(start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
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
                            .clip(CircleShape)
                            .background(
                                if (!isListMode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else Color.Transparent
                            )
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
                            .clip(CircleShape)
                            .background(
                                if (isListMode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else Color.Transparent
                            )
                            .clickable { settingsViewModel.toggleTasbeehListMode(true) }
                            .padding(10.dp),
                    )
                }
            }

            itemsIndexed(
                items = filtered,
                key = { _, it -> "real_${it.id}" }
            ) { index, collection ->
                val isVisible = true 

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

                val (resolvedBg, resolvedContent) = if (settings.tasbeehDynamicColors) {
                    when (index % 3) {
                        0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                        1 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                    }
                } else {
                    Color(collection.colorInt) to Color.White
                }

                CollectionCard(
                    collection = collection,
                    isListMode = isListMode,
                    containerColor = resolvedBg,
                    contentColor = resolvedContent,
                    settings = settings,
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
        val index = collections.indexOf(collection)
        val (resolvedBg, resolvedContent) = if (settings.tasbeehDynamicColors && index != -1) {
            when (index % 3) {
                0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                1 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
            }
        } else {
            Color(collection.colorInt) to Color.White
        }

        CollectionDetailSheet(
            collection = collection,
            accentColor = if (settings.tasbeehDynamicColors) resolvedBg else MaterialTheme.colorScheme.primary,
            settingsViewModel = settingsViewModel,
            canvasViewModel = canvasViewModel,
            onDismiss = { selectedCollection = null },
            onStart = {
                onCollectionTap(collection)
                selectedCollection = null
            },
            onCustomizeCanvas = onCustomizeCanvas,
            onEdit = {
                viewModel.loadCollectionForEdit(collection)
                selectedCollection = null
                onEditCollection()
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
    accentColor: Color,
    settingsViewModel: SettingsViewModel,
    canvasViewModel: TasbeehCanvasViewModel,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onCustomizeCanvas: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showBeadCustomizer by remember { mutableStateOf(false) }
    var showPresets by remember { mutableStateOf(false) }
    var showCustomization by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val layout by canvasViewModel.layout.collectAsStateWithLifecycle()
    val presets by canvasViewModel.presets.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding(),
        ) {
            // Header (Sticky)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 28.dp, top = 30.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (showCustomization) {
                        Text(
                            text = "Back",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showCustomization = false }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    } else {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onEdit() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "Customize",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showCustomization = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                scope.launch {
                                    sheetState.hide()
                                    onDismiss()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 28.dp, end = 28.dp, bottom = 8.dp),
            ) {
                if (showCustomization) {
                    item {
                        Spacer(Modifier.height(10.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PrepPreviewCard(
                                title = "Screen UI",
                                cardHeight = 236.dp,
                                onClick = { showPresets = true }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MiniTasbihScreenPreview(
                                        collection = collection,
                                        layout = layout,
                                        settings = settings,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .padding(vertical = 10.dp)
                                            .aspectRatio(0.54f)
                                    )
                                }
                            }

                            PrepPreviewCard(
                                title = "Bead Style",
                                cardHeight = 96.dp,
                                onClick = { showBeadCustomizer = true }
                            ) {
                               val customBeadStyle = settings.customBeadStyles.find { it.id == settings.activeBeadStyleId }
                               val legacyBeadStyle = if (settings.tasbihBeadStyle == "DARK_ONYX") BeadStyle.DARK_ONYX else BeadStyle.CLASSIC_AMBER
                               val customShape = customBeadStyle?.let { beadShapeTypeToShape(it.shapeType) }
                               val noiseShader = GlobalNoiseShader.value
                               val noiseBrush = remember(noiseShader) { ShaderBrush(noiseShader) }

                               Canvas(modifier = Modifier.size(52.dp)) {
                                   val r = size.minDimension / 2f
                                   drawBeadWrapper(
                                       center = Offset(r, r),
                                       radius = r,
                                       legacyStyle = legacyBeadStyle,
                                       customStyle = customBeadStyle,
                                       customShape = customShape,
                                       noiseBrush = noiseBrush
                                   )
                               }
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                    }
                } else {
                    item {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = collection.title?.takeIf { it.isNotBlank() } ?: "Collection",
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showDeleteConfirm = true }
                                    .padding(8.dp)
                                    .padding(top = 20.dp, end = 10.dp, bottom = 0.dp)
                            )
                        }
                        Text(
                            text = "${collection.items.size} dhikr items",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "DHIKR LIST",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(collection.items) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
            }

            // Footer (Sticky)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 24.dp)
            ) {
                if (showDeleteConfirm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = false },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) { Text("Cancel", style = MaterialTheme.typography.titleSmall) }
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) { Text("Confirm Delete", style = MaterialTheme.typography.titleSmall) }
                    }
                } else {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            text = "Start Tasbih",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }

    if (showBeadCustomizer) {
        TasbihBeadCustomizerSheet(
            settingsViewModel = settingsViewModel,
            onDismiss = { showBeadCustomizer = false }
        )
    }

    if (showPresets) {
        PrepPresetsPickerSheet(
            presets = presets,
            onLoad = { canvasViewModel.loadPreset(it) },
            onCustomizeMyOwn = { 
                showPresets = false
                onDismiss()
                onCustomizeCanvas()
            },
            onDismiss = { showPresets = false }
        )
    }
}

@Composable
private fun MiniTasbihScreenPreview(
    collection: TasbeehCollection,
    layout: TasbeehCanvasLayout,
    settings: UserSettings,
    modifier: Modifier = Modifier,
) {
    val currentItem = collection.items.firstOrNull()
    val customBeadStyle = settings.customBeadStyles.find { it.id == settings.activeBeadStyleId }
    val beadStyle = if (settings.tasbihBeadStyle == "DARK_ONYX") BeadStyle.DARK_ONYX else BeadStyle.CLASSIC_AMBER

    BoxWithConstraints(
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(layout.backgroundColorInt.toLong()))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        val previewWidth = constraints.maxWidth.toFloat()
        val previewHeight = constraints.maxHeight.toFloat()
        val scaleMultiplier = 0.24f

        layout.widgets.sortedBy { it.zIndex }.forEach { widget ->
            Box(
                modifier = Modifier.graphicsLayer {
                    translationX = (widget.offsetX * previewWidth) - (size.width / 2f)
                    translationY = (widget.offsetY * previewHeight) - (size.height / 2f)
                    scaleX = widget.scale * scaleMultiplier
                    scaleY = widget.scale * scaleMultiplier
                    transformOrigin = TransformOrigin.Center
                    clip = false
                }
            ) {
                TasbihWidgetRenderer(
                    widget = widget,
                    currentCount = (currentItem?.targetCount ?: 33) / 3,
                    currentItem = currentItem,
                    countedBeads = (currentItem?.targetCount ?: 33) / 3,
                    totalBeads = currentItem?.targetCount ?: 33,
                    beadStyle = beadStyle,
                    customBeadStyle = customBeadStyle,
                )
            }
        }
    }
}

@Composable
private fun PrepPreviewCard(
    modifier: Modifier = Modifier,
    title: String,
    cardHeight: Dp = 104.dp,
    onClick: () -> Unit,
    previewContent: @Composable BoxScope.() -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
                content = previewContent
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(vertical = 6.dp), 
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title, 
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.Tune, null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrepPresetsPickerSheet(
    presets: List<TasbeehCanvasPresetDomain>,
    onLoad: (TasbeehCanvasPresetDomain) -> Unit,
    onCustomizeMyOwn: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Choose Screen Preset",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(presets) { preset ->
                    OutlinedCard(
                        onClick = { onLoad(preset); onDismiss() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(preset.name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                            Text(
                                "${preset.widgets.size} widgets", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = onCustomizeMyOwn,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Icon(Icons.Default.Tune, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Customize my own")
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
    containerColor: Color,
    contentColor: Color,
    settings: com.kaizen.khushu.data.repository.UserSettings,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customBeadStyle = settings.customBeadStyles.find { it.id == settings.activeBeadStyleId }
    val legacyBeadStyle = if (settings.tasbihBeadStyle == "DARK_ONYX") BeadStyle.DARK_ONYX else BeadStyle.CLASSIC_AMBER

    if (isListMode) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(containerColor)
                .clickable(onClick = onTap)
        ) {
            // --- Islamic Background Pattern ---
            Canvas(modifier = Modifier.size(200.dp).align(Alignment.CenterEnd).offset(x = 60.dp, y = 30.dp)) {
                val starSize = size.minDimension
                val squareSize = starSize * 0.707f
                val starColor = contentColor
                val starAlpha = 0.1f

                drawContext.canvas.save()
                drawContext.transform.rotate(0f, center)
                drawRect(
                    color = starColor,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - squareSize/2, center.y - squareSize/2),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()),
                    alpha = starAlpha
                )
                drawContext.canvas.restore()
                drawContext.canvas.save()
                drawContext.transform.rotate(45f, center)
                drawRect(
                    color = starColor,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - squareSize/2, center.y - squareSize/2),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()),
                    alpha = starAlpha
                )
                drawContext.canvas.restore()
            }

            // Text content — leaves 70dp on the right for the sidebar
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 20.dp, end = 78.dp, top = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                if (!collection.title.isNullOrBlank()) {
                    Text(
                        text = collection.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = BeVietnamPro,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = "${collection.items.size} dhikr",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.6f),
                )
                if (collection.items.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    collection.items.take(2).forEach { item ->
                        Text(
                            text = "· ${item.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // --- The Physical Sidebar (String + 3 Beads) ---
            // Placed directly in the outer Box so the string spans the full card height
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                // The String — full card height, no padding clipping
                Canvas(modifier = Modifier.fillMaxHeight().width(8.dp)) {
                    val centerX = size.width / 2f
                    // Drop shadow
                    drawLine(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height
                        ),
                        start = androidx.compose.ui.geometry.Offset(centerX + 1.5.dp.toPx(), 0f),
                        end = androidx.compose.ui.geometry.Offset(centerX + 1.5.dp.toPx(), size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                    // Main thread
                    drawLine(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                contentColor.copy(alpha = 0.4f),
                                contentColor.copy(alpha = 0.7f),
                                contentColor.copy(alpha = 0.7f),
                                contentColor.copy(alpha = 0.4f)
                            ),
                            startY = 0f,
                            endY = size.height
                        ),
                        start = androidx.compose.ui.geometry.Offset(centerX, 0f),
                        end = androidx.compose.ui.geometry.Offset(centerX, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // 3 Beads
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(3) {
                        PreviewBead(
                            baseColor = contentColor,
                            legacyStyle = legacyBeadStyle,
                            customStyle = customBeadStyle,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(containerColor)
                .clickable(onClick = onTap)
                .padding(12.dp),
        ) {
            // --- Islamic Pattern (Mini) ---
            Canvas(modifier = Modifier.size(100.dp).align(Alignment.BottomEnd).offset(x = 20.dp, y = 20.dp)) {
                val squareSize = size.minDimension * 0.707f
                drawContext.canvas.save()
                drawRect(
                    color = contentColor,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - squareSize/2, center.y - squareSize/2),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()),
                    alpha = 0.08f
                )
                drawContext.transform.rotate(45f, center)
                drawRect(
                    color = contentColor,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - squareSize/2, center.y - squareSize/2),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()),
                    alpha = 0.08f
                )
                drawContext.canvas.restore()
            }

            Column {
                if (!collection.title.isNullOrBlank()) {
                    Text(
                        text = collection.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
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
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                        )
                        Text(
                            text = item.targetCount.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = contentColor,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                if (collection.items.size > 3) {
                    Text(
                        text = "...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewBead(
    baseColor: Color,
    legacyStyle: BeadStyle,
    customStyle: com.kaizen.khushu.data.model.CustomBeadStyle?,
    modifier: Modifier = Modifier
) {
    val noiseShader = GlobalNoiseShader.value
    val noiseBrush = remember(noiseShader) { ShaderBrush(noiseShader) }
    // Card preview should borrow the active shape/effects, but keep card-driven color
    // and omit engraving so the card stays compact and legible.
    val resolvedStyle = customStyle?.copy(
        baseColor = baseColor.toArgb().toLong() and 0xFFFFFFFFL,
        engravingText = ""
    )
    val customShape: androidx.compose.ui.graphics.Shape? = if (resolvedStyle != null) {
        beadShapeTypeToShape(resolvedStyle.shapeType)
    } else null

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f

        drawBeadWrapper(
            center = androidx.compose.ui.geometry.Offset(radius, radius),
            radius = radius,
            legacyStyle = legacyStyle,
            customStyle = resolvedStyle,
            customShape = customShape,
            noiseBrush = noiseBrush
        )
    }
}
