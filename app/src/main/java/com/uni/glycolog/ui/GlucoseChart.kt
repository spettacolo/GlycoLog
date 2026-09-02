package com.uni.glycolog.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uni.glycolog.util.ChartData
import com.uni.glycolog.util.GlucoseStats
import kotlin.math.max
import kotlin.math.min

// grafico a linea (canvas)
@Composable
fun GlucoseChart(data: ChartData, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val bandColor = lineColor.copy(alpha = 0.10f)
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val leftPad = 30.dp.toPx()
        val rightPad = 8.dp.toPx()
        val topPad = 8.dp.toPx()
        val bottomPad = 18.dp.toPx()
        val chartWidth = size.width - leftPad - rightPad
        val chartHeight = size.height - topPad - bottomPad

        // scala verticale
        val values = data.points.map { it.value }
        val yMin = min(50f, (values.minOrNull() ?: GlucoseStats.RANGE_MIN.toFloat()) - 20f)
        val yMax = max(220f, (values.maxOrNull() ?: GlucoseStats.RANGE_MAX.toFloat()) + 20f)

        fun yPos(value: Float): Float = topPad + chartHeight * (1f - (value - yMin) / (yMax - yMin))
        fun xPos(position: Float): Float = leftPad + chartWidth * position

        val rangeMin = GlucoseStats.RANGE_MIN.toFloat()
        val rangeMax = GlucoseStats.RANGE_MAX.toFloat()

        // banda del range ottimale
        drawRect(
            color = bandColor,
            topLeft = Offset(leftPad, yPos(rangeMax)),
            size = Size(chartWidth, yPos(rangeMin) - yPos(rangeMax))
        )

        // linee tratteggiate ai limiti del range
        val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
        for (limit in listOf(rangeMin, rangeMax)) {
            drawLine(
                color = gridColor,
                start = Offset(leftPad, yPos(limit)),
                end = Offset(leftPad + chartWidth, yPos(limit)),
                strokeWidth = 1f,
                pathEffect = dash
            )
        }

        // etichette dei limiti (70 e 180) e dei tempi in basso
        val textPaint = android.graphics.Paint().apply {
            color = labelColor.toArgb()
            textSize = 10.sp.toPx()
            isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            GlucoseStats.RANGE_MIN.toString(), 2f, yPos(rangeMin) + 4f, textPaint
        )
        drawContext.canvas.nativeCanvas.drawText(
            GlucoseStats.RANGE_MAX.toString(), 2f, yPos(rangeMax) + 4f, textPaint
        )

        val centerPaint = android.graphics.Paint(textPaint).apply {
            textAlign = android.graphics.Paint.Align.CENTER
        }
        data.labels.forEachIndexed { index, label ->
            val fraction =
                if (data.labels.size <= 1) 0f else index.toFloat() / (data.labels.size - 1)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                xPos(fraction),
                size.height - 4f,
                centerPaint
            )
        }

        // linea dei valori
        if (data.points.size >= 2) {
            val path = Path()
            data.points.forEachIndexed { index, point ->
                val x = xPos(point.position)
                val y = yPos(point.value)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // punti delle misurazioni (solo se non sono troppi)
        if (data.points.size <= 31) {
            data.points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 3.dp.toPx(),
                    center = Offset(xPos(point.position), yPos(point.value))
                )
            }
        }
    }
}
