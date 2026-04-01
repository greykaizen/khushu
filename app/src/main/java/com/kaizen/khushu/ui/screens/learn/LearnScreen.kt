package com.kaizen.khushu.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.unit.Density
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal val prayerCards = listOf(
    "Salah Basics" to Color(0xFF3B4A6B),
    "Surah Al-Fatiha" to Color(0xFF4A3B6B),
    "Prayer Times" to Color(0xFF3B6B4A),
    "Wudu Guide" to Color(0xFF6B4A3B),
    "Qibla Direction" to Color(0xFF3B6B6B),
    "Friday Prayer" to Color(0xFF6B3B4A),
)

internal val duaCards = listOf(
    "Morning Adhkar" to Color(0xFF4A6B3B),
    "Evening Adhkar" to Color(0xFF6B6B3B),
    "Dua After Salah" to Color(0xFF3B4A6B),
)

@Composable
fun LearnScreen(
    onSectionTap: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
    ) {
        item(key = "search") {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier.padding(
                    start = 16.dp, end = 16.dp,
                    top = 16.dp, bottom = 8.dp,
                ),
            )
        }

        item(key = "prayers_header") {
            SectionRow(
                title = "Prayers",
                onArrowClick = { onSectionTap("Prayers") },
                modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
            )
        }

        item(key = "prayers_row") {
            val pagerState = rememberPagerState(pageCount = { prayerCards.size })
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                pageSpacing = 12.dp,
                pageSize = object : PageSize {
                    override fun Density.calculateMainAxisPageSize(availableSpace: Int, pageSpacing: Int): Int {
                        return (availableSpace * 0.475f).toInt()
                    }
                },
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    pagerSnapDistance = PagerSnapDistance.atMost(2)
                )
            ) { page ->
                val (title, color) = prayerCards[page]
                LearnCard(title = title, color = color, modifier = Modifier.fillMaxWidth())
            }
        }

        item(key = "spacer") { Spacer(Modifier.height(8.dp)) }

        item(key = "duas_header") {
            SectionRow(
                title = "Duas",
                onArrowClick = { onSectionTap("Duas") },
                modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
            )
        }

        item(key = "duas_row") {
            val pagerState = rememberPagerState(pageCount = { duaCards.size })
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                pageSpacing = 12.dp,
                pageSize = object : PageSize {
                    override fun Density.calculateMainAxisPageSize(availableSpace: Int, pageSpacing: Int): Int {
                        return (availableSpace * 0.475f).toInt()
                    }
                },
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    pagerSnapDistance = PagerSnapDistance.atMost(2)
                )
            ) { page ->
                val (title, color) = duaCards[page]
                LearnCard(title = title, color = color, modifier = Modifier.fillMaxWidth())
            }
        }
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
            color = Color.White,
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
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
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
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
    )
}
