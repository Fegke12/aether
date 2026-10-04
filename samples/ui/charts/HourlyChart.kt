package com.aether.weather.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HourlyChart(
    values: List<Float>,
    timestamps: List<Long>,
    color: Color,
    onSurfaceMuted: Color,
    unit: String,
    modifier: Modifier = Modifier,
    yPadding: Float = 1f,
) {
    if (values.size < 2) return
    val minV = values.min() - yPadding
    val maxV = values.max() + yPadding
    val rangeV = (maxV - minV).coerceAtLeast(1f)

    val hourFmt = remember { SimpleDateFormat("HH", Locale.getDefault()) }

    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val w = size.width
                val h = size.height
                val stepX = w / (values.size - 1)

                val points = List(values.size) { i ->
                    val x = i * stepX
                    val norm = (values[i] - minV) / rangeV
                    val y = h - norm * h * 0.9f - h * 0.05f
                    Offset(x, y)
                }

                val linePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 0 until points.size - 1) {
                        val p0 = if (i > 0) points[i - 1] else points[i]
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val p3 = if (i + 2 < points.size) points[i + 2] else p2
                        val c1x = p1.x + (p2.x - p0.x) / 6f
                        val c1y = p1.y + (p2.y - p0.y) / 6f
                        val c2x = p2.x - (p3.x - p1.x) / 6f
                        val c2y = p2.y - (p3.y - p1.y) / 6f
                        cubicTo(c1x, c1y, c2x, c2y, p2.x, p2.y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(points.last().x, h)
                    lineTo(points.first().x, h)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0f)),
                    ),
                )
                drawPath(
                    path = linePath,
                    color = color,
                    style = Stroke(width = 2.5.dp.toPx()),
                )
                for (p in points) {
                    drawCircle(color = color, radius = 3.dp.toPx(), center = p)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            timestamps.forEach { ts ->
                Text(
                    text = hourFmt.format(Date(ts * 1000)),
                    color = onSurfaceMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "мин ${values.min().toInt()}$unit",
                color = onSurfaceMuted,
                style = MaterialTheme.typography.labelSmall,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "макс ${values.max().toInt()}$unit",
                color = onSurfaceMuted,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
