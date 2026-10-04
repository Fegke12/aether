package com.aether.weather.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import com.aether.weather.R

internal val WidgetTextWhite = ColorProvider(Color.White)
internal val WidgetTextMuted = ColorProvider(Color(0xE6FFFFFF))
internal val WidgetTextFaint = ColorProvider(Color(0x99FFFFFF))

internal fun widgetIconResFor(iconCode: String): Int {
    if (iconCode.isEmpty()) return R.drawable.ic_w_cloud
    val group = iconCode.dropLast(1)
    val isNight = iconCode.endsWith("n")
    return when (group) {
        "01" -> if (isNight) R.drawable.ic_w_moon else R.drawable.ic_w_sun
        "02", "03", "04" -> R.drawable.ic_w_cloud
        "09", "10", "11" -> R.drawable.ic_w_rain
        "13" -> R.drawable.ic_w_snow
        "50" -> R.drawable.ic_w_cloud
        else -> R.drawable.ic_w_cloud
    }
}
