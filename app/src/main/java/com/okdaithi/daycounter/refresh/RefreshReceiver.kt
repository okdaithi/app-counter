package com.okdaithi.daycounter.refresh

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.okdaithi.daycounter.DayCounterApp
import com.okdaithi.daycounter.widget.WidgetUpdater
import kotlinx.coroutines.launch

/**
 * Refreshes widgets at local midnight and whenever the clock, time zone or date changes,
 * after boot and after an app update. Re-arms the midnight alarm each time.
 */
class RefreshReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val appContext = context.applicationContext
        DayCounterApp.scope.launch {
            try {
                WidgetUpdater.updateAll(appContext)
            } finally {
                RefreshScheduler.scheduleAll(appContext)
                pending.finish()
            }
        }
    }
}
