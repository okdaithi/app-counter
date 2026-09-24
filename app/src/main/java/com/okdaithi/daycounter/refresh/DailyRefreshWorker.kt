package com.okdaithi.daycounter.refresh

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.okdaithi.daycounter.widget.WidgetUpdater

/** Daily backstop in case the midnight alarm is deferred or dropped. */
class DailyRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        WidgetUpdater.pruneBindings(applicationContext)
        WidgetUpdater.updateAll(applicationContext)
        RefreshScheduler.scheduleMidnight(applicationContext)
        return Result.success()
    }
}
