package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.canvas.ColoringStudioScreen
import com.example.ui.canvas.TimelapseReplayDialog
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.CustomColorMixerDialog
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HomeAtolyeScreen
import com.example.ui.screens.PalettesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainTab
import com.example.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RenkAtolyesiApp()
            }
        }
    }
}

@Composable
fun RenkAtolyesiApp(
    viewModel: StudioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedArtworks by viewModel.savedArtworks.collectAsStateWithLifecycle()
    val customSwatches by viewModel.customSwatches.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    // Full-screen Studio Canvas when an artwork is opened
    if (uiState.activeTemplate != null) {
        ColoringStudioScreen(
            uiState = uiState,
            customSwatches = customSwatches,
            onBack = { viewModel.closeArtworkStudio() },
            onSelectMode = { viewModel.setColoringMode(it) },
            onSelectTexture = { viewModel.setFillTexture(it) },
            onSelectPalette = { viewModel.selectPalette(it) },
            onSelectPaletteColor = { idx, color -> viewModel.selectPaletteColor(idx, color) },
            onSelectCustomColor = { viewModel.selectCustomColor(it) },
            onBrushSizeChange = { viewModel.setBrushSize(it) },
            onToggleStayInsideLines = { viewModel.toggleStayInsideLines() },
            onToggleEyedropper = { viewModel.toggleEyedropper() },
            onRegionHit = { regId, isNew -> viewModel.onRegionTappedOrDragged(regId, isNew) },
            onAddFreehandStroke = { regId, pts -> viewModel.addFreehandStroke(regId, pts) },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onSmartHint = { viewModel.triggerSmartHint() },
            onMagicHarmonyFill = { viewModel.autoFillByNumbersHarmony() },
            onClearCanvas = { viewModel.clearCanvas() },
            onOpenTimelapse = {
                val tpl = uiState.activeTemplate
                if (tpl != null) {
                    viewModel.openTimelapseReplay(tpl, uiState.timelapseSteps)
                }
            },
            onOpenColorMixer = { viewModel.setShowColorMixerDialog(true) },
            onDismissCelebration = { viewModel.dismissCelebrationDialog() }
        )
    } else {
        // Handle Back button on secondary tabs to return to STUDIO tab
        if (uiState.activeTab != MainTab.STUDIO) {
            BackHandler {
                viewModel.selectMainTab(MainTab.STUDIO)
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = uiState.activeTab == MainTab.STUDIO,
                        onClick = { viewModel.selectMainTab(MainTab.STUDIO) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.activeTab == MainTab.STUDIO)
                                    Icons.Filled.Brush else Icons.Outlined.Brush,
                                contentDescription = "Atölye"
                            )
                        },
                        label = { Text("Atölye") },
                        modifier = Modifier.testTag("nav_tab_studio")
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == MainTab.GALLERY,
                        onClick = { viewModel.selectMainTab(MainTab.GALLERY) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.activeTab == MainTab.GALLERY)
                                    Icons.Filled.Collections else Icons.Outlined.Collections,
                                contentDescription = "Galerim"
                            )
                        },
                        label = { Text("Galerim") },
                        modifier = Modifier.testTag("nav_tab_gallery")
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == MainTab.PALETTES,
                        onClick = { viewModel.selectMainTab(MainTab.PALETTES) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.activeTab == MainTab.PALETTES)
                                    Icons.Filled.Palette else Icons.Outlined.Palette,
                                contentDescription = "Paletler"
                            )
                        },
                        label = { Text("Paletler") },
                        modifier = Modifier.testTag("nav_tab_palettes")
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == MainTab.ACHIEVEMENTS,
                        onClick = { viewModel.selectMainTab(MainTab.ACHIEVEMENTS) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.activeTab == MainTab.ACHIEVEMENTS)
                                    Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
                                contentDescription = "Başarılar"
                            )
                        },
                        label = { Text("Başarılar") },
                        modifier = Modifier.testTag("nav_tab_achievements")
                    )
                }
            }
        ) { innerPadding ->
            Crossfade(
                targetState = uiState.activeTab,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "main_tab_crossfade"
            ) { tab ->
                when (tab) {
                    MainTab.STUDIO -> {
                        HomeAtolyeScreen(
                            templates = viewModel.allTemplates,
                            savedArtworks = savedArtworks,
                            selectedCategory = uiState.selectedCategory,
                            selectedDifficulty = uiState.selectedDifficulty,
                            onSelectCategory = { viewModel.selectCategory(it) },
                            onSelectDifficulty = { viewModel.selectDifficulty(it) },
                            onOpenArtwork = { viewModel.openArtworkStudio(it) }
                        )
                    }
                    MainTab.GALLERY -> {
                        GalleryScreen(
                            templates = viewModel.allTemplates,
                            savedArtworks = savedArtworks,
                            onOpenStudio = { viewModel.openArtworkStudio(it) },
                            onWatchTimelapse = { tpl, steps ->
                                viewModel.openTimelapseReplay(tpl, steps)
                            },
                            onDeleteArtwork = { viewModel.resetSavedArtworkFromGallery(it) },
                            onExploreTemplates = { viewModel.selectMainTab(MainTab.STUDIO) }
                        )
                    }
                    MainTab.PALETTES -> {
                        PalettesScreen(
                            activePaletteId = uiState.activePaletteId,
                            customSwatches = customSwatches,
                            onSelectPalette = { viewModel.selectPalette(it) },
                            onCreateCustomSwatch = { name, argb ->
                                viewModel.createCustomSwatch(name, argb)
                            },
                            onDeleteCustomSwatch = { viewModel.deleteCustomSwatch(it) }
                        )
                    }
                    MainTab.ACHIEVEMENTS -> {
                        AchievementsScreen(
                            savedArtworks = savedArtworks,
                            achievements = achievements
                        )
                    }
                }
            }
        }
    }

    // Timelapse Replay Modal (Accessible from both Studio and Gallery)
    val timelapseTemplate = uiState.timelapseDialogTemplate
    if (timelapseTemplate != null) {
        TimelapseReplayDialog(
            template = timelapseTemplate,
            steps = uiState.timelapseDialogSteps,
            onDismiss = { viewModel.closeTimelapseReplay() }
        )
    }

    // Custom Color Mixer Modal (Accessible from Studio '+' button)
    if (uiState.showColorMixerDialog) {
        CustomColorMixerDialog(
            onDismiss = { viewModel.setShowColorMixerDialog(false) },
            onSaveColor = { name, argb -> viewModel.createCustomSwatch(name, argb) }
        )
    }
}
