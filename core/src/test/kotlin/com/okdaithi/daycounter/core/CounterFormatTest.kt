package com.okdaithi.daycounter.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CounterFormatTest {

    private val today = LocalDate.of(2026, 9, 24)

    @Test fun widgetNumberAbbreviation() {
        assertEquals("0", CounterFormat.widgetNumber(0))
        assertEquals("999", CounterFormat.widgetNumber(999))
        assertEquals("1k", CounterFormat.widgetNumber(1000))
        assertEquals("1k", CounterFormat.widgetNumber(1099))
        assertEquals("1.1k", CounterFormat.widgetNumber(1100))
        assertEquals("3.9k", CounterFormat.widgetNumber(3916))
        assertEquals("9.9k", CounterFormat.widgetNumber(9999))
        assertEquals("10k", CounterFormat.widgetNumber(10_000))
        assertEquals("12k", CounterFormat.widgetNumber(12_345))
    }

    @Test fun fullNumberIsGrouped() {
        assertEquals("3,916", CounterFormat.fullNumber(3916))
        assertEquals("55", CounterFormat.fullNumber(55))
        assertEquals("1,234,567", CounterFormat.fullNumber(1_234_567))
    }

    @Test fun suffixes() {
        assertEquals("", CounterFormat.widgetSuffix(CountUnit.DAYS))
        assertEquals("w", CounterFormat.widgetSuffix(CountUnit.WEEKS))
        assertEquals("mo", CounterFormat.widgetSuffix(CountUnit.MONTHS))
        assertEquals("y", CounterFormat.widgetSuffix(CountUnit.YEARS))
    }

    @Test fun widgetFontSizeScalesWithLength() {
        assertEquals(30f, CounterFormat.widgetFontSizeSp("55", ""))
        assertEquals(27f, CounterFormat.widgetFontSizeSp("926", ""))
        assertEquals(27f, CounterFormat.widgetFontSizeSp("12", "w"))
        assertEquals(22f, CounterFormat.widgetFontSizeSp("3.9k", ""))
        assertEquals(22f, CounterFormat.widgetFontSizeSp("12", "mo"))
        assertEquals(18f, CounterFormat.widgetFontSizeSp("277", "mo"))
        assertEquals(15f, CounterFormat.widgetFontSizeSp("3.9k", "mo"))
    }

    @Test fun dates() {
        assertEquals("24 Sep 2026", CounterFormat.date(today))
        assertEquals("7 February", CounterFormat.dayMonth(LocalDate.of(2027, 2, 7)))
    }

    private fun c(date: LocalDate, unit: CountUnit = CountUnit.DAYS, includeToday: Boolean = false, repeat: Boolean = false) =
        Counter("x", "t", date, unit, includeToday, repeat)

    private fun subtitle(c: Counter) = CounterFormat.subtitle(c, CounterMath.calc(c, today))
    private fun caption(c: Counter) = CounterFormat.unitCaption(c.unit, c.includeToday, CounterMath.calc(c, today))

    @Test fun subtitles() {
        assertEquals("Until 18 Nov 2026", subtitle(c(LocalDate.of(2026, 11, 18))))
        assertEquals("Since 12 Mar 2024", subtitle(c(LocalDate.of(2024, 3, 12))))
        assertEquals("Today, 24 Sep 2026", subtitle(c(today)))
        assertEquals("Every 7 February", subtitle(c(LocalDate.of(2019, 2, 7), repeat = true)))
    }

    @Test fun captions() {
        assertEquals("days until", caption(c(LocalDate.of(2026, 11, 18))))
        assertEquals("days since", caption(c(LocalDate.of(2024, 3, 12))))
        assertEquals("day since", caption(c(today.minusDays(1))))
        assertEquals("day until", caption(c(today.plusDays(1))))
        assertEquals("weeks since", caption(c(LocalDate.of(2021, 6, 1), unit = CountUnit.WEEKS)))
        assertEquals("today", caption(c(today)))
        assertEquals("day since", caption(c(today, includeToday = true)))
    }

    @Test fun previewLines() {
        val japan = c(LocalDate.of(2026, 11, 18))
        assertEquals(
            "55 days until 18 Nov 2026",
            CounterFormat.previewLine(japan.unit, japan.includeToday, CounterMath.calc(japan, today)),
        )
        val t = c(today)
        assertEquals("Today", CounterFormat.previewLine(t.unit, t.includeToday, CounterMath.calc(t, today)))
        assertEquals("Pick a date", CounterFormat.previewLine(CountUnit.DAYS, false, null))
        val halden = c(LocalDate.of(2016, 1, 4), includeToday = true)
        assertEquals(
            "3,917 days since 4 Jan 2016",
            CounterFormat.previewLine(halden.unit, halden.includeToday, CounterMath.calc(halden, today)),
        )
    }
}
