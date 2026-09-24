package com.okdaithi.daycounter.core

import java.time.LocalDate
import java.util.Locale

/** Copy and number formatting from the design handoff (README rules 5–6 and screen copy). */
object CounterFormat {

    // Fixed English abbreviations: the design uses "Sep", which some en-GB locale data renders as "Sept".
    private val MONTH_SHORT = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val MONTH_LONG = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    /** Widget number: under 1000 as-is, 1000–9999 as "3.9k", 10000+ as "12k". */
    fun widgetNumber(n: Int): String = when {
        n < 1000 -> n.toString()
        n < 10_000 -> {
            val tenths = n / 100
            if (tenths % 10 == 0) "${tenths / 10}k" else "${tenths / 10}.${tenths % 10}k"
        }
        else -> "${n / 1000}k"
    }

    /** In-app number: always in full with grouping, e.g. "3,916". */
    fun fullNumber(n: Int): String = String.format(Locale.US, "%,d", n)

    fun widgetSuffix(unit: CountUnit): String = when (unit) {
        CountUnit.DAYS -> ""
        CountUnit.WEEKS -> "w"
        CountUnit.MONTHS -> "mo"
        CountUnit.YEARS -> "y"
    }

    /** Widget number size in sp, scaled by character count. */
    fun widgetFontSizeSp(number: String, suffix: String): Float {
        val len = number.length + suffix.length * 0.6
        return when {
            len <= 2 -> 30f
            len <= 3 -> 27f
            len <= 4 -> 22f
            len <= 5 -> 18f
            else -> 15f
        }
    }

    fun unitWord(unit: CountUnit, n: Int): String {
        val (one, many) = when (unit) {
            CountUnit.DAYS -> "day" to "days"
            CountUnit.WEEKS -> "week" to "weeks"
            CountUnit.MONTHS -> "month" to "months"
            CountUnit.YEARS -> "year" to "years"
        }
        return if (n == 1) one else many
    }

    /** "18 Nov 2026" */
    fun date(d: LocalDate): String = "${d.dayOfMonth} ${MONTH_SHORT[d.monthValue - 1]} ${d.year}"

    /** "7 February" */
    fun dayMonth(d: LocalDate): String = "${d.dayOfMonth} ${MONTH_LONG[d.monthValue - 1]}"

    /** List-row subtitle: "Until 18 Nov 2026", "Since 12 Mar 2024", "Today, 24 Sep 2026", "Every 7 February". */
    fun subtitle(counter: Counter, r: CountResult): String = when {
        counter.repeatYearly -> "Every ${dayMonth(r.target)}"
        r.isToday -> "Today, ${date(r.target)}"
        else -> "${if (r.future) "Until" else "Since"} ${date(r.target)}"
    }

    /** Caption under the list-row number: "days until", "day since", "today". */
    fun unitCaption(unit: CountUnit, includeToday: Boolean, r: CountResult): String =
        if (r.isToday && !includeToday) "today"
        else "${unitWord(unit, r.n)} ${if (r.future) "until" else "since"}"

    /** Editor preview line: "55 days until 18 Nov 2026", "Today", or "Pick a date". */
    fun previewLine(unit: CountUnit, includeToday: Boolean, r: CountResult?): String = when {
        r == null -> "Pick a date"
        r.isToday && !includeToday -> "Today"
        else -> "${fullNumber(r.n)} ${unitWord(unit, r.n)} ${if (r.future) "until" else "since"} ${date(r.target)}"
    }
}
