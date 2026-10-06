package com.example.model

import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class ArtCategory(val titleTr: String, val subtitleTr: String) {
    ALL("Tümü", "Tüm koleksiyon"),
    MANDALA("Mandala & Zen", "Simetrik ve meditatif desenler"),
    NATURE("Doğa & Botanik", "Çiçekler, yapraklar ve manzaralar"),
    ANIMALS("Hayvanlar Alemi", "Büyülü canlılar ve kuşlar"),
    STAINED_GLASS("Vitray & Mozaik", "Geometrik ışık ve cam sanatı"),
    COSMIC("Fantastik & Uzay", "Galaksiler, kristaller ve mitoloji")
}

enum class DifficultyLevel(val labelTr: String, val stars: Int) {
    EASY("Kolay", 1),
    MEDIUM("Orta", 2),
    EXPERT("Uzman", 3)
}

enum class FillTexture(val labelTr: String, val shortTr: String) {
    SOLID("Mat Gouache", "Mat"),
    WATERCOLOR("Suluboya Efekti", "Suluboya"),
    GRADIENT("İpek Gradyan", "Gradyan"),
    STIPPLE("Noktalı Doku", "Noktalı"),
    CROSSHATCH("Gravür Çizgi", "Çizgili")
}

enum class ColoringMode(val labelTr: String, val descriptionTr: String) {
    SMART_FILL("Akıllı Dolgu", "Dokunarak veya sürükleyerek bölgeleri boyayın"),
    COLOR_BY_NUMBER("Sayılarla Boya", "Numaralı renk rehberini takip edin"),
    FREE_BRUSH("Sınır Korumalı Fırça", "Bölge dışına taşmayan serbest fırça")
}

data class ColorPaletteModel(
    val id: String,
    val nameTr: String,
    val moodTr: String,
    val colors: List<Long> // ARGB Long values e.g. 0xFFE85D4A
)

data class RegionFillState(
    val regionId: Int,
    val colorArgb: Long,
    val texture: FillTexture = FillTexture.SOLID
)

data class StrokePoint(
    val x: Float, // Normalized 0..1000
    val y: Float  // Normalized 0..1000
)

data class FreehandStroke(
    val id: Long,
    val regionId: Int?, // If non-null, clipped to that region's path!
    val colorArgb: Long,
    val strokeWidth: Float, // In 1000x1000 canvas units (e.g., 14f..48f)
    val alpha: Float,
    val points: List<StrokePoint>
)

data class TimelapseStep(
    val regionId: Int,
    val colorArgb: Long,
    val texture: FillTexture,
    val timestampMs: Long = System.currentTimeMillis()
)

data class ColorRegion(
    val id: Int,
    val nameTr: String,
    val targetColorIndex: Int, // 0..7 index in the template's recommended palette
    val labelCenter: Offset,   // In 0..1000 coordinate space
    val path: Path,            // Android graphics Path in 0..1000 coordinate space
    val hitPolygon: List<Offset> = emptyList()
)

data class ArtworkTemplate(
    val id: String,
    val titleTr: String,
    val artistNoteTr: String,
    val category: ArtCategory,
    val difficulty: DifficultyLevel,
    val defaultPaletteId: String,
    val isDailyFeatured: Boolean = false,
    val regions: List<ColorRegion>
)

data class CanvasHistorySnapshot(
    val fills: Map<Int, RegionFillState>,
    val strokes: List<FreehandStroke>
)

data class AchievementBadge(
    val id: String,
    val titleTr: String,
    val descriptionTr: String,
    val progress: Int,
    val target: Int,
    val isUnlocked: Boolean,
    val accentColorArgb: Long
)

fun Long.toComposeColor(): Color = Color((this and 0xFFFFFFFFL) or 0xFF000000L)
