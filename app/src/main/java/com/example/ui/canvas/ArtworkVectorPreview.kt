package com.example.ui.canvas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ArtworkTemplate
import com.example.model.FreehandStroke
import com.example.model.RegionFillState
import com.example.model.TimelapseStep
import kotlinx.coroutines.delay

@Composable
fun ArtworkVectorPreview(
    template: ArtworkTemplate,
    fills: Map<Int, RegionFillState>,
    strokes: List<FreehandStroke> = emptyList(),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        CanvasHitTester.drawArtworkOnAndroidCanvas(
            canvas = drawContext.canvas.nativeCanvas,
            canvasWidth = size.width,
            canvasHeight = size.height,
            template = template,
            fills = fills,
            strokes = strokes,
            showNumbers = false
        )
    }
}

@Composable
fun TimelapseReplayDialog(
    template: ArtworkTemplate,
    steps: List<TimelapseStep>,
    onDismiss: () -> Unit
) {
    val totalSteps = steps.size
    var currentStepIndex by remember(steps) { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var speedMultiplier by remember { mutableIntStateOf(1) }

    LaunchedEffect(isPlaying, currentStepIndex, speedMultiplier, totalSteps) {
        if (isPlaying && totalSteps > 0) {
            if (currentStepIndex >= totalSteps) {
                isPlaying = false
            } else {
                val frameDelay = (260L / speedMultiplier).coerceAtLeast(45L)
                delay(frameDelay)
                currentStepIndex = (currentStepIndex + 1).coerceAtMost(totalSteps)
            }
        }
    }

    // Build partial fill map up to currentStepIndex
    val replayFills = remember(steps, currentStepIndex) {
        val map = mutableMapOf<Int, RegionFillState>()
        for (i in 0 until currentStepIndex.coerceAtMost(steps.size)) {
            val s = steps[i]
            map[s.regionId] = RegionFillState(s.regionId, s.colorArgb, s.texture)
        }
        map
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .testTag("timelapse_replay_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Canlı Zaman Atlama (Timelapse)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = template.titleTr,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_timelapse_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Framed Canvas Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFFBF8F3))
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ArtworkVectorPreview(
                        template = template,
                        fills = replayFills,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Step badge overlay
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "Adım $currentStepIndex / $totalSteps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (totalSteps > 0) {
                    Slider(
                        value = currentStepIndex.toFloat(),
                        onValueChange = {
                            isPlaying = false
                            currentStepIndex = it.toInt()
                        },
                        valueRange = 0f..totalSteps.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("timelapse_slider")
                    )
                } else {
                    Text(
                        text = "Henüz kaydedilmiş boyama adımı yok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Playback controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = {
                            if (currentStepIndex >= totalSteps) {
                                currentStepIndex = 0
                                isPlaying = true
                            } else {
                                isPlaying = !isPlaying
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("timelapse_play_pause_button")
                    ) {
                        Icon(
                            imageVector = when {
                                currentStepIndex >= totalSteps -> Icons.Default.Replay
                                isPlaying -> Icons.Default.Pause
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = "Oynat veya Duraklat"
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 4).forEach { spd ->
                            FilterChip(
                                selected = speedMultiplier == spd,
                                onClick = { speedMultiplier = spd },
                                label = {
                                    Text(
                                        text = "${spd}x Hız",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
