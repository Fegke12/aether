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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private data class Star(val x: Float, val y: Float, val radius: Float, val phase: Float, val speed: Float)
private data class Shooting(var x: Float, var y: Float, val vx: Float, val vy: Float, var life: Float, val maxLife: Float)
private data class Flash(val x: Float, val y: Float, var age: Float)

@Composable
fun StarsEffect() {
    val stars = remember {
        List(60) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.75f,
                radius = Random.nextFloat() * 2.5f + 0.6f,
                phase = Random.nextFloat() * 6.28f,
                speed = 1.5f + Random.nextFloat() * 1.5f,
            )
        }
    }
    val shooting = remember { mutableListOf<Shooting>() }
    val flashes = remember { mutableListOf<Flash>() }
    var timeSec by remember { mutableLongStateOf(0L) }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        var last = 0L
        var acc = 0f
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0f else ((now - last) / 1e9f).coerceAtMost(0.05f)
                last = now
                acc += dt
                timeSec = (acc * 1000).toLong()

                val si = shooting.listIterator()
                while (si.hasNext()) {
                    val s = si.next()
                    s.x += s.vx * dt
                    s.y += s.vy * dt
                    s.life -= dt
                    if (s.life <= 0f) si.remove()
                }
                val fi = flashes.listIterator()
                while (fi.hasNext()) {
                    val f = fi.next()
                    f.age += dt
                    if (f.age > 0.7f) fi.remove()
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
                    flashes.add(Flash(nx, ny, 0f))
                    // Комета летит через точку тапа, диагонально сверху-справа вниз-влево
                    val vx = -0.35f - Random.nextFloat() * 0.15f
                    val vy = 0.3f + Random.nextFloat() * 0.15f
                    shooting.add(
                        Shooting(
                            x = nx - vx * 0.4f,
                            y = ny - vy * 0.4f,
                            vx = vx,
                            vy = vy,
                            life = 1.6f,
                            maxLife = 1.6f,
                        )
                    )
                }
            }
    ) {
        frameTick
        val t = timeSec / 1000f

        // звёзды с моргалкой и «подсветкой» рядом с последней вспышкой
        stars.forEach { star ->
            val twinkle = (sin(t * star.speed + star.phase) + 1f) / 2f
            var alpha = 0.35f + twinkle * 0.5f
            var radius = star.radius
            flashes.forEach { fl ->
                val dx = star.x - fl.x
                val dy = star.y - fl.y
                val dist = hypot(dx, dy)
                if (dist < 0.25f) {
                    val boost = (1f - dist / 0.25f) * (1f - fl.age / 0.7f)
                    alpha = (alpha + boost).coerceAtMost(1f)
                    radius += boost * 2f
                }
            }
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = Offset(star.x * size.width, star.y * size.height),
            )
        }

        // кометы
        shooting.forEach { s ->
            val progress = 1f - s.life / s.maxLife
            val alpha = (s.life / s.maxLife).coerceIn(0f, 1f)
            val cx = s.x * size.width
            val cy = s.y * size.height
            val vxPx = s.vx * size.width
            val vyPx = s.vy * size.height
            // хвост — 12 сегментов
            val segments = 12
            for (i in 1..segments) {
                val f = i.toFloat() / segments
                val tailX = cx - vxPx * 0.12f * f
                val tailY = cy - vyPx * 0.12f * f
                val a = alpha * (1f - f) * 0.9f
                drawCircle(
                    color = Color.White.copy(alpha = a),
                    radius = (6f - i * 0.4f).coerceAtLeast(1f),
                    center = Offset(tailX, tailY),
                )
            }
            // голова с glow
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.35f),
                radius = 18f,
                center = Offset(cx, cy),
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 6f,
                center = Offset(cx, cy),
            )
        }
    }
}
