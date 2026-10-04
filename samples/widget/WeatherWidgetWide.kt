package com.aether.weather.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.aether.weather.MainActivity
import com.aether.weather.R
import com.aether.weather.data.WeatherFetcher
import com.aether.weather.data.cache.CachedWeather
import com.aether.weather.data.cache.WeatherCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Широкий виджет 4×2 в стиле iOS Liquid Glass. */
class WeatherWidgetWide : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val cached = WeatherCache(context).read()
        provideContent { WideWidget(cached) }
    }

    @Composable
    private fun WideWidget(cached: CachedWeather?) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.widget_bg))
                .clickable(actionStartActivity<MainActivity>()),
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (cached == null) {
                        Text(
                            text = "Aether",
                            style = TextStyle(color = WidgetTextWhite, fontSize = 20.sp, fontWeight = FontWeight.Medium),
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = "Откройте приложение",
                            style = TextStyle(color = WidgetTextFaint, fontSize = 12.sp),
                        )
                    } else {
                        Text(
                            text = cached.city.uppercase(),
                            style = TextStyle(
                                color = WidgetTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            maxLines = 1,
                        )
                        Text(
                            text = cached.description,
                            style = TextStyle(color = WidgetTextFaint, fontSize = 12.sp),
                            maxLines = 1,
                        )
                        Spacer(GlanceModifier.height(6.dp))
                        Text(
                            text = "${cached.temperature}°",
                            style = TextStyle(
                                color = WidgetTextWhite,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = "макс ${cached.maxTemp}°  ·  мин ${cached.minTemp}°",
                            style = TextStyle(color = WidgetTextFaint, fontSize = 12.sp),
                        )
                        Text(
                            text = "Ощущается как ${cached.feelsLike}°",
                            style = TextStyle(color = WidgetTextFaint, fontSize = 11.sp),
                        )
                    }
                }
                if (cached != null) {
                    Box(
                        modifier = GlanceModifier.size(96.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.widget_icon_halo),
                            contentDescription = null,
                            modifier = GlanceModifier.size(96.dp),
                        )
                        Image(
                            provider = ImageProvider(widgetIconResFor(cached.iconCode)),
                            contentDescription = null,
                            modifier = GlanceModifier.size(76.dp),
                        )
                    }
                }
            }
        }
    }
}

class WeatherWidgetWideReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherWidgetWide()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CoroutineScope(Dispatchers.IO).launch {
            WeatherFetcher(context).refresh()
            androidx.glance.appwidget.GlanceAppWidgetManager(context)
                .getGlanceIds(WeatherWidgetWide::class.java)
                .forEach { id -> glanceAppWidget.update(context, id) }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        CoroutineScope(Dispatchers.IO).launch {
            WeatherFetcher(context).refresh()
            androidx.glance.appwidget.GlanceAppWidgetManager(context)
                .getGlanceIds(WeatherWidgetWide::class.java)
                .forEach { id -> glanceAppWidget.update(context, id) }
        }
    }
}
