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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Flake(
    var x: Float,
    var y: Float,
    val vy: Float,
    val amplitude: Float,
    val phase: Float,
    val radius: Float,
    var life: Float = Float.POSITIVE_INFINITY,
    val alpha: Float,
)

@Composable
fun SnowEffect() {
    val flakes = remember {
        MutableList(60) {
            Flake(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vy = 0.08f + Random.nextFloat() * 0.1f,
                amplitude = 0.02f + Random.nextFloat() * 0.05f,
                phase = Random.nextFloat() * 6.28f,
                radius = 1.5f + Random.nextFloat() * 3f,
                alpha = 0.5f + Random.nextFloat() * 0.4f,
            )
        }
    }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        var last = 0L
        var timeSec = 0f
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0f else ((now - last) / 1e9f).coerceAtMost(0.05f)
                last = now
                timeSec += dt

                val it = flakes.listIterator()
                while (it.hasNext()) {
                    val f = it.next()
                    f.y += f.vy * dt
                    val baseX = f.x + sin(f.phase + timeSec) * f.amplitude
                    if (f.life != Float.POSITIVE_INFINITY) {
                        f.life -= dt
                        if (f.life <= 0f) {
                            it.remove(); continue
                        }
                    } else if (f.y > 1.05f) {
                        f.y = -0.05f
                        f.x = Random.nextFloat()
                    }
                    f.x = baseX.coerceIn(-0.1f, 1.1f)
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
                    val nx = offset.x / size.width
                    val ny = offset.y / size.height
                    repeat(14) {
                        val angle = Random.nextFloat() * 6.28f
                        val speed = 0.05f + Random.nextFloat() * 0.15f
                        flakes.add(
                            Flake(
                                x = nx + cos(angle) * 0.02f,
                                y = ny + sin(angle) * 0.02f,
                                vy = 0.15f + Random.nextFloat() * 0.2f,
                                amplitude = 0.005f,
                                phase = Random.nextFloat() * 6.28f,
                                radius = 2f + Random.nextFloat() * 2.5f,
                                life = 1.4f,
                                alpha = 0.9f,
                            )
                        )
                    }
                }
            }
    ) {
        frameTick
        flakes.forEach { f ->
            drawCircle(
                color = Color.White.copy(alpha = f.alpha),
                radius = f.radius,
                center = Offset(f.x * size.width, f.y * size.height),
            )
        }
    }
}
