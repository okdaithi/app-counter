package com.okdaithi.daycounter.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CounterMathTest {

    private val today = LocalDate.of(2026, 9, 24)

    private fun counter(
        date: LocalDate,
        unit: CountUnit = CountUnit.DAYS,
        includeToday: Boolean = false,
        repeatYearly: Boolean = false,
    ) = Counter(id = "x", title = "t", date = date, unit = unit, includeToday = includeToday, repeatYearly = repeatYearly)

    private fun calc(c: Counter, on: LocalDate = today) = CounterMath.calc(c, on)

    @Test fun futureDateCountsUntil() {
        val r = calc(counter(LocalDate.of(2026, 11, 18)))
        assertEquals(55, r.n)
        assertEquals(55, r.diff)
        assertTrue(r.future)
    }

    @Test fun pastDateCountsSince() {
        val r = calc(counter(LocalDate.of(2024, 3, 12)))
        assertEquals(926, r.n)
        assertEquals(-926, r.diff)
        assertFalse(r.future)
    }

    @Test fun includeTodayAddsOneDay() {
        assertEquals(3917, calc(counter(LocalDate.of(2016, 1, 4), includeToday = true)).n)
        assertEquals(56, calc(counter(LocalDate.of(2026, 11, 18), includeToday = true)).n)
    }

    @Test fun todayIsZeroAndNotFuture() {
        val r = calc(counter(today))
        assertEquals(0, r.n)
        assertTrue(r.isToday)
        assertFalse(r.future)
        assertEquals(1, calc(counter(today, includeToday = true)).n)
    }

    @Test fun futureDateFlipsToSinceOncePassed() {
        val c = counter(LocalDate.of(2026, 9, 24))
        val r = calc(c, on = LocalDate.of(2026, 9, 25))
        assertFalse(r.future)
        assertEquals(1, r.n)
        assertEquals(-1, r.diff)
    }

    @Test fun dayCountAcrossLeapDay() {
        val r = calc(counter(LocalDate.of(2024, 3, 1)), on = LocalDate.of(2024, 2, 28))
        assertEquals(2, r.n)
    }

    @Test fun repeatYearlyTargetsNextOccurrence() {
        val r = calc(counter(LocalDate.of(2019, 2, 7), repeatYearly = true))
        assertEquals(LocalDate.of(2027, 2, 7), r.target)
        assertEquals(136, r.n)
        assertTrue(r.future)
    }

    @Test fun repeatYearlyLaterThisYear() {
        val r = calc(counter(LocalDate.of(2000, 12, 25), repeatYearly = true))
        assertEquals(LocalDate.of(2026, 12, 25), r.target)
    }

    @Test fun repeatYearlyOnTheDayIsToday() {
        val r = calc(counter(LocalDate.of(1990, 9, 24), repeatYearly = true))
        assertEquals(today, r.target)
        assertTrue(r.isToday)
        assertEquals(0, r.n)
    }

    @Test fun repeatYearlyFutureOriginStillUsesNextOccurrence() {
        val r = calc(counter(LocalDate.of(2030, 10, 1), repeatYearly = true))
        assertEquals(LocalDate.of(2026, 10, 1), r.target)
    }

    @Test fun feb29RepeatUsesFeb28InNonLeapYear() {
        val c = counter(LocalDate.of(2020, 2, 29), repeatYearly = true)
        assertEquals(LocalDate.of(2027, 2, 28), calc(c).target)
        assertEquals(LocalDate.of(2027, 2, 28), calc(c, on = LocalDate.of(2027, 2, 28)).target)
    }

    @Test fun feb29RepeatUsesFeb29InLeapYear() {
        val c = counter(LocalDate.of(2020, 2, 29), repeatYearly = true)
        assertEquals(LocalDate.of(2028, 2, 29), calc(c, on = LocalDate.of(2027, 3, 1)).target)
        assertEquals(LocalDate.of(2028, 2, 29), calc(c, on = LocalDate.of(2028, 1, 10)).target)
    }

    @Test fun weeksAreFloored() {
        assertEquals(277, calc(counter(LocalDate.of(2021, 6, 1), unit = CountUnit.WEEKS)).n)
        assertEquals(0, calc(counter(today.plusDays(6), unit = CountUnit.WEEKS)).n)
        assertEquals(1, calc(counter(today.plusDays(6), unit = CountUnit.WEEKS, includeToday = true)).n)
    }

    @Test fun monthsAtMonthEnd() {
        val jan31 = LocalDate.of(2026, 1, 31)
        assertEquals(0, CounterMath.wholeMonthsBetween(jan31, LocalDate.of(2026, 2, 28)))
        assertEquals(1, CounterMath.wholeMonthsBetween(jan31, LocalDate.of(2026, 3, 30)))
        assertEquals(2, CounterMath.wholeMonthsBetween(jan31, LocalDate.of(2026, 3, 31)))
        assertEquals(1, CounterMath.wholeMonthsBetween(LocalDate.of(2024, 1, 29), LocalDate.of(2024, 2, 29)))
    }

    @Test fun monthsInBothDirections() {
        val until = calc(counter(LocalDate.of(2026, 11, 18), unit = CountUnit.MONTHS))
        assertEquals(1, until.n)
        assertTrue(until.future)
        val since = calc(counter(LocalDate.of(2024, 3, 12), unit = CountUnit.MONTHS))
        assertEquals(30, since.n)
        assertFalse(since.future)
    }

    @Test fun monthsSameDayIsZero() {
        assertEquals(0, calc(counter(today, unit = CountUnit.MONTHS)).n)
    }

    @Test fun yearsAreWholeMonthsOverTwelve() {
        assertEquals(10, calc(counter(LocalDate.of(2016, 1, 4), unit = CountUnit.YEARS)).n)
        assertEquals(9, calc(counter(LocalDate.of(2016, 9, 25), unit = CountUnit.YEARS)).n)
        assertEquals(10, calc(counter(LocalDate.of(2016, 9, 24), unit = CountUnit.YEARS)).n)
    }
}
