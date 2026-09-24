package com.okdaithi.daycounter.widget

import com.okdaithi.daycounter.core.CountResult
import com.okdaithi.daycounter.core.Counter
import com.okdaithi.daycounter.core.CounterFormat
import com.okdaithi.daycounter.core.CounterMath
import java.time.LocalDate

/** What a widget shows: the short number, its unit suffix and whether it counts "until". */
data class WidgetFace(
    val number: String,
    val suffix: String,
    val future: Boolean,
    val empty: Boolean = false,
) {
    companion object {
        /** Shown when the bound counter no longer exists. */
        val Empty = WidgetFace(number = "–", suffix = "", future = false, empty = true)

        fun of(counter: Counter, today: LocalDate): WidgetFace = of(counter, CounterMath.calc(counter, today))

        fun of(counter: Counter, result: CountResult): WidgetFace = WidgetFace(
            number = CounterFormat.widgetNumber(result.n),
            suffix = CounterFormat.widgetSuffix(counter.unit),
            future = result.future,
        )
    }
}
