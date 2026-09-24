package com.okdaithi.daycounter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.okdaithi.daycounter.core.Counter
import com.okdaithi.daycounter.core.CounterFormat
import com.okdaithi.daycounter.core.CounterMath
import com.okdaithi.daycounter.data.CounterRepository
import com.okdaithi.daycounter.widget.WidgetFace
import com.okdaithi.daycounter.widget.WidgetUpdater
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

data class CounterRowUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val number: String,
    val caption: String,
    val future: Boolean,
    val onHomeScreen: Boolean,
    val face: WidgetFace,
)

data class ToastUi(val message: String = "", val visible: Boolean = false)

/** Activity-scoped state: the counter list and the toast. */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = CounterRepository(app)

    /** Emits today's date and again just after each local midnight while the app is open. */
    private val today = flow {
        while (true) {
            emit(LocalDate.now())
            val untilMidnight = Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay())
            delay(untilMidnight.toMillis() + 1_000)
        }
    }

    val rows: StateFlow<List<CounterRowUi>?> =
        combine(repository.counters, repository.bindings, today) { counters, bindings, day ->
            val bound = bindings.values.toSet()
            counters.map { it.toRow(day, it.id in bound) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _toast = MutableStateFlow(ToastUi())
    val toast: StateFlow<ToastUi> = _toast.asStateFlow()
    private var toastJob: Job? = null

    init {
        viewModelScope.launch { WidgetUpdater.pruneBindings(app) }
    }

    fun showToast(message: String) {
        toastJob?.cancel()
        _toast.value = ToastUi(message, visible = true)
        toastJob = viewModelScope.launch {
            delay(1_800)
            _toast.value = _toast.value.copy(visible = false)
        }
    }

    private fun Counter.toRow(today: LocalDate, onHome: Boolean): CounterRowUi {
        val r = CounterMath.calc(this, today)
        return CounterRowUi(
            id = id,
            title = title,
            subtitle = CounterFormat.subtitle(this, r),
            number = CounterFormat.fullNumber(r.n),
            caption = CounterFormat.unitCaption(unit, includeToday, r),
            future = r.future,
            onHomeScreen = onHome,
            face = WidgetFace.of(this, r),
        )
    }
}
