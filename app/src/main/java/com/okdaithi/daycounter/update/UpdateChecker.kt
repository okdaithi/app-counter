package com.okdaithi.daycounter.update

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
private data class GhRelease(val tag_name: String)

data class UpdateInfo(val currentVersion: String, val latestVersion: String, val downloadUrl: String)

object UpdateChecker {

    private const val API_URL = "https://api.github.com/repos/okdaithi/app-counter/releases/latest"
    private const val DOWNLOAD_URL = "https://github.com/okdaithi/app-counter/releases/latest/download/DayCounter.apk"
    private const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L
    private const val PREFS_NAME = "update_checker"
    private const val KEY_LAST_CHECK = "last_check"
    private const val KEY_LATEST_VERSION = "latest_version"
    private const val KEY_DISMISSED_VERSION = "dismissed_version"

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun check(context: Context, force: Boolean = false): UpdateInfo? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0)

        val latestTag: String
        if (!force && now - lastCheck < CHECK_INTERVAL_MS) {
            latestTag = prefs.getString(KEY_LATEST_VERSION, null) ?: return null
        } else {
            latestTag = fetchLatestTag() ?: return null
            prefs.edit()
                .putLong(KEY_LAST_CHECK, now)
                .putString(KEY_LATEST_VERSION, latestTag)
                .apply()
        }

        val latest = latestTag.removePrefix("v")
        val current = currentVersion(context) ?: return null

        if (compareVersions(latest, current) <= 0) return null
        if (prefs.getString(KEY_DISMISSED_VERSION, null) == latest) return null

        return UpdateInfo(current, latest, DOWNLOAD_URL)
    }

    fun dismiss(context: Context, version: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DISMISSED_VERSION, version)
            .apply()
    }

    private suspend fun fetchLatestTag(): String? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(API_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.connectTimeout = 5_000
            conn.readTimeout = 5_000
            try {
                if (conn.responseCode != 200) return@withContext null
                val body = conn.inputStream.bufferedReader().readText()
                json.decodeFromString<GhRelease>(body).tag_name
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun currentVersion(context: Context): String? = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    internal fun compareVersions(a: String, b: String): Int {
        val pa = a.split(".").map { it.toIntOrNull() ?: 0 }
        val pb = b.split(".").map { it.toIntOrNull() ?: 0 }
        val len = maxOf(pa.size, pb.size)
        for (i in 0 until len) {
            val va = pa.getOrElse(i) { 0 }
            val vb = pb.getOrElse(i) { 0 }
            if (va != vb) return va.compareTo(vb)
        }
        return 0
    }
}
