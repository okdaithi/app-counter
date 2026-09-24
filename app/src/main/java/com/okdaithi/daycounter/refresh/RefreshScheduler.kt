package com.okdaithi.daycounter.refresh

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Keeps widget numbers in step with the local date:
 * an inexact alarm just after local midnight, plus a daily WorkManager backstop.
 * Time, time-zone, date and boot broadcasts are handled by [RefreshReceiver].
 */
object RefreshScheduler {

    const val ACTION_MIDNIGHT = "com.okdaithi.daycounter.action.MIDNIGHT"
    private const val WORK_NAME = "daily-widget-refresh"

    fun scheduleAll(context: Context) {
        scheduleMidnight(context)
        scheduleDailyWork(context)
    }

    fun scheduleMidnight(context: Context) {
        val zone = ZoneId.systemDefault()
        // A few seconds past midnight so LocalDate.now() has already rolled over when the alarm fires.
        val triggerAt = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() + 5_000
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, midnightIntent(context))
    }

    private fun scheduleDailyWork(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyRefreshWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun midnightIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, RefreshReceiver::class.java).setAction(ACTION_MIDNIGHT),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
