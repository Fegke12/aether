package com.aether.weather.ui.effects

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WeatherEffects(iconCode: String, modifier: Modifier = Modifier) {
    if (iconCode.isEmpty()) return
    val group = iconCode.dropLast(1)
    val isNight = iconCode.endsWith("n")
    Box(modifier = modifier.fillMaxSize()) {
        when (group) {
            "01" -> if (isNight) StarsEffect() else SunEffect()
            "02" -> if (isNight) StarsEffect() else SunEffect(muted = true)
            "09", "10" -> RainEffect()
            "11" -> ThunderEffect()
            "13" -> SnowEffect()
            // 03, 04 (облачно/пасмурно) и 50 (туман) — только градиент
            else -> Unit
        }
    }
}
