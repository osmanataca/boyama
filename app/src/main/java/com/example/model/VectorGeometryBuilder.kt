package com.example.model

import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class VectorShape(
    val path: Path,
    val hitPolygon: List<Offset>
)

object VectorGeometryBuilder {

    fun circleShape(cx: Float, cy: Float, radius: Float): VectorShape {
        val path = Path().apply {
            addCircle(cx, cy, radius, Path.Direction.CW)
        }
        val pts = (0 until 36).map { i ->
            val rad = (i * 10.0) * (PI / 180.0)
            Offset(
                x = cx + (radius * cos(rad)).toFloat(),
                y = cy + (radius * sin(rad)).toFloat()
            )
        }
        return VectorShape(path, pts)
    }

    fun ovalShape(cx: Float, cy: Float, rx: Float, ry: Float): VectorShape {
        val path = Path().apply {
            addOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), Path.Direction.CW)
        }
        val pts = (0 until 36).map { i ->
            val rad = (i * 10.0) * (PI / 180.0)
            Offset(
                x = cx + (rx * cos(rad)).toFloat(),
                y = cy + (ry * sin(rad)).toFloat()
            )
        }
        return VectorShape(path, pts)
    }

    fun roundedRectShape(left: Float, top: Float, right: Float, bottom: Float, cornerRadius: Float): VectorShape {
        val path = Path().apply {
            addRoundRect(
                RectF(left, top, right, bottom),
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
            )
        }
        val pts = listOf(
            Offset(left, top),
            Offset(right, top),
            Offset(right, bottom),
            Offset(left, bottom)
        )
        return VectorShape(path, pts)
    }

    fun polygonShape(points: List<Pair<Float, Float>>): VectorShape {
        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().first, points.first().second)
                for (i in 1 until points.size) {
                    lineTo(points[i].first, points[i].second)
                }
                close()
            }
        }
        val pts = points.map { Offset(it.first, it.second) }
        return VectorShape(path, pts)
    }

    fun regularPolygonShape(cx: Float, cy: Float, radius: Float, sides: Int, startAngleDeg: Float = -90f): VectorShape {
        val pts = (0 until sides).map { i ->
            val rad = (startAngleDeg + i * (360f / sides)) * (PI / 180.0)
            Pair(
                cx + (radius * cos(rad)).toFloat(),
                cy + (radius * sin(rad)).toFloat()
            )
        }
        return polygonShape(pts)
    }

    fun starShape(cx: Float, cy: Float, outerR: Float, innerR: Float, pointsCount: Int, startAngleDeg: Float = -90f): VectorShape {
        val pts = (0 until pointsCount * 2).map { i ->
            val r = if (i % 2 == 0) outerR else innerR
            val rad = (startAngleDeg + i * (180f / pointsCount)) * (PI / 180.0)
            Pair(
                cx + (r * cos(rad)).toFloat(),
                cy + (r * sin(rad)).toFloat()
            )
        }
        return polygonShape(pts)
    }

    fun petalShape(
        cx: Float,
        cy: Float,
        innerR: Float,
        outerR: Float,
        angleDeg: Float,
        spreadDeg: Float
    ): VectorShape {
        val radCenter = angleDeg * (PI / 180.0)
        val radLeft = (angleDeg - spreadDeg) * (PI / 180.0)
        val radRight = (angleDeg + spreadDeg) * (PI / 180.0)
        val midR = innerR + (outerR - innerR) * 0.58f

        val startX = cx + (innerR * cos(radLeft * 0.35 + radCenter * 0.65)).toFloat()
        val startY = cy + (innerR * sin(radLeft * 0.35 + radCenter * 0.65)).toFloat()

        val endX = cx + (innerR * cos(radRight * 0.35 + radCenter * 0.65)).toFloat()
        val endY = cy + (innerR * sin(radRight * 0.35 + radCenter * 0.65)).toFloat()

        val ctrlLeftX = cx + (midR * 1.12f * cos(radLeft)).toFloat()
        val ctrlLeftY = cy + (midR * 1.12f * sin(radLeft)).toFloat()

        val ctrlRightX = cx + (midR * 1.12f * cos(radRight)).toFloat()
        val ctrlRightY = cy + (midR * 1.12f * sin(radRight)).toFloat()

        val tipX = cx + (outerR * cos(radCenter)).toFloat()
        val tipY = cy + (outerR * sin(radCenter)).toFloat()

        val path = Path().apply {
            moveTo(startX, startY)
            quadTo(ctrlLeftX, ctrlLeftY, tipX, tipY)
            quadTo(ctrlRightX, ctrlRightY, endX, endY)
            close()
        }

        val pts = mutableListOf<Offset>()
        val steps = 12
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val u = 1f - t
            val x = u * u * startX + 2f * u * t * ctrlLeftX + t * t * tipX
            val y = u * u * startY + 2f * u * t * ctrlLeftY + t * t * tipY
            pts.add(Offset(x, y))
        }
        for (i in 1..steps) {
            val t = i / steps.toFloat()
            val u = 1f - t
            val x = u * u * tipX + 2f * u * t * ctrlRightX + t * t * endX
            val y = u * u * tipY + 2f * u * t * ctrlRightY + t * t * endY
            pts.add(Offset(x, y))
        }
        return VectorShape(path, pts)
    }

    fun ringSectorShape(
        cx: Float,
        cy: Float,
        innerR: Float,
        outerR: Float,
        startAngleDeg: Float,
        sweepAngleDeg: Float
    ): VectorShape {
        val outerRect = RectF(cx - outerR, cy - outerR, cx + outerR, cy + outerR)
        val innerRect = RectF(cx - innerR, cy - innerR, cx + innerR, cy + innerR)
        val path = Path().apply {
            arcTo(outerRect, startAngleDeg, sweepAngleDeg)
            arcTo(innerRect, startAngleDeg + sweepAngleDeg, -sweepAngleDeg)
            close()
        }
        val pts = mutableListOf<Offset>()
        val steps = 14
        for (i in 0..steps) {
            val ang = startAngleDeg + (sweepAngleDeg * i / steps)
            pts.add(polarOffset(cx, cy, outerR, ang))
        }
        for (i in steps downTo 0) {
            val ang = startAngleDeg + (sweepAngleDeg * i / steps)
            pts.add(polarOffset(cx, cy, innerR, ang))
        }
        return VectorShape(path, pts)
    }

    fun polarOffset(cx: Float, cy: Float, radius: Float, angleDeg: Float): Offset {
        val rad = angleDeg * (PI / 180.0)
        return Offset(
            x = cx + (radius * cos(rad)).toFloat(),
            y = cy + (radius * sin(rad)).toFloat()
        )
    }

    fun waveBandShape(
        left: Float,
        right: Float,
        topY: Float,
        bottomY: Float,
        amplitude: Float,
        waves: Int,
        phaseShift: Float = 0f
    ): VectorShape {
        val path = Path()
        val pts = mutableListOf<Offset>()
        val steps = waves * 16
        val width = right - left
        path.moveTo(left, bottomY)
        pts.add(Offset(left, bottomY))
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val x = left + t * width
            val y = topY + amplitude * sin((t * waves * 2 * PI) + phaseShift).toFloat()
            path.lineTo(x, y)
            pts.add(Offset(x, y))
        }
        path.lineTo(right, bottomY)
        pts.add(Offset(right, bottomY))
        path.close()
        return VectorShape(path, pts)
    }
}
