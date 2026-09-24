package com.okdaithi.daycounter

import android.app.Application
import com.okdaithi.daycounter.refresh.RefreshScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class DayCounterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        RefreshScheduler.scheduleAll(this)
    }

    companion object {
        /** Process-wide scope for short background writes started from receivers. */
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
