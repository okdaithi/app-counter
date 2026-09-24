package com.okdaithi.daycounter.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import com.okdaithi.daycounter.MainActivity
import com.okdaithi.daycounter.core.Counter
import com.okdaithi.daycounter.core.CounterFormat
import com.okdaithi.daycounter.core.CounterMath
import com.okdaithi.daycounter.data.CounterRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 1×1 home-screen widget. Each instance is bound to one counter ID; the face is drawn by
 * [WidgetRenderer] into a bitmap so Inter, letter-spacing, the ring glow and tabular figures
 * render exactly as designed.
 */
class CounterWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val repository = CounterRepository(context)
        val flow = repository.counterForWidget(appWidgetId)
        val initial = flow.first()
        val renderer = WidgetRenderer(context)

        provideContent {
            val counter by remember { flow }.collectAsState(initial)
            WidgetContent(counter, renderer)
        }
    }

    @Composable
    private fun WidgetContent(counter: Counter?, renderer: WidgetRenderer) {
        val context = LocalContext.current
        val size = LocalSize.current
        val density = context.resources.displayMetrics.density
        val sidePx = (min(size.width.value, size.height.value) * density).roundToInt().coerceIn(48, 720)

        // Recomputed on every update, so a midnight refresh rolls the number over.
        val today = LocalDate.now()
        val face = counter?.let { WidgetFace.of(it, today) } ?: WidgetFace.Empty
        val bitmap = renderer.render(face, WidgetConfig.style, sidePx)

        val intent = if (counter != null) MainActivity.editIntent(context, counter.id) else MainActivity.listIntent(context)
        val description = counter?.let {
            val r = CounterMath.calc(it, today)
            "${it.title}: ${CounterFormat.previewLine(it.unit, it.includeToday, r)}"
        } ?: "Counter removed. Open the Counter app."

        Box(
            modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity(intent)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = description,
                contentScale = ContentScale.Fit,
                modifier = GlanceModifier.fillMaxSize(),
            )
        }
    }
}
