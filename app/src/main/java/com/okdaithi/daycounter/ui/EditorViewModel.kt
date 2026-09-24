package com.okdaithi.daycounter.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.okdaithi.daycounter.core.CountUnit
import com.okdaithi.daycounter.core.Counter
import com.okdaithi.daycounter.data.CounterRepository
import com.okdaithi.daycounter.widget.WidgetUpdater
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class Draft(
    val title: String = "",
    val date: LocalDate? = LocalDate.now(),
    val unit: CountUnit = CountUnit.DAYS,
    val includeToday: Boolean = false,
    val repeatYearly: Boolean = false,
    val notes: String = "",
)

data class DraftErrors(val title: Boolean = false, val date: Boolean = false) {
    val any: Boolean get() = title || date
}

class EditorViewModel(app: Application, savedState: SavedStateHandle) : AndroidViewModel(app) {

    private val repository = CounterRepository(app)

    /** Null for a new counter. */
    val counterId: String? = savedState.get<String>(ARG_ID)?.takeIf { it.isNotEmpty() }

    var draft by mutableStateOf(Draft())
        private set
    var errors by mutableStateOf(DraftErrors())
        private set
    /** True once an edit target has been looked up and does not exist (e.g. deleted elsewhere). */
    var missing by mutableStateOf(false)
        private set

    init {
        if (counterId != null) {
            viewModelScope.launch {
                val c = repository.get(counterId)
                if (c == null) {
                    missing = true
                } else {
                    draft = Draft(c.title, c.date, c.unit, c.includeToday, c.repeatYearly, c.notes)
                }
            }
        }
    }

    /** Applies an edit and clears validation errors, as in the prototype. */
    fun edit(transform: (Draft) -> Draft) {
        draft = transform(draft)
        errors = DraftErrors()
    }

    /** Validates and saves. Returns false when validation fails. */
    suspend fun save(): Boolean {
        val d = draft
        val validation = DraftErrors(title = d.title.isBlank(), date = d.date == null)
        if (validation.any) {
            errors = validation
            return false
        }
        val counter = Counter(
            id = counterId ?: UUID.randomUUID().toString(),
            title = d.title.trim(),
            date = d.date!!,
            unit = d.unit,
            includeToday = d.includeToday,
            repeatYearly = d.repeatYearly,
            notes = d.notes,
        )
        repository.upsert(counter)
        WidgetUpdater.updateAll(getApplication())
        return true
    }

    suspend fun delete() {
        val id = counterId ?: return
        repository.delete(id)
        WidgetUpdater.updateAll(getApplication())
    }

    companion object {
        const val ARG_ID = "id"
    }
}
