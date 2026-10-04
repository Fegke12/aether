package com.aether.weather.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.withFrameNanos
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SunEffect(muted: Boolean = false) {
    var frameTick by remember { mutableLongStateOf(0L) }
    var timeSec by remember { mutableFloatStateOf(0f) }
    var pulse by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0f else ((now - last) / 1e9f).coerceAtMost(0.05f)
                last = now
                timeSec += dt
                if (pulse > 0f) pulse = (pulse - dt * 1.5f).coerceAtLeast(0f)
                frameTick = now
            }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { pulse = 1f }
            }
    ) {
        frameTick // read tick to invalidate

        val cx = size.width * 0.85f
        val cy = size.height * 0.15f
        val baseRadius = size.minDimension * 0.09f
        val radius = baseRadius * (1f + pulse * 0.25f)

        val alphaCore = if (muted) 0.55f else 0.85f
        val alphaGlow = if (muted) 0.25f else 0.4f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFF3B0).copy(alpha = alphaGlow + pulse * 0.2f),
                    Color(0xFFFFC65C).copy(alpha = 0f),
                ),
                center = Offset(cx, cy),
                radius = radius * 4f,
            ),
            radius = radius * 4f,
            center = Offset(cx, cy),
        )

        val rayCount = 12
        val rayLen = radius * (1.4f + pulse * 0.6f)
        val rotation = timeSec * 0.25f
        repeat(rayCount) { i ->
            val angle = rotation + i * (Math.PI.toFloat() * 2f / rayCount)
            val start = Offset(cx + cos(angle) * radius * 1.15f, cy + sin(angle) * radius * 1.15f)
            val end = Offset(cx + cos(angle) * (radius * 1.15f + rayLen), cy + sin(angle) * (radius * 1.15f + rayLen))
            drawLine(
                color = Color(0xFFFFE082).copy(alpha = alphaCore * 0.7f),
                start = start,
                end = end,
                strokeWidth = 4f,
            )
        }

        drawCircle(
            color = Color(0xFFFFCA28).copy(alpha = alphaCore),
            radius = radius,
            center = Offset(cx, cy),
        )
    }
}
