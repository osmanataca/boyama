package com.example.data

import com.example.model.ArtworkTemplate
import com.example.model.FreehandStroke
import com.example.model.RegionFillState
import com.example.model.TimelapseStep
import com.example.model.VectorArtCatalog
import kotlinx.coroutines.flow.Flow

class StudioRepository(private val dao: StudioDao) {

    val savedArtworksFlow: Flow<List<SavedArtworkEntity>> = dao.observeAllArtworks()
    val customSwatchesFlow: Flow<List<CustomSwatchEntity>> = dao.observeCustomSwatches()

    fun getAllTemplates(): List<ArtworkTemplate> = VectorArtCatalog.allTemplates

    fun getTemplateById(templateId: String): ArtworkTemplate = VectorArtCatalog.findById(templateId)

    suspend fun getSavedArtwork(templateId: String): SavedArtworkEntity? {
        return dao.getArtworkById(templateId)
    }

    suspend fun saveArtworkProgress(
        template: ArtworkTemplate,
        fills: Map<Int, RegionFillState>,
        strokes: List<FreehandStroke>,
        timelapseSteps: List<TimelapseStep>,
        elapsedSeconds: Long,
        selectedPaletteId: String
    ) {
        val total = template.regions.size.coerceAtLeast(1)
        val coloredCount = fills.size.coerceAtMost(total)
        val completionPercent = ((coloredCount * 100f) / total).toInt().coerceIn(0, 100)
        val isCompleted = coloredCount >= total
        val stars = when {
            completionPercent >= 100 -> 3
            completionPercent >= 65 -> 2
            completionPercent >= 25 -> 1
            else -> 0
        }

        val entity = SavedArtworkEntity(
            templateId = template.id,
            fillsEncoded = ArtworkProgressCodec.encodeFills(fills),
            strokesEncoded = ArtworkProgressCodec.encodeStrokes(strokes),
            timelapseEncoded = ArtworkProgressCodec.encodeTimelapse(timelapseSteps),
            coloredCount = coloredCount,
            totalRegions = total,
            completionPercent = completionPercent,
            isCompleted = isCompleted,
            starsEarned = stars,
            elapsedSeconds = elapsedSeconds,
            selectedPaletteId = selectedPaletteId,
            lastModifiedMs = System.currentTimeMillis()
        )
        dao.upsertArtwork(entity)
    }

    suspend fun resetArtwork(templateId: String) {
        dao.deleteArtwork(templateId)
    }

    suspend fun addCustomSwatch(nameTr: String, colorArgb: Long) {
        dao.insertCustomSwatch(
            CustomSwatchEntity(
                nameTr = nameTr.ifBlank { "Özel Pigment" },
                colorArgb = colorArgb
            )
        )
    }

    suspend fun removeCustomSwatch(swatchId: Int) {
        dao.deleteCustomSwatch(swatchId)
    }
}
