package com.aether.weather.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.random.Random

@Composable
fun ThunderEffect() {
    var flashIntensity by remember { mutableFloatStateOf(0f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        var last = 0L
        var nextFlashInSec = 3f + Random.nextFloat() * 4f
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0f else ((now - last) / 1e9f).coerceAtMost(0.05f)
                last = now
                if (flashIntensity > 0f) {
                    flashIntensity = (flashIntensity - dt * 3.5f).coerceAtLeast(0f)
                }
                nextFlashInSec -= dt
                if (nextFlashInSec <= 0f) {
                    flashIntensity = 0.8f + Random.nextFloat() * 0.2f
                    nextFlashInSec = 3.5f + Random.nextFloat() * 5f
                }
                frameTick = now
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RainEffect(heavy = true)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { flashIntensity = 1f }
                }
        ) {
            frameTick
            if (flashIntensity > 0f) {
                drawRect(color = Color.White.copy(alpha = flashIntensity * 0.35f))
            }
        }
    }
}
