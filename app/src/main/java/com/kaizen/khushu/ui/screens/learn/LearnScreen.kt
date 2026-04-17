package com.kaizen.khushu.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaizen.khushu.data.model.LearnSection
import com.kaizen.khushu.data.model.LearnTopic
import com.kaizen.khushu.ui.components.KhushuAppBar
import com.kaizen.khushu.ui.navigation.AppDestinations
import com.kaizen.khushu.ui.screens.settings.SettingsViewModel
import com.kaizen.khushu.ui.theme.BeVietnamPro
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    onSectionTap: (String) -> Unit = {},
    onCardTap: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    hazeState: HazeState,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
    learnViewModel: LearnViewModel = viewModel(),
    settingsViewModel: SettingsViewModel,
) {
    var query by remember { mutableStateOf("") }
    var showBookmarks by remember { mutableStateOf(false) }
    val sections = learnViewModel.sections
    val settings by settingsViewModel.settings.collectAsState()

    // Find last read topic
    val lastReadTopic: LearnTopic? = remember(settings.lastReadTopicId, sections) {
        settings.lastReadTopicId?.let { id ->
            sections.flatMap { it.topics }.find { it.id == id }
        }
    }

    // Filter sections and topics by search query
    val filteredSections = remember(sections, query) {
        if (query.isBlank()) sections
        else sections.mapNotNull { section ->
            val matchingTopics = section.topics.filter {
                it.title.contains(query, ignoreCase = true)
            }
            // Include section if title matches or any topic matches
            if (section.sectionTitle.contains(query, ignoreCase = true) || matchingTopics.isNotEmpty()) {
                section.copy(topics = if (section.sectionTitle.contains(query, ignoreCase = true)) section.topics else matchingTopics)
            } else null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().haze(state = hazeState),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            ),
        ) {
            item(key = "search") {
                SearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    modifier = Modifier.padding(
                        start = 20.dp, end = 20.dp,
                        top = 32.dp, bottom = 4.dp,
                    ),
                )
            }

            if (lastReadTopic != null && query.isBlank() && settings.showContinueReading) {
                item(key = "continue_reading") {
                    ContinueReadingBanner(
                        topic = lastReadTopic,
                        onClick = { onCardTap(lastReadTopic.id) },
                        onDismiss = { settingsViewModel.clearLastReadTopicId() },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }

            filteredSections.forEachIndexed { index, section ->
                if (index > 0) {
                    item(key = "spacer_$index") { Spacer(Modifier.height(8.dp)) }
                }

                item(key = "header_${section.id}") {
                    SectionRow(
                        title = section.sectionTitle,
                        onArrowClick = { onSectionTap(section.sectionTitle) },
                        modifier = Modifier.padding(
                            start = 22.dp, end = 15.dp,
                            top = 8.dp, bottom = 4.dp,
                        ),
                    )
                }

                item(key = "cards_${section.id}") {
                    SectionCards(
                        section = section,
                        onCardTap = onCardTap,
                        masteredTopicIds = settings.masteredTopicIds
                    )
                }
            }
        }

        KhushuAppBar(
            title = AppDestinations.LEARN.label,
            onSettingsClick = onSettingsClick,
            onBookmarksClick = { showBookmarks = true },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        if (showBookmarks) {
            BookmarksSheet(
                bookmarkedAyahs = settings.bookmarkedAyahs,
                sections = sections,
                onDismiss = { showBookmarks = false },
                onBookmarkTap = { topicId, ayahIndex ->
                    showBookmarks = false
                    onCardTap("$topicId?ayah=$ayahIndex")
                }
            )
        }
    }
}

@Composable
private fun SectionCards(
    section: LearnSection,
    onCardTap: (String) -> Unit,
    masteredTopicIds: Set<String>,
) {
    val sectionColor = Color(section.color)
    val pagerState = rememberPagerState(pageCount = { section.topics.size })

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        pageSpacing = 8.dp,
        pageSize = object : PageSize {
            override fun Density.calculateMainAxisPageSize(
                availableSpace: Int,
                pageSpacing: Int,
            ): Int = (availableSpace * 0.475f).toInt()
        },
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            pagerSnapDistance = PagerSnapDistance.atMost(2),
        ),
    ) { page ->
        val topic = section.topics[page]
        val isMastered = masteredTopicIds.contains(topic.id)
        LearnCard(
            title = topic.title,
            color = sectionColor,
            onClick = { onCardTap(topic.id) },
            isMastered = isMastered,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SectionRow(
    title: String,
    onArrowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(
                    onClick = onArrowClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "See all $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun LearnCard(
    title: String,
    color: Color,
    onClick: (() -> Unit)? = null,
    isMastered: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(color)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(18.dp),
    ) {
        if (isMastered) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Mastered",
                tint = Color(0xFF10B981), // Emerald
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarksSheet(
    bookmarkedAyahs: Set<String>,
    sections: List<LearnSection>,
    onDismiss: () -> Unit,
    onBookmarkTap: (String, Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val bookmarkedTopics = remember(bookmarkedAyahs, sections) {
        bookmarkedAyahs.mapNotNull { key ->
            val parts = key.split(":")
            val topicId = parts.getOrNull(0) ?: return@mapNotNull null
            val ayahIndex = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val topic = sections.flatMap { it.topics }.find { it.id == topicId }
            if (topic != null) Triple(topic, ayahIndex, key) else null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                "Bookmarks",
                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold,
                fontFamily = com.kaizen.khushu.ui.theme.BeVietnamPro,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            if (bookmarkedTopics.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No bookmarks yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookmarkedTopics) { (topic, ayahIndex, key) ->
                        BookmarkRow(
                            topic = topic,
                            ayahIndex = ayahIndex,
                            onClick = { onBookmarkTap(topic.id, ayahIndex) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkRow(
    topic: LearnTopic,
    ayahIndex: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    topic.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (ayahIndex > 0) {
                    Text(
                        "Ayah ${ayahIndex + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinueReadingBanner(
    topic: LearnTopic,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            .clickable(onClick = onClick),
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 44.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CONTINUE READING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = com.kaizen.khushu.ui.theme.BeVietnamPro,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            androidx.compose.material3.IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    modifier = Modifier.size(14.dp)
                )
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
                "Search lessons...",
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
