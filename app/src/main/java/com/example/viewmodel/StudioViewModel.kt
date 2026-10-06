package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ArtworkProgressCodec
import com.example.data.CustomSwatchEntity
import com.example.data.SavedArtworkEntity
import com.example.data.StudioDatabase
import com.example.data.StudioRepository
import com.example.model.AchievementBadge
import com.example.model.ArtCategory
import com.example.model.ArtworkTemplate
import com.example.model.CanvasHistorySnapshot
import com.example.model.ColorPaletteModel
import com.example.model.ColoringMode
import com.example.model.CuratedPalettes
import com.example.model.DifficultyLevel
import com.example.model.FillTexture
import com.example.model.FreehandStroke
import com.example.model.RegionFillState
import com.example.model.StrokePoint
import com.example.model.TimelapseStep
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab {
    STUDIO,
    GALLERY,
    PALETTES,
    ACHIEVEMENTS
}

data class StudioUiState(
    val activeTab: MainTab = MainTab.STUDIO,
    val selectedCategory: ArtCategory = ArtCategory.ALL,
    val selectedDifficulty: DifficultyLevel? = null,
    val activeTemplate: ArtworkTemplate? = null,
    val regionFills: Map<Int, RegionFillState> = emptyMap(),
    val freehandStrokes: List<FreehandStroke> = emptyList(),
    val timelapseSteps: List<TimelapseStep> = emptyList(),
    val undoStack: List<CanvasHistorySnapshot> = emptyList(),
    val redoStack: List<CanvasHistorySnapshot> = emptyList(),
    val coloringMode: ColoringMode = ColoringMode.SMART_FILL,
    val fillTexture: FillTexture = FillTexture.SOLID,
    val activePaletteId: String = CuratedPalettes.allPalettes.first().id,
    val selectedColorArgb: Long = CuratedPalettes.allPalettes.first().colors.first(),
    val selectedPaletteIndex: Int = 0,
    val brushSize: Float = 26f,
    val stayInsideLines: Boolean = true,
    val isEyedropperActive: Boolean = false,
    val hintRegionId: Int? = null,
    val lastFilledRegionName: String? = null,
    val elapsedSeconds: Long = 0L,
    val showCelebrationDialog: Boolean = false,
    val timelapseDialogTemplate: ArtworkTemplate? = null,
    val timelapseDialogSteps: List<TimelapseStep> = emptyList(),
    val showColorMixerDialog: Boolean = false
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudioRepository = StudioRepository(
        StudioDatabase.getDatabase(application).studioDao()
    )

    val allTemplates: List<ArtworkTemplate> = repository.getAllTemplates()

    val savedArtworks: StateFlow<List<SavedArtworkEntity>> = repository.savedArtworksFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customSwatches: StateFlow<List<CustomSwatchEntity>> = repository.customSwatchesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val achievements: StateFlow<List<AchievementBadge>> = combine(
        savedArtworks,
        customSwatches
    ) { artworks, swatches ->
        computeAchievements(artworks, swatches)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = computeAchievements(emptyList(), emptyList())
    )

    private var timerJob: Job? = null

    fun selectMainTab(tab: MainTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectCategory(category: ArtCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectDifficulty(difficulty: DifficultyLevel?) {
        _uiState.update {
            val next = if (it.selectedDifficulty == difficulty) null else difficulty
            it.copy(selectedDifficulty = next)
        }
    }

    fun openArtworkStudio(templateId: String) {
        val template = repository.getTemplateById(templateId)
        viewModelScope.launch {
            val saved = repository.getSavedArtwork(templateId)
            val paletteId = saved?.selectedPaletteId ?: template.defaultPaletteId
            val palette = CuratedPalettes.findById(paletteId)
            val fills = saved?.let { ArtworkProgressCodec.decodeFills(it.fillsEncoded) } ?: emptyMap()
            val strokes = saved?.let { ArtworkProgressCodec.decodeStrokes(it.strokesEncoded) } ?: emptyList()
            val timelapse = saved?.let { ArtworkProgressCodec.decodeTimelapse(it.timelapseEncoded) } ?: emptyList()
            val elapsed = saved?.elapsedSeconds ?: 0L

            _uiState.update {
                it.copy(
                    activeTemplate = template,
                    regionFills = fills,
                    freehandStrokes = strokes,
                    timelapseSteps = timelapse,
                    undoStack = emptyList(),
                    redoStack = emptyList(),
                    activePaletteId = palette.id,
                    selectedColorArgb = palette.colors.first(),
                    selectedPaletteIndex = 0,
                    isEyedropperActive = false,
                    hintRegionId = null,
                    lastFilledRegionName = null,
                    elapsedSeconds = elapsed,
                    showCelebrationDialog = false
                )
            }
            startStudioTimer()
        }
    }

    fun closeArtworkStudio() {
        persistCurrentStudioProgress()
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                activeTemplate = null,
                hintRegionId = null,
                isEyedropperActive = false,
                showCelebrationDialog = false
            )
        }
    }

    private fun startStudioTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (_uiState.value.activeTemplate != null) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1L) }
                }
            }
        }
    }

    fun setColoringMode(mode: ColoringMode) {
        _uiState.update { state ->
            // When switching to COLOR_BY_NUMBER, make sure selectedColorArgb matches activePalette[selectedPaletteIndex]
            val palette = CuratedPalettes.findById(state.activePaletteId)
            val safeIdx = state.selectedPaletteIndex.coerceIn(0, palette.colors.lastIndex)
            val syncedColor = if (mode == ColoringMode.COLOR_BY_NUMBER) {
                palette.colors[safeIdx]
            } else {
                state.selectedColorArgb
            }
            state.copy(
                coloringMode = mode,
                selectedPaletteIndex = safeIdx,
                selectedColorArgb = syncedColor,
                isEyedropperActive = false
            )
        }
    }

    fun setFillTexture(texture: FillTexture) {
        _uiState.update { it.copy(fillTexture = texture) }
    }

    fun selectPalette(paletteId: String) {
        val palette = CuratedPalettes.findById(paletteId)
        _uiState.update {
            it.copy(
                activePaletteId = palette.id,
                selectedPaletteIndex = 0,
                selectedColorArgb = palette.colors.first(),
                isEyedropperActive = false
            )
        }
        persistCurrentStudioProgress()
    }

    fun selectPaletteColor(index: Int, colorArgb: Long) {
        _uiState.update {
            it.copy(
                selectedPaletteIndex = index,
                selectedColorArgb = colorArgb,
                isEyedropperActive = false,
                hintRegionId = null
            )
        }
    }

    fun selectCustomColor(colorArgb: Long) {
        _uiState.update {
            it.copy(
                selectedPaletteIndex = -1,
                selectedColorArgb = colorArgb,
                isEyedropperActive = false
            )
        }
    }

    fun setBrushSize(size: Float) {
        _uiState.update { it.copy(brushSize = size.coerceIn(10f, 64f)) }
    }

    fun toggleStayInsideLines() {
        _uiState.update { it.copy(stayInsideLines = !it.stayInsideLines) }
    }

    fun toggleEyedropper() {
        _uiState.update { it.copy(isEyedropperActive = !it.isEyedropperActive, hintRegionId = null) }
    }

    fun onRegionTappedOrDragged(regionId: Int, isNewGesture: Boolean) {
        val state = _uiState.value
        val template = state.activeTemplate ?: return
        val region = template.regions.find { it.id == regionId } ?: return

        // Eyedropper mode: pick existing color from the region
        if (state.isEyedropperActive) {
            val existing = state.regionFills[regionId]?.colorArgb ?: return
            val palette = CuratedPalettes.findById(state.activePaletteId)
            val idxInPalette = palette.colors.indexOf(existing)
            _uiState.update {
                it.copy(
                    selectedColorArgb = existing,
                    selectedPaletteIndex = idxInPalette,
                    isEyedropperActive = false,
                    lastFilledRegionName = "${region.nameTr} rengi seçildi"
                )
            }
            return
        }

        // Determine color to apply
        val palette = CuratedPalettes.findById(state.activePaletteId)
        val colorToApply = if (state.coloringMode == ColoringMode.COLOR_BY_NUMBER) {
            // In Color-by-Number mode, if user taps a region matching the active number or any region,
            // apply either the selected color or if it matches the target index
            state.selectedColorArgb
        } else {
            state.selectedColorArgb
        }

        val currentFill = state.regionFills[regionId]
        if (currentFill?.colorArgb == colorToApply && currentFill.texture == state.fillTexture) {
            return // Already filled with exact same pigment & texture
        }

        val snapshot = CanvasHistorySnapshot(
            fills = state.regionFills,
            strokes = state.freehandStrokes
        )
        val updatedUndo = if (isNewGesture) {
            (state.undoStack + snapshot).takeLast(45)
        } else {
            state.undoStack
        }

        val newFill = RegionFillState(
            regionId = regionId,
            colorArgb = colorToApply,
            texture = state.fillTexture
        )
        val updatedFills = state.regionFills + (regionId to newFill)
        val updatedTimelapse = state.timelapseSteps + TimelapseStep(
            regionId = regionId,
            colorArgb = colorToApply,
            texture = state.fillTexture
        )

        val wasCompletedBefore = state.regionFills.size >= template.regions.size
        val isCompletedNow = updatedFills.size >= template.regions.size

        _uiState.update {
            it.copy(
                regionFills = updatedFills,
                timelapseSteps = updatedTimelapse,
                undoStack = updatedUndo,
                redoStack = emptyList(),
                hintRegionId = if (it.hintRegionId == regionId) null else it.hintRegionId,
                lastFilledRegionName = region.nameTr,
                showCelebrationDialog = !wasCompletedBefore && isCompletedNow
            )
        }
        persistCurrentStudioProgress()
    }

    fun addFreehandStroke(
        regionId: Int?,
        points: List<StrokePoint>
    ) {
        if (points.size < 2) return
        val state = _uiState.value
        val snapshot = CanvasHistorySnapshot(
            fills = state.regionFills,
            strokes = state.freehandStrokes
        )
        val newStroke = FreehandStroke(
            id = System.currentTimeMillis(),
            regionId = if (state.stayInsideLines) regionId else null,
            colorArgb = state.selectedColorArgb,
            strokeWidth = state.brushSize,
            alpha = 0.88f,
            points = points
        )
        _uiState.update {
            it.copy(
                freehandStrokes = it.freehandStrokes + newStroke,
                undoStack = (it.undoStack + snapshot).takeLast(45),
                redoStack = emptyList()
            )
        }
        persistCurrentStudioProgress()
    }

    fun undo() {
        val state = _uiState.value
        if (state.undoStack.isEmpty()) return
        val previous = state.undoStack.last()
        val currentSnapshot = CanvasHistorySnapshot(
            fills = state.regionFills,
            strokes = state.freehandStrokes
        )
        _uiState.update {
            it.copy(
                regionFills = previous.fills,
                freehandStrokes = previous.strokes,
                undoStack = it.undoStack.dropLast(1),
                redoStack = (it.redoStack + currentSnapshot).takeLast(45),
                hintRegionId = null
            )
        }
        persistCurrentStudioProgress()
    }

    fun redo() {
        val state = _uiState.value
        if (state.redoStack.isEmpty()) return
        val next = state.redoStack.last()
        val currentSnapshot = CanvasHistorySnapshot(
            fills = state.regionFills,
            strokes = state.freehandStrokes
        )
        _uiState.update {
            it.copy(
                regionFills = next.fills,
                freehandStrokes = next.strokes,
                undoStack = (it.undoStack + currentSnapshot).takeLast(45),
                redoStack = it.redoStack.dropLast(1),
                hintRegionId = null
            )
        }
        persistCurrentStudioProgress()
    }

    fun triggerSmartHint() {
        val state = _uiState.value
        val template = state.activeTemplate ?: return
        val palette = CuratedPalettes.findById(state.activePaletteId)

        // Find an uncolored region, preferring the currently selected number if in Color-by-Number mode
        val uncoloredRegions = template.regions.filter { !state.regionFills.containsKey(it.id) }
        if (uncoloredRegions.isEmpty()) return

        val targetRegion = if (state.coloringMode == ColoringMode.COLOR_BY_NUMBER && state.selectedPaletteIndex >= 0) {
            uncoloredRegions.find { it.targetColorIndex == state.selectedPaletteIndex } ?: uncoloredRegions.first()
        } else {
            uncoloredRegions.first()
        }

        // If the same hint region is already highlighted, fill it automatically with its harmonious palette color!
        if (state.hintRegionId == targetRegion.id) {
            val recommendedIdx = targetRegion.targetColorIndex.coerceIn(0, palette.colors.lastIndex)
            val recommendedColor = palette.colors[recommendedIdx]
            selectPaletteColor(recommendedIdx, recommendedColor)
            onRegionTappedOrDragged(targetRegion.id, isNewGesture = true)
        } else {
            val recommendedIdx = targetRegion.targetColorIndex.coerceIn(0, palette.colors.lastIndex)
            val recommendedColor = palette.colors[recommendedIdx]
            _uiState.update {
                it.copy(
                    hintRegionId = targetRegion.id,
                    selectedPaletteIndex = recommendedIdx,
                    selectedColorArgb = recommendedColor,
                    lastFilledRegionName = "İpucu: ${targetRegion.nameTr} (Bölge #${recommendedIdx + 1})"
                )
            }
        }
    }

    fun autoFillByNumbersHarmony() {
        val state = _uiState.value
        val template = state.activeTemplate ?: return
        val palette = CuratedPalettes.findById(state.activePaletteId)
        val uncolored = template.regions.filter { !state.regionFills.containsKey(it.id) }
        if (uncolored.isEmpty()) return

        // Fill up to 3 uncolored regions using their recommended palette colors as a creative boost
        val snapshot = CanvasHistorySnapshot(state.regionFills, state.freehandStrokes)
        val toFill = uncolored.take(3)
        val newFills = state.regionFills.toMutableMap()
        val newSteps = state.timelapseSteps.toMutableList()
        toFill.forEach { reg ->
            val idx = reg.targetColorIndex.coerceIn(0, palette.colors.lastIndex)
            val color = palette.colors[idx]
            newFills[reg.id] = RegionFillState(reg.id, color, state.fillTexture)
            newSteps.add(TimelapseStep(reg.id, color, state.fillTexture))
        }
        val wasComplete = state.regionFills.size >= template.regions.size
        val nowComplete = newFills.size >= template.regions.size

        _uiState.update {
            it.copy(
                regionFills = newFills,
                timelapseSteps = newSteps,
                undoStack = (it.undoStack + snapshot).takeLast(45),
                redoStack = emptyList(),
                hintRegionId = null,
                lastFilledRegionName = "${toFill.size} bölge uyumla boyandı",
                showCelebrationDialog = !wasComplete && nowComplete
            )
        }
        persistCurrentStudioProgress()
    }

    fun clearCanvas() {
        val state = _uiState.value
        if (state.regionFills.isEmpty() && state.freehandStrokes.isEmpty()) return
        val snapshot = CanvasHistorySnapshot(state.regionFills, state.freehandStrokes)
        _uiState.update {
            it.copy(
                regionFills = emptyMap(),
                freehandStrokes = emptyList(),
                timelapseSteps = emptyList(),
                undoStack = (it.undoStack + snapshot).takeLast(45),
                redoStack = emptyList(),
                hintRegionId = null,
                lastFilledRegionName = "Tuval temizlendi"
            )
        }
        val templateId = state.activeTemplate?.id ?: return
        viewModelScope.launch {
            repository.resetArtwork(templateId)
        }
    }

    fun resetSavedArtworkFromGallery(templateId: String) {
        viewModelScope.launch {
            repository.resetArtwork(templateId)
        }
    }

    fun dismissCelebrationDialog() {
        _uiState.update { it.copy(showCelebrationDialog = false) }
    }

    fun openTimelapseReplay(template: ArtworkTemplate, steps: List<TimelapseStep>) {
        // If steps is empty (e.g. from legacy state), synthesize steps from current fills
        val effectiveSteps = if (steps.isNotEmpty()) {
            steps
        } else {
            _uiState.value.regionFills.values.map {
                TimelapseStep(it.regionId, it.colorArgb, it.texture)
            }
        }
        _uiState.update {
            it.copy(
                timelapseDialogTemplate = template,
                timelapseDialogSteps = effectiveSteps
            )
        }
    }

    fun closeTimelapseReplay() {
        _uiState.update {
            it.copy(
                timelapseDialogTemplate = null,
                timelapseDialogSteps = emptyList()
            )
        }
    }

    fun setShowColorMixerDialog(show: Boolean) {
        _uiState.update { it.copy(showColorMixerDialog = show) }
    }

    fun createCustomSwatch(nameTr: String, colorArgb: Long) {
        viewModelScope.launch {
            repository.addCustomSwatch(nameTr, colorArgb)
            _uiState.update {
                it.copy(
                    selectedColorArgb = colorArgb,
                    selectedPaletteIndex = -1,
                    showColorMixerDialog = false
                )
            }
        }
    }

    fun deleteCustomSwatch(swatchId: Int) {
        viewModelScope.launch {
            repository.removeCustomSwatch(swatchId)
        }
    }

    private fun persistCurrentStudioProgress() {
        val state = _uiState.value
        val template = state.activeTemplate ?: return
        viewModelScope.launch {
            if (state.regionFills.isEmpty() && state.freehandStrokes.isEmpty()) {
                repository.resetArtwork(template.id)
            } else {
                repository.saveArtworkProgress(
                    template = template,
                    fills = state.regionFills,
                    strokes = state.freehandStrokes,
                    timelapseSteps = state.timelapseSteps,
                    elapsedSeconds = state.elapsedSeconds,
                    selectedPaletteId = state.activePaletteId
                )
            }
        }
    }

    private fun computeAchievements(
        artworks: List<SavedArtworkEntity>,
        swatches: List<CustomSwatchEntity>
    ): List<AchievementBadge> {
        val totalRegionsColored = artworks.sumOf { it.coloredCount }
        val completedCount = artworks.count { it.isCompleted }
        val inProgressOrDone = artworks.count { it.coloredCount > 0 }
        val totalMinutes = (artworks.sumOf { it.elapsedSeconds } / 60L).toInt()
        val usedTexturesCount = artworks
            .flatMap { ArtworkProgressCodec.decodeFills(it.fillsEncoded).values }
            .map { it.texture }
            .distinct()
            .size

        return listOf(
            AchievementBadge(
                id = "first_stroke",
                titleTr = "İlk Fırça Darbesi",
                descriptionTr = "İlk eserinizi boyamaya başlayın ve tuvale hayat verin.",
                progress = inProgressOrDone.coerceAtMost(1),
                target = 1,
                isUnlocked = inProgressOrDone >= 1,
                accentColorArgb = 0xFFE85D4AL
            ),
            AchievementBadge(
                id = "region_explorer",
                titleTr = "Detay Ustası",
                descriptionTr = "Toplam 25 farklı illüstrasyon bölgesini renklendirin.",
                progress = totalRegionsColored.coerceAtMost(25),
                target = 25,
                isUnlocked = totalRegionsColored >= 25,
                accentColorArgb = 0xFF2A9D8FL
            ),
            AchievementBadge(
                id = "master_artisan",
                titleTr = "Başkalfa Nakkaş",
                descriptionTr = "Toplam 100 farklı bölgeyi özenle boyayın.",
                progress = totalRegionsColored.coerceAtMost(100),
                target = 100,
                isUnlocked = totalRegionsColored >= 100,
                accentColorArgb = 0xFFFFB703L
            ),
            AchievementBadge(
                id = "first_masterpiece",
                titleTr = "İlk Başyapıt",
                descriptionTr = "Bir illüstrasyonu %100 oranında eksiksiz tamamlayın.",
                progress = completedCount.coerceAtMost(1),
                target = 1,
                isUnlocked = completedCount >= 1,
                accentColorArgb = 0xFF9B5DE5L
            ),
            AchievementBadge(
                id = "gallery_curator",
                titleTr = "Galeri Küratörü",
                descriptionTr = "Koleksiyonunuzda 3 farklı eseri %100 tamamlayın.",
                progress = completedCount.coerceAtMost(3),
                target = 3,
                isUnlocked = completedCount >= 3,
                accentColorArgb = 0xFFF72585L
            ),
            AchievementBadge(
                id = "color_alchemist",
                titleTr = "Renk Kimyacısı",
                descriptionTr = "Renk Laboratuvarında 2 özel pigment oluşturup kaydedin.",
                progress = swatches.size.coerceAtMost(2),
                target = 2,
                isUnlocked = swatches.size >= 2,
                accentColorArgb = 0xFF00B4D8L
            ),
            AchievementBadge(
                id = "texture_virtuoso",
                titleTr = "Doku Virtüözü",
                descriptionTr = "Suluboya, Gradyan veya Noktalı gibi 3 farklı boya dokusu kullanın.",
                progress = usedTexturesCount.coerceAtMost(3),
                target = 3,
                isUnlocked = usedTexturesCount >= 3,
                accentColorArgb = 0xFF52B788L
            ),
            AchievementBadge(
                id = "zen_meditation",
                titleTr = "Zen Odaklanması",
                descriptionTr = "Sanat atölyesinde toplam 5 dakika boyama keyfi yaşayın.",
                progress = totalMinutes.coerceAtMost(5),
                target = 5,
                isUnlocked = totalMinutes >= 5,
                accentColorArgb = 0xFF8338ECL
            )
        )
    }
}
