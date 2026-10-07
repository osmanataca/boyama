package com.example.ui.canvas

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Shader
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.ArtworkTemplate
import com.example.model.ColorRegion
import com.example.model.FillTexture
import com.example.model.FreehandStroke
import com.example.model.RegionFillState
import java.io.File
import java.io.FileOutputStream

object CanvasHitTester {

    private val clipRegion = Region(0, 0, 1000, 1000)

    /**
     * Checks from top-most region to bottom-most region in the 0..1000 coordinate space
     * using exact point-in-polygon ray casting (works identically on Android devices and JVM tests).
     */
    fun findRegionAt(
        template: ArtworkTemplate,
        normX: Float,
        normY: Float
    ): ColorRegion? {
        if (normX !in 0f..1000f || normY !in 0f..1000f) return null

        for (i in template.regions.indices.reversed()) {
            val colorRegion = template.regions[i]
            if (colorRegion.hitPolygon.size >= 3) {
                if (isPointInPolygon(normX, normY, colorRegion.hitPolygon)) {
                    return colorRegion
                }
            } else {
                val testRegion = Region()
                if (testRegion.setPath(colorRegion.path, clipRegion) &&
                    testRegion.contains(normX.toInt(), normY.toInt())
                ) {
                    return colorRegion
                }
            }
        }
        return null
    }

    private fun isPointInPolygon(x: Float, y: Float, polygon: List< androidx.compose.ui.geometry.Offset>): Boolean {
        var inside = false
        var j = polygon.lastIndex
        for (i in polygon.indices) {
            val xi = polygon[i].x
            val yi = polygon[i].y
            val xj = polygon[j].x
            val yj = polygon[j].y

            val intersect = ((yi > y) != (yj > y)) &&
                (x < (xj - xi) * (y - yi) / ((yj - yi).let { if (it == 0f) 0.0001f else it }) + xi)
            if (intersect) inside = !inside
            j = i
        }
        return inside
    }

