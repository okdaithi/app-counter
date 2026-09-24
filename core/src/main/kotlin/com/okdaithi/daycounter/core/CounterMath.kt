package com.okdaithi.daycounter.core

import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.max

/**
 * @property n the value to display, in the counter's unit.
 * @property future true when counting "until" (target is after today).
 * @property diff signed whole days from today to [target].
 * @property target the effective target date (next occurrence for yearly counters).
 */
data class CountResult(
    val n: Int,
    val future: Boolean,
    val diff: Int,
    val target: LocalDate,
) {
    val isToday: Boolean get() = diff == 0
}

/** Counting rules from the design handoff (README "Product rules", 1–4). */
object CounterMath {

    fun calc(counter: Counter, today: LocalDate): CountResult =
        calc(counter.date, counter.unit, counter.includeToday, counter.repeatYearly, today)

    fun calc(
        date: LocalDate,
        unit: CountUnit,
        includeToday: Boolean,
        repeatYearly: Boolean,
        today: LocalDate,
    ): CountResult {
        val target = effectiveTarget(date, repeatYearly, today)
        val diff = ChronoUnit.DAYS.between(today, target).toInt()
        val future = diff > 0
        val days = abs(diff) + if (includeToday) 1 else 0
        val n = when (unit) {
            CountUnit.DAYS -> days
            CountUnit.WEEKS -> days / 7
            CountUnit.MONTHS -> wholeMonths(today, target, future)
            CountUnit.YEARS -> wholeMonths(today, target, future) / 12
        }
        return CountResult(n = n, future = future, diff = diff, target = target)
    }

    /**
     * Yearly counters target the next occurrence of their month and day: this year if it is
     * still ahead or is today, otherwise next year. Feb 29 falls back to Feb 28 in non-leap years.
     */
    fun effectiveTarget(date: LocalDate, repeatYearly: Boolean, today: LocalDate): LocalDate {
        if (!repeatYearly) return date
        val monthDay = MonthDay.from(date)
        val thisYear = monthDay.atYear(today.year)
        return if (thisYear.isBefore(today)) monthDay.atYear(today.year + 1) else thisYear
    }

    private fun wholeMonths(today: LocalDate, target: LocalDate, future: Boolean): Int {
        val earlier = if (future) today else target
        val later = if (future) target else today
        return wholeMonthsBetween(earlier, later)
    }

    /** Whole calendar months from [earlier] to [later]; never negative. */
    fun wholeMonthsBetween(earlier: LocalDate, later: LocalDate): Int {
        var m = (later.year - earlier.year) * 12 + later.monthValue - earlier.monthValue
        if (later.dayOfMonth < earlier.dayOfMonth) m--
        return max(0, m)
    }
}
