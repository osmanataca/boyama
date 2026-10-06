package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CustomSwatchEntity
import com.example.model.CuratedPalettes
import com.example.model.toComposeColor

@Composable
fun PalettesScreen(
    activePaletteId: String,
    customSwatches: List<CustomSwatchEntity>,
    onSelectPalette: (String) -> Unit,
    onCreateCustomSwatch: (nameTr: String, colorArgb: Long) -> Unit,
    onDeleteCustomSwatch: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var hue by remember { mutableFloatStateOf(16f) }
    var saturation by remember { mutableFloatStateOf(0.82f) }
    var lightness by remember { mutableFloatStateOf(0.56f) }
    var swatchName by remember { mutableStateOf("İznik Mercanı") }

    val previewArgb = remember(hue, saturation, lightness) {
        CuratedPalettes.hslToArgbLong(hue, saturation, lightness)
    }
    val complementaryArgb = remember(hue, saturation, lightness) {
        CuratedPalettes.hslToArgbLong((hue + 180f) % 360f, saturation, lightness)
    }
    val analogousLeftArgb = remember(hue, saturation, lightness) {
        CuratedPalettes.hslToArgbLong((hue + 330f) % 360f, saturation, lightness)
    }
    val analogousRightArgb = remember(hue, saturation, lightness) {
        CuratedPalettes.hslToArgbLong((hue + 30f) % 360f, saturation, lightness)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("palettes_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Renk Laboratuvarı & Paletler",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Kendi özel pigmentlerinizi karıştırın veya küratörlü sanatçı paletlerini keşfedin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Interactive HSL Pigment Mixer Studio Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hsl_color_mixer_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Özel Pigment Karıştırıcı",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Renk Özü (Hue), Doygunluk ve Parlaklık uyumu",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Live Swatch + Hex Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = CuratedPalettes.argbToHex(previewArgb),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(previewArgb.toComposeColor())
                                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Color Harmony Strip (Main, Analogous, Complementary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val harmonies = listOf(
                            "Ana Ton" to previewArgb,
                            "Komşu 1" to analogousLeftArgb,
                            "Komşu 2" to analogousRightArgb,
                            "Karşıt" to complementaryArgb
                        )
                        harmonies.forEach { (label, argb) ->
                            val dark = CuratedPalettes.isColorDark(argb)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(argb.toComposeColor()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (dark) Color.White else Color.Black,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sliders
                    Text(
                        text = "Renk Tonu (${hue.toInt()}°)",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = hue,
                        onValueChange = { hue = it },
                        valueRange = 0f..360f,
                        modifier = Modifier.testTag("mixer_hue_slider")
                    )

                    Text(
                        text = "Doygunluk (%${(saturation * 100).toInt()})",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = saturation,
                        onValueChange = { saturation = it },
                        valueRange = 0.1f..1f,
                        modifier = Modifier.testTag("mixer_saturation_slider")
                    )

                    Text(
                        text = "Işık & Parlaklık (%${(lightness * 100).toInt()})",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = lightness,
                        onValueChange = { lightness = it },
                        valueRange = 0.15f..0.88f,
                        modifier = Modifier.testTag("mixer_lightness_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = swatchName,
                            onValueChange = { swatchName = it },
                            label = { Text("Pigment Adı") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_swatch_name_input")
                        )

                        Button(
                            onClick = {
                                onCreateCustomSwatch(swatchName, previewArgb)
                            },
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("save_custom_swatch_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekle")
                        }
                    }
                }
            }
        }

        // 2. Saved Custom Swatches Section
        if (customSwatches.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kaydedilen Özel Pigmentler (${customSwatches.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(customSwatches, key = { it.id }) { swatch ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(swatch.colorArgb.toComposeColor())
                                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    )
                                    Column {
                                        Text(
                                            text = swatch.nameTr,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = CuratedPalettes.argbToHex(swatch.colorArgb),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteCustomSwatch(swatch.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Sil",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Curated Studio Palettes List
        item {
            Text(
                text = "Küratörlü Sanatçı Paletleri (8 Koleksiyon)",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(CuratedPalettes.allPalettes, key = { it.id }) { palette ->
            val isSelected = palette.id == activePaletteId
            Card(
                onClick = { onSelectPalette(palette.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("curated_palette_card_${palette.id}"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = palette.nameTr,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = palette.moodTr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Seçili",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        palette.colors.forEach { argb ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(argb.toComposeColor())
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomColorMixerDialog(
    onDismiss: () -> Unit,
    onSaveColor: (nameTr: String, colorArgb: Long) -> Unit
) {
    var hue by remember { mutableFloatStateOf(195f) }
    var saturation by remember { mutableFloatStateOf(0.85f) }
    var lightness by remember { mutableFloatStateOf(0.52f) }
    var name by remember { mutableStateOf("Atölye Mavisi") }

    val colorArgb = remember(hue, saturation, lightness) {
        CuratedPalettes.hslToArgbLong(hue, saturation, lightness)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Özel Renk Karıştırıcı") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(colorArgb.toComposeColor())
                            .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Renk Adı") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Renk Özü (${hue.toInt()}°)", style = MaterialTheme.typography.labelSmall)
                Slider(value = hue, onValueChange = { hue = it }, valueRange = 0f..360f)

                Text("Doygunluk (%${(saturation * 100).toInt()})", style = MaterialTheme.typography.labelSmall)
                Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0.1f..1f)

                Text("Parlaklık (%${(lightness * 100).toInt()})", style = MaterialTheme.typography.labelSmall)
                Slider(value = lightness, onValueChange = { lightness = it }, valueRange = 0.15f..0.85f)
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveColor(name, colorArgb) },
                modifier = Modifier.testTag("confirm_custom_color_dialog_button")
            ) {
                Text("Palete Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
