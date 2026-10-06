package com.example.ui.canvas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.model.ArtworkTemplate
import com.example.model.ColoringMode
import com.example.model.FreehandStroke
import com.example.model.RegionFillState
import com.example.model.StrokePoint
import com.example.model.toComposeColor

private data class SplashRipple(
    val normX: Float,
    val normY: Float,
    val color: Color
)

@Composable
fun InteractiveColoringCanvas(
    template: ArtworkTemplate,
    fills: Map<Int, RegionFillState>,
    strokes: List<FreehandStroke>,
    coloringMode: ColoringMode,
    selectedColorArgb: Long,
    selectedPaletteIndex: Int,
    brushSize: Float,
    stayInsideLines: Boolean,
    hintRegionId: Int?,
    onRegionHit: (regionId: Int, isNewGesture: Boolean) -> Unit,
    onAddFreehandStroke: (regionId: Int?, points: List<StrokePoint>) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasBoxSize by remember { mutableStateOf(IntSize.Zero) }
    var zoomScale by remember(template.id) { mutableFloatStateOf(1f) }
    var panOffset by remember(template.id) { mutableStateOf(Offset.Zero) }
    var isPanToolActive by remember { mutableStateOf(false) }

    // Live freehand stroke preview state
    val liveStrokePoints = remember { mutableStateListOf<StrokePoint>() }
    var liveStrokeRegionId by remember { mutableStateOf<Int?>(null) }

    // Ripple animation when a region is filled
    var activeSplash by remember { mutableStateOf<SplashRipple?>(null) }
    val splashProgress = remember { Animatable(0f) }

    LaunchedEffect(activeSplash) {
        if (activeSplash != null) {
            splashProgress.snapTo(0f)
            splashProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 360)
            )
            activeSplash = null
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "canvas_pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_phase"
    )

    // Helper to convert a pointer position inside the un-transformed outer box to 0..1000 normalized coordinates
    fun screenToNormalized(touch: Offset, size: IntSize, scale: Float, pan: Offset): Offset {
        if (size.width <= 0 || size.height <= 0) return Offset.Zero
        val cx = size.width / 2f
        val cy = size.height / 2f
        // graphicsLayer applies translation after scaling around center
        val contentX = (touch.x - cx - pan.x) / scale + cx
        val contentY = (touch.y - cy - pan.y) / scale + cy
        return Offset(
            x = (contentX / size.width) * 1000f,
            y = (contentY / size.height) * 1000f
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFFF3EDE2))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                shape = RoundedCornerShape(26.dp)
            )
            .onSizeChanged { canvasBoxSize = it }
            .testTag("interactive_coloring_canvas")
            .pointerInput(
                template.id,
                coloringMode,
                selectedColorArgb,
                brushSize,
                stayInsideLines,
                isPanToolActive
            ) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startNorm = screenToNormalized(down.position, canvasBoxSize, zoomScale, panOffset)

                    if (isPanToolActive) {
                        // Pan / Zoom gesture loop
                        do {
                            val event = awaitPointerEvent()
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            val newScale = (zoomScale * zoomChange).coerceIn(1f, 4f)
                            val maxPanX = (canvasBoxSize.width * (newScale - 1f)) / 2f
                            val maxPanY = (canvasBoxSize.height * (newScale - 1f)) / 2f
                            zoomScale = newScale
                            panOffset = Offset(
                                x = (panOffset.x + panChange.x).coerceIn(-maxPanX, maxPanX),
                                y = (panOffset.y + panChange.y).coerceIn(-maxPanY, maxPanY)
                            )
                            event.changes.forEach { it.consume() }
                        } while (event.changes.any { it.pressed })
                        return@awaitEachGesture
                    }

                    // Standard Painting or Multi-touch Pinch-Zoom
                    if (coloringMode == ColoringMode.FREE_BRUSH) {
                        val startReg = CanvasHitTester.findRegionAt(template, startNorm.x, startNorm.y)
                        liveStrokeRegionId = if (stayInsideLines) startReg?.id else null
                        liveStrokePoints.clear()
                        liveStrokePoints.add(StrokePoint(startNorm.x, startNorm.y))

                        do {
                            val event = awaitPointerEvent()
                            if (event.changes.size >= 2) {
                                // Switch to multi-touch zoom if second finger lands
                                liveStrokePoints.clear()
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                val newScale = (zoomScale * zoomChange).coerceIn(1f, 4f)
                                val maxPanX = (canvasBoxSize.width * (newScale - 1f)) / 2f
                                val maxPanY = (canvasBoxSize.height * (newScale - 1f)) / 2f
                                zoomScale = newScale
                                panOffset = Offset(
                                    x = (panOffset.x + panChange.x).coerceIn(-maxPanX, maxPanX),
                                    y = (panOffset.y + panChange.y).coerceIn(-maxPanY, maxPanY)
                                )
                            } else {
                                val pos = event.changes.first().position
                                val ptNorm = screenToNormalized(pos, canvasBoxSize, zoomScale, panOffset)
                                if (ptNorm.x in 0f..1000f && ptNorm.y in 0f..1000f) {
                                    liveStrokePoints.add(StrokePoint(ptNorm.x, ptNorm.y))
                                }
                            }
                            event.changes.forEach { it.consume() }
                        } while (event.changes.any { it.pressed })

                        if (liveStrokePoints.size >= 2) {
                            onAddFreehandStroke(liveStrokeRegionId, liveStrokePoints.toList())
                        }
                        liveStrokePoints.clear()
                    } else {
                        // SMART_FILL or COLOR_BY_NUMBER
                        var lastHitRegionId: Int? = null
                        val initialHit = CanvasHitTester.findRegionAt(template, startNorm.x, startNorm.y)
                        if (initialHit != null) {
                            lastHitRegionId = initialHit.id
                            onRegionHit(initialHit.id, true)
                            activeSplash = SplashRipple(
                                normX = startNorm.x,
                                normY = startNorm.y,
                                color = selectedColorArgb.toComposeColor()
                            )
                        }

                        do {
                            val event = awaitPointerEvent()
                            if (event.changes.size >= 2) {
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                val newScale = (zoomScale * zoomChange).coerceIn(1f, 4f)
                                val maxPanX = (canvasBoxSize.width * (newScale - 1f)) / 2f
                                val maxPanY = (canvasBoxSize.height * (newScale - 1f)) / 2f
                                zoomScale = newScale
                                panOffset = Offset(
                                    x = (panOffset.x + panChange.x).coerceIn(-maxPanX, maxPanX),
                                    y = (panOffset.y + panChange.y).coerceIn(-maxPanY, maxPanY)
                                )
                            } else {
                                val pos = event.changes.first().position
                                val moveNorm = screenToNormalized(pos, canvasBoxSize, zoomScale, panOffset)
                                val moveHit = CanvasHitTester.findRegionAt(template, moveNorm.x, moveNorm.y)
                                if (moveHit != null && moveHit.id != lastHitRegionId) {
                                    lastHitRegionId = moveHit.id
                                    onRegionHit(moveHit.id, false)
                                }
                            }
                            event.changes.forEach { it.consume() }
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
    ) {
        // Vector Canvas with Zoom & Pan transform
        val combinedStrokes = if (liveStrokePoints.size >= 2) {
            strokes + FreehandStroke(
                id = -1L,
                regionId = liveStrokeRegionId,
                colorArgb = selectedColorArgb,
                strokeWidth = brushSize,
                alpha = 0.88f,
                points = liveStrokePoints.toList()
            )
        } else {
            strokes
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .graphicsLayer {
                    scaleX = zoomScale
                    scaleY = zoomScale
                    translationX = panOffset.x
                    translationY = panOffset.y
                }
        ) {
            CanvasHitTester.drawArtworkOnAndroidCanvas(
                canvas = drawContext.canvas.nativeCanvas,
                canvasWidth = size.width,
                canvasHeight = size.height,
                template = template,
                fills = fills,
                strokes = combinedStrokes,
                showNumbers = coloringMode == ColoringMode.COLOR_BY_NUMBER,
                highlightedPaletteIndex = selectedPaletteIndex,
                hintRegionId = hintRegionId,
                pulsePhase = pulsePhase
            )

            // Draw tap splash ring if active
            val splash = activeSplash
            if (splash != null) {
                val sx = (splash.normX / 1000f) * size.width
                val sy = (splash.normY / 1000f) * size.height
                val radius = 14f + splashProgress.value * 58f
                val alpha = (1f - splashProgress.value).coerceIn(0f, 1f)
                drawCircle(
                    color = splash.color.copy(alpha = alpha * 0.55f),
                    radius = radius,
                    center = Offset(sx, sy)
                )
            }
        }

        // Floating Zoom & Pan HUD Controls (Bottom-End of Canvas)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Toggle Pan/Hand Mode
                IconButton(
                    onClick = { isPanToolActive = !isPanToolActive },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPanToolActive) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                        .testTag("toggle_pan_tool_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PanTool,
                        contentDescription = "Kaydırma Aracı",
                        tint = if (isPanToolActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Zoom Out
                IconButton(
                    onClick = {
                        val next = (zoomScale - 0.5f).coerceAtLeast(1f)
                        zoomScale = next
                        if (next == 1f) panOffset = Offset.Zero
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("zoom_out_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Uzaklaştır",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Current Zoom Badge / Reset
                Surface(
                    onClick = {
                        zoomScale = 1f
                        panOffset = Offset.Zero
                        isPanToolActive = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("zoom_reset_button")
                ) {
                    Text(
                        text = "${(zoomScale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                // Zoom In
                IconButton(
                    onClick = {
                        zoomScale = (zoomScale + 0.5f).coerceAtMost(4f)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("zoom_in_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Yakınlaştır",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
