package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.SavedArtworkEntity
import com.example.model.AchievementBadge
import com.example.model.toComposeColor
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldStar

@Composable
fun AchievementsScreen(
    savedArtworks: List<SavedArtworkEntity>,
    achievements: List<AchievementBadge>,
    modifier: Modifier = Modifier
) {
    val totalColoredRegions = remember(savedArtworks) {
        savedArtworks.sumOf { it.coloredCount }
    }
    val completedCount = remember(savedArtworks) {
        savedArtworks.count { it.isCompleted }
    }
    val totalStars = remember(savedArtworks) {
        savedArtworks.sumOf { it.starsEarned }
    }
    val totalMinutes = remember(savedArtworks) {
        (savedArtworks.sumOf { it.elapsedSeconds } / 60L).toInt()
    }
    val unlockedBadgesCount = remember(achievements) {
        achievements.count { it.isUnlocked }
    }

    val artistTitle = when {
        totalColoredRegions >= 120 -> "Başkalfa Nakkaş"
        totalColoredRegions >= 50 -> "Usta İllüstratör"
        totalColoredRegions >= 15 -> "Kalfa Ressam"
        else -> "Yetenekli Çırak"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("achievements_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Sanatçı Profili & Başarılar",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Atölyedeki ustalığınızı, istatistiklerinizi ve kazandığınız rozetleri takip edin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Artist Rank Hero Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E1B2E),
                                    Color(0xFF3A2449),
                                    Color(0xFFE85D4A)
                                )
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.16f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = GoldStar,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SANATÇI UNVANI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFD166),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = artistTitle,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "$unlockedBadgesCount / ${achievements.size} Rozet Açıldı",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2DCEB)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // 4 Studio Metrics Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricPill(
                                value = "$totalColoredRegions",
                                label = "Boyanan Bölge",
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                value = "$completedCount",
                                label = "Biten Eser",
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                value = "$totalStars ★",
                                label = "Altın Yıldız",
                                modifier = Modifier.weight(1f)
                            )
                            MetricPill(
                                value = "${totalMinutes}dk",
                                label = "Atölye Süresi",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 2. Achievement Badges Section
        item {
            Text(
                text = "Sanatçı Rozetleri",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(achievements, key = { it.id }) { badge ->
            val fraction = (badge.progress.toFloat() / badge.target.coerceAtLeast(1)).coerceIn(0f, 1f)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("achievement_card_${badge.id}"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (badge.isUnlocked) 4.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (badge.isUnlocked) badge.accentColorArgb.toComposeColor()
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (badge.isUnlocked) Icons.Default.Verified else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (badge.isUnlocked) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = badge.titleTr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${badge.progress}/${badge.target}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (badge.isUnlocked) EmeraldSuccess
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = badge.descriptionTr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = if (badge.isUnlocked) EmeraldSuccess
                            else badge.accentColorArgb.toComposeColor()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFE2DCEB)
            )
        }
    }
}
