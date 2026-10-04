package com.aether.weather.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.withFrameNanos
import kotlin.random.Random

private data class Drop(var x: Float, var y: Float, val speed: Float, val length: Float, val alpha: Float)
private data class Ripple(val x: Float, val y: Float, var age: Float)

@Composable
fun RainEffect(heavy: Boolean = false) {
    val count = if (heavy) 120 else 70
    val drops = remember {
        MutableList(count) {
            Drop(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = 1.1f + Random.nextFloat() * 0.7f,
                length = 14f + Random.nextFloat() * 10f,
                alpha = 0.25f + Random.nextFloat() * 0.35f,
            )
        }
    }
    val ripples = remember { mutableListOf<Ripple>() }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0f else ((now - last) / 1e9f).coerceAtMost(0.05f)
                last = now

                drops.forEachIndexed { i, d ->
                    d.y += d.speed * dt
                    if (d.y > 1.05f) {
                        d.y = -0.05f
                        d.x = Random.nextFloat()
                    }
                }

                val it = ripples.listIterator()
                while (it.hasNext()) {
                    val r = it.next()
                    r.age += dt
                    if (r.age > 0.9f) it.remove()
                }
                frameTick = now
            }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    ripples.add(
                        Ripple(
                            x = offset.x / size.width,
                            y = offset.y / size.height,
                            age = 0f,
                        )
                    )
                }
            }
    ) {
        frameTick

        drops.forEach { d ->
            val px = d.x * size.width
            val py = d.y * size.height
            drawLine(
                color = Color(0xFFB3D4FF).copy(alpha = d.alpha),
                start = Offset(px, py),
                end = Offset(px - 2f, py + d.length),
                strokeWidth = 1.5f,
            )
        }

        ripples.forEach { r ->
            val progress = r.age / 0.9f
            val radius = 12f + progress * size.minDimension * 0.15f
            val alpha = (1f - progress) * 0.7f
            drawCircle(
                color = Color(0xFFB3D4FF).copy(alpha = alpha),
                radius = radius,
                center = Offset(r.x * size.width, r.y * size.height),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
            )
        }
    }
}
