package com.okdaithi.daycounter.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    @Test
    fun sameVersion() {
        assertEquals(0, UpdateChecker.compareVersions("0.1.0", "0.1.0"))
    }

    @Test
    fun newerPatch() {
        assertTrue(UpdateChecker.compareVersions("0.1.1", "0.1.0") > 0)
    }

    @Test
    fun newerMinor() {
        assertTrue(UpdateChecker.compareVersions("0.2.0", "0.1.0") > 0)
    }

    @Test
    fun newerMajor() {
        assertTrue(UpdateChecker.compareVersions("1.0.0", "0.9.9") > 0)
    }

    @Test
    fun olderVersion() {
        assertTrue(UpdateChecker.compareVersions("0.1.0", "0.2.0") < 0)
    }

    @Test
    fun differentLengths() {
        assertTrue(UpdateChecker.compareVersions("1.0.0.1", "1.0.0") > 0)
        assertEquals(0, UpdateChecker.compareVersions("1.0", "1.0.0"))
    }
}
