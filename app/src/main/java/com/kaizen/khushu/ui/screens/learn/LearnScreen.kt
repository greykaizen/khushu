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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import com.kaizen.khushu.ui.components.KhushuAppBar
import com.kaizen.khushu.ui.navigation.AppDestinations
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

import com.kaizen.khushu.ui.theme.prayerCardPalette
import com.kaizen.khushu.ui.theme.duaCardPalette

@Composable
fun LearnScreen(
    onSectionTap: (String) -> Unit,
    onSettingsClick: () -> Unit,
    hazeState: HazeState,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().haze(state = hazeState),
            contentPadding = PaddingValues(
                start = 0.dp, end = 0.dp,
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
        ) {
            item(key = "search") {
                Spacer(modifier = Modifier.height(16.dp))
                SearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    modifier = Modifier.padding(
                        start = 20.dp, end = 20.dp,
                        top = 16.dp, bottom = 8.dp,
                    ),
                )
            }

            item(key = "prayers_header") {
                SectionRow(
                    title = "Prayers",
                    onArrowClick = { onSectionTap("Prayers") },
                    modifier = Modifier.padding(start = 20.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
                )
            }

            item(key = "prayers_row") {
                val pagerState = rememberPagerState(pageCount = { prayerCardPalette.size })
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
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
                    val (title, color) = prayerCardPalette[page]
                    LearnCard(title = title, color = color, modifier = Modifier.fillMaxWidth())
                }
            }

            item(key = "spacer") { Spacer(Modifier.height(8.dp)) }

            item(key = "duas_header") {
                SectionRow(
                    title = "Duas",
                    onArrowClick = { onSectionTap("Duas") },
                    modifier = Modifier.padding(start = 20.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
                )
            }

            item(key = "duas_row") {
                val pagerState = rememberPagerState(pageCount = { duaCardPalette.size })
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
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
                    val (title, color) = duaCardPalette[page]
                    LearnCard(title = title, color = color, modifier = Modifier.fillMaxWidth())
                }
            }

            item(key = "bottom_spacer") { Spacer(Modifier.height(32.dp)) }
        }

        KhushuAppBar(
            title = AppDestinations.LEARN.label,
            onSettingsClick = onSettingsClick,
//            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.TopCenter),
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
            color = MaterialTheme.colorScheme.onBackground,
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