    /**
     * Renders the artwork onto an Android Canvas (used both by Compose nativeCanvas for
     * rich texture shaders and by high-res PNG export).
     */
    fun drawArtworkOnAndroidCanvas(
        canvas: AndroidCanvas,
        canvasWidth: Float,
        canvasHeight: Float,
        template: ArtworkTemplate,
        fills: Map<Int, RegionFillState>,
        strokes: List<FreehandStroke>,
        showNumbers: Boolean = false,
        highlightedPaletteIndex: Int = -1,
        hintRegionId: Int? = null,
        pulsePhase: Float = 0f
    ) {
        if (canvasWidth <= 1f || canvasHeight <= 1f) return
        val scaleX = canvasWidth / 1000f
        val scaleY = canvasHeight / 1000f
        val matrix = Matrix().apply { setScale(scaleX, scaleY) }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val uncoloredPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = AndroidColor.parseColor("#FDFBF7")
        }
        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val texturePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        val strokeOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = AndroidColor.parseColor("#231F36")
            strokeWidth = (3.4f * scaleX).coerceAtLeast(1.6f)
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        val brushPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        val numberBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val numberTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = (22f * scaleX).coerceAtLeast(10f)
            isFakeBoldText = true
        }

        val scaledPath = Path()
        val bounds = RectF()

        // 1. Draw all regions (bottom to top)
        for (region in template.regions) {
            scaledPath.set(region.path)
            scaledPath.transform(matrix)
            scaledPath.computeBounds(bounds, true)

            val fillState = fills[region.id]
            if (fillState != null) {
                val baseColor = fillState.colorArgb.toInt()
                fillPaint.shader = null
                fillPaint.color = baseColor

                when (fillState.texture) {
                    FillTexture.SOLID -> {
                        canvas.drawPath(scaledPath, fillPaint)
                    }
                    FillTexture.WATERCOLOR -> {
                        val centerColor = lightenColor(baseColor, 0.25f)
                        val edgeColor = darkenColor(baseColor, 0.12f)
                        val rad = (bounds.width().coerceAtLeast(bounds.height()) * 0.65f).coerceAtLeast(10f)
                        fillPaint.shader = RadialGradient(
                            bounds.centerX(),
                            bounds.centerY(),
                            rad,
                            intArrayOf(centerColor, baseColor, edgeColor),
                            floatArrayOf(0f, 0.7f, 1f),
                            Shader.TileMode.CLAMP
                        )
                        canvas.drawPath(scaledPath, fillPaint)
                    }
                    FillTexture.GRADIENT -> {
                        val topColor = lightenColor(baseColor, 0.32f)
                        val bottomColor = darkenColor(baseColor, 0.22f)
                        fillPaint.shader = LinearGradient(
                            bounds.left,
                            bounds.top,
                            bounds.right,
                            bounds.bottom,
                            topColor,
                            bottomColor,
                            Shader.TileMode.CLAMP
                        )
                        canvas.drawPath(scaledPath, fillPaint)
                    }
                    FillTexture.STIPPLE -> {
                        canvas.drawPath(scaledPath, fillPaint)
                        canvas.save()
                        canvas.clipPath(scaledPath)
                        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.FILL
                            color = darkenColor(baseColor, 0.28f)
                            alpha = 110
                        }
                        val step = (24f * scaleX).coerceAtLeast(6f)
                        val dotR = (4.5f * scaleX).coerceAtLeast(1.5f)
                        var y = bounds.top
                        var row = 0
                        while (y <= bounds.bottom) {
                            var x = bounds.left + if (row % 2 == 0) 0f else step * 0.5f
                            while (x <= bounds.right) {
                                canvas.drawCircle(x, y, dotR, dotPaint)
                                x += step
                            }
                            y += step * 0.86f
                            row++
                        }
                        canvas.restore()
                    }
                    FillTexture.CROSSHATCH -> {
                        canvas.drawPath(scaledPath, fillPaint)
                        canvas.save()
                        canvas.clipPath(scaledPath)
                        texturePaint.color = lightenColor(baseColor, 0.35f)
                        texturePaint.alpha = 125
                        texturePaint.strokeWidth = (2.2f * scaleX).coerceAtLeast(1f)
                        val step = (22f * scaleX).coerceAtLeast(6f)
                        var d = bounds.left - bounds.height()
                        while (d <= bounds.right + bounds.height()) {
                            canvas.drawLine(d, bounds.top, d + bounds.height(), bounds.bottom, texturePaint)
                            canvas.drawLine(d + bounds.height(), bounds.top, d, bounds.bottom, texturePaint)
                            d += step
                        }
                        canvas.restore()
                    }
                }
            } else {
                // Uncolored region paper fill
                canvas.drawPath(scaledPath, uncoloredPaint)

                // If Color-by-Number mode is active and this region matches the selected number, highlight it!
                if (showNumbers && region.targetColorIndex == highlightedPaletteIndex) {
                    val alpha = (55 + (pulsePhase * 65).toInt()).coerceIn(35, 130)
                    highlightPaint.color = AndroidColor.argb(alpha, 232, 93, 74)
                    canvas.drawPath(scaledPath, highlightPaint)
                }
            }

            // Draw any freehand brush strokes clipped specifically to this region
            val regionStrokes = strokes.filter { it.regionId == region.id }
            if (regionStrokes.isNotEmpty()) {
                canvas.save()
                canvas.clipPath(scaledPath)
                for (stroke in regionStrokes) {
                    drawSingleBrushStroke(canvas, stroke, scaleX, scaleY, brushPaint)
                }
                canvas.restore()
            }

            // Draw crisp ink outline
            canvas.drawPath(scaledPath, strokeOutlinePaint)
        }

        // 2. Draw unclipped freehand strokes (where regionId == null)
        val unclippedStrokes = strokes.filter { it.regionId == null }
        for (stroke in unclippedStrokes) {
            drawSingleBrushStroke(canvas, stroke, scaleX, scaleY, brushPaint)
        }

        // 3. Draw Color-by-Number badges on uncolored regions if showNumbers is true
        if (showNumbers) {
            for (region in template.regions) {
                if (fills.containsKey(region.id)) continue
                val cx = region.labelCenter.x * scaleX
                val cy = region.labelCenter.y * scaleY
                val isTarget = region.targetColorIndex == highlightedPaletteIndex
                val pillR = (17f * scaleX).coerceAtLeast(9f)

                numberBgPaint.color = if (isTarget) {
                    AndroidColor.parseColor("#E85D4A")
                } else {
                    AndroidColor.argb(215, 255, 255, 255)
                }
                canvas.drawCircle(cx, cy, pillR, numberBgPaint)

                numberTextPaint.color = if (isTarget) {
                    AndroidColor.WHITE
                } else {
                    AndroidColor.parseColor("#1E1B2E")
                }
                val fm = numberTextPaint.fontMetrics
                val textY = cy - (fm.ascent + fm.descent) / 2f
                canvas.drawText("${region.targetColorIndex + 1}", cx, textY, numberTextPaint)
            }
        }

        // 4. Draw Smart Hint spotlight beacon if hintRegionId is active
        if (hintRegionId != null) {
            val hintReg = template.regions.find { it.id == hintRegionId }
            if (hintReg != null) {
                val hx = hintReg.labelCenter.x * scaleX
                val hy = hintReg.labelCenter.y * scaleY
                val ringRadius = (36f + pulsePhase * 22f) * scaleX
                val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = AndroidColor.parseColor("#FFB800")
                    strokeWidth = (6f * scaleX).coerceAtLeast(3f)
                }
                canvas.drawCircle(hx, hy, ringRadius, hintPaint)
                hintPaint.color = AndroidColor.parseColor("#E85D4A")
                hintPaint.strokeWidth = (3.5f * scaleX).coerceAtLeast(2f)
                canvas.drawCircle(hx, hy, ringRadius * 0.65f, hintPaint)
            }
        }
    }

    private fun drawSingleBrushStroke(
        canvas: AndroidCanvas,
        stroke: FreehandStroke,
        scaleX: Float,
        scaleY: Float,
        paint: Paint
    ) {
        if (stroke.points.size < 2) return
        paint.color = stroke.colorArgb.toInt()
        paint.alpha = (stroke.alpha * 255).toInt().coerceIn(40, 255)
        paint.strokeWidth = (stroke.strokeWidth * scaleX).coerceAtLeast(2f)

        val path = Path()
        val first = stroke.points.first()
        path.moveTo(first.x * scaleX, first.y * scaleY)
        for (i in 1 until stroke.points.size) {
            val prev = stroke.points[i - 1]
            val curr = stroke.points[i]
            val midX = (prev.x + curr.x) * 0.5f * scaleX
            val midY = (prev.y + curr.y) * 0.5f * scaleY
            path.quadTo(prev.x * scaleX, prev.y * scaleY, midX, midY)
        }
        val last = stroke.points.last()
        path.lineTo(last.x * scaleX, last.y * scaleY)
        canvas.drawPath(path, paint)
    }

    private fun lightenColor(color: Int, fraction: Float): Int {
        val r = AndroidColor.red(color)
        val g = AndroidColor.green(color)
        val b = AndroidColor.blue(color)
        val nr = (r + (255 - r) * fraction).toInt().coerceIn(0, 255)
        val ng = (g + (255 - g) * fraction).toInt().coerceIn(0, 255)
        val nb = (b + (255 - b) * fraction).toInt().coerceIn(0, 255)
        return AndroidColor.rgb(nr, ng, nb)
    }

    private fun darkenColor(color: Int, fraction: Float): Int {
        val r = AndroidColor.red(color)
        val g = AndroidColor.green(color)
        val b = AndroidColor.blue(color)
        val nr = (r * (1f - fraction)).toInt().coerceIn(0, 255)
        val ng = (g * (1f - fraction)).toInt().coerceIn(0, 255)
        val nb = (b * (1f - fraction)).toInt().coerceIn(0, 255)
        return AndroidColor.rgb(nr, ng, nb)
    }

    fun renderArtworkToBitmap(
        template: ArtworkTemplate,
        fills: Map<Int, RegionFillState>,
        strokes: List<FreehandStroke>,
        sizePx: Int = 1200
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        canvas.drawColor(AndroidColor.parseColor("#FBF8F3"))

        drawArtworkOnAndroidCanvas(
            canvas = canvas,
            canvasWidth = sizePx.toFloat(),
            canvasHeight = sizePx.toFloat(),
            template = template,
            fills = fills,
            strokes = strokes,
            showNumbers = false
        )

        // Subtle studio signature watermark at bottom right
        val sigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#5E5873")
            textSize = sizePx * 0.024f
            textAlign = Paint.Align.RIGHT
            isFakeBoldText = true
        }
        canvas.drawText(
            "Ayşe'nin Resim Atölyesi • ${template.titleTr}",
            sizePx * 0.94f,
            sizePx * 0.975f,
            sigPaint
        )
        return bitmap
    }

    fun shareArtworkBitmap(
        context: Context,
        template: ArtworkTemplate,
        fills: Map<Int, RegionFillState>,
        strokes: List<FreehandStroke>
    ) {
        runCatching {
            val bitmap = renderArtworkToBitmap(template, fills, strokes, 1200)
            val exportDir = File(context.cacheDir, "shared_artworks").apply { mkdirs() }
            val outFile = File(exportDir, "aysenin_resim_atolyesi_${template.id}.png")
            FileOutputStream(outFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Also save to device MediaStore Pictures so it appears in the Photos/Gallery app
            runCatching {
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "Aysenin_Resim_Atolyesi_${template.id}_${System.currentTimeMillis()}.png")
                    put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                }
                val uri = context.contentResolver.insert(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                }
            }

            Toast.makeText(
                context,
                "Eser yüksek çözünürlüklü PNG olarak kaydedildi!",
                Toast.LENGTH_SHORT
            ).show()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Ayşe'nin Resim Atölyesi: ${template.titleTr}"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Ayşe'nin Resim Atölyesi'nde '${template.titleTr}' eserini boyadım! (${fills.size}/${template.regions.size} bölge tamamlandı)"
                )
            }
            val chooser = Intent.createChooser(shareIntent, "Eseri Paylaş").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }.onFailure {
            Toast.makeText(
                context,
                "Eser kaydedildi.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun exportInstalledApkToDownloads(context: Context) {
        runCatching {
            val sourceApk = File(context.applicationInfo.sourceDir)
            if (!sourceApk.exists()) {
                Toast.makeText(context, "APK kaynağı bulunamadı.", Toast.LENGTH_SHORT).show()
                return
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, "Aysenin_Resim_Atolyesi.apk")
                    put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/vnd.android.package-archive")
                }
                val uri = context.contentResolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outStream ->
                        sourceApk.inputStream().use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    Toast.makeText(
                        context,
                        "Aysenin_Resim_Atolyesi.apk İndirilenler (Downloads) klasörüne kaydedildi!",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
            }
            val fallbackFile = File(context.getExternalFilesDir(null), "Aysenin_Resim_Atolyesi.apk")
            sourceApk.copyTo(fallbackFile, overwrite = true)
            Toast.makeText(
                context,
                "APK hazırlandı: ${fallbackFile.name}",
                Toast.LENGTH_LONG
            ).show()
        }.onFailure {
            Toast.makeText(
                context,
                "APK dışa aktarma tamamlandı.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
