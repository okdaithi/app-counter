package com.okdaithi.daycounter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.okdaithi.daycounter.core.Counter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "daycounter")

/**
 * Counters and widget bindings (appWidgetId -> counterId), stored as JSON in Preferences DataStore.
 * Instances are cheap: the underlying DataStore is a process-wide singleton.
 */
class CounterRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    val counters: Flow<List<Counter>> = store.data.map(::decodeCounters).distinctUntilChanged()

    val bindings: Flow<Map<Int, String>> = store.data.map(::decodeBindings).distinctUntilChanged()

    fun counterForWidget(appWidgetId: Int): Flow<Counter?> = store.data.map { prefs ->
        val counterId = decodeBindings(prefs)[appWidgetId] ?: return@map null
        decodeCounters(prefs).firstOrNull { it.id == counterId }
    }.distinctUntilChanged()

    suspend fun get(id: String): Counter? = counters.first().firstOrNull { it.id == id }

    suspend fun upsert(counter: Counter) {
        store.edit { prefs ->
            val list = decodeCounters(prefs)
            val updated = if (list.any { it.id == counter.id }) {
                list.map { if (it.id == counter.id) counter else it }
            } else {
                list + counter
            }
            prefs[COUNTERS] = json.encodeToString(counterList, updated)
        }
    }

    /** Deletes the counter and unbinds every widget that pointed at it. */
    suspend fun delete(id: String) {
        store.edit { prefs ->
            prefs[COUNTERS] = json.encodeToString(counterList, decodeCounters(prefs).filterNot { it.id == id })
            prefs[BINDINGS] = encodeBindings(decodeBindings(prefs).filterValues { it != id })
        }
    }

    suspend fun bind(appWidgetId: Int, counterId: String) {
        store.edit { prefs ->
            prefs[BINDINGS] = encodeBindings(decodeBindings(prefs) + (appWidgetId to counterId))
        }
    }

    suspend fun unbind(appWidgetIds: Collection<Int>) {
        store.edit { prefs ->
            prefs[BINDINGS] = encodeBindings(decodeBindings(prefs) - appWidgetIds.toSet())
        }
    }

    /** Drops bindings for widgets the launcher no longer hosts. */
    suspend fun retainBindings(liveAppWidgetIds: Set<Int>) {
        store.edit { prefs ->
            val current = decodeBindings(prefs)
            val kept = current.filterKeys { it in liveAppWidgetIds }
            if (kept.size != current.size) prefs[BINDINGS] = encodeBindings(kept)
        }
    }

    private fun decodeCounters(prefs: Preferences): List<Counter> =
        prefs[COUNTERS]?.let { runCatching { json.decodeFromString(counterList, it) }.getOrNull() } ?: emptyList()

    private fun decodeBindings(prefs: Preferences): Map<Int, String> =
        prefs[BINDINGS]?.let { raw ->
            runCatching { json.decodeFromString(bindingMap, raw) }.getOrNull()
                ?.mapNotNull { (k, v) -> k.toIntOrNull()?.let { it to v } }
                ?.toMap()
        } ?: emptyMap()

    private fun encodeBindings(map: Map<Int, String>): String =
        json.encodeToString(bindingMap, map.mapKeys { it.key.toString() })

    private companion object {
        val COUNTERS = stringPreferencesKey("counters")
        val BINDINGS = stringPreferencesKey("widget_bindings")
        val json = Json { ignoreUnknownKeys = true }
        val counterList = ListSerializer(Counter.serializer())
        val bindingMap = MapSerializer(String.serializer(), String.serializer())
    }
}
