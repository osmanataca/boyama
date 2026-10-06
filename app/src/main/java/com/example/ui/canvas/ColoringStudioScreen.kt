package com.example.ui.canvas

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CustomSwatchEntity
import com.example.model.ColoringMode
import com.example.model.CuratedPalettes
import com.example.model.FillTexture
import com.example.model.StrokePoint
import com.example.model.toComposeColor
import com.example.ui.theme.GoldStar
import com.example.viewmodel.StudioUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColoringStudioScreen(
    uiState: StudioUiState,
    customSwatches: List<CustomSwatchEntity>,
    onBack: () -> Unit,
    onSelectMode: (ColoringMode) -> Unit,
    onSelectTexture: (FillTexture) -> Unit,
    onSelectPalette: (String) -> Unit,
    onSelectPaletteColor: (index: Int, colorArgb: Long) -> Unit,
    onSelectCustomColor: (colorArgb: Long) -> Unit,
    onBrushSizeChange: (Float) -> Unit,
    onToggleStayInsideLines: () -> Unit,
    onToggleEyedropper: () -> Unit,
    onRegionHit: (regionId: Int, isNewGesture: Boolean) -> Unit,
    onAddFreehandStroke: (regionId: Int?, points: List<StrokePoint>) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSmartHint: () -> Unit,
    onMagicHarmonyFill: () -> Unit,
    onClearCanvas: () -> Unit,
    onOpenTimelapse: () -> Unit,
    onOpenColorMixer: () -> Unit,
    onDismissCelebration: () -> Unit
) {
    val template = uiState.activeTemplate ?: return
    val context = LocalContext.current
    var showPalettePickerSheet by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    val totalRegions = template.regions.size.coerceAtLeast(1)
    val coloredCount = uiState.regionFills.size.coerceAtMost(totalRegions)
    val completionPercent = ((coloredCount * 100f) / totalRegions).toInt().coerceIn(0, 100)
    val activePalette = CuratedPalettes.findById(uiState.activePaletteId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("coloring_studio_screen")
    ) {
        // 1. Top Studio Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("studio_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atölyeye Dön"
                            )
                        }
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = template.titleTr,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "$coloredCount/$totalRegions Bölge (%$completionPercent)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                uiState.lastFilledRegionName?.let { lastRegion ->
                                    Text(
                                        text = "• $lastRegion",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons: Undo, Redo, Timelapse, Share, Clear
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onUndo,
                            enabled = uiState.undoStack.isNotEmpty(),
                            modifier = Modifier.testTag("studio_undo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Geri Al"
                            )
                        }
                        IconButton(
                            onClick = onRedo,
                            enabled = uiState.redoStack.isNotEmpty(),
                            modifier = Modifier.testTag("studio_redo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "İleri Al"
                            )
                        }
                        IconButton(
                            onClick = onOpenTimelapse,
                            modifier = Modifier.testTag("studio_timelapse_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "Canlandır (Timelapse)",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(
                            onClick = {
                                CanvasHitTester.shareArtworkBitmap(
                                    context = context,
                                    template = template,
                                    fills = uiState.regionFills,
                                    strokes = uiState.freehandStrokes
                                )
                            },
                            modifier = Modifier.testTag("studio_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Eseri Paylaş"
                            )
                        }
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier.testTag("studio_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Tuvali Temizle"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { completionPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // 2. Mode Switcher & Smart Studio Tools Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                ColoringMode.entries.forEach { mode ->
                    val selected = uiState.coloringMode == mode
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectMode(mode) },
                        label = {
                            Text(
                                text = when (mode) {
                                    ColoringMode.SMART_FILL -> "Dolgu"
                                    ColoringMode.COLOR_BY_NUMBER -> "Sayılarla"
                                    ColoringMode.FREE_BRUSH -> "Fırça"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (mode) {
                                    ColoringMode.SMART_FILL -> Icons.Default.FormatColorFill
                                    ColoringMode.COLOR_BY_NUMBER -> Icons.Default.LooksOne
                                    ColoringMode.FREE_BRUSH -> Icons.Default.Brush
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("mode_chip_${mode.name.lowercase()}")
                    )
                }
            }

            // Quick Utility Tools: Eyedropper, Smart Hint, Magic Harmony
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    onClick = onToggleEyedropper,
                    shape = CircleShape,
                    color = if (uiState.isEyedropperActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("studio_eyedropper_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Colorize,
                            contentDescription = "Damlalık",
                            tint = if (uiState.isEyedropperActive) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    onClick = onSmartHint,
                    shape = CircleShape,
                    color = if (uiState.hintRegionId != null) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("studio_hint_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Akıllı İpucu",
                            tint = if (uiState.hintRegionId != null) MaterialTheme.colorScheme.onTertiary
                            else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    onClick = onMagicHarmonyFill,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("studio_magic_wand_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Sihirli Dokunuş (3 Bölge Boya)",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 3. Center Interactive Vector Canvas Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            InteractiveColoringCanvas(
                template = template,
                fills = uiState.regionFills,
                strokes = uiState.freehandStrokes,
                coloringMode = uiState.coloringMode,
                selectedColorArgb = uiState.selectedColorArgb,
                selectedPaletteIndex = uiState.selectedPaletteIndex,
                brushSize = uiState.brushSize,
                stayInsideLines = uiState.stayInsideLines,
                hintRegionId = uiState.hintRegionId,
                onRegionHit = onRegionHit,
                onAddFreehandStroke = onAddFreehandStroke
            )
        }

        // 4. Bottom Dock: Texture/Brush Selector + Color Swatches
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 12.dp)
            ) {
                // Sub-row: Either Fill Texture Chips OR Freehand Brush Settings
                if (uiState.coloringMode == ColoringMode.FREE_BRUSH) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = uiState.stayInsideLines,
                            onClick = onToggleStayInsideLines,
                            label = {
                                Text(
                                    text = if (uiState.stayInsideLines) "Sınır Korumalı" else "Serbest Tuval",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (uiState.stayInsideLines) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            modifier = Modifier.testTag("stay_inside_lines_chip")
                        )

                        Text(
                            text = "Kalınlık",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Slider(
                            value = uiState.brushSize,
                            onValueChange = onBrushSizeChange,
                            valueRange = 12f..56f,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .testTag("brush_size_slider")
                        )
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(FillTexture.entries) { texture ->
                            val isSelected = uiState.fillTexture == texture
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectTexture(texture) },
                                label = {
                                    Text(
                                        text = texture.labelTr,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.testTag("texture_chip_${texture.name.lowercase()}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Main Color Palette Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Palette Picker Button
                    Surface(
                        onClick = { showPalettePickerSheet = true },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("open_palette_sheet_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Palet Değiştir",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Palet",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Scrollable Pigment Swatches
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(activePalette.colors) { idx, colorArgb ->
                            val isSelected = uiState.selectedColorArgb == colorArgb
                            val isDark = CuratedPalettes.isColorDark(colorArgb)
                            // Check if all regions with targetColorIndex == idx are already colored
                            val matchingRegions = template.regions.filter { it.targetColorIndex == idx }
                            val allMatchingColored = matchingRegions.isNotEmpty() &&
                                matchingRegions.all { uiState.regionFills.containsKey(it.id) }

                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 52.dp else 44.dp)
                                    .clip(CircleShape)
                                    .background(colorArgb.toComposeColor())
                                    .border(
                                        width = if (isSelected) 3.5.dp else 1.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { onSelectPaletteColor(idx, colorArgb) }
                                    .testTag("palette_swatch_$idx"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (allMatchingColored && uiState.coloringMode == ColoringMode.COLOR_BY_NUMBER) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Tamamlandı",
                                        tint = if (isDark) Color.White else Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (uiState.coloringMode == ColoringMode.COLOR_BY_NUMBER) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seçili Renk",
                                        tint = if (isDark) Color.White else Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Custom Swatches created by User
                        items(customSwatches, key = { "custom_${it.id}" }) { custom ->
                            val isSelected = uiState.selectedColorArgb == custom.colorArgb
                            val isDark = CuratedPalettes.isColorDark(custom.colorArgb)
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 52.dp else 44.dp)
                                    .clip(CircleShape)
                                    .background(custom.colorArgb.toComposeColor())
                                    .border(
                                        width = if (isSelected) 3.5.dp else 1.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable { onSelectCustomColor(custom.colorArgb) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = custom.nameTr,
                                        tint = if (isDark) Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Add Custom Color '+' Button
                        item {
                            Surface(
                                onClick = onOpenColorMixer,
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("studio_add_custom_color_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Özel Renk Oluştur"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Palette Switcher Bottom Sheet
    if (showPalettePickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPalettePickerSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Küratörlü Renk Paletleri",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Eserinizin atmosferini değiştirmek için bir palet seçin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                CuratedPalettes.allPalettes.forEach { palette ->
                    val isCurrent = palette.id == uiState.activePaletteId
                    Card(
                        onClick = {
                            onSelectPalette(palette.id)
                            showPalettePickerSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = palette.nameTr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isCurrent) {
                                    Text(
                                        text = "Aktif Palet",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = palette.moodTr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                palette.colors.forEach { c ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(c.toComposeColor())
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Clear Canvas Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Tuvali Sıfırla") },
            text = {
                Text("Bu eserdeki tüm boyamaları temizlemek istediğinize emin misiniz? (Geri Al butonu ile son işlemi geri döndürebilirsiniz.)")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearCanvas()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Temizle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    // 100% Masterpiece Celebration Dialog
    if (uiState.showCelebrationDialog) {
        AlertDialog(
            onDismissRequest = onDismissCelebration,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldStar,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tebrikler! Başyapıt Tamamlandı",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "'${template.titleTr}' eserinin tüm bölgelerini eksiksiz renklendirdiniz ve 3 Altın Yıldız kazandınız!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFBF8F3))
                            .padding(8.dp)
                    ) {
                        ArtworkVectorPreview(
                            template = template,
                            fills = uiState.regionFills,
                            strokes = uiState.freehandStrokes,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissCelebration()
                        onOpenTimelapse()
                    },
                    modifier = Modifier.testTag("celebration_watch_timelapse_button")
                ) {
                    Text("Canlı Timelapse İzle")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissCelebration) {
                    Text("Atölyede Kal")
                }
            }
        )
    }
}
