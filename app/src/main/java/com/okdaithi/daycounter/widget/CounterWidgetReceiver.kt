package com.okdaithi.daycounter.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.okdaithi.daycounter.DayCounterApp
import com.okdaithi.daycounter.data.CounterRepository
import com.okdaithi.daycounter.refresh.RefreshScheduler
import kotlinx.coroutines.launch

class CounterWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = CounterWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        RefreshScheduler.scheduleAll(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // Glance's receiver already holds goAsync(); use the app scope for the binding cleanup.
        val ids = appWidgetIds.toList()
        DayCounterApp.scope.launch { CounterRepository(context).unbind(ids) }
    }
}
