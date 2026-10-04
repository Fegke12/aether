package com.aether.weather.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash в стиле Pixel/Gemini:
 *   1. появляется облако (лого)
 *   2. плавно морфит в 4-конечную искру
 *   3. искра пульсирует и вращается
 *   4. обратный морф в облако
 *   5. снизу проступает "AETHER"
 *   6. весь оверлей fade-out
 */
@Composable
fun AetherSplash(onFinished: () -> Unit) {
    val cloudAlpha = remember { Animatable(0f) }
    val cloudScale = remember { Animatable(0.6f) }
    val sparkAlpha = remember { Animatable(0f) }
    val sparkScale = remember { Animatable(0.6f) }
    val sparkRotate = remember { Animatable(0f) }
    val sparkPulse = remember { Animatable(1f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(16f) }
    val overlayAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Phase 1: cloud in
        launch { cloudAlpha.animateTo(1f, tween(500, easing = LinearEasing)) }
        launch {
            cloudScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            )
        }
        delay(650)

        // Phase 2: cloud → sparkle morph (cross-fade + scale swap)
        launch { cloudAlpha.animateTo(0f, tween(350, easing = FastOutSlowInEasing)) }
        launch { cloudScale.animateTo(0.6f, tween(350, easing = FastOutSlowInEasing)) }
        launch { sparkAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
        launch { sparkScale.animateTo(1f, tween(400, easing = EaseOutCubic)) }
        delay(450)

        // Phase 3: sparkle pulses + rotates ~1.2s
        val rotJob = launch { sparkRotate.animateTo(360f, tween(1400, easing = LinearEasing)) }
        launch {
            sparkPulse.animateTo(1.18f, tween(400, easing = EaseInOutCubic))
            sparkPulse.animateTo(1f, tween(400, easing = EaseInOutCubic))
            sparkPulse.animateTo(1.12f, tween(300, easing = EaseInOutCubic))
            sparkPulse.animateTo(1f, tween(300, easing = EaseInOutCubic))
        }
        rotJob.join()

        // Phase 4: sparkle → cloud morph back
        launch { sparkAlpha.animateTo(0f, tween(350, easing = FastOutSlowInEasing)) }
        launch { sparkScale.animateTo(0.6f, tween(350, easing = FastOutSlowInEasing)) }
        launch { cloudAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
        launch { cloudScale.animateTo(1f, tween(400, easing = EaseOutCubic)) }
        delay(450)

        // Phase 5: reveal text
        launch { textAlpha.animateTo(1f, tween(500, easing = LinearEasing)) }
        launch { textOffset.animateTo(0f, tween(500, easing = EaseOutCubic)) }
        delay(700)

        // Phase 6: fade the whole overlay
        overlayAlpha.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(overlayAlpha.value)
            .background(Color(0xFF26507D)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Cloud (main logo)
                Icon(
                    imageVector = Icons.Filled.WbCloudy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(84.dp)
                        .graphicsLayer {
                            alpha = cloudAlpha.value
                            scaleX = cloudScale.value
                            scaleY = cloudScale.value
                        },
                )
                // Sparkle (4-pointed star, like Gemini)
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(84.dp)
                        .graphicsLayer {
                            alpha = sparkAlpha.value
                            val s = sparkScale.value * sparkPulse.value
                            scaleX = s
                            scaleY = s
                            rotationZ = sparkRotate.value
                        },
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "AETHER",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 12.sp,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .graphicsLayer { translationY = textOffset.value },
            )
        }
    }
}
