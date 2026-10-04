package com.aether.weather.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

data class WeatherPalette(
    val top: Color,
    val bottom: Color,
    val onSurface: Color = Color(0xFFFFFFFF),
    val onSurfaceMuted: Color = Color(0xCCFFFFFF),
    val cardBackground: Color = Color(0x22FFFFFF),
)

private val ClearDay = WeatherPalette(top = Color(0xFF4A90E2), bottom = Color(0xFFB0D8FF))
private val ClearNight = WeatherPalette(top = Color(0xFF0B1633), bottom = Color(0xFF2E1E5E))
private val CloudsDay = WeatherPalette(top = Color(0xFF6B7A99), bottom = Color(0xFFB8C4D6))
private val CloudsNight = WeatherPalette(top = Color(0xFF1B2434), bottom = Color(0xFF3B4A66))
private val RainDay = WeatherPalette(top = Color(0xFF4A5A75), bottom = Color(0xFF7A8AA5))
private val RainNight = WeatherPalette(top = Color(0xFF0F1729), bottom = Color(0xFF2E3B54))
private val Thunder = WeatherPalette(top = Color(0xFF1A1A2E), bottom = Color(0xFF443A5C))
private val SnowDay = WeatherPalette(top = Color(0xFF7B92B2), bottom = Color(0xFFD8E4F3))
private val SnowNight = WeatherPalette(top = Color(0xFF1B2540), bottom = Color(0xFF445D80))
private val MistDay = WeatherPalette(top = Color(0xFF8B95A4), bottom = Color(0xFFC6CCD4))
private val MistNight = WeatherPalette(top = Color(0xFF1F2530), bottom = Color(0xFF454E5A))
private val Fallback = WeatherPalette(top = Color(0xFF2C3E50), bottom = Color(0xFF4A6278))

// Тёплые «сумерки» — рассвет и закат
private val Dawn = WeatherPalette(top = Color(0xFF2A1A4E), bottom = Color(0xFFFFB47B))
private val Dusk = WeatherPalette(top = Color(0xFF3F1450), bottom = Color(0xFFFF7A59))

fun paletteForIcon(iconCode: String): WeatherPalette {
    if (iconCode.isEmpty()) return Fallback
    val group = iconCode.dropLast(1)
    val isNight = iconCode.endsWith("n")
    return when (group) {
        "01" -> if (isNight) ClearNight else ClearDay
        "02", "03", "04" -> if (isNight) CloudsNight else CloudsDay
        "09", "10" -> if (isNight) RainNight else RainDay
        "11" -> Thunder
        "13" -> if (isNight) SnowNight else SnowDay
        "50" -> if (isNight) MistNight else MistDay
        else -> Fallback
    }
}

/**
 * Выбирает палитру с учётом сумерек. В окне ±30 мин от восхода/заката
 * плавно интерполирует между базовой палитрой и тёплой сумеречной.
 */
fun paletteForWeather(
    iconCode: String,
    sunriseSec: Long?,
    sunsetSec: Long?,
    nowSec: Long,
): WeatherPalette {
    val base = paletteForIcon(iconCode)
    if (sunriseSec == null || sunsetSec == null) return base
    val windowSec = 30 * 60L

    val diffSunrise = nowSec - sunriseSec
    val diffSunset = nowSec - sunsetSec

    val (mix, target) = when {
        kotlin.math.abs(diffSunrise) <= windowSec -> {
            val t = 1f - kotlin.math.abs(diffSunrise).toFloat() / windowSec
            t to Dawn
        }
        kotlin.math.abs(diffSunset) <= windowSec -> {
            val t = 1f - kotlin.math.abs(diffSunset).toFloat() / windowSec
            t to Dusk
        }
        else -> return base
    }
    return WeatherPalette(
        top = lerp(base.top, target.top, mix.coerceIn(0f, 1f)),
        bottom = lerp(base.bottom, target.bottom, mix.coerceIn(0f, 1f)),
    )
}
