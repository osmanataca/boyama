package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.ArtworkProgressCodec
import com.example.data.SavedArtworkEntity
import com.example.model.ArtworkTemplate
import com.example.model.TimelapseStep
import com.example.ui.canvas.ArtworkVectorPreview
import com.example.ui.canvas.CanvasHitTester
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldStar

@Composable
fun GalleryScreen(
    templates: List<ArtworkTemplate>,
    savedArtworks: List<SavedArtworkEntity>,
    onOpenStudio: (String) -> Unit,
    onWatchTimelapse: (ArtworkTemplate, List<TimelapseStep>) -> Unit,
    onDeleteArtwork: (String) -> Unit,
    onExploreTemplates: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filterMode by remember { mutableIntStateOf(0) } // 0 = Tümü, 1 = Devam Edenler, 2 = Tamamlananlar

    val activeArtworks = remember(savedArtworks, filterMode) {
        val nonEmpty = savedArtworks.filter { it.coloredCount > 0 || it.strokesEncoded.isNotBlank() }
        when (filterMode) {
            1 -> nonEmpty.filter { !it.isCompleted }
            2 -> nonEmpty.filter { it.isCompleted }
            else -> nonEmpty
        }
    }

    val templatesById = remember(templates) {
        templates.associateBy { it.id }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("gallery_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Kişisel Sanat Galerim",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Boyadığınız tüm eserleri inceleyin, zaman atlamalı (timelapse) izleyin veya paylaşın",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val labels = listOf("Tümü", "Devam Edenler", "Tamamlananlar")
                    labels.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = filterMode == idx,
                            onClick = { filterMode = idx },
                            label = { Text(label) },
                            modifier = Modifier.testTag("gallery_filter_$idx")
                        )
                    }
                }
            }
        }

        if (activeArtworks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .testTag("empty_gallery_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Galerinizde Henüz Eser Yok",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Atölyeden dilediğiniz bir mandala, vitray veya doğa illüstrasyonunu seçerek ilk eserinizi boyamaya başlayın.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onExploreTemplates,
                            modifier = Modifier.testTag("explore_studio_from_empty_gallery_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Atölyeyi Keşfet")
                        }
                    }
                }
            }
        } else {
            items(activeArtworks, key = { it.templateId }) { saved ->
                val template = templatesById[saved.templateId] ?: return@items
                GalleryArtworkCard(
                    template = template,
                    saved = saved,
                    onContinue = { onOpenStudio(template.id) },
                    onWatchTimelapse = {
                        val steps = ArtworkProgressCodec.decodeTimelapse(saved.timelapseEncoded)
                        onWatchTimelapse(template, steps)
                    },
                    onDelete = { onDeleteArtwork(template.id) }
                )
            }
        }
    }
}

@Composable
private fun GalleryArtworkCard(
    template: ArtworkTemplate,
    saved: SavedArtworkEntity,
    onContinue: () -> Unit,
    onWatchTimelapse: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val fills = remember(saved.fillsEncoded) {
        ArtworkProgressCodec.decodeFills(saved.fillsEncoded)
    }
    val strokes = remember(saved.strokesEncoded) {
        ArtworkProgressCodec.decodeStrokes(saved.strokesEncoded)
    }

    val mins = saved.elapsedSeconds / 60
    val secs = saved.elapsedSeconds % 60
    val timeLabel = if (mins > 0) "${mins}dk ${secs}sn" else "${secs}sn"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gallery_card_${template.id}"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Framed Artwork Thumbnail
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFBF8F3))
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(6.dp)
                ) {
                    ArtworkVectorPreview(
                        template = template,
                        fills = fills,
                        strokes = strokes,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Metadata & Progress
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(3) { starIdx ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (starIdx < saved.starsEarned) GoldStar
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• $timeLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = template.titleTr,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${saved.coloredCount} / ${saved.totalRegions} bölge renklendirildi (%${saved.completionPercent})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { saved.completionPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (saved.isCompleted) EmeraldSuccess else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gallery_continue_${template.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (saved.isCompleted) "Düzenle" else "Devam Et")
                }

                FilledTonalButton(
                    onClick = onWatchTimelapse,
                    modifier = Modifier.testTag("gallery_timelapse_${template.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = "Canlandır",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Canlandır")
                }

                IconButton(
                    onClick = {
                        CanvasHitTester.shareArtworkBitmap(
                            context = context,
                            template = template,
                            fills = fills,
                            strokes = strokes
                        )
                    },
                    modifier = Modifier.testTag("gallery_share_${template.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Paylaş"
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("gallery_delete_${template.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Sıfırla",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
