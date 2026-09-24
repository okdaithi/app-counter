package com.okdaithi.daycounter.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.updateAll
import com.okdaithi.daycounter.data.CounterRepository

object WidgetUpdater {

    /** Re-renders every counter widget. Cheap enough to call on any data or date change. */
    suspend fun updateAll(context: Context) {
        CounterWidget().updateAll(context)
    }

    fun liveAppWidgetIds(context: Context): Set<Int> =
        AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, CounterWidgetReceiver::class.java))
            .toSet()

    /** Drops bindings for widgets that were removed while the app was not listening. */
    suspend fun pruneBindings(context: Context) {
        CounterRepository(context).retainBindings(liveAppWidgetIds(context))
    }
}
