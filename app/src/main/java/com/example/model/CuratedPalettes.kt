package com.example.model

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object CuratedPalettes {

    val allPalettes: List<ColorPaletteModel> = listOf(
        ColorPaletteModel(
            id = "cappadocia_sunset",
            nameTr = "Kapadokya Gün Batımı",
            moodTr = "Sıcak toprak, kızıl vadi ve altın saat tonları",
            colors = listOf(
                0xFFE85D4AL, // Terracotta Coral
                0xFFF4A261L, // Warm Apricot
                0xFFE9C46AL, // Golden Ochre
                0xFF2A9D8FL, // Turquoise Balloon
                0xFF264653L, // Deep Canyon Slate
                0xFF9B5DE5L, // Twilight Violet
                0xFFF15BB5L, // Rose Horizon
                0xFFF7EDE2L  // Soft Tuff Cream
            )
        ),
        ColorPaletteModel(
            id = "aegean_breeze",
            nameTr = "Ege Kıyıları",
            moodTr = "Serin deniz köpüğü, begonvil ve çivit mavisi",
            colors = listOf(
                0xFF0077B6L, // Aegean Cobalt
                0xFF00B4D8L, // Clear Lagoon
                0xFF90E0EFL, // Seafoam Sky
                0xFFFF5D8FL, // Bougainvillea Pink
                0xFFFFB703L, // Sunlit Citrus
                0xFFFB8500L, // Warm Amber
                0xFF023E8AL, // Deep Midnight Sea
                0xFFCAF0F8L  // Whitewashed Pearl
            )
        ),
        ColorPaletteModel(
            id = "botanical_garden",
            nameTr = "Botanik Bahçe",
            moodTr = "Zümrüt yapraklar, adaçayı ve egzotik orkideler",
            colors = listOf(
                0xFF2D6A4FL, // Deep Monstera Green
                0xFF52B788L, // Fresh Fern
                0xFF95D5B2L, // Mint Leaf
                0xFFD8F3DCL, // Dew Mist
                0xFFD90429L, // Crimson Hibiscus
                0xFFFF758FL, // Orchid Blush
                0xFFFFB703L, // Pollen Gold
                0xFF7F4F24L  // Terracotta Pot
            )
        ),
        ColorPaletteModel(
            id = "stained_cathedral",
            nameTr = "Vitray Katedrali",
            moodTr = "Mücevher parlaklığında yakut, safir ve kehribar",
            colors = listOf(
                0xFFD90429L, // Ruby Glass
                0xFFFF9F1CL, // Amber Glow
                0xFFFFBF69L, // Sunbeam Gold
                0xFF2EC4B6L, // Emerald Shard
                0xFF3A86FFL, // Sapphire Pane
                0xFF8338ECL, // Amethyst Arch
                0xFFFF006EL, // Magenta Rose
                0xFF1D3557L  // Lead Frame Navy
            )
        ),
        ColorPaletteModel(
            id = "cosmic_nebula",
            nameTr = "Kozmik Nebula",
            moodTr = "Yıldız tozu, galaksi moru ve siber camgöbeği",
            colors = listOf(
                0xFF7209B7L, // Deep Nebula Purple
                0xFF3F37C9L, // Cosmic Indigo
                0xFF4CC9F0L, // Cyber Cyan
                0xFFF72585L, // Supernova Magenta
                0xFFB5179EL, // Astral Orchid
                0xFFFFBE0BL, // Starlight Gold
                0xFF06D6A0L, // Aurora Green
                0xFF10002BL  // Void Violet
            )
        ),
        ColorPaletteModel(
            id = "pastel_dreams",
            nameTr = "Pastel Rüyalar",
            moodTr = "Yumuşak lavanta, şeftali ve pamuk şeker dinginliği",
            colors = listOf(
                0xFFFFADADL, // Soft Berry
                0xFFFFD6A5L, // Peach Cream
                0xFFFDFFB6L, // Buttercup Lemon
                0xFFCAFFBF,  // Matcha Pastel
                0xFF9BF6FFL, // Baby Sky
                0xFFA0C4FFL, // Periwinkle Cloud
                0xFFBDB2FFL, // Sweet Lavender
                0xFFFFC6FFL  // Cotton Candy
            )
        ),
        ColorPaletteModel(
            id = "autumn_forest",
            nameTr = "Sonbahar Ormanı",
            moodTr = "Tarçın, kehribar yapraklar ve çam yeşili",
            colors = listOf(
                0xFFBC4749L, // Maple Red
                0xFFD4A373L, // Warm Fawn
                0xFFE9EDC9L, // Birch Bark
                0xFFCCD5AEL, // Sage Moss
                0xFF386641L, // Pine Needle
                0xFF6A994EL, // Woodland Fern
                0xFF9C6644L, // Chestnut Bark
                0xFFFB8500L  // Pumpkin Spice
            )
        ),
        ColorPaletteModel(
            id = "nordic_minimal",
            nameTr = "İskandinav Minimal",
            moodTr = "Modern mimari, keten, zeytin ve dingin toprak",
            colors = listOf(
                0xFF3D405BL, // Charcoal Slate
                0xFFE07A5FL, // Muted Clay
                0xFFF4F1DEL, // Warm Linen
                0xFF81B29AL, // Nordic Sage
                0xFFF2CC8FL, // Honey Sand
                0xFF457B9DL, // Fjord Blue
                0xFFA8DADCL, // Glacier Mist
                0xFF6D597AL  // Dusty Plum
            )
        )
    )

    fun findById(id: String): ColorPaletteModel {
        return allPalettes.find { it.id == id } ?: allPalettes.first()
    }

    fun hslToArgbLong(hue: Float, saturation: Float, lightness: Float): Long {
        val c = (1f - abs(2f * lightness - 1f)) * saturation
        val x = c * (1f - abs((hue / 60f) % 2f - 1f))
        val m = lightness - c / 2f
        val (r1, g1, b1) = when {
            hue < 60f -> Triple(c, x, 0f)
            hue < 120f -> Triple(x, c, 0f)
            hue < 180f -> Triple(0f, c, x)
            hue < 240f -> Triple(0f, x, c)
            hue < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        val r = ((r1 + m) * 255f).toInt().coerceIn(0, 255)
        val g = ((g1 + m) * 255f).toInt().coerceIn(0, 255)
        val b = ((b1 + m) * 255f).toInt().coerceIn(0, 255)
        return (0xFF000000L) or ((r.toLong() and 0xFF) shl 16) or ((g.toLong() and 0xFF) shl 8) or (b.toLong() and 0xFF)
    }

    fun argbToHex(argb: Long): String {
        val rgb = argb and 0xFFFFFFL
        return String.format("#%06X", rgb)
    }

    fun isColorDark(argb: Long): Boolean {
        val r = ((argb shr 16) and 0xFF) / 255.0
        val g = ((argb shr 8) and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0
        val luminance = 0.299 * r + 0.587 * g + 0.114 * b
        return luminance < 0.52
    }
}
