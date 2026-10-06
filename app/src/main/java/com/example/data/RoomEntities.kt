package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.FillTexture
import com.example.model.FreehandStroke
import com.example.model.RegionFillState
import com.example.model.StrokePoint
import com.example.model.TimelapseStep

@Entity(tableName = "saved_artworks")
data class SavedArtworkEntity(
    @PrimaryKey val templateId: String,
    val fillsEncoded: String,          // Encoded Map<Int, RegionFillState>
    val strokesEncoded: String,        // Encoded List<FreehandStroke>
    val timelapseEncoded: String,      // Encoded List<TimelapseStep>
    val coloredCount: Int,
    val totalRegions: Int,
    val completionPercent: Int,        // 0..100
    val isCompleted: Boolean,
    val starsEarned: Int,              // 0..3
    val elapsedSeconds: Long,
    val selectedPaletteId: String,
    val lastModifiedMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_swatches")
data class CustomSwatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameTr: String,
    val colorArgb: Long,
    val createdAtMs: Long = System.currentTimeMillis()
)

object ArtworkProgressCodec {

    // Format: "regionId:colorArgb:textureName;regionId:colorArgb:textureName"
    fun encodeFills(fills: Map<Int, RegionFillState>): String {
        if (fills.isEmpty()) return ""
        return fills.values.joinToString(";") { state ->
            "${state.regionId}:${state.colorArgb}:${state.texture.name}"
        }
    }

    fun decodeFills(encoded: String): Map<Int, RegionFillState> {
        if (encoded.isBlank()) return emptyMap()
        val result = mutableMapOf<Int, RegionFillState>()
        encoded.split(";").forEach { token ->
            val parts = token.split(":")
            if (parts.size >= 3) {
                val regionId = parts[0].toIntOrNull()
                val colorArgb = parts[1].toLongOrNull()
                val texture = runCatching { FillTexture.valueOf(parts[2]) }.getOrDefault(FillTexture.SOLID)
                if (regionId != null && colorArgb != null) {
                    result[regionId] = RegionFillState(regionId, colorArgb, texture)
                }
            }
        }
        return result
    }

    // Format: "id|regionIdOrNull|colorArgb|strokeWidth|alpha|x1,y1~x2,y2;..."
    fun encodeStrokes(strokes: List<FreehandStroke>): String {
        if (strokes.isEmpty()) return ""
        return strokes.takeLast(120).joinToString(";") { stroke ->
            val regionPart = stroke.regionId?.toString() ?: "none"
            val ptsPart = stroke.points.joinToString("~") { pt ->
                "${(pt.x * 10).toInt()},${(pt.y * 10).toInt()}"
            }
            "${stroke.id}|$regionPart|${stroke.colorArgb}|${stroke.strokeWidth}|${stroke.alpha}|$ptsPart"
        }
    }

    fun decodeStrokes(encoded: String): List<FreehandStroke> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split(";").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size < 6) return@mapNotNull null
            val id = parts[0].toLongOrNull() ?: return@mapNotNull null
            val regionId = if (parts[1] == "none") null else parts[1].toIntOrNull()
            val colorArgb = parts[2].toLongOrNull() ?: return@mapNotNull null
            val width = parts[3].toFloatOrNull() ?: 20f
            val alpha = parts[4].toFloatOrNull() ?: 0.85f
            val points = parts[5].split("~").mapNotNull { pStr ->
                val xy = pStr.split(",")
                if (xy.size == 2) {
                    val x = (xy[0].toIntOrNull() ?: 0) / 10f
                    val y = (xy[1].toIntOrNull() ?: 0) / 10f
                    StrokePoint(x, y)
                } else null
            }
            if (points.isEmpty()) null else FreehandStroke(id, regionId, colorArgb, width, alpha, points)
        }
    }

    // Format: "regionId:colorArgb:textureName;..."
    fun encodeTimelapse(steps: List<TimelapseStep>): String {
        if (steps.isEmpty()) return ""
        return steps.takeLast(250).joinToString(";") { step ->
            "${step.regionId}:${step.colorArgb}:${step.texture.name}"
        }
    }

    fun decodeTimelapse(encoded: String): List<TimelapseStep> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split(";").mapNotNull { token ->
            val parts = token.split(":")
            if (parts.size >= 3) {
                val regionId = parts[0].toIntOrNull() ?: return@mapNotNull null
                val colorArgb = parts[1].toLongOrNull() ?: return@mapNotNull null
                val texture = runCatching { FillTexture.valueOf(parts[2]) }.getOrDefault(FillTexture.SOLID)
                TimelapseStep(regionId, colorArgb, texture)
            } else null
        }
    }
}
