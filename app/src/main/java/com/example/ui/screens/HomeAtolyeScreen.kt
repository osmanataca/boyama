package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ArtworkProgressCodec
import com.example.data.SavedArtworkEntity
import com.example.model.ArtCategory
import com.example.model.ArtworkTemplate
import com.example.model.CuratedPalettes
import com.example.model.DifficultyLevel
import com.example.model.toComposeColor
import com.example.ui.canvas.ArtworkVectorPreview
import com.example.ui.canvas.CanvasHitTester
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldStar

@Composable
fun HomeAtolyeScreen(
    templates: List<ArtworkTemplate>,
    savedArtworks: List<SavedArtworkEntity>,
    selectedCategory: ArtCategory,
    selectedDifficulty: DifficultyLevel?,
    onSelectCategory: (ArtCategory) -> Unit,
    onSelectDifficulty: (DifficultyLevel?) -> Unit,
    onOpenArtwork: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedById = remember(savedArtworks) {
        savedArtworks.associateBy { it.templateId }
    }
    val totalStars = remember(savedArtworks) {
        savedArtworks.sumOf { it.starsEarned }
    }
    val totalColoredRegions = remember(savedArtworks) {
        savedArtworks.sumOf { it.coloredCount }
    }

    val filteredTemplates = remember(templates, selectedCategory, selectedDifficulty) {
        templates.filter { tpl ->
            val catMatch = selectedCategory == ArtCategory.ALL || tpl.category == selectedCategory
            val diffMatch = selectedDifficulty == null || tpl.difficulty == selectedDifficulty
            catMatch && diffMatch
        }
    }

    val dailyFeatured = remember(templates) {
        templates.find { it.isDailyFeatured } ?: templates.first()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_atolye_screen"),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Studio Header with Star & Region Counters
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ayşe'nin Resim Atölyesi",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Profesyonel Dijital Sanat ve Boyama Stüdyosu",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Studio Progress Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Toplam Yıldız",
                                tint = GoldStar,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "$totalStars",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(14.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = "Boyanan Bölge",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$totalColoredRegions",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 1b. Quick Android APK Export Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Android Kurulum Paketi (.APK)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aysenin_Resim_Atolyesi.apk dosyasını indirilenlere kaydet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedButton(
                        onClick = { CanvasHitTester.exportInstalledApkToDownloads(context) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("export_apk_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "APK İndir",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ".APK Kaydet",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Hero Daily Inspiration Banner (with generated img_studio_hero + live vector preview)
        item {
            val dailySaved = savedById[dailyFeatured.id]
            val dailyFills = remember(dailySaved) {
                dailySaved?.let { ArtworkProgressCodec.decodeFills(it.fillsEncoded) } ?: emptyMap()
            }
            val dailyStrokes = remember(dailySaved) {
                dailySaved?.let { ArtworkProgressCodec.decodeStrokes(it.strokesEncoded) } ?: emptyList()
            }

            Card(
                onClick = { onOpenArtwork(dailyFeatured.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("daily_featured_card"),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Generated Studio Hero Image Background
                    Image(
                        painter = painterResource(id = R.drawable.img_studio_hero),
                        contentDescription = "Sanat Stüdyosu Başlığı",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(215.dp)
                    )

                    // Rich gradient scrim for crisp typography contrast
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(215.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xEE181426),
                                        Color(0xCC1E1B2E),
                                        Color(0x881E1B2E)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(215.dp)
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFB800).copy(alpha = 0.22f),
                                modifier = Modifier.border(
                                    1.dp,
                                    Color(0xFFFFB800).copy(alpha = 0.6f),
                                    CircleShape
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD166),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "GÜNÜN İLHAM ESERİ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFFD166),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = dailyFeatured.titleTr,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = dailyFeatured.artistNoteTr,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE0DCEB),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onOpenArtwork(dailyFeatured.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("start_daily_artwork_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (dailySaved != null && dailySaved.coloredCount > 0)
                                        "Boyamaya Devam Et (%${dailySaved.completionPercent})"
                                    else "Hemen Boyamaya Başla",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        // Live Vector Preview Medallion
                        Box(
                            modifier = Modifier
                                .size(132.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFFFBF8F3))
                                .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                                .padding(6.dp)
                        ) {
                            ArtworkVectorPreview(
                                template = dailyFeatured,
                                fills = dailyFills,
                                strokes = dailyStrokes,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // 3. Category & Difficulty Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ArtCategory.entries) { category ->
                        val selected = selectedCategory == category
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCategory(category) },
                            label = {
                                Text(
                                    text = category.titleTr,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                        )
                    }
                }

                // Difficulty Sub-filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zorluk:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DifficultyLevel.entries.forEach { level ->
                        val isSelected = selectedDifficulty == level
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectDifficulty(level) },
                            label = {
                                Text(
                                    text = "${level.labelTr} (${"★".repeat(level.stars)})",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.testTag("difficulty_chip_${level.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // 4. 2-Column Responsive Grid of Vector Artwork Cards
        val rows = filteredTemplates.chunked(2)
        items(rows) { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                for (template in rowItems) {
                    val saved = savedById[template.id]
                    ArtworkTemplateCard(
                        template = template,
                        saved = saved,
                        onClick = { onOpenArtwork(template.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ArtworkTemplateCard(
    template: ArtworkTemplate,
    saved: SavedArtworkEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fills = remember(saved) {
        saved?.let { ArtworkProgressCodec.decodeFills(it.fillsEncoded) } ?: emptyMap()
    }
    val strokes = remember(saved) {
        saved?.let { ArtworkProgressCodec.decodeStrokes(it.strokesEncoded) } ?: emptyList()
    }
    val palette = remember(template.defaultPaletteId) {
        CuratedPalettes.findById(template.defaultPaletteId)
    }

    Card(
        onClick = onClick,
        modifier = modifier.testTag("template_card_${template.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Vector Artwork Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFBF8F3))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(8.dp)
            ) {
                ArtworkVectorPreview(
                    template = template,
                    fills = fills,
                    strokes = strokes,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-start Region Count Pill
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "${template.regions.size} Bölge",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Top-end Completion Check or Difficulty Stars
                if (saved?.isCompleted == true) {
                    Surface(
                        shape = CircleShape,
                        color = EmeraldSuccess,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Tamamlandı",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Bitti",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = template.titleTr,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.category.titleTr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "★".repeat(template.difficulty.stars),
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldStar
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Either Progress Bar (if started) or Recommended Palette Swatch Strip
            if (saved != null && saved.coloredCount > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (saved.isCompleted) "Tamamlandı" else "Devam Ediyor",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "%${saved.completionPercent}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { saved.completionPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape),
                        color = if (saved.isCompleted) EmeraldSuccess else MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    palette.colors.take(6).forEach { c ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(c.toComposeColor())
                        )
                    }
                }
            }
        }
    }
}
