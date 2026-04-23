package com.kaizen.khushu.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.data.repository.LearnRepository
import com.kaizen.khushu.ui.theme.BeVietnamPro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnSectionDetailScreen(
    sectionTitle: String,
    onBack: () -> Unit,
    onCardTap: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val titleFraction = scrollBehavior.state.collapsedFraction
    val titleFontSize = androidx.compose.ui.util.lerp(28f, 20f, titleFraction).sp
    val context = androidx.compose.ui.platform.LocalContext.current

    val section = LearnRepository.getSections(context).find { it.sectionTitle == sectionTitle }
    val sectionId = section?.id ?: ""
    val sectionColor = section?.let { androidx.compose.ui.graphics.Color(it.color) }
        ?: androidx.compose.ui.graphics.Color(0xFF3B4A6BL)
    val topics = section?.topics ?: emptyList()

    // Outer background matches the system/app background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = sectionTitle,
                            fontFamily = BeVietnamPro,
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                )
            },
        ) { paddingValues ->
            // The inner Surface creates the rounded "Card" look for the whole screen
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(topics.size) { index ->
                        val topic = topics[index]
                        LearnCard(
                            title = topic.title,
                            subtitle = topic.arabicText,
                            color = sectionColor,
                            sectionId = sectionId,
                            shape = RoundedCornerShape(28.dp), // Consistent card rounding
                            onClick = { onCardTap(topic.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}
